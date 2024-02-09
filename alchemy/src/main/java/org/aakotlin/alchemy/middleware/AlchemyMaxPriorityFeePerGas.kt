/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.alchemy.middleware

import org.web3j.protocol.core.Response
import org.web3j.utils.Numeric
import java.math.BigInteger

class AlchemyMaxPriorityFeePerGas: Response<String>() {
    val maxPriorityFeePerGas: BigInteger
        get() = Numeric.decodeQuantity(result)
}
