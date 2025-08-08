/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.core.accounts

import org.aakotlin.core.Address
import org.aakotlin.core.EntryPoint
import org.aakotlin.core.UserOperationCallData
import org.aakotlin.core.signer.SmartAccountSigner
import java.math.BigInteger

interface ISmartContractAccount {
    /**
     * @returns the init code for the account
     */
    suspend fun getInitCode(): String

    /**
     * This is useful for estimating gas costs. It should return a signature that doesn't cause the account to revert
     * when validation is run during estimation.
     *
     * @returns a dummy signature that doesn't cause the account to revert during estimation
     */
    fun getDummySignature(): ByteArray

    /**
     * Encodes a call to the account's execute function.
     *
     * @param target - the address receiving the call data
     * @param value - optionally the amount of native token to send
     * @param data - the call data or "0x" if empty
     */
    suspend fun encodeExecute(target: Address, value: BigInteger, data: ByteArray): String

    /**
     * Encodes a batch of transactions to the account's batch execute function.
     * NOTE: not all accounts support batching.
     * @param txs - An Array of objects containing the target, value, and data for each transaction
     * @returns the encoded callData for a UserOperation
     */
    suspend fun encodeBatchExecute(txs: List<UserOperationCallData>): String

    /**
     * @returns the nonce of the account
     */
    suspend fun getNonce(): BigInteger

    /**
     * @returns a signed and prefixed message.
     * @param msg - the message to sign
     */
    suspend fun signMessage(msg: ByteArray): ByteArray

    /**
     * @returns the signer associated with this account
     */
    fun getSigner(): SmartAccountSigner

//    /**
//     * If the account is not deployed, it will sign the message and then wrap it in 6492 format
//     *
//     * @param msg - the message to sign
//     * @returns ths signature wrapped in 6492 format
//     */
//    suspend fun signMessageWith6492(msg: ByteArray): ByteArray

    /**
     * @returns the address of the account
     */
    suspend fun getAddress(): String

    /**
     * @returns the address of the smart contract account for the specified signer
     */
    suspend fun getAddressForSigner(signerAddress: String): String

    /**
     * @returns the address of the factory contract for the smart contract account
     */
    suspend fun getFactoryAddress(): String?

    suspend fun getFactoryData(initCode: String?): String?

    /**
     * @returns the address of the entry point contract for the smart contract account
     */
    fun getEntryPoint(): EntryPoint

    suspend fun isAccountDeployed(): Boolean

    fun getImplementationAddress(): String
}
