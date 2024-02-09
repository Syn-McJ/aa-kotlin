/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.core.client

import java.math.BigInteger

data class FeeValuesEIP1559(
    /** Base fee per gas. */
    val gasPrice: BigInteger,
    /** Total fee per gas in wei (gasPrice/baseFeePerGas + maxPriorityFeePerGas). */
    val maxFeePerGas: BigInteger,
    /** Max priority fee per gas (in wei). */
    val maxPriorityFeePerGas: BigInteger
)
