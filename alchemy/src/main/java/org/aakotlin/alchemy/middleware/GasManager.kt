/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.alchemy.middleware

import org.aakotlin.alchemy.provider.AlchemyProvider
import org.aakotlin.core.Chain
import org.aakotlin.core.provider.ClientMiddlewareFn
import org.aakotlin.core.util.await
import org.aakotlin.core.util.toUserOperationRequest
import org.web3j.utils.Numeric
import java.math.BigInteger

data class AlchemyGasManagerConfig(
    val policyId: String
)

data class AlchemyGasEstimationOptions(
    /** if true, this will use `alchemy_requestGasAndPaymasterAndData` else will use `alchemy_requestPaymasterAndData` */
    val disableGasEstimation: Boolean = false,
    val fallbackGasEstimator: ClientMiddlewareFn? = null,
    val fallbackFeeDataGetter: ClientMiddlewareFn? = null
)

/**
 * This middleware wraps the Alchemy Gas Manager APIs to provide more flexible UserOperation gas sponsorship.
 *
 * If `estimateGas` is true, it will use `alchemy_requestGasAndPaymasterAndData` to get all of the gas estimates + paymaster data
 * in one RPC call.
 *
 * Otherwise, it will use `alchemy_requestPaymasterAndData` to get only paymaster data, allowing you
 * to customize the gas and fee estimation middleware.
 *
 * @param self - the smart account provider to override to use the alchemy gas manager
 * @param config - the alchemy gas manager configuration
 * @param gasEstimationOptions - options to customize gas estimation middleware
 * @returns the provider augmented to use the alchemy gas manager
 */
fun AlchemyProvider.withAlchemyGasManager(
    config: AlchemyGasManagerConfig,
    gasEstimationOptions: AlchemyGasEstimationOptions? = null
) = apply {
    val fallbackFeeDataGetter = gasEstimationOptions?.fallbackFeeDataGetter ?: alchemyFeeEstimator
    val fallbackGasEstimator = gasEstimationOptions?.fallbackGasEstimator ?: ::defaultGasEstimator
    val disableGasEstimation = gasEstimationOptions?.disableGasEstimation ?: false

    withGasEstimator(
        if (disableGasEstimation) {
            fallbackGasEstimator
        } else {
            { client, uoStruct, overrides ->
                uoStruct.apply {
                    callGasLimit = BigInteger.ZERO
                    preVerificationGas = BigInteger.ZERO
                    verificationGasLimit = BigInteger.ZERO
                }

                if (!overrides.paymasterAndData.isNullOrEmpty()) {
                    fallbackGasEstimator(client, uoStruct, overrides)
                } else {
                    uoStruct
                }
            }
        }
    )

    withFeeDataGetter(
        if (disableGasEstimation) {
            fallbackFeeDataGetter
        } else {
            { client, struct, overrides ->
                var newMaxFeePerGas = struct.maxFeePerGas ?: 0.toBigInteger()
                var newMaxPriorityFeePerGas = struct.maxPriorityFeePerGas ?: 0.toBigInteger()

                // but if user is bypassing paymaster to fallback to having the account to pay the gas (one-off override),
                // we cannot delegate gas estimation to the bundler because paymaster middleware will not be called
                if (overrides.paymasterAndData == "0x") {
                    val result = fallbackFeeDataGetter(client, struct, overrides)
                    newMaxFeePerGas = result.maxFeePerGas ?: newMaxFeePerGas
                    newMaxPriorityFeePerGas = result.maxPriorityFeePerGas ?: newMaxPriorityFeePerGas
                }

                struct.apply {
                    maxFeePerGas = newMaxFeePerGas
                    maxPriorityFeePerGas = newMaxPriorityFeePerGas
                }
            }
        }
    )

    return if (disableGasEstimation) {
        requestPaymasterAndData(this, config)
    } else {
        requestGasAndPaymasterData(this, config)
    }
}

/**
 * This uses the alchemy RPC method: `alchemy_requestPaymasterAndData`, which does not estimate gas. It's recommended to use
 * this middleware if you want more customization over the gas and fee estimation middleware, including setting
 * non-default buffer values for the fee/gas estimation.
 *
 * @param config - the alchemy gas manager configuration
 * @returns middleware overrides for paymaster middlewares
 */
fun requestPaymasterAndData(
    provider: AlchemyProvider,
    config: AlchemyGasManagerConfig
): AlchemyProvider = provider.apply {
    withPaymasterMiddleware(
        { _, struct, _ ->
            struct.apply {
                paymasterAndData = dummyPaymasterAndData(provider.chain.id)
            }
        },
        { _, struct, _ ->
            val data = (provider.rpcClient as AlchemyClient).requestPaymasterAndData(
                PaymasterAndDataParams(
                    config.policyId,
                    provider.getEntryPointAddress().address,
                    struct.toUserOperationRequest()
                )
            ).await().result.paymasterAndData

            struct.apply {
                paymasterAndData = data
            }
        }
    )
}

/**
 * This uses the alchemy RPC method: `alchemy_requestGasAndPaymasterAndData` to get all of the gas estimates + paymaster data
 * in one RPC call. It will no-op the gas estimator and fee data getter middleware and set a custom middleware that makes the RPC call.
 *
 * @param config - the alchemy gas manager configuration
 * @returns middleware overrides for paymaster middlewares
 */
fun requestGasAndPaymasterData(
    provider: AlchemyProvider,
    config: AlchemyGasManagerConfig
): AlchemyProvider = provider.apply {
    withPaymasterMiddleware(
        { _, struct, _ ->
            struct.apply {
                paymasterAndData = dummyPaymasterAndData(provider.chain.id)
            }
        },
        { _, struct, overrides ->
            val userOperation = struct.toUserOperationRequest()
            val feeOverride = FeeOverride(
                maxFeePerGas = overrides.maxFeePerGas?.let(Numeric::encodeQuantity),
                maxPriorityFeePerGas = overrides.maxPriorityFeePerGas?.let(Numeric::encodeQuantity),
                callGasLimit = overrides.callGasLimit?.let(Numeric::encodeQuantity),
                verificationGasLimit = overrides.verificationGasLimit?.let(Numeric::encodeQuantity),
                preVerificationGas = overrides.preVerificationGas?.let(Numeric::encodeQuantity)
            )

            val result = (provider.rpcClient as AlchemyClient).requestGasAndPaymasterAndData(
                PaymasterAndDataParams(
                    config.policyId,
                    provider.getEntryPointAddress().address,
                    userOperation,
                    userOperation.signature,
                    if (feeOverride.isEmpty) null else feeOverride
                )
            ).await().result

            struct.apply {
                paymasterAndData = result.paymasterAndData
                callGasLimit = result.callGasLimit
                verificationGasLimit = result.verificationGasLimit
                preVerificationGas = result.preVerificationGas
                maxFeePerGas = result.maxFeePerGas
                maxPriorityFeePerGas = result.maxPriorityFeePerGas
            }
        })
}

private fun dummyPaymasterAndData(chainId: Long): String {
    return when (chainId) {
        Chain.MainNet.id,
        Chain.Optimism.id,
        Chain.Polygon.id,
        Chain.Arbitrum.id -> "0x4Fd9098af9ddcB41DA48A1d78F91F1398965addcfffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff0000000000000000000000000000000007aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1c"
        else -> "0xc03aac639bb21233e0139381970328db8bceeb67fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff0000000000000000000000000000000007aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1c"
    }
}
