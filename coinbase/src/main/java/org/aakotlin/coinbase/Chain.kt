/*
 * Copyright (c) 2025 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.coinbase

import org.aakotlin.core.Chain

val SupportedChains = mapOf(
    Chain.Base.id to Chain.Base,
    Chain.BaseSepolia.id to Chain.BaseSepolia,
)

val Chain.coinbasePaymasterAndBundlerUrl: String?
    get() = when (this) {
        Chain.Base -> "https://api.developer.coinbase.com/rpc/v1/base"
        Chain.BaseSepolia -> "https://api.developer.coinbase.com/rpc/v1/base-sepolia"
        else -> null
    }
