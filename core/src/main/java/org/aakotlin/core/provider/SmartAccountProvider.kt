/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.core.provider

import kotlinx.coroutines.delay
import org.aakotlin.core.Address
import org.aakotlin.core.Chain
import org.aakotlin.core.SendUserOperationResult
import org.aakotlin.core.UserOperationCallData
import org.aakotlin.core.UserOperationOverrides
import org.aakotlin.core.UserOperationReceipt
import org.aakotlin.core.UserOperationRequest
import org.aakotlin.core.UserOperationStruct
import org.aakotlin.core.accounts.ISmartContractAccount
import org.aakotlin.core.client.Erc4337Client
import org.aakotlin.core.client.EthEstimateUserOperationGas
import org.aakotlin.core.client.createPublicErc4337Client
import org.aakotlin.core.util.Defaults
import org.aakotlin.core.util.await
import org.aakotlin.core.util.bigIntPercent
import org.aakotlin.core.util.getUserOperationHash
import org.aakotlin.core.util.isValidRequest
import org.aakotlin.core.util.toUserOperationRequest
import org.web3j.utils.Numeric
import java.math.BigInteger
import kotlin.math.pow
import kotlin.random.Random

open class SmartAccountProvider(
    client: Erc4337Client?,
    rpcUrl: String?,
    private val entryPointAddress: Address?,
    val chain: Chain,
    private val opts: SmartAccountProviderOpts? = null,
) : ISmartAccountProvider {
    val rpcClient: Erc4337Client

    private var account: ISmartContractAccount? = null
    private var gasEstimator: ClientMiddlewareFn = ::defaultGasEstimator
        private set
    private var feeDataGetter: ClientMiddlewareFn = ::defaultFeeDataGetter
        private set
    private var paymasterDataMiddleware: ClientMiddlewareFn = ::defaultPaymasterDataMiddleware
        private set
    private var overridePaymasterDataMiddleware: ClientMiddlewareFn = ::defaultOverridePaymasterDataMiddleware
        private set
    private var dummyPaymasterDataMiddleware: ClientMiddlewareFn = ::defaultDummyPaymasterDataMiddleware
        private set

    override val isConnected: Boolean
        get() = this.account != null

    init {
        this.rpcClient = client ?: rpcUrl?.let {
            createPublicErc4337Client(it)
        } ?: throw IllegalArgumentException("No rpcUrl or client provided")
    }

    fun connect(account: ISmartContractAccount) {
        this.account = account
        // TODO: this method isn't very useful atm
    }

    override suspend fun getAddress(): Address {
        val account = this.account ?: throw IllegalStateException("Account not connected")
        return account.getAddress()
    }

    override suspend fun sendUserOperation(
        data: UserOperationCallData,
        overrides: UserOperationOverrides?
    ): SendUserOperationResult {
        this.account ?: throw IllegalStateException("Account not connected")

        val uoStruct = this.buildUserOperation(data, overrides)
        return sendUserOperation(uoStruct)
    }

    override suspend fun dropAndReplaceUserOperation(
        uoToDrop: UserOperationRequest,
        overrides: UserOperationOverrides?
    ): SendUserOperationResult {
        val uoToSubmit = UserOperationStruct(
            initCode = uoToDrop.initCode,
            sender = uoToDrop.sender,
            nonce = Numeric.decodeQuantity(uoToDrop.nonce),
            callData = uoToDrop.callData,
            signature = Numeric.hexStringToByteArray(uoToDrop.signature),
            paymasterAndData = "0x",
        )

        // Run once to get the fee estimates
        // This can happen at any part of the middleware stack, so we want to run it all
        val estimates = this.runMiddlewareStack(uoToSubmit, overrides ?: UserOperationOverrides())
        val newOverrides = UserOperationOverrides(
            maxFeePerGas = (estimates.maxFeePerGas ?: BigInteger.ZERO).max(
                bigIntPercent(
                    Numeric.decodeQuantity(uoToDrop.maxFeePerGas),
                    BigInteger.valueOf(110),
                )
            ),
            maxPriorityFeePerGas = (estimates.maxPriorityFeePerGas ?: BigInteger.ZERO).max(
                bigIntPercent(
                    Numeric.decodeQuantity(uoToDrop.maxPriorityFeePerGas),
                    BigInteger.valueOf(110),
                )
            )
        )

        val uoToSend = runMiddlewareStack(uoToSubmit, newOverrides)
        return sendUserOperation(uoToSend)
    }

    override suspend fun buildUserOperation(
        data: UserOperationCallData,
        overrides: UserOperationOverrides?
    ): UserOperationStruct {
        val account = this.account ?: throw IllegalStateException("Account not connected")

        return runMiddlewareStack(
            UserOperationStruct(
                initCode = account.getInitCode(),
                sender = getAddress().address,
                nonce = account.getNonce(),
                callData = account.encodeExecute(
                    data.target,
                    data.value ?: BigInteger.ZERO,
                    data.data
                ),
                signature = account.getDummySignature(),
                paymasterAndData = "0x",
            ),
            overrides ?: UserOperationOverrides()
        )
    }

    override suspend fun waitForUserOperationTransaction(hash: String): UserOperationReceipt {
        val txMaxRetries = opts?.txMaxRetries ?: 5
        val txRetryIntervalMs = opts?.txRetryIntervalMs ?: 2000
        val txRetryMultiplier = opts?.txRetryMultiplier ?: 1.5

        for (i in 0 until txMaxRetries) {
            val txRetryIntervalWithJitterMs =
                txRetryIntervalMs * txRetryMultiplier.pow(i.toDouble()) + Random.nextDouble() * 100
            delay(txRetryIntervalWithJitterMs.toLong())

            try {
                return rpcClient.getUserOperationReceipt(hash).await().result
            } catch (ex: Exception) {
                if (i == txMaxRetries - 1) {
                    throw ex
                }
            }
        }

        throw Exception("Failed to find transaction for User Operation")
    }

    override fun withFeeDataGetter(feeDataGetter: ClientMiddlewareFn): ISmartAccountProvider {
        this.feeDataGetter = feeDataGetter
        return this
    }

    override fun withGasEstimator(gasEstimator: ClientMiddlewareFn): ISmartAccountProvider {
        this.gasEstimator = gasEstimator
        return this
    }

    override fun withPaymasterMiddleware(
        dummyPaymasterDataMiddleware: ClientMiddlewareFn?,
        paymasterDataMiddleware: ClientMiddlewareFn?,
    ): ISmartAccountProvider {
        if (dummyPaymasterDataMiddleware != null) {
            this.dummyPaymasterDataMiddleware = dummyPaymasterDataMiddleware
        }

        if (paymasterDataMiddleware != null) {
            this.paymasterDataMiddleware = paymasterDataMiddleware
        }

        return this
    }

    /**
     * Note that the connected account's entryPointAddress always takes the precedence
     */
    fun getEntryPointAddress(): Address {
        return this.entryPointAddress
            ?: this.account?.getEntryPointAddress()
            ?: Defaults.getDefaultEntryPointAddress(this.chain)
    }

    private suspend fun sendUserOperation(struct: UserOperationStruct): SendUserOperationResult {
        val account = this.account ?: throw IllegalStateException("Account not connected")

        if (!struct.isValidRequest()) {
            throw IllegalArgumentException(
                "Request is missing parameters. All properties on UserOperationStruct must be set. struct: $struct",
            )
        }

        struct.signature = account.signMessage(
            getUserOperationHash(
                struct,
                this.getEntryPointAddress(),
                this.chain.id
            )
        )

        val request = struct.toUserOperationRequest()
        val hash = rpcClient.sendUserOperation(
            request,
            this.getEntryPointAddress().address,
        ).await().result

        return SendUserOperationResult(hash, request)
    }

    private suspend fun runMiddlewareStack(
        struct: UserOperationStruct,
        overrides: UserOperationOverrides
    ): UserOperationStruct {
        // Reversed order - dummyPaymasterDataMiddleware is called first

        val asyncPipe = if (overrides.paymasterAndData != null) {
            overridePaymasterDataMiddleware
        } else {
            paymasterDataMiddleware
        } chain
          gasEstimator chain
          feeDataGetter chain
          dummyPaymasterDataMiddleware

        return asyncPipe(rpcClient, struct, overrides)
    }

    // These are dependent on the specific paymaster being used
    // You should implement your own middleware to override these
    // or extend this class and provider your own implementation

    protected open suspend fun defaultDummyPaymasterDataMiddleware(
        client: Erc4337Client,
        struct: UserOperationStruct,
        overrides: UserOperationOverrides
    ): UserOperationStruct {
        struct.paymasterAndData = "0x"
        return struct
    }

    private suspend fun defaultOverridePaymasterDataMiddleware(
        client: Erc4337Client,
        struct: UserOperationStruct,
        overrides: UserOperationOverrides
    ): UserOperationStruct {
        struct.paymasterAndData = overrides.paymasterAndData ?: "0x"
        return struct
    }

    protected open suspend fun defaultPaymasterDataMiddleware(
        client: Erc4337Client,
        struct: UserOperationStruct,
        overrides: UserOperationOverrides
    ): UserOperationStruct {
        struct.paymasterAndData = "0x"
        return struct
    }

    protected open suspend fun defaultFeeDataGetter(
        client: Erc4337Client,
        struct: UserOperationStruct,
        overrides: UserOperationOverrides
    ): UserOperationStruct {
        // maxFeePerGas must be at least the sum of maxPriorityFeePerGas and baseFee
        // so we need to accommodate for the fee option applied maxPriorityFeePerGas for the maxFeePerGas
        //
        // Note that if maxFeePerGas is not at least the sum of maxPriorityFeePerGas and required baseFee
        // after applying the fee options, then the transaction will fail
        //
        // Refer to https://docs.alchemy.com/docs/maxpriorityfeepergas-vs-maxfeepergas
        // for more information about maxFeePerGas and maxPriorityFeePerGas
        val feeData = rpcClient.estimateFeesPerGas(chain)
        val maxPriorityFeePerGas = overrides.maxPriorityFeePerGas ?:
            rpcClient.ethMaxPriorityFeePerGas().await().maxPriorityFeePerGas

        val maxFeePerGas = overrides.maxFeePerGas ?:
            (feeData.maxFeePerGas - feeData.maxPriorityFeePerGas + maxPriorityFeePerGas)

        struct.maxFeePerGas = maxFeePerGas
        struct.maxPriorityFeePerGas = maxPriorityFeePerGas

        return struct
    }

    open suspend fun defaultGasEstimator(
        client: Erc4337Client,
        struct: UserOperationStruct,
        overrides: UserOperationOverrides
    ): UserOperationStruct {
        var estimates: EthEstimateUserOperationGas.EstimateUserOperationGas? = null

        if (overrides.callGasLimit == null ||
            overrides.verificationGasLimit == null ||
            overrides.preVerificationGas == null
        ) {
            val request = struct.toUserOperationRequest()
            estimates = rpcClient.estimateUserOperationGas(
                request,
                this.getEntryPointAddress().address
            ).await().result
        }

        struct.preVerificationGas = overrides.preVerificationGas ?: estimates!!.preVerificationGas
        struct.verificationGasLimit = overrides.verificationGasLimit ?: estimates!!.verificationGasLimit
        struct.callGasLimit = overrides.callGasLimit ?: estimates!!.callGasLimit

        return struct
    }

    private infix fun (ClientMiddlewareFn).chain(g: ClientMiddlewareFn): ClientMiddlewareFn {
        return { x, y, z -> this(x, g(x, y, z), z) }
    }
}
