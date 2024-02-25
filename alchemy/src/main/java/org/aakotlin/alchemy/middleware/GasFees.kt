/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.alchemy.middleware

import org.aakotlin.alchemy.provider.AlchemyProvider
import org.aakotlin.core.util.await
import org.web3j.protocol.core.DefaultBlockParameterName
import java.math.BigInteger

fun AlchemyProvider.withAlchemyGasFeeEstimator(
    baseFeeBufferPercent: BigInteger,
    maxPriorityFeeBufferPercent: BigInteger
) = apply {
    withFeeDataGetter { struct, overrides ->
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
            val priorityFeePerGas =
                // it's a fair assumption that if someone is using this Alchemy Middleware, then they are using Alchemy RPC
                (rpcClient as AlchemyClient).maxPriorityFeePerGas().await().maxPriorityFeePerGas

            val baseFeeIncrease =
                (baseFeePerGas * (100.toBigInteger() + baseFeeBufferPercent)) / 100.toBigInteger()
            val priorityFeeIncrease =
                (priorityFeePerGas * (100.toBigInteger() + maxPriorityFeeBufferPercent)) / 100.toBigInteger()

            struct.apply {
                maxFeePerGas = baseFeeIncrease + priorityFeeIncrease
                maxPriorityFeePerGas = priorityFeeIncrease
            }
        }
    }
}
