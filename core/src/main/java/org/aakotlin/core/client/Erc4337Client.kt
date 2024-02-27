/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.core.client

import org.aakotlin.core.Chain
import org.aakotlin.core.UserOperationRequest
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.Request

interface Erc4337Client : Web3j {
    /**
     * calls eth_estimateUserOperationGas and  returns the result
     *
     * @param request - the {@link UserOperationRequest} to estimate gas for
     * @param entryPoint - the entrypoint address the op will be sent to
     * @returns the gas estimates for the given response (see: {@link UserOperationEstimateGasResponse})
     */
    fun estimateUserOperationGas(
        request: UserOperationRequest,
        entryPoint: String,
    ): Request<*, EthEstimateUserOperationGas>

    /**
     * calls eth_sendUserOperation and returns the hash of the sent UserOperation
     *
     * @param request - the {@link UserOperationRequest} to send
     * @param entryPoint - the entrypoint address the op will be sent to
     * @returns the hash of the sent UserOperation
     */
    fun sendUserOperation(
        request: UserOperationRequest,
        entryPoint: String,
    ): Request<*, EthSendUserOperation>

    /**
     * calls `eth_getUserOperationReceipt` and returns the {@link UserOperationReceipt}
     *
     * @param hash - the hash of the UserOperation to get the receipt for
     * @returns - {@link UserOperationResponse}
     */
    fun getUserOperationReceipt(hash: String): Request<*, EthGetUserOperationReceipt>

    /**
     * Returns an estimate for the fees per gas (in wei) for a
     * transaction to be likely included in the next block.
     * Defaults to [`chain.fees.estimateFeesPerGas`](/docs/clients/chains.html#fees-estimatefeespergas) if set.
     *
     * - Docs: https://viem.sh/docs/actions/public/estimateFeesPerGas.html
     *
     * @returns An estimate (in wei) for the fees per gas. {@link EstimateFeesPerGasReturnType}
     */
    suspend fun estimateFeesPerGas(chain: Chain): FeeValuesEIP1559
}
