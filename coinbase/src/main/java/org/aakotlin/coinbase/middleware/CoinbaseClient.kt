/*
 * Copyright (c) 2025 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.coinbase.middleware

import org.aakotlin.core.UserOperationRequest
import org.aakotlin.core.client.BundlerClient
import org.aakotlin.core.client.Erc7677Client
import org.web3j.protocol.core.Request

interface CoinbaseClient: BundlerClient, Erc7677Client {
    fun sponsorUserOperation(
        userOp: UserOperationRequest,
        entryPoint: String
    ): Request<*, SponsoredUserOperation>
}
