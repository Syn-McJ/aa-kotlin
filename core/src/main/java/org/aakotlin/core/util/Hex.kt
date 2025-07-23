/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.core.util

import org.aakotlin.core.UserOperationRequest
import org.aakotlin.core.UserOperationStruct
import org.web3j.utils.Numeric
import java.math.BigInteger

fun concatHex(values: List<String>): String {
    return "0x" + values.joinToString("") { it.replace("0x", "") }
}

fun UserOperationStruct.toUserOperationRequest(): UserOperationRequest {
    return UserOperationRequest(
        sender,
        Numeric.encodeQuantity(nonce),
        initCode,
        callData,
        Numeric.encodeQuantity(callGasLimit ?: BigInteger.ZERO),
        Numeric.encodeQuantity(verificationGasLimit ?: BigInteger.ZERO),
        Numeric.encodeQuantity(preVerificationGas ?: BigInteger.ZERO),
        maxFeePerGas?.let { Numeric.encodeQuantity(it) } ?: "0x",
        maxPriorityFeePerGas?.let { Numeric.encodeQuantity(it) } ?: "0x",
        paymasterAndData = paymasterAndData,
        signature = Numeric.toHexString(signature),
        eip7702Auth = eip7702Auth,
        paymaster = paymaster,
        paymasterData = paymasterData,
        paymasterPostOpGasLimit = Numeric.encodeQuantity(paymasterPostOpGasLimit ?: BigInteger.ZERO),
        paymasterVerificationGasLimit = Numeric.encodeQuantity(paymasterVerificationGasLimit ?: BigInteger.ZERO),
    )
}
