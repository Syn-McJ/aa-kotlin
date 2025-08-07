package org.aakotlin.core.middleware.defaults

import org.aakotlin.core.provider.ClientMiddlewareFn
import org.aakotlin.core.util.getUserOperationHash

val defaultUserOpSigner: ClientMiddlewareFn = { client, account, struct, overrides ->
    val uoHash = getUserOperationHash(
        struct,
        account.getEntryPoint(),
        account.getEntryPoint().chain.id
    )
    struct.signature = account.signMessage(uoHash)

    struct
}