/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.core.util

import kotlinx.coroutines.future.await
import org.web3j.protocol.core.Request
import org.web3j.protocol.core.Response
import org.web3j.protocol.exceptions.JsonRpcError

suspend fun <S, T : Response<*>> Request<S, T>.await(): T {
    val response = this.sendAsync().await()

    if (response.hasError()) {
        throw JsonRpcError(response.error)
    }

    return response
}
