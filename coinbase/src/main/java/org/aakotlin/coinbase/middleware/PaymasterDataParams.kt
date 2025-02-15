/*
 * Copyright (c) 2025 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.coinbase.middleware

import org.aakotlin.core.UserOperationRequest

data class Policy(
    val policyId: String
)

data class PaymasterDataParams(
    val userOperation: UserOperationRequest,
    val entryPoint: String,
    val chainId: String,
    val policy: Policy
)