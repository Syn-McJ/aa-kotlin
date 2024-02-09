/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.core.client

import org.web3j.protocol.core.Response

class EthSendUserOperation : Response<String>() {
    val transactionHash: String
        get() = result
}
