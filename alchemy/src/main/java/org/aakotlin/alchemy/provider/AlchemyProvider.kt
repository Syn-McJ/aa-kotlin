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
import org.aakotlin.alchemy.middleware.withAlchemyGasFeeEstimator
import org.aakotlin.core.Address
import org.aakotlin.core.Chain
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
            val chain =
                SupportedChains[config.chain.id]
                    ?: throw IllegalArgumentException("Unsupported chain id: ${config.chain.id}")

            val rpcUrl =
                config.connectionConfig.rpcUrl
                    ?: chain.alchemyRpcHttpUrl?.let { "$it/${config.connectionConfig.apiKey ?: ""}" }
                    ?: throw IllegalArgumentException("No rpcUrl found for chain ${config.chain.id}")

            val rpcClient =
                createAlchemyClient(
                    rpcUrl,
                    config.connectionConfig.jwt?.let { jwt ->
                        mapOf("Authorization" to "Bearer $jwt")
                    } ?: mapOf(),
                )
            this.rpcUrl = rpcUrl

            return rpcClient
        }
    }

    private val pvgBuffer: Int
    private val feeOptsSet: Boolean

    init {
        withAlchemyGasFeeEstimator(
            (config.feeOpts?.baseFeeBufferPercent ?: 50).toBigInteger(),
            (config.feeOpts?.maxPriorityFeeBufferPercent ?: 5).toBigInteger(),
        )

        if (config.feeOpts?.preVerificationGasBufferPercent != null) {
            this.pvgBuffer = config.feeOpts.preVerificationGasBufferPercent
        } else if (
            setOf(
                Chain.Arbitrum.id,
                Chain.ArbitrumGoerli.id,
                Chain.Optimism.id,
                Chain.OptimismGoerli.id,
            ).contains(config.chain.id)
        ) {
            this.pvgBuffer = 5
        } else {
            this.pvgBuffer = 0
        }

        this.feeOptsSet = config.feeOpts != null
    }

    override suspend fun defaultGasEstimator(struct: UserOperationStruct): UserOperationStruct {
        val request = struct.toUserOperationRequest()
        val estimates = rpcClient.estimateUserOperationGas(
            request,
            getEntryPointAddress().address
        ).await().result

        struct.preVerificationGas = (estimates.preVerificationGas * (100 + this.pvgBuffer).toBigInteger()) / 100.toBigInteger()
        struct.verificationGasLimit = estimates.verificationGasLimit
        struct.callGasLimit = estimates.callGasLimit

        return struct
    }
}
