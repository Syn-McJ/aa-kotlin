package org.aakotlin.core.middleware.defaults

import org.aakotlin.core.client.EthEstimateUserOperationGas
import org.aakotlin.core.provider.ClientMiddlewareFn
import org.aakotlin.core.util.await
import org.aakotlin.core.util.toUserOperationRequest
import java.math.BigInteger

val defaultGasEstimator: ClientMiddlewareFn = { client, account, struct, overrides ->
    var estimates: EthEstimateUserOperationGas.EstimateUserOperationGas? = null
    val is070 = account.getEntryPoint().version == "0.7.0"

    if (overrides.callGasLimit == null ||
        overrides.verificationGasLimit == null ||
        overrides.preVerificationGas == null ||
        (is070 && overrides.paymasterVerificationGasLimit == null)
    ) {
        val request = struct.toUserOperationRequest()
        estimates = client.estimateUserOperationGas(
            request,
            account.getEntryPoint().address
        ).await().result
    }

    struct.preVerificationGas = overrides.preVerificationGas ?: estimates!!.preVerificationGas
    struct.verificationGasLimit = overrides.verificationGasLimit ?: estimates!!.verificationGasLimit
    struct.callGasLimit = overrides.callGasLimit ?: estimates!!.callGasLimit

    if (is070) {
        struct.paymasterVerificationGasLimit = overrides.paymasterVerificationGasLimit ?: estimates!!.paymasterVerificationGasLimit
        struct.paymasterPostOpGasLimit = struct.paymasterPostOpGasLimit ?: if (struct.paymaster == null) null else BigInteger.ZERO
    }

    struct
}