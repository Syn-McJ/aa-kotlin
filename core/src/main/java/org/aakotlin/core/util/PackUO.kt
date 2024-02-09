/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.core.util

import org.aakotlin.core.Address
import org.aakotlin.core.UserOperationStruct
import org.web3j.abi.DefaultFunctionEncoder
import org.web3j.abi.datatypes.generated.Bytes32
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Hash
import org.web3j.utils.Numeric

/**
 * Generates a hash for a UserOperation valid from entrypoint version 0.6 onwards
 *
 * @param request - the UserOperation to get the hash for
 * @param entryPointAddress - the entry point address that will be used to execute the UserOperation
 * @param chainId - the chain on which this UserOperation will be executed
 * @returns the hash of the UserOperation
 */
fun getUserOperationHash(
    request: UserOperationStruct,
    entryPointAddress: Address,
    chainId: Long
): ByteArray {
    val encoded = DefaultFunctionEncoder().encodeParameters(
        listOf(
            Bytes32(Hash.sha3(packUo(request))),
            org.web3j.abi.datatypes.Address(entryPointAddress.address),
            Uint256(chainId)
        )
    )

    return Hash.sha3(Numeric.hexStringToByteArray(encoded))
}

private fun packUo(request: UserOperationStruct): ByteArray {
    val hashedInitCode = Numeric.hexStringToByteArray(Hash.sha3(request.initCode))
    val hashedCallData = Numeric.hexStringToByteArray(Hash.sha3(request.callData))
    val hashedPaymasterAndData = Numeric.hexStringToByteArray(Hash.sha3(request.paymasterAndData))

    val encoded = DefaultFunctionEncoder().encodeParameters(
        listOf(
            org.web3j.abi.datatypes.Address(request.sender),
            Uint256(request.nonce),
            Bytes32(hashedInitCode),
            Bytes32(hashedCallData),
            Uint256(request.callGasLimit),
            Uint256(request.verificationGasLimit),
            Uint256(request.preVerificationGas),
            Uint256(request.maxFeePerGas),
            Uint256(request.maxPriorityFeePerGas),
            Bytes32(hashedPaymasterAndData)
        )
    )

    return Numeric.hexStringToByteArray(encoded)
}
