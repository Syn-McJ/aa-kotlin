/*
 * Copyright (c) 2025 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.coinbase.provider

import org.aakotlin.coinbase.SupportedChains
import org.aakotlin.coinbase.coinbasePaymasterAndBundlerUrl
import org.aakotlin.core.Address
import org.aakotlin.core.client.Erc4337Client
import org.aakotlin.core.provider.ProviderConfig
import org.aakotlin.core.provider.SmartAccountProvider

class CoinbaseProvider(
    entryPointAddress: Address?,
    config: ProviderConfig,
) : SmartAccountProvider(createRpcClient(config), null, entryPointAddress, config.chain, config.opts) {
    companion object {
        private lateinit var rpcUrl: String

        internal fun createRpcClient(config: ProviderConfig): Erc4337Client {
            val chain = SupportedChains[config.chain.id]
                ?: throw IllegalArgumentException("Unsupported chain id: ${config.chain.id}")

            val rpcUrl = config.connectionConfig.rpcUrl
                ?: chain.coinbasePaymasterAndBundlerUrl?.let { "$it/${config.connectionConfig.apiKey ?: ""}" }
                ?: throw IllegalArgumentException("No rpcUrl found for chain ${config.chain.id}")

            val rpcClient = createCoinbaseClient(
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
