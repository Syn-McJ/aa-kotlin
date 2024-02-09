/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.alchemy.middleware

import org.aakotlin.core.Chain
import org.web3j.protocol.http.HttpService

fun createAlchemyClient(
    rpcUrl: String,
    headers: Map<String, String> = emptyMap()
): AlchemyClient {
    val version = Chain::class.java.`package`.implementationVersion
    val service = HttpService(rpcUrl).apply {
        addHeader("AA-Kotlin-Sdk-Version", version)
        addHeaders(headers)
    }

    return AlchemyRpcClient(service)
}
