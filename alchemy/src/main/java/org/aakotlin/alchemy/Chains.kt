/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.alchemy

import org.aakotlin.core.Chain

val SupportedChains = mapOf(
    Chain.Polygon.id to Chain.Polygon,
    Chain.MainNet.id to Chain.MainNet,
    Chain.Sepolia.id to Chain.Sepolia,
    Chain.ArbitrumGoerli.id to Chain.ArbitrumGoerli,
    Chain.ArbitrumGoerli.id to Chain.ArbitrumSepolia,
    Chain.Arbitrum.id to Chain.Arbitrum,
    Chain.Optimism.id to Chain.Optimism,
    Chain.OptimismGoerli.id to Chain.OptimismGoerli,
    Chain.OptimismSepolia.id to Chain.OptimismSepolia,
    Chain.Base.id to Chain.Base,
    Chain.BaseGoerli.id to Chain.BaseGoerli,
    Chain.BaseSepolia.id to Chain.BaseSepolia,
)

val Chain.alchemyRpcHttpUrl: String?
    get() = when (this) {
        Chain.Polygon -> "https://polygon-mainnet.g.alchemy.com/v2"
        Chain.MainNet -> "https://eth-mainnet.g.alchemy.com/v2"
        Chain.Sepolia -> "https://eth-sepolia.g.alchemy.com/v2"
        Chain.ArbitrumGoerli -> "https://arb-goerli.g.alchemy.com/v2"
        Chain.Arbitrum -> "https://arb-mainnet.g.alchemy.com/v2"
        Chain.Optimism -> "https://opt-mainnet.g.alchemy.com/v2"
        Chain.OptimismGoerli -> "https://opt-goerli.g.alchemy.com/v2"
        Chain.Base -> "https://base-mainnet.g.alchemy.com/v2"
        Chain.BaseGoerli -> "https://base-goerli.g.alchemy.com/v2"
        Chain.BaseSepolia -> "https://base-sepolia.g.alchemy.com/v2"
        else -> null
    }

val Chain.alchemyRpcWebSocketUrl: String?
    get() = when(this) {
        Chain.Polygon -> "wss://polygon-mainnet.g.alchemy.com/v2"
        Chain.MainNet -> "wss://eth-mainnet.g.alchemy.com/v2"
        Chain.Sepolia -> "wss://eth-sepolia.g.alchemy.com/v2"
        Chain.ArbitrumGoerli -> "wss://arb-goerli.g.alchemy.com/v2"
        Chain.Arbitrum -> "wss://arb-mainnet.g.alchemy.com/v2"
        Chain.Optimism -> "wss://opt-mainnet.g.alchemy.com/v2"
        Chain.OptimismGoerli -> "wss://opt-goerli.g.alchemy.com/v2"
        Chain.Base -> "wss://base-mainnet.g.alchemy.com/v2"
        Chain.BaseGoerli -> "wss://base-goerli.g.alchemy.com/v2"
        Chain.BaseSepolia -> "wss://base-sepolia.g.alchemy.com/v2"
        else -> null
    }