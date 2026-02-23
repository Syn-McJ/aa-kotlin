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
import org.aakotlin.core.auth.Authorization
import org.aakotlin.core.auth.AuthorizationSignature
import org.web3j.crypto.Credentials
import org.web3j.crypto.Sign
import org.web3j.rlp.RlpEncoder
import org.web3j.rlp.RlpList
import org.web3j.rlp.RlpString
import org.web3j.utils.Numeric

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
            assembleSignature(signature)
        }
    }

    override suspend fun signHash(hash: ByteArray): ByteArray {
        val credentials = credentials.value ?: throw IllegalStateException("Credentials not set")

        return withContext(Dispatchers.IO) {
            val signature = Sign.signMessage(hash, credentials.ecKeyPair, false)
            assembleSignature(signature)
        }
    }

    private fun assembleSignature(signature: Sign.SignatureData): ByteArray {
        val sig = ByteArray(65)
        System.arraycopy(signature.r, 0, sig, 0, 32)
        System.arraycopy(signature.s, 0, sig, 32, 32)
        System.arraycopy(signature.v, 0, sig, 64, 1)
        return sig
    }

    override suspend fun signAuthorization(authorization: Authorization): AuthorizationSignature {
        val credentials = credentials.value ?: throw IllegalStateException("Credentials not set")

        return withContext(Dispatchers.IO) {
            // EIP-7702 authorization hash calculation
            // keccak256(0x05 || rlp([chainId, contractAddress, nonce]))
            val encodedData = encodeAuthorizationForSigning(authorization)
            val messageHash = org.web3j.crypto.Hash.sha3(encodedData)
            
            // EIP-7702 uses raw message signing without prefix
            val sig = assembleSignature(Sign.signMessage(messageHash, credentials.ecKeyPair, false))

            // Convert v to yParity (v - 27), following aa-sdk implementation
            val vValue = sig[64].toInt() and 0xFF
            val yParity = "0x${(vValue - 27).toString(16)}"

            AuthorizationSignature(
                r = Numeric.toHexString(sig.copyOfRange(0, 32)),
                s = Numeric.toHexString(sig.copyOfRange(32, 64)),
                yParity = yParity
            )
        }
    }

    private fun encodeAuthorizationForSigning(authorization: Authorization): ByteArray {
        // EIP-7702 encoding: 0x05 prefix + RLP([chainId, contractAddress, nonce])
        val rlpList = RlpList(
            RlpString.create(authorization.chainId),
            RlpString.create(Numeric.hexStringToByteArray(authorization.contractAddress)),
            RlpString.create(authorization.nonce)
        )
        
        val rlpEncoded = RlpEncoder.encode(rlpList)
        
        // Prepend 0x05 magic byte
        val result = ByteArray(1 + rlpEncoded.size)
        result[0] = 0x05
        System.arraycopy(rlpEncoded, 0, result, 1, rlpEncoded.size)
        
        return result
    }
}
