/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.core.util

import org.aakotlin.core.UserOperationStruct
import java.math.BigInteger

fun UserOperationStruct.isValidRequest(): Boolean =
    (this.callGasLimit?.equals(BigInteger.ZERO) == false) &&
        (this.maxFeePerGas?.equals(BigInteger.ZERO) == false) &&
        (this.maxPriorityFeePerGas != null) &&
        (this.preVerificationGas?.equals(BigInteger.ZERO) == false) &&
        (this.verificationGasLimit?.equals(BigInteger.ZERO) == false)
