/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.alchemy.middleware

import org.aakotlin.core.provider.ClientMiddlewareFn
import org.aakotlin.core.util.await
import org.web3j.protocol.core.DefaultBlockParameterName

val alchemyFeeEstimator: ClientMiddlewareFn = { rpcClient, struct, overrides ->
    if (overrides.maxFeePerGas != null && overrides.maxPriorityFeePerGas != null) {
        struct.apply {
            maxFeePerGas = overrides.maxFeePerGas
            maxPriorityFeePerGas = overrides.maxPriorityFeePerGas
        }
    } else {
        val block = rpcClient.ethGetBlockByNumber(
            DefaultBlockParameterName.LATEST,
            false
        ).await().block
        val baseFeePerGas = block.baseFeePerGas
        val maxPriorityFeePerGasEstimate =
            // it's a fair assumption that if someone is using this Alchemy Middleware, then they are using Alchemy RPC
            (rpcClient as AlchemyClient).maxPriorityFeePerGas().await().maxPriorityFeePerGas

        val maxPriorityFeePerGas = overrides.maxPriorityFeePerGas ?: maxPriorityFeePerGasEstimate

        struct.apply {
            this.maxPriorityFeePerGas = maxPriorityFeePerGas
            this.maxFeePerGas = baseFeePerGas + maxPriorityFeePerGas
        }
    }
}
