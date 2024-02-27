/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.core.accounts

import org.aakotlin.core.Address
import org.aakotlin.core.Chain
import org.aakotlin.core.client.Erc4337Client
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
    private val rpcClient: Erc4337Client,
    private val entryPointAddress: Address? = null,
    private val factoryAddress: Address,
    private val signer: SmartAccountSigner,
    private val chain: Chain,
    private val accountAddress: Address? = null,
    private val index: Long? = null
): BaseSmartContractAccount(
    rpcClient,
    entryPointAddress,
    signer,
    chain,
    accountAddress
) {
    override suspend fun getAccountInitCode(): String {
        val address = signer.getAddress()
        val function = Function(
            "createAccount",
            listOf(
                org.web3j.abi.datatypes.Address(address),
                Uint256(index ?: 0)
            ),
            listOf(
                TypeReference.create(org.web3j.abi.datatypes.Address::class.java)
            )
        )
        val encoded = FunctionEncoder.encode(function)

        return concatHex(listOf(
            factoryAddress.address,
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

//    override suspend fun encodeBatchExecute(txs: List<UserOperationCallData>): String {
//        TODO("Not yet implemented")
//    }

    override suspend fun signMessage(msg: ByteArray): ByteArray {
        return signer.signMessage(msg)
    }

//    override suspend fun signMessageWith6492(msg: ByteArray): ByteArray {
//        TODO("Not yet implemented")
//    }

    override suspend fun getOwner(): SmartAccountSigner? {
        return signer
    }

    override suspend fun getFactoryAddress(): Address {
        return factoryAddress
    }
}