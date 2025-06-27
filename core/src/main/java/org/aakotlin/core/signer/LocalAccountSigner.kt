/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.core.signer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.web3j.crypto.Credentials
import org.web3j.crypto.Sign

class LocalAccountSigner : SmartAccountSigner {
    companion object {
        fun privateKeyToAccountSigner(key: String): LocalAccountSigner {
            val signer = LocalAccountSigner()
            signer.setCredentials(Credentials.create(key))

            return signer
        }
    }

    override val signerType: String = "local"

    private val _credentials: MutableStateFlow<Credentials?> = MutableStateFlow(null)
    override val credentials: StateFlow<Credentials?>
        get() = _credentials.asStateFlow()

    fun setCredentials(credentials: Credentials) {
        _credentials.value = credentials
    }

    fun logout() {
        _credentials.value = null
    }

    override suspend fun getAddress(): String {
        return credentials.value?.address ?: throw IllegalStateException("Credentials not set")
    }

    override suspend fun signMessage(msg: ByteArray): ByteArray {
        val credentials = credentials.value ?: throw IllegalStateException("Credentials not set")

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
