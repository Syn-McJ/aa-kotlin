/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.alchemy.provider

import org.aakotlin.alchemy.SupportedChains
import org.aakotlin.alchemy.alchemyRpcHttpUrl
import org.aakotlin.alchemy.middleware.createAlchemyClient
import org.aakotlin.core.client.BundlerClient
import org.aakotlin.core.provider.ProviderConfig
import org.aakotlin.core.provider.SmartAccountProvider

class AlchemyProvider(
    config: ProviderConfig,
) : SmartAccountProvider(createRpcClient(config), null, config.chain, config.opts) {
    companion object {
        private lateinit var rpcUrl: String

        internal fun createRpcClient(config: ProviderConfig): BundlerClient {
            val chain = SupportedChains[config.chain.id]
                ?: throw IllegalArgumentException("Unsupported chain id: ${config.chain.id}")

            val rpcUrl = config.connectionConfig.rpcUrl ?: chain.alchemyRpcHttpUrl?.let { baseUrl ->
                config.connectionConfig.apiKey?.let { "$baseUrl/$it" } ?: baseUrl
            } ?: throw IllegalArgumentException("No rpcUrl found for chain ${config.chain.id}")

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
}
