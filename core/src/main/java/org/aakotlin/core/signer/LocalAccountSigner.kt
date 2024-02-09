/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.core.signer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.crypto.Credentials
import org.web3j.crypto.Sign

class LocalAccountSigner(
    override val credentials: Credentials
) : SmartAccountSigner {
    override val signerType: String = "local"

    override suspend fun getAddress(): String {
        return credentials.address
    }

    override suspend fun signMessage(msg: ByteArray): ByteArray {
        return withContext(Dispatchers.IO) {
            val signature = Sign.signPrefixedMessage(msg, credentials.ecKeyPair)
            val sig = ByteArray(65)
            System.arraycopy(signature.r, 0, sig, 0, 32)
            System.arraycopy(signature.s, 0, sig, 32, 32)
            System.arraycopy(signature.v, 0, sig, 64, 1)

            sig
        }
    }
}
