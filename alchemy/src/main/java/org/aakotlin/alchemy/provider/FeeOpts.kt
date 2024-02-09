/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.alchemy.provider

data class FeeOpts(
    /** this adds a percent buffer on top of the base fee estimated (default 50%)
     * NOTE: this is only applied if the default fee estimator is used.
     */
    val baseFeeBufferPercent: Int? = null,

    /** this adds a percent buffer on top of the priority fee estimated (default 5%)'
     * * NOTE: this is only applied if the default fee estimator is used.
     */
    val maxPriorityFeeBufferPercent: Int? = null,

    /** this adds a percent buffer on top of the preVerificationGas estimated
     *
     * Defaults 5% on Arbitrum and Optimism, 0% elsewhere
     *
     * This is only useful on Arbitrum and Optimism, where the preVerificationGas is
     * dependent on the gas fee during the time of estimation. To improve chances of
     * the UserOperation being mined, users can increase the preVerificationGas by
     * a buffer. This buffer will always be charged, regardless of price at time of mine.
     *
     * NOTE: this is only applied if the defualt gas estimator is used.
     */
    val preVerificationGasBufferPercent: Int? = null,
)