/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.alchemy.provider

import org.aakotlin.alchemy.SupportedChains
import org.aakotlin.alchemy.alchemyRpcHttpUrl
import org.aakotlin.alchemy.middleware.alchemyFeeEstimator
import org.aakotlin.alchemy.middleware.createAlchemyClient
import org.aakotlin.core.Address
import org.aakotlin.core.UserOperationOverrides
import org.aakotlin.core.UserOperationStruct
import org.aakotlin.core.client.Erc4337Client
import org.aakotlin.core.provider.SmartAccountProvider
import org.aakotlin.core.util.await
import org.aakotlin.core.util.toUserOperationRequest

class AlchemyProvider(
    entryPointAddress: Address?,
    config: AlchemyProviderConfig,
) : SmartAccountProvider(createRpcClient(config), null, entryPointAddress, config.chain, config.opts) {
    companion object {
        private lateinit var rpcUrl: String

        private fun createRpcClient(config: AlchemyProviderConfig): Erc4337Client {
            val chain = SupportedChains[config.chain.id]
                ?: throw IllegalArgumentException("Unsupported chain id: ${config.chain.id}")

            val rpcUrl = config.connectionConfig.rpcUrl
                ?: chain.alchemyRpcHttpUrl?.let { "$it/${config.connectionConfig.apiKey ?: ""}" }
                ?: throw IllegalArgumentException("No rpcUrl found for chain ${config.chain.id}")

            val rpcClient = createAlchemyClient(
                rpcUrl,
                config.connectionConfig.jwt?.let { jwt ->
                    mapOf("Authorization" to "Bearer $jwt")
                } ?: mapOf()
            )
            this.rpcUrl = rpcUrl

            return rpcClient
        }
    }

    init {
        withGasEstimator(alchemyFeeEstimator)
    }

    override suspend fun defaultGasEstimator(
        client: Erc4337Client,
        struct: UserOperationStruct,
        overrides: UserOperationOverrides
    ): UserOperationStruct {
        if (overrides.preVerificationGas != null &&
            overrides.verificationGasLimit != null &&
            overrides.callGasLimit != null
        ) {
            struct.preVerificationGas = overrides.preVerificationGas
            struct.verificationGasLimit = overrides.verificationGasLimit
            struct.callGasLimit = overrides.callGasLimit
        } else {
            val request = struct.toUserOperationRequest()
            val estimates = rpcClient.estimateUserOperationGas(
                request,
                getEntryPointAddress().address
            ).await().result

            struct.preVerificationGas = overrides.preVerificationGas ?: estimates.preVerificationGas
            struct.verificationGasLimit = overrides.verificationGasLimit ?: estimates.verificationGasLimit
            struct.callGasLimit = overrides.callGasLimit ?: estimates.callGasLimit
        }

        return struct
    }
}
