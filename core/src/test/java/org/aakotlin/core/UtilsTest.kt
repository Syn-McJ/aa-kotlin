/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.core

import org.aakotlin.core.util.getUserOperationHash
import org.junit.Assert.assertEquals
import org.junit.Test
import org.web3j.utils.Numeric

class UtilsTest {
    @Test
    fun `getUserOperationHash should correctly hash a request`() {
        val entrypointAddress = "0x5FF137D4b0FDCD49DcA30c7CF57E578a026d2789"
        val hash = getUserOperationHash(
            UserOperationStruct(
                callData = "0xb61d27f6000000000000000000000000b856dbd4fa1a79a46d426f537455e7d3e79ab7c4000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000600000000000000000000000000000000000000000000000000000000000000000",
                callGasLimit = Numeric.decodeQuantity("0x2f6c"),
                initCode = "0x",
                maxFeePerGas = Numeric.decodeQuantity("0x59682f1e"),
                maxPriorityFeePerGas = Numeric.decodeQuantity("0x59682f00"),
                nonce = Numeric.decodeQuantity("0x1f"),
                paymasterAndData = "0x",
                preVerificationGas = Numeric.decodeQuantity("0xa890"),
                sender = "0xb856DBD4fA1A79a46D426f537455e7d3E79ab7c4",
                signature = Numeric.hexStringToByteArray("0xd16f93b584fbfdc03a5ee85914a1f29aa35c44fea5144c387ee1040a3c1678252bf323b7e9c3e9b4dfd91cca841fc522f4d3160a1e803f2bf14eb5fa037aae4a1b"),
                verificationGasLimit = Numeric.decodeQuantity("0x0114c2"),
            ),
            Address(entrypointAddress),
            80001L
        )

        assertEquals("0xa70d0af2ebb03a44dcd0714a8724f622e3ab876d0aa312f0ee04823285d6fb1b", Numeric.toHexString(hash))
    }
}