/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.alchemy.middleware

import org.aakotlin.core.UserOperationRequest

data class PaymasterAndDataParams(
    val policyId: String,
    val entryPoint: String,
    val userOperation: UserOperationRequest,
    val dummySignature: String? = null,
    val feeOverride: FeeOverride? = null
)