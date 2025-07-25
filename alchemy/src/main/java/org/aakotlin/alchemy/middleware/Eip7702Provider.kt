/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.alchemy.middleware

import org.aakotlin.alchemy.account.ModularAccountV2
import org.aakotlin.core.PaymasterDataParams
import org.aakotlin.core.Policy
import org.aakotlin.core.auth.AccountMode
import org.aakotlin.core.client.Erc7677Client
import org.aakotlin.core.middleware.defaults.default7702UserOpSigner
import org.aakotlin.core.middleware.defaults.defaultUserOpSigner
import org.aakotlin.core.provider.SmartAccountProvider
import org.aakotlin.core.util.await
import org.aakotlin.core.util.toUserOperationRequest
import org.web3j.utils.Numeric
import java.math.BigInteger

fun SmartAccountProvider.erc7677Middleware(policyId: String): SmartAccountProvider = apply {
    withDummyPaymasterMiddleware { client, account, struct, _ ->
        // Those values will be set after fee estimation.
        struct.maxFeePerGas = BigInteger.ZERO
        struct.maxPriorityFeePerGas = BigInteger.ZERO
        struct.callGasLimit = BigInteger.ZERO
        struct.verificationGasLimit = BigInteger.ZERO
        struct.preVerificationGas = BigInteger.ZERO

        val entryPoint = account.getEntryPoint()

        if (entryPoint.version == "0.7.0") {
            struct.paymasterVerificationGasLimit = BigInteger.ZERO
            struct.paymasterPostOpGasLimit = BigInteger.ZERO
        }

        val result = (client as Erc7677Client).getPaymasterStubData(
            params = PaymasterDataParams(
                struct.toUserOperationRequest(),
                entryPoint.address,
                Numeric.encodeQuantity(chain.id.toBigInteger()),
                policy = Policy(
                    policyId = policyId
                )
            )
        ).await().result

        if (entryPoint.version == "0.6.0") {
            struct.paymasterAndData = result.paymasterAndData
            return@withDummyPaymasterMiddleware struct
        }

        struct.paymaster = result.paymaster
        struct.paymasterData = result.paymasterData
        struct.paymasterVerificationGasLimit = result.paymasterVerificationGasLimit
        struct.paymasterPostOpGasLimit = result.paymasterPostOpGasLimit

        struct
    }

    withUserOperationSigner { client, account, struct, overrides ->
        if (account is ModularAccountV2 && account.getMode() == AccountMode.EIP7702) {
            default7702UserOpSigner(client, account, struct, overrides)
        } else {
            defaultUserOpSigner(client, account, struct, overrides)
        }
    }
}
