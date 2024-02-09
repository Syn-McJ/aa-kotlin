/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.alchemy.middleware

import org.aakotlin.core.client.Erc4337Client
import org.web3j.protocol.core.Request

interface AlchemyClient: Erc4337Client {
    fun maxPriorityFeePerGas(): Request<*, AlchemyMaxPriorityFeePerGas>

    fun requestPaymasterAndData(
        params: PaymasterAndDataParams
    ): Request<*, AlchemyPaymasterAndData>

    fun requestGasAndPaymasterAndData(
        params: PaymasterAndDataParams
    ): Request<*, AlchemyGasAndPaymasterAndData>
}
