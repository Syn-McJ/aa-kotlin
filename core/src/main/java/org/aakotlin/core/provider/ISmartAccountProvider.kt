/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.core.provider

import org.aakotlin.core.Address
import org.aakotlin.core.UserOperationCallData
import org.aakotlin.core.UserOperationReceipt
import org.aakotlin.core.UserOperationStruct

typealias AccountMiddlewareFn = suspend (UserOperationStruct) -> UserOperationStruct

// Based on https://github.com/alchemyplatform/aa-sdk/blob/main/packages/core/src/provider/types.ts#L95
interface ISmartAccountProvider {
    /**
     * @returns boolean flag indicating if the account is connected
     */
    val isConnected: Boolean

    /**
     * @returns the address of the connected account
     */
    suspend fun getAddress(): Address

    /**
     * Sends a user operation using the connected account.
     * Before executing, sendUserOperation will run the user operation through the middleware pipeline.
     * The order of the middlewares is:
     * 1. dummyPaymasterDataMiddleware -- populates a dummy paymaster data to use in estimation (default: "0x")
     * 2. feeDataGetter -- sets maxfeePerGas and maxPriorityFeePerGas
     * 3. gasEstimator -- calls eth_estimateUserOperationGas
     * 4. paymasterMiddleware -- used to set paymasterAndData. (default: "0x")
     * 5. customMiddleware -- allows you to override any of the results returned by previous middlewares
     *
     * @param data - either {@link UserOperationCallData} or {@link BatchUserOperationCallData}
     * @returns - {@link SendUserOperationResult} containing the hash and request
     */
    suspend fun sendUserOperation(data: UserOperationCallData): String

    /**
     * Allows you to get the unsigned UserOperation struct with all of the middleware run on it
     *
     * @param data - either {@link UserOperationCallData} or {@link BatchUserOperationCallData}
     * @returns - {@link UserOperationStruct} resulting from the middleware pipeline
     */
    suspend fun buildUserOperation(data: UserOperationCallData): UserOperationStruct

    /**
     * This will wait for the user operation to be included in a transaction that's been mined.
     * The default retry and wait logic is configured on the Provider itself
     *
     * @param hash the user operation hash you want to wait for
     * @returns the receipt of the user operation
     */
    suspend fun waitForUserOperationTransaction(hash: String): UserOperationReceipt

    // Middleware Overriders

    /**
     * Overrides the feeDataGetter middleware which is used for setting the fee fields on the UserOperation
     * prior to execution.
     *
     * @param override - a function for overriding the default feeDataGetter middleware
     * @returns
     */
    fun withFeeDataGetter(feeDataGetter: AccountMiddlewareFn): ISmartAccountProvider

    /**
     * Overrides the gasEstimator middleware which is used for setting the gasLimit fields on the UserOperation
     * prior to execution.
     *
     * @param override - a function for overriding the default gas estimator middleware
     * @returns
     */
    fun withGasEstimator(gasEstimator: AccountMiddlewareFn): ISmartAccountProvider

    /**
     * This method allows you to override the default dummy paymaster data middleware and get paymaster
     * and data middleware. These middleware are often used together. The dummy paymaster data is used in
     * gas estimation before we actually have paymaster data. Because the paymaster data has an impact on
     * the gas estimation, it's good to supply dummy paymaster data that is valid for your paymaster contract.
     * The getPaymasterAndDataMiddleware is used to make an RPC call to the paymaster contract to get the value
     * for paymasterAndData.
     *
     * @param paymasterDataMiddleware - optional function for overriding the default paymaster middleware
     * @param dummyPaymasterDataMiddleware - optional function for overriding the dummy paymaster middleware
     * @returns an update instance of this, which now uses the new middleware
     */
    fun withPaymasterMiddleware(
        dummyPaymasterDataMiddleware: AccountMiddlewareFn?,
        paymasterDataMiddleware: AccountMiddlewareFn?,
    ): ISmartAccountProvider
}
