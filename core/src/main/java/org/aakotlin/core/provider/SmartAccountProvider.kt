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
import org.aakotlin.core.UserOperationCallData
import org.aakotlin.core.UserOperationReceipt
import org.aakotlin.core.UserOperationStruct
import org.aakotlin.core.accounts.ISmartContractAccount
import org.aakotlin.core.client.Erc4337Client
import org.aakotlin.core.client.createPublicErc4337Client
import org.aakotlin.core.util.Defaults
import org.aakotlin.core.util.await
import org.aakotlin.core.util.bigIntPercent
import org.aakotlin.core.util.getUserOperationHash
import org.aakotlin.core.util.isValidRequest
import org.aakotlin.core.util.toUserOperationRequest
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
    companion object {
        private val minPriorityFeePerBidDefaults =
            mapOf<Long, Long>(
                Chain.Arbitrum.id to 10_000_000,
                Chain.ArbitrumGoerli.id to 10_000_000,
                Chain.ArbitrumSepolia.id to 10_000_000,
            )
    }

    val rpcClient: Erc4337Client

    private var account: ISmartContractAccount? = null
    private var gasEstimator: AccountMiddlewareFn = ::defaultGasEstimator
    private var feeDataGetter: AccountMiddlewareFn = ::defaultFeeDataGetter
    private var paymasterDataMiddleware: AccountMiddlewareFn = ::defaultPaymasterDataMiddleware
    private var dummyPaymasterDataMiddleware: AccountMiddlewareFn = ::defaultDummyPaymasterDataMiddleware

    private val minPriorityFeePerBid =
        BigInteger.valueOf(
            opts?.minPriorityFeePerBid
                ?: minPriorityFeePerBidDefaults[chain.id]
                ?: 100_000_000,
        )

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

    override suspend fun sendUserOperation(data: UserOperationCallData): String {
        this.account ?: throw IllegalStateException("Account not connected")

        val uoStruct = this.buildUserOperation(data)
        return sendUserOperation(uoStruct)
    }

    override suspend fun buildUserOperation(data: UserOperationCallData): UserOperationStruct {
        val account = this.account ?: throw IllegalStateException("Account not connected")

        return runMiddlewareStack(
            UserOperationStruct(
                initCode = account.getInitCode(),
                sender = getAddress().address,
                nonce = account.getNonce(),
                callData =
                    account.encodeExecute(
                        data.target,
                        data.value ?: BigInteger.ZERO,
                        data.data,
                    ),
                signature = account.getDummySignature(),
                paymasterAndData = "0x",
            ),
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

    override fun withFeeDataGetter(feeDataGetter: AccountMiddlewareFn): ISmartAccountProvider {
        this.feeDataGetter = feeDataGetter
        return this
    }

    override fun withGasEstimator(gasEstimator: AccountMiddlewareFn): ISmartAccountProvider {
        this.gasEstimator = gasEstimator
        return this
    }

    override fun withPaymasterMiddleware(
        dummyPaymasterDataMiddleware: AccountMiddlewareFn?,
        paymasterDataMiddleware: AccountMiddlewareFn?,
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

    private suspend fun sendUserOperation(struct: UserOperationStruct): String {
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
        return rpcClient.sendUserOperation(
            request,
            this.getEntryPointAddress().address,
        ).await().result
    }

    private suspend fun runMiddlewareStack(struct: UserOperationStruct): UserOperationStruct {
        // Reversed order - dummyPaymasterDataMiddleware is called first
        val asyncPipe =
            paymasterDataMiddleware chain
                gasEstimator chain
                feeDataGetter chain
                dummyPaymasterDataMiddleware

        return asyncPipe(struct)
    }

    // These are dependent on the specific paymaster being used
    // You should implement your own middleware to override these
    // or extend this class and provider your own implementation

    protected open suspend fun defaultDummyPaymasterDataMiddleware(struct: UserOperationStruct): UserOperationStruct {
        struct.paymasterAndData = "0x"
        return struct
    }

    protected open suspend fun defaultPaymasterDataMiddleware(struct: UserOperationStruct): UserOperationStruct {
        struct.paymasterAndData = "0x"
        return struct
    }

    protected open suspend fun defaultFeeDataGetter(struct: UserOperationStruct): UserOperationStruct {
        val maxPriorityFeePerGas = rpcClient.ethMaxPriorityFeePerGas().await().maxPriorityFeePerGas
        val feeData = rpcClient.estimateFeesPerGas(chain)

        // set maxPriorityFeePerGasBid to the max between 33% added priority fee estimate and
        // the min priority fee per gas set for the provider
        val maxPriorityFeePerGasBid =
            bigIntPercent(
                maxPriorityFeePerGas,
                BigInteger.valueOf(100 + (opts?.maxPriorityFeePerGasEstimateBuffer ?: 33)),
            ).max(this.minPriorityFeePerBid)

        val maxFeePerGasBid = feeData.maxFeePerGas - feeData.maxPriorityFeePerGas + maxPriorityFeePerGasBid

        struct.maxFeePerGas = maxFeePerGasBid
        struct.maxPriorityFeePerGas = maxPriorityFeePerGasBid

        return struct
    }

    protected open suspend fun defaultGasEstimator(struct: UserOperationStruct): UserOperationStruct {
        val request = struct.toUserOperationRequest()
        val estimates =
            rpcClient.estimateUserOperationGas(
                request,
                this.getEntryPointAddress().address,
            ).await().result

        struct.preVerificationGas = estimates.preVerificationGas
        struct.verificationGasLimit = estimates.verificationGasLimit
        struct.callGasLimit = estimates.callGasLimit

        return struct
    }

    private infix fun <A, B, C> (suspend (B) -> C).chain(g: suspend (A) -> B): suspend (A) -> C {
        return { x -> this(g(x)) }
    }
}
