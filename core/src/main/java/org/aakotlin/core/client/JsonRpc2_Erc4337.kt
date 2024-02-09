/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.core.client

import org.aakotlin.core.Chain
import org.aakotlin.core.UserOperationRequest
import org.aakotlin.core.util.await
import org.web3j.protocol.Web3jService
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.JsonRpc2_0Web3j
import org.web3j.protocol.core.Request
import java.math.BigDecimal
import java.math.BigInteger
import java.util.concurrent.ScheduledExecutorService
import kotlin.math.ceil
import kotlin.math.pow

open class JsonRpc2_Erc4337 : JsonRpc2_0Web3j, Erc4337Client {
    constructor(web3jService: Web3jService) : super(web3jService)

    constructor(
        web3jService: Web3jService,
        pollingInterval: Long,
        scheduledExecutorService: ScheduledExecutorService,
    ) : super(web3jService, pollingInterval, scheduledExecutorService)

    override fun estimateUserOperationGas(
        request: UserOperationRequest,
        entryPoint: String,
    ): Request<*, EthEstimateUserOperationGas> {
        return Request(
            "eth_estimateUserOperationGas",
            listOf(request, entryPoint),
            web3jService,
            EthEstimateUserOperationGas::class.java,
        )
    }

    override fun sendUserOperation(
        request: UserOperationRequest,
        entryPoint: String,
    ): Request<*, EthSendUserOperation> {
        return Request(
            "eth_sendUserOperation",
            listOf(request, entryPoint),
            web3jService,
            EthSendUserOperation::class.java,
        )
    }

    override fun getUserOperationReceipt(hash: String): Request<*, EthGetUserOperationReceipt> {
        return Request(
            "eth_getUserOperationReceipt",
            listOf(hash),
            web3jService,
            EthGetUserOperationReceipt::class.java,
        )
    }

    override suspend fun estimateFeesPerGas(chain: Chain): FeeValuesEIP1559 {
        val baseFeeMultiplier = chain.baseFeeMultiplier ?: 1.2

        if (baseFeeMultiplier < 1) {
            throw IllegalArgumentException("`baseFeeMultiplier` must be greater than 1.")
        }

        val decimals = BigDecimal(baseFeeMultiplier).scale()
        val denominator = 10.0.pow(decimals).toLong()

        val multiply: (BigInteger) -> BigInteger = { base ->
            base.multiply(
                BigInteger.valueOf(
                    ceil(baseFeeMultiplier * denominator).toLong(),
                ),
            ) / BigInteger.valueOf(denominator)
        }

        val block =
            ethGetBlockByNumber(
                DefaultBlockParameterName.LATEST,
                false,
            ).await().block

        val maxPriorityFeePerGas =
            chain.defaultPriorityFee
                ?: ethMaxPriorityFeePerGas().await().maxPriorityFeePerGas

        val baseFeePerGas = multiply(block.baseFeePerGas)
        val maxFeePerGas = baseFeePerGas + maxPriorityFeePerGas

        return FeeValuesEIP1559(
            baseFeePerGas,
            maxFeePerGas,
            maxPriorityFeePerGas,
        )
    }
}
