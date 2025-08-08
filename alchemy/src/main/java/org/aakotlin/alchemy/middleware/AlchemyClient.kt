/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.alchemy.middleware

import org.aakotlin.core.client.PaymasterData
import org.aakotlin.core.client.BundlerClient
import org.aakotlin.core.client.Erc7677Client
import org.web3j.protocol.core.Request

interface AlchemyClient: BundlerClient, Erc7677Client {
    fun maxPriorityFeePerGas(): Request<*, AlchemyMaxPriorityFeePerGas>

    fun requestPaymasterAndData(
        params: PaymasterAndDataParams
    ): Request<*, PaymasterData>

    fun requestGasAndPaymasterAndData(
        params: PaymasterAndDataParams
    ): Request<*, AlchemyGasAndPaymasterAndData>
}
