package org.aakotlin.core.middleware.defaults

import org.aakotlin.core.UserOperationOverrides
import org.aakotlin.core.UserOperationStruct
import org.aakotlin.core.accounts.ISmartContractAccount
import org.aakotlin.core.auth.Eip7702Auth
import org.aakotlin.core.client.BundlerClient
import org.aakotlin.core.provider.ClientMiddlewareFn

suspend fun default7702GasEstimator(
    client: BundlerClient,
    account: ISmartContractAccount,
    struct: UserOperationStruct,
    overrides: UserOperationOverrides,
    continued: ClientMiddlewareFn
): UserOperationStruct {
    val entryPoint = account.getEntryPoint()

    if (entryPoint.version != "0.7.0") {
        return continued(client, account, struct, overrides)
    }

    val implementationAddress = account.getImplementationAddress()

    // Note: does not omit the delegation from estimation if the account is already 7702 delegated.
    struct.initCode = null
    struct.eip7702Auth = Eip7702Auth(
        chainId = "0x0",
        nonce = "0x0",
        address = implementationAddress,
        r = "0x0000000000000000000000000000000000000000000000000000000000000000",
        s = "0x0000000000000000000000000000000000000000000000000000000000000000",
        yParity = "0x0"
    )

    return continued(client, account, struct, overrides)
}
