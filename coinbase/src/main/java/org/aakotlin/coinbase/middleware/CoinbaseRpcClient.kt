/*
 * Copyright (c) 2025 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.coinbase.middleware

import org.aakotlin.core.PaymasterDataParams
import org.aakotlin.core.UserOperationRequest
import org.aakotlin.core.client.JsonRpc2_BundlerClient
import org.aakotlin.core.client.PaymasterData
import org.web3j.protocol.Web3jService
import org.web3j.protocol.core.Request

class CoinbaseRpcClient(
    web3jService: Web3jService
): JsonRpc2_BundlerClient(web3jService), CoinbaseClient {
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

    override fun sponsorUserOperation(
        userOp: UserOperationRequest,
        entryPoint: String
    ): Request<*, SponsoredUserOperation> {
        return Request(
            "pm_sponsorUserOperation",
            listOf(userOp, entryPoint),
            web3jService,
            SponsoredUserOperation::class.java
        )
    }
}
