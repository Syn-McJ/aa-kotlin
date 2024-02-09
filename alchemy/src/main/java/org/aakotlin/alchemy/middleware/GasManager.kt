/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.alchemy.middleware

import org.aakotlin.alchemy.provider.AlchemyProvider
import org.aakotlin.core.Chain
import org.aakotlin.core.provider.AccountMiddlewareFn
import org.aakotlin.core.util.await
import org.aakotlin.core.util.toUserOperationRequest
import java.math.BigInteger

data class AlchemyGasManagerConfig(
    val policyId: String
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
 * @param provider - the smart account provider to override to use the alchemy gas manager
 * @param config - the alchemy gas manager configuration
 * @param estimateGas - if true, this will use `alchemy_requestGasAndPaymasterAndData` else will use `alchemy_requestPaymasterAndData`
 * @returns the provider augmented to use the alchemy gas manager
 */
fun AlchemyProvider.withAlchemyGasManager(
    config: AlchemyGasManagerConfig,
    estimateGas: Boolean = true
) = apply {
    if (estimateGas) {
        withGasEstimator { uoStruct ->
            // no-op gas estimator
            uoStruct.apply {
                callGasLimit = 0.toBigInteger()
                preVerificationGas = 0.toBigInteger()
                verificationGasLimit = 0.toBigInteger()
            }
        }
        // no-op fee because the alchemy api will do it
        withFeeDataGetter { uoStruct ->
            uoStruct.apply {
                maxFeePerGas = maxFeePerGas ?: 0.toBigInteger()
                maxPriorityFeePerGas = maxPriorityFeePerGas ?: 0.toBigInteger()
            }
        }
        val middlewarePair = withAlchemyGasAndPaymasterAndDataMiddleware(this, config)
        withPaymasterMiddleware(middlewarePair.first, middlewarePair.second)
    } else {
        val middlewarePair = withAlchemyPaymasterAndDataMiddleware(this, config)
        withPaymasterMiddleware(middlewarePair.first, middlewarePair.second)
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
fun withAlchemyPaymasterAndDataMiddleware(
    provider: AlchemyProvider,
    config: AlchemyGasManagerConfig
): Pair<AccountMiddlewareFn?, AccountMiddlewareFn?> {
    val dummyPaymasterDataMiddleware: AccountMiddlewareFn = { struct ->
        struct.apply {
            paymasterAndData = when (provider.chain.id) {
                Chain.MainNet.id,
                Chain.Optimism.id,
                Chain.Polygon.id,
                Chain.Arbitrum.id -> "0x4Fd9098af9ddcB41DA48A1d78F91F1398965addcfffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff0000000000000000000000000000000007aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1c"
                else -> "0xc03aac639bb21233e0139381970328db8bceeb67fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff0000000000000000000000000000000007aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1c"
            }
        }
    }

    val paymasterDataMiddleware: AccountMiddlewareFn = { struct ->
        struct.apply {
            paymasterAndData = (provider.rpcClient as AlchemyClient).requestPaymasterAndData(
                PaymasterAndDataParams(
                    config.policyId,
                    provider.getEntryPointAddress().address,
                    struct.toUserOperationRequest()
                )
            ).await().result.paymasterAndData
        }
    }

    return Pair(dummyPaymasterDataMiddleware, paymasterDataMiddleware)
}

/**
 * This uses the alchemy RPC method: `alchemy_requestGasAndPaymasterAndData` to get all of the gas estimates + paymaster data
 * in one RPC call. It will no-op the gas estimator and fee data getter middleware and set a custom middleware that makes the RPC call.
 *
 * @param config - the alchemy gas manager configuration
 * @returns middleware overrides for paymaster middlewares
 */
fun withAlchemyGasAndPaymasterAndDataMiddleware(
    provider: AlchemyProvider,
    config: AlchemyGasManagerConfig
): Pair<AccountMiddlewareFn?, AccountMiddlewareFn?> {
    val paymasterDataMiddleware: AccountMiddlewareFn = { struct ->
        val userOperation = struct.toUserOperationRequest()
        var feeOverride: FeeOverride? = null

        if ((struct.maxFeePerGas ?: BigInteger.ZERO) > BigInteger.ZERO) {
            feeOverride = FeeOverride(
                maxFeePerGas = userOperation.maxFeePerGas,
                maxPriorityFeePerGas = userOperation.maxPriorityFeePerGas
            )
        }

        val result = (provider.rpcClient as AlchemyClient).requestGasAndPaymasterAndData(
            PaymasterAndDataParams(
                config.policyId,
                provider.getEntryPointAddress().address,
                userOperation,
                userOperation.signature,
                feeOverride
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
    }

    return Pair(null, paymasterDataMiddleware)
}
