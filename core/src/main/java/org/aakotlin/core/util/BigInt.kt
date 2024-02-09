/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.core.util

import java.math.BigInteger

fun bigIntPercent(base: BigInteger, percent: BigInteger): BigInteger {
    return base * percent / BigInteger.valueOf(100)
}