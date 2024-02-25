/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.alchemy.middleware

data class FeeOverride(
    val maxFeePerGas: String? = null,
    val maxPriorityFeePerGas: String? = null,
    val callGasLimit: String? = null,
    val verificationGasLimit: String? = null,
    val preVerificationGas: String? = null
) {
    val isEmpty = maxFeePerGas == null &&
        maxPriorityFeePerGas == null &&
        callGasLimit == null &&
        verificationGasLimit == null &&
        preVerificationGas == null
}