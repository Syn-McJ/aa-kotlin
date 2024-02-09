/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.core.provider

data class SmartAccountProviderOpts(
    /**
     * The maximum number of times to try fetching a transaction receipt before giving up (default: 5)
     */
    val txMaxRetries: Int? = null,
    /**
     * The interval in milliseconds to wait between retries while waiting for tx receipts (default: 2_000)
     */
    val txRetryIntervalMs: Long? = null,
    /**
     * The multiplier on interval length to wait between retries while waiting for tx receipts (default: 1.5)
     */
    val txRetryMultiplier: Double? = null,
    /**
     * used when computing the fees for a user operation (default: 100_000_000)
     */
    val minPriorityFeePerBid: Long? = null,
    /**
     * Percent value for maxPriorityFeePerGas estimate added buffer. maxPriorityFeePerGasBid is set to the max
     * between the buffer "added" priority fee estimate and the minPriorityFeePerBid (default: 33)
     */
    val maxPriorityFeePerGasEstimateBuffer: Long? = null,
)
