/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.alchemy.middleware

import org.aakotlin.core.client.PaymasterAndData
import org.aakotlin.core.client.JsonRpc2_Erc4337
import org.web3j.protocol.Web3jService
import org.web3j.protocol.core.Request

class AlchemyRpcClient(
    web3jService: Web3jService
): JsonRpc2_Erc4337(web3jService), AlchemyClient {
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
    ): Request<*, PaymasterAndData> {
        return Request(
            "alchemy_requestPaymasterAndData",
            listOf(params),
            web3jService,
            PaymasterAndData::class.java
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
}
