/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.core.util

import org.aakotlin.core.EntryPoint
import org.aakotlin.core.UserOperationStruct
import org.web3j.abi.DefaultFunctionEncoder
import org.web3j.abi.datatypes.generated.Bytes32
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Hash
import org.web3j.utils.Numeric
import java.math.BigInteger

/**
 * Generates a hash for a UserOperation valid from entrypoint version 0.6 onwards
 *
 * @param request - the UserOperation to get the hash for
 * @param entryPoint - the entry point that will be used to execute the UserOperation
 * @param chainId - the chain on which this UserOperation will be executed
 * @returns the hash of the UserOperation
 */
fun getUserOperationHash(
    request: UserOperationStruct,
    entryPoint: EntryPoint,
    chainId: Long
): ByteArray {
    val packed = if (entryPoint.version == "0.7.0") {
        packUOv070(request)
    } else {
        packUOv060(request)
    }

    val encoded = DefaultFunctionEncoder().encodeParameters(
        listOf(
            Bytes32(Hash.sha3(packed)),
            org.web3j.abi.datatypes.Address(entryPoint.address),
            Uint256(chainId)
        )
    )

    return Hash.sha3(Numeric.hexStringToByteArray(encoded))
}

private fun packUOv060(request: UserOperationStruct): ByteArray {
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

private fun packUOv070(request: UserOperationStruct): ByteArray {
    val initCode = if (request.factory != null && request.factoryData != null) {
        concatHex(listOf(request.factory!!, request.factoryData!!))
    } else {
        "0x"
    }
    val hashedInitCode = Numeric.hexStringToByteArray(Hash.sha3(initCode))
    val accountGasLimits = packAccountGasLimits(
        request.verificationGasLimit ?: BigInteger.ZERO,
        request.callGasLimit ?: BigInteger.ZERO
    )
    val gasFees = packAccountGasLimits(
        request.maxPriorityFeePerGas ?: BigInteger.ZERO,
        request.maxFeePerGas ?: BigInteger.ZERO
    )

    val paymasterAndData = if (request.paymaster != null) {
        packPaymasterData(
            request.paymaster!!,
            request.paymasterVerificationGasLimit ?: BigInteger.ZERO,
            request.paymasterPostOpGasLimit ?: BigInteger.ZERO,
            request.paymasterData
        )
    } else {
        "0x"
    }
    val hashedCallData = Numeric.hexStringToByteArray(Hash.sha3(request.callData))
    val hashedPaymasterAndData = Numeric.hexStringToByteArray(Hash.sha3(paymasterAndData))

    val encoded = DefaultFunctionEncoder().encodeParameters(
        listOf(
            org.web3j.abi.datatypes.Address(request.sender),
            Uint256(request.nonce),
            Bytes32(hashedInitCode),
            Bytes32(hashedCallData),
            Bytes32(Numeric.hexStringToByteArray(accountGasLimits)),
            Uint256(request.preVerificationGas),
            Bytes32(Numeric.hexStringToByteArray(gasFees)),
            Bytes32(hashedPaymasterAndData)
        )
    )

    return Numeric.hexStringToByteArray(encoded)
}

/**
 * Packs two BigInteger values into a single hex string, with each value padded to 16 bytes (32 hex chars).
 * Used for packing gas limits and fee values in EntryPoint v0.7.
 */
fun packAccountGasLimits(value1: BigInteger, value2: BigInteger): String {
    val hex1 = padHex(Numeric.toHexStringNoPrefix(value1), 32)
    val hex2 = padHex(Numeric.toHexStringNoPrefix(value2), 32)
    return "0x$hex1$hex2"
}

/**
 * Packs paymaster data for EntryPoint v0.7.
 * Concatenates paymaster address with padded gas limits and paymaster data.
 */
fun packPaymasterData(
    paymaster: String,
    paymasterVerificationGasLimit: BigInteger,
    paymasterPostOpGasLimit: BigInteger,
    paymasterData: String?
): String {
    if (paymasterData == null) {
        return "0x"
    }
    
    val verificationGasLimitHex = padHex(Numeric.toHexStringNoPrefix(paymasterVerificationGasLimit), 32)
    val postOpGasLimitHex = padHex(Numeric.toHexStringNoPrefix(paymasterPostOpGasLimit), 32)
    
    return concatHex(listOf(
        paymaster,
        "0x$verificationGasLimitHex",
        "0x$postOpGasLimitHex",
        paymasterData
    ))
}

/**
 * Left-pads a hex string to the specified length.
 * 
 * @param hex The hex string to pad (without 0x prefix)
 * @param length The target length in characters
 * @return The padded hex string
 */
private fun padHex(hex: String, length: Int): String {
    return hex.padStart(length, '0')
}