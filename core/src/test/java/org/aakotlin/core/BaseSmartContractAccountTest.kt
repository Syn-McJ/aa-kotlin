/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.core

import kotlinx.coroutines.test.runTest
import org.aakotlin.core.accounts.SimpleSmartContractAccount
import org.aakotlin.core.client.BundlerClient
import org.aakotlin.core.signer.SmartAccountSigner
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.testng.Assert

class BaseSmartContractAccountTest {
    private val rpcClient = mock<BundlerClient>()
    private val signer = mock<SmartAccountSigner> {
        onBlocking { getAddress() } doReturn "0x29DF43F75149D0552475A6f9B2aC96E28796ed0b"
    }

    @Test
    fun `encodeGetSenderAddress should return correct hex`() = runTest {
        val scAccount = SimpleSmartContractAccount(
            rpcClient = rpcClient,
            factoryAddress = "0x000000893A26168158fbeaDD9335Be5bC96592E2",
            signer = signer,
            chain = Chain.BaseSepolia,
            accountAddress = null
        )

        val initCode = scAccount.getAccountInitCode(signer.getAddress())
        val encoded = scAccount.encodeGetSenderAddress(initCode)
        Assert.assertEquals(
            encoded,
            "0x9b249f6900000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000000058000000893a26168158fbeadd9335be5bc96592e25fbfb9cf00000000000000000000000029df43f75149d0552475a6f9b2ac96e28796ed0b00000000000000000000000000000000000000000000000000000000000000000000000000000000"
        )
    }
}