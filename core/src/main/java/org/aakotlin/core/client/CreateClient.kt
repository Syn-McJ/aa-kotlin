/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.core.client

import org.aakotlin.core.Chain
import org.web3j.protocol.http.HttpService

fun createPublicErc4337Client(
    rpcUrl: String,
    headers: Map<String, String> = emptyMap(),
): Erc4337Client {
    val version = Chain::class.java.`package`.implementationVersion
    val service =
        HttpService(rpcUrl).apply {
            addHeader("AA-Kotlin-Sdk-Version", version)
            addHeaders(headers)
        }

    return JsonRpc2_Erc4337(service)
}
