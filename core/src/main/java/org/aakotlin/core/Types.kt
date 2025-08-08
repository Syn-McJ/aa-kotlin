/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.core

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.aakotlin.core.auth.Eip7702Auth
import java.math.BigInteger

@JvmInline
value class Address(val address: String) {
    init {
        require(address.startsWith("0x")) { "Address must start with 0x" }
    }
}

data class SendUserOperationResult(
    val hash: String,
    val request: UserOperationRequest
)

data class UserOperationOverrides(
    val callGasLimit: BigInteger? = null,
    val maxFeePerGas: BigInteger? = null,
    val maxPriorityFeePerGas: BigInteger? = null,
    val preVerificationGas: BigInteger? = null,
    val verificationGasLimit: BigInteger? = null,
    val paymasterVerificationGasLimit: BigInteger? = null,
    val paymasterAndData: String? = null
)

class UserOperationCallData(
    /** the target of the call */
    val target: Address,
    /** the data passed to the target */
    val data: ByteArray,
    /** the amount of native token to send to the target (default: 0) */
    val value: BigInteger? = null
)

/** represents the request as it needs to be formatted for RPC requests */
data class UserOperationRequest(
    /** the origin of the request */
    val sender: String,
    /** nonce of the transaction, returned from the entrypoint for this Address */
    val nonce: String,
    /** the initCode for creating the sender if it does not exist yet, otherwise "0x" */
    val initCode: String?,
    /** the callData passed to the target */
    val callData: String,

    /** Value used by inner account execution */
    val callGasLimit: String?,
    /** Actual gas used by the validation of this UserOperation */
    val verificationGasLimit: String?,
    /** Gas overhead of this UserOperation */
    val preVerificationGas: String?,
    /** Maximum fee per gas (similar to EIP-1559 max_fee_per_gas) */
    val maxFeePerGas: String?,
    /** Maximum priority fee per gas (similar to EIP-1559 max_priority_fee_per_gas) */
    val maxPriorityFeePerGas: String?,
    /** Data passed into the account along with the nonce during the verification step */
    val signature: String,

    // v6 fields
    /** Address of paymaster sponsoring the transaction, followed by extra data to send to the paymaster ("0x" for self-sponsored transaction) */
    val paymasterAndData: String?,

    // v7 fields
    /** address of paymaster contract, (or empty, if account pays for itself) */
    val paymaster: String?,
    /** the amount of gas to allocate for the paymaster validation code */
    val paymasterVerificationGasLimit: String?,
    /** the amount of gas to allocate for the paymaster post-operation code */
    val paymasterPostOpGasLimit: String?,
    /** data for paymaster (only if paymaster exists) */
    val paymasterData: String?,
    /** EIP-7702 authorization tuple for account delegation (optional) */
    var eip7702Auth: Eip7702Auth?,
    /** account factory, only for new accounts */
    var factory: String? = null,
    /** data for account factory (only if account factory exists) */
    var factoryData: String? = null
)

// based on @account-abstraction/common
// this is used for building requests
class UserOperationStruct(
    /** the origin of the request */
    var sender: String,
    /** nonce of the transaction, returned from the entrypoint for this Address */
    var nonce: BigInteger,
    /** the initCode for creating the sender if it does not exist yet, otherwise "0x" */
    var initCode: String?,
    /** the callData passed to the target */
    var callData: String,
    /** Value used by inner account execution */
    var callGasLimit: BigInteger? = null,
    /** Actual gas used by the validation of this UserOperation */
    var verificationGasLimit: BigInteger? = null,
    /** Gas overhead of this UserOperation */
    var preVerificationGas: BigInteger? = null,
    /** Maximum fee per gas (similar to EIP-1559 max_fee_per_gas) */
    var maxFeePerGas: BigInteger? = null,
    /** Maximum priority fee per gas (similar to EIP-1559 max_priority_fee_per_gas) */
    var maxPriorityFeePerGas: BigInteger? = null,
    /** Data passed into the account along with the nonce during the verification step */
    var signature: ByteArray,

    // v6 fields
    /** Address of paymaster sponsoring the transaction, followed by extra data to send to the paymaster ("0x" for self-sponsored transaction) */
    var paymasterAndData: String? = null,

    // v7 fields
    /** address of paymaster contract, (or empty, if account pays for itself) */
    var paymaster: String? = null,
    /** the amount of gas to allocate for the paymaster validation code */
    var paymasterVerificationGasLimit: BigInteger? = null,
    /** the amount of gas to allocate for the paymaster post-operation code */
    var paymasterPostOpGasLimit: BigInteger? = null,
    /** data for paymaster (only if paymaster exists) */
    var paymasterData: String? = null,
    /** account factory, only for new accounts */
    var factory: String? = null,
    /** data for account factory (only if account factory exists) */
    var factoryData: String? = null,
    /** EIP-7702 authorization tuple for account delegation (optional) */
    var eip7702Auth: Eip7702Auth? = null
)

data class UserOperationReceipt @JsonCreator constructor(
    /** The request hash of the UserOperation. */
    @JsonProperty(value = "userOpHash")
    val userOpHash: String,
    /** The entry point address used for the UserOperation. */
    @JsonProperty(value = "entryPoint")
    val entryPoint: String,
    /** The account initiating the UserOperation. */
    @JsonProperty(value = "sender")
    val sender: String,
    /** The nonce used in the UserOperation. */
    @JsonProperty(value = "nonce")
    val nonce: String,
    /** The paymaster used for this UserOperation (or empty). */
    @JsonProperty(value = "paymaster")
    val paymaster: String?,
    /** The actual amount paid (by account or paymaster) for this UserOperation. */
    @JsonProperty(value = "actualGasCost")
    val actualGasCost: String,
    /** The total gas used by this UserOperation (including preVerification, creation, validation, and execution). */
    @JsonProperty(value = "actualGasUsed")
    val actualGasUsed: String,
    /** Indicates whether the execution completed without reverting. */
    @JsonProperty(value = "success")
    val success: String,
    /** In case of revert, this is the revert reason. */
    @JsonProperty(value = "reason")
    val reason: String?
)

data class EntryPoint(
    val address: String,
    val version: String,
    val chain: Chain
)

data class Policy(
    val policyId: String
)

data class PaymasterDataParams(
    val userOperation: UserOperationRequest,
    val entryPoint: String,
    val chainId: String,
    val policy: Policy?
)
