/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.core

import org.aakotlin.core.accounts.SimpleSmartContractAccount
import org.aakotlin.core.client.BundlerClient
import org.aakotlin.core.signer.SmartAccountSigner
import org.junit.Test
import org.mockito.kotlin.mock
import kotlinx.coroutines.test.runTest
import org.mockito.kotlin.doReturn
import org.testng.Assert.assertEquals
import java.math.BigInteger

@OptIn(ExperimentalStdlibApi::class)
class SimpleSmartContractAccountTest {
    private val rpcClient = mock<BundlerClient>()
    private val signer = mock<SmartAccountSigner> {
        onBlocking { getAddress() } doReturn "0x29DF43F75149D0552475A6f9B2aC96E28796ed0b"
    }
    private val scAccount = SimpleSmartContractAccount(
        rpcClient = rpcClient,
        factoryAddress = "0x5FF137D4b0FDCD49DcA30c7CF57E578a026d2789",
        signer = signer,
        chain = Chain.Polygon,
        accountAddress = null,
        index = null
    )

    @Test
    fun `getAccountInitCode should return correct hex`() = runTest {
        val initCode = scAccount.getAccountInitCode(signer.getAddress())
        assertEquals(initCode, "0x5FF137D4b0FDCD49DcA30c7CF57E578a026d27895fbfb9cf00000000000000000000000029df43f75149d0552475a6f9b2ac96e28796ed0b0000000000000000000000000000000000000000000000000000000000000000")
    }

    @Test
    fun `encodeExecute should return correct hex`() = runTest {
        val initCode = scAccount.encodeExecute(Address("0x8C8D7C46219D9205f056f28fee5950aD564d7465"), BigInteger.valueOf(0), "68656C6C6F20776F726C64".hexToByteArray())
        assertEquals(initCode, "0xb61d27f60000000000000000000000008c8d7c46219d9205f056f28fee5950ad564d746500000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000060000000000000000000000000000000000000000000000000000000000000000b68656c6c6f20776f726c64000000000000000000000000000000000000000000")
    }
}