/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.alchemy.middleware

import org.aakotlin.core.PaymasterDataParams
import org.aakotlin.core.client.PaymasterData
import org.aakotlin.core.client.JsonRpc2_BundlerClient
import org.web3j.protocol.Web3jService
import org.web3j.protocol.core.Request

class AlchemyRpcClient(
    web3jService: Web3jService
): JsonRpc2_BundlerClient(web3jService), AlchemyClient {
    override fun maxPriorityFeePerGas(): Request<*, AlchemyMaxPriorityFeePerGas> {
        return Request(
            "rundler_maxPriorityFeePerGas",
            listOf<String>(),
            web3jService,
            AlchemyMaxPriorityFeePerGas::class.java
        )
    }

    override fun requestPaymasterAndData(
        params: PaymasterAndDataParams
    ): Request<*, PaymasterData> {
        return Request(
            "alchemy_requestPaymasterAndData",
            listOf(params),
            web3jService,
            PaymasterData::class.java
        )
    }

    override fun requestGasAndPaymasterAndData(
        params: PaymasterAndDataParams
    ): Request<*, AlchemyGasAndPaymasterAndData> {
        return Request(
            "alchemy_requestGasAndPaymasterAndData",
            listOf(params),
            web3jService,
            AlchemyGasAndPaymasterAndData::class.java
        )
    }

    override fun getPaymasterStubData(
        params: PaymasterDataParams
    ): Request<*, PaymasterData> {
        return Request(
            "pm_getPaymasterStubData",
            listOf(params.userOperation, params.entryPoint, params.chainId, params.policy),
            web3jService,
            PaymasterData::class.java
        )
    }
}
