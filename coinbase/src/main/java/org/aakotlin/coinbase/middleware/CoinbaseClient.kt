/*
 * Copyright (c) 2025 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.coinbase.middleware

import org.aakotlin.core.UserOperationRequest
import org.aakotlin.core.client.Erc4337Client
import org.aakotlin.core.client.PaymasterAndData
import org.web3j.protocol.core.Request

interface CoinbaseClient: Erc4337Client {
    fun getPaymasterData(
        params: PaymasterDataParams
    ): Request<*, PaymasterAndData>

    fun sponsorUserOperation(
        userOp: UserOperationRequest,
        entryPoint: String
    ): Request<*, SponsoredUserOperation>
}
