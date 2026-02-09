/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.alchemy.account

import org.aakotlin.core.Address
import org.aakotlin.core.Chain
import org.aakotlin.core.UserOperationCallData
import org.aakotlin.core.accounts.BaseSmartContractAccount
import org.aakotlin.core.auth.AccountMode
import org.aakotlin.core.client.BundlerClient
import org.aakotlin.core.signer.SmartAccountSigner
import org.aakotlin.core.util.Defaults
import org.aakotlin.core.util.concatHex
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.utils.Numeric
import java.math.BigInteger

/**
 * Alchemy's ModularAccountV2 implementation supporting both ERC-4337 and EIP-7702
 */
class ModularAccountV2(
    private val rpcClient: BundlerClient,
    private val factoryAddress: String? = null,
    private val signer: SmartAccountSigner,
    private val chain: Chain,
    private val mode: AccountMode = AccountMode.DEFAULT,
    private val accountAddress: String? = null,
) : BaseSmartContractAccount(
        rpcClient = rpcClient,
        entryPoint = Defaults.getV7EntryPoint(chain),
        signer = signer,
        chain = chain,
        accountAddress = accountAddress
) {
    companion object {
        /**
         * ModularAccountV2 implementation contract address for EIP-7702 delegation
         */
        const val IMPLEMENTATION_ADDRESS = "0x69007702764179f14F51cdce752f4f775d74E139"
        
        /**
         * Default factory address for ModularAccountV2 when using ERC-4337 mode
         */
        private const val DEFAULT_FACTORY_ADDRESS = "0x00000000000017c61b5bEe81050EC8eFc9c6fecd"

        private const val DEFAULT_OWNER_ENTITY_ID = 0
    }

    /**
     * Get the account mode (ERC-4337 or EIP-7702)
     */
    fun getMode(): AccountMode = mode

    override suspend fun getAccountInitCode(forAddress: String): String {
        return when (mode) {
            AccountMode.EIP7702 -> "0x"
            AccountMode.DEFAULT -> {
                // ERC-4337 mode: use factory to create account
                val factory = factoryAddress ?: DEFAULT_FACTORY_ADDRESS
                concatHex(
                    listOf(
                        factory,
                        FunctionEncoder.encode(
                            Function(
                                "createSemiModularAccount",
                                listOf(
                                    org.web3j.abi.datatypes.Address(forAddress),
                                    Uint256(0) // salt
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
    }

    override suspend fun getAddress(): String {
        return when (mode) {
            AccountMode.EIP7702 -> {
                // EIP-7702 uses the signer's EOA address directly
                signer.getAddress()
            }
            AccountMode.DEFAULT -> {
                // ERC-4337 uses counterfactual address calculation
                super.getAddress()
            }
        }
    }

    override suspend fun getAddressForSigner(signerAddress: String): String {
        return when (mode) {
            AccountMode.EIP7702 -> {
                // EIP-7702 uses the signer's EOA address directly
                val address = signer.getAddress()

                if (address != signerAddress) {
                    throw IllegalStateException("signerAddress parameter and the address of account's signer must match")
                }

                return signerAddress
            }
            AccountMode.DEFAULT -> {
                // ERC-4337 uses counterfactual address calculation
                super.getAddressForSigner(signerAddress)
            }
        }
    }

    override suspend fun getNonce(): BigInteger {
        return super.getNonce(buildFullNonceKey(
            nonceKey = 0,
            entityId = DEFAULT_OWNER_ENTITY_ID,
            isGlobalValidation = true,
            isDeferredAction = false
        ))
    }

    private fun buildFullNonceKey(
        nonceKey: Long = 0,
        entityId: Int = 0,
        isGlobalValidation: Boolean = true,
        isDeferredAction: Boolean = false
    ): BigInteger {
        return BigInteger.valueOf(nonceKey).shiftLeft(40)  // Shift nonce key left by 40 bits
            .add(BigInteger.valueOf(entityId.toLong()).shiftLeft(8))  // Entity ID in bits 8-39
            .add(if (isDeferredAction) BigInteger.valueOf(2) else BigInteger.ZERO)  // Deferred action flag
            .add(if (isGlobalValidation) BigInteger.ONE else BigInteger.ZERO)  // Global validation flag
    }

    /**
     * Get the implementation address for this account
     */
    override fun getImplementationAddress(): String = IMPLEMENTATION_ADDRESS
    
    /**
     * Get the account signer
     */
    override fun getSigner(): SmartAccountSigner = signer
    
    // Implement abstract methods from BaseSmartContractAccount/ISmartContractAccount
    
    override fun getDummySignature(): ByteArray {
        return Numeric.hexStringToByteArray(
            concatHex(
                listOf(
                    "0xFF",
                    "0x00",
                    "0xfffffffffffffffffffffffffffffff0000000000000000000000000000000007aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1c"
                )
            )
        )
    }

    override suspend fun signMessage(msg: ByteArray): ByteArray {
        return packUOSignature(signer.signMessage(msg))
    }
    
    override suspend fun getFactoryAddress(): String? {
        if (mode != AccountMode.DEFAULT) {
            return null
        }

        return factoryAddress ?: DEFAULT_FACTORY_ADDRESS
    }

    override suspend fun getFactoryData(initCode: String?): String? {
        if (mode != AccountMode.DEFAULT) {
            return null
        }

        val resolvedInitCode = initCode ?: getInitCode()
        return parseFactoryAddressFromAccountInitCode(resolvedInitCode).second
    }

    override suspend fun encodeExecute(target: Address, value: BigInteger, data: ByteArray): String {
        val function = Function(
            "execute",
            listOf(
                org.web3j.abi.datatypes.Address(target.address),
                Uint256(value),
                org.web3j.abi.datatypes.DynamicBytes(data)
            ),
            listOf()
        )
        
        return FunctionEncoder.encode(function)
    }
    
    override suspend fun encodeBatchExecute(txs: List<UserOperationCallData>): String {
        val tuples = txs.map { tx ->
            org.web3j.abi.datatypes.DynamicStruct(
                listOf(
                    org.web3j.abi.datatypes.Address(tx.target.address),
                    Uint256(tx.value ?: BigInteger.ZERO),
                    org.web3j.abi.datatypes.DynamicBytes(tx.data)
                )
            )
        }
        
        val function = Function(
            "executeBatch",
            listOf(
                org.web3j.abi.datatypes.DynamicArray(
                    org.web3j.abi.datatypes.DynamicStruct::class.java,
                    tuples
                )
            ),
            listOf()
        )
        
        return FunctionEncoder.encode(function)
    }

    private fun packUOSignature(sig: ByteArray): ByteArray {
        return byteArrayOf(0xFF.toByte(), 0x00) + sig
    }

    /**
     * Parses the factory address and factory calldata from the provided account initialization code (initCode).
     * val (address, calldata) = parseFactoryAddressFromAccountInitCode("0xAddressCalldata");
     *
     * @param {Hex} initCode The initialization code from which to parse the factory address and calldata
     * @returns {[Address, Hex]} A tuple containing the parsed factory address and factory calldata
     */
    private fun parseFactoryAddressFromAccountInitCode(initCode: String): Pair<String, String> {
        if (initCode.length < 44) {
            return Pair("0x", "0x")
        }

        val factoryAddress = "0x${initCode.substring(2, 42)}"
        val factoryCalldata = "0x${initCode.substring(42)}"
        
        return Pair(factoryAddress, factoryCalldata)
    }
}