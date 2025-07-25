/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.core.accounts

import org.aakotlin.core.Address
import org.aakotlin.core.Chain
import org.aakotlin.core.EntryPoint
import org.aakotlin.core.UserOperationCallData
import org.aakotlin.core.client.BundlerClient
import org.aakotlin.core.signer.SmartAccountSigner
import org.aakotlin.core.util.concatHex
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.DynamicBytes
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.utils.Numeric
import java.math.BigInteger

open class SimpleSmartContractAccount(
    private val rpcClient: BundlerClient,
    private val entryPoint: EntryPoint? = null,
    private val factoryAddress: String,
    private val signer: SmartAccountSigner,
    private val chain: Chain,
    private val accountAddress: String? = null,
    private val index: Long? = null
): BaseSmartContractAccount(
    rpcClient,
    entryPoint,
    signer,
    chain,
    accountAddress
) {
    override suspend fun getAccountInitCode(forAddress: String): String {
        val function = Function(
            "createAccount",
            listOf(
                org.web3j.abi.datatypes.Address(forAddress),
                Uint256(index ?: 0)
            ),
            listOf(
                TypeReference.create(org.web3j.abi.datatypes.Address::class.java)
            )
        )
        val encoded = FunctionEncoder.encode(function)

        return concatHex(listOf(
            factoryAddress,
            encoded
        ))
    }

    override fun getDummySignature(): ByteArray {
        return Numeric.hexStringToByteArray("0xfffffffffffffffffffffffffffffff0000000000000000000000000000000007aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1c")
    }

    override suspend fun encodeExecute(target: Address, value: BigInteger, data: ByteArray): String {
        val function = Function(
            "execute",
            listOf(
                org.web3j.abi.datatypes.Address(target.address),
                Uint256(value),
                DynamicBytes(data)
            ),
            listOf()
        )

        return FunctionEncoder.encode(function)
    }

    override suspend fun encodeBatchExecute(txs: List<UserOperationCallData>): String {
        val targets = txs.map { org.web3j.abi.datatypes.Address(it.target.address) }
        val datas = txs.map { DynamicBytes(it.data) }

        val function = Function(
            "executeBatch",
            listOf(
                org.web3j.abi.datatypes.DynamicArray(org.web3j.abi.datatypes.Address::class.java, targets),
                org.web3j.abi.datatypes.DynamicArray(DynamicBytes::class.java, datas),
            ),
            listOf()
        )

        return FunctionEncoder.encode(function)
    }

    override suspend fun signMessage(msg: ByteArray): ByteArray {
        return signer.signMessage(msg)
    }

    override fun getSigner(): SmartAccountSigner = signer

//    override suspend fun signMessageWith6492(msg: ByteArray): ByteArray {
//        TODO("Not yet implemented")
//    }

    override suspend fun getOwner(): SmartAccountSigner? {
        return signer
    }

    override suspend fun getFactoryAddress(): String {
        return factoryAddress
    }

    override suspend fun getFactoryData(initCode: String?): String {
        TODO("Not yet implemented")
    }

    override fun getImplementationAddress(): String {
        TODO("Not yet implemented")
    }
}