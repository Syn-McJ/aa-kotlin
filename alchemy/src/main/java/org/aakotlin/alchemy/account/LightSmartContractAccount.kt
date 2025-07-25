/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.alchemy.account

import org.aakotlin.core.Chain
import org.aakotlin.core.EntryPoint
import org.aakotlin.core.accounts.SimpleSmartContractAccount
import org.aakotlin.core.client.BundlerClient
import org.aakotlin.core.signer.SmartAccountSigner
import org.aakotlin.core.util.concatHex
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256

// Alchemy's implementation of erc4337 account
class LightSmartContractAccount(
    private val rpcClient: BundlerClient,
    private val entryPoint: EntryPoint? = null,
    private val factoryAddress: String,
    private val signer: SmartAccountSigner,
    private val chain: Chain,
    private val accountAddress: String? = null,
) : SimpleSmartContractAccount(
        rpcClient,
        entryPoint,
        factoryAddress,
        signer,
        chain,
        accountAddress
) {
    override suspend fun getAccountInitCode(forAddress: String): String {
        return concatHex(
            listOf(
                factoryAddress,
                FunctionEncoder.encode(
                    Function(
                        "createAccount",
                        listOf(
                            org.web3j.abi.datatypes.Address(forAddress),
                            // light account does not support sub-accounts
                            Uint256(0)
                        ),
                        listOf(
                            TypeReference.create(org.web3j.abi.datatypes.Address::class.java),
                        )
                    )
                )
            )
        )
    }
}
