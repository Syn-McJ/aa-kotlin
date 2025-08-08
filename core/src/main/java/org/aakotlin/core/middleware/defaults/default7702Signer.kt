package org.aakotlin.core.middleware.defaults

import org.aakotlin.core.auth.Authorization
import org.aakotlin.core.auth.Eip7702Auth
import org.aakotlin.core.provider.ClientMiddlewareFn
import org.aakotlin.core.util.await
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.utils.Numeric
import java.math.BigInteger

/**
 * Provides a default middleware function for signing user operations with a client account when using EIP-7702 delegated accounts.
 * If the signer doesn't support `signAuthorization`, then this just runs the provided `signUserOperation` middleware.
 * This function is only compatible with accounts using EntryPoint v0.7.0, and the account must have an implementation address defined in `getImplementationAddress()`.
 */
val default7702UserOpSigner: ClientMiddlewareFn = { client, account, struct, overrides ->
    val uo = defaultUserOpSigner(client, account, struct, overrides)
    val code = client.ethGetCode(account.getAddress(), DefaultBlockParameterName.LATEST).await().code ?: "0x"
    val implAddress = account.getImplementationAddress()
    val expectedCode = buildExpectedDelegationCode(implAddress)

    if (code.lowercase() == expectedCode.lowercase()) {
        // Already delegated, no authorization needed
        uo.eip7702Auth = null
        uo
    } else {
        val accountNonce = client.ethGetTransactionCount(
            account.getAddress(),
            DefaultBlockParameterName.LATEST
        ).await().transactionCount

        val signer = account.getSigner()
        val chainId = account.getEntryPoint().chain.id
        val authSignature = signer.signAuthorization(
            Authorization(
                chainId = chainId,
                contractAddress = implAddress,
                nonce = accountNonce
            )
        )

        uo.eip7702Auth = Eip7702Auth(
            chainId = Numeric.encodeQuantity(BigInteger.valueOf(chainId)),
            nonce = Numeric.encodeQuantity(accountNonce),
            address = implAddress,
            r = authSignature.r,
            s = authSignature.s,
            yParity = authSignature.yParity
        )

        uo
    }
}

private fun buildExpectedDelegationCode(implementationAddress: String): String {
    val cleanAddress = implementationAddress.removePrefix("0x").lowercase()
    return "0xef0100$cleanAddress"
}
