/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.core.accounts

import org.aakotlin.core.Chain
import org.aakotlin.core.EntryPoint
import org.aakotlin.core.client.BundlerClient
import org.aakotlin.core.signer.SmartAccountSigner
import org.aakotlin.core.util.Defaults
import org.aakotlin.core.util.await
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.DynamicBytes
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint192
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.exceptions.JsonRpcError
import org.web3j.utils.Numeric
import java.math.BigInteger

class CounterfactualAddressException(message: String) : Exception(message)

enum class DeploymentState(val value: String) {
    UNDEFINED("0x0"),
    NOT_DEPLOYED("0x1"),
    DEPLOYED("0x2"),
}

abstract class BaseSmartContractAccount(
    private val rpcClient: BundlerClient,
    private val entryPoint: EntryPoint? = null,
    private val signer: SmartAccountSigner,
    private val chain: Chain,
    private var accountAddress: String? = null,
) : ISmartContractAccount {
    protected var deploymentState = DeploymentState.UNDEFINED

    abstract suspend fun getAccountInitCode(forAddress: String): String

    override suspend fun getInitCode(): String {
        if (this.deploymentState == DeploymentState.DEPLOYED) {
            return "0x"
        }

        val ethGetCode = rpcClient.ethGetCode(getAddress(), DefaultBlockParameterName.LATEST).await()
        val contractCode = ethGetCode.code

        if (contractCode.length > 2) {
            this.deploymentState = DeploymentState.DEPLOYED
            return "0x"
        } else {
            this.deploymentState = DeploymentState.NOT_DEPLOYED
        }

        return getAccountInitCode(signer.getAddress())
    }

    override suspend fun getNonce(): BigInteger {
        if (!isAccountDeployed()) {
            return BigInteger.ZERO
        }

        return getNonce(BigInteger.ZERO)
    }

    suspend fun getNonce(nonceKey: BigInteger): BigInteger {
        val address = this.getAddress()
        val encodedCall = FunctionEncoder.encode(
            Function(
                "getNonce",
                listOf(
                    org.web3j.abi.datatypes.Address(address),
                    Uint192(nonceKey),
                ),
                listOf(object : TypeReference<Uint256>() {}),
            )
        )

        val result = rpcClient.ethCall(
            Transaction.createEthCallTransaction(
                signer.getAddress(),
                getEntryPoint().address,
                encodedCall
            ),
            DefaultBlockParameterName.LATEST,
        ).await().result

        return Numeric.toBigInt(result)
    }

    override suspend fun getAddress(): String {
        accountAddress?.let {
            return it
        }

        val address = getAddressForSigner(signer.getAddress())
        this.accountAddress = address

        return address
    }

    override suspend fun getAddressForSigner(signerAddress: String): String {
        val initCode = getAccountInitCode(signerAddress)
        val encodedCall = encodeGetSenderAddress(initCode)

        try {
            rpcClient.ethCall(
                Transaction.createEthCallTransaction(
                    signerAddress,
                    getEntryPoint().address,
                    encodedCall,
                ),
                DefaultBlockParameterName.LATEST,
            ).await()
        } catch (ex: JsonRpcError) {
            return "0x${(ex.data as String).trim('"', ' ').takeLast(40)}"
        }

        throw CounterfactualAddressException("Failed to get smart contract account address")
    }

    override fun getEntryPoint(): EntryPoint {
        return this.entryPoint ?: Defaults.getDefaultEntryPoint(this.chain)
    }

    internal fun encodeGetSenderAddress(initCode: String): String {
        return FunctionEncoder.encode(
            Function(
                "getSenderAddress",
                listOf(
                    DynamicBytes(
                        Numeric.hexStringToByteArray(initCode),
                    )
                ),
                listOf()
            )
        )
    }

    private suspend fun isAccountDeployed(): Boolean {
        return this.getDeploymentState() == DeploymentState.DEPLOYED
    }

    private suspend fun getDeploymentState(): DeploymentState {
        return if (this.deploymentState == DeploymentState.UNDEFINED) {
            if (getInitCode() == "0x") DeploymentState.DEPLOYED else DeploymentState.NOT_DEPLOYED
        } else {
            this.deploymentState
        }
    }
}
