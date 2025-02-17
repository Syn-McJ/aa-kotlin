package org.aakotlin.coinbase.middleware

import org.aakotlin.coinbase.provider.CoinbaseProvider
import org.aakotlin.core.provider.ConnectionConfig
import org.aakotlin.core.provider.ProviderConfig
import org.aakotlin.core.provider.SmartAccountProvider
import org.aakotlin.core.util.await
import org.aakotlin.core.util.toUserOperationRequest
import java.math.BigInteger

fun SmartAccountProvider.withCoinbaseGasManager(connectionConfig: ConnectionConfig) = apply {
    withGasEstimator { _, uoStruct, overrides ->
        uoStruct.apply {
            callGasLimit = overrides.callGasLimit ?: BigInteger.ZERO
            preVerificationGas = overrides.preVerificationGas ?: BigInteger.ZERO
            verificationGasLimit = overrides.verificationGasLimit ?: BigInteger.ZERO
        }
    }

    withPaymasterMiddleware(
        { _, struct, _ ->
            struct.apply {
                paymasterAndData = "0xc03aac639bb21233e0139381970328db8bceeb67fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff0000000000000000000000000000000007aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1c"
            }
        },
        { middlewareClient, struct, overrides ->
            struct.apply {
                callGasLimit = overrides.callGasLimit
                preVerificationGas = overrides.preVerificationGas
                verificationGasLimit = overrides.verificationGasLimit
                paymasterAndData = overrides.paymasterAndData ?: "0x"
            }

            if (overrides.callGasLimit == null ||
                overrides.preVerificationGas == null ||
                overrides.verificationGasLimit == null ||
                overrides.paymasterAndData == null
            ) {
                val request = struct.toUserOperationRequest()
                val result = (middlewareClient as CoinbaseClient).sponsorUserOperation(
                    request,
                    getEntryPointAddress().address,
                ).await().result

                struct.apply {
                    callGasLimit = result.callGasLimit
                    preVerificationGas = result.preVerificationGas
                    verificationGasLimit = result.verificationGasLimit
                    paymasterAndData = result.paymasterAndData
                }
            }

            struct
        }
    )
}.withMiddlewareRpcClient(
    CoinbaseProvider.createRpcClient(
        ProviderConfig(chain, connectionConfig)
    )
)
