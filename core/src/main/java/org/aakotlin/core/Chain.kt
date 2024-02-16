/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-swift,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.core

import java.math.BigInteger

data class Currency(
    val name: String,
    val symbol: String,
    val decimals: Int
)

sealed class Chain(
    val id: Long,
    val network: String,
    val name: String,
    val currency: Currency,
    val baseFeeMultiplier: Double? = null,
    val defaultPriorityFee: BigInteger? = null
) {
    data object MainNet: Chain(
        1,
        "homestead",
        "Ethereum",
        Currency("Ether", "ETH", 18)
    )

    data object Sepolia: Chain(
        11_155_111,
        "sepolia",
        "Sepolia",
        Currency("Sepolia Ether", "SEP", 18)
    )

    data object Goerli: Chain(
        5,
        "goerli",
        "Goerli",
        Currency("Goerli Ether", "ETH", 18)
    )

    data object Polygon: Chain(
        137,
        "matic",
        "Polygon",
        Currency("MATIC", "MATIC", 18)
    )

    data object PolygonMumbai: Chain(
        80_001,
        "maticmum",
        "Polygon Mumbai",
        Currency("MATIC", "MATIC", 18)
    )

    data object Optimism: Chain(
        10,
        "optimism",
        "OP Mainnet",
        Currency("Ether", "ETH", 18)
    )

    data object OptimismGoerli: Chain(
        420,
        "optimism-goerli",
        "Optimism Goerli",
        Currency("Goerli Ether", "ETH", 18)
    )

    data object OptimismSepolia: Chain(
        420_69,
        "optimism-sepolia",
        "Optimism Sepolia",
        Currency("Sepolia Ether", "ETH", 18)
    )

    data object Arbitrum: Chain(
        42_161,
        "arbitrum",
        "Arbitrum One",
        Currency("Ether", "ETH", 18)
    )

    data object ArbitrumGoerli: Chain(
        421_613,
        "arbitrum-goerli",
        "Arbitrum Goerli",
        Currency("Goerli Ether", "ETH", 18)
    )

    data object ArbitrumSepolia: Chain(
        421_614,
        "arbitrum-sepolia",
        "Arbitrum Sepolia",
        Currency("Arbitrum Sepolia Ether", "ETH", 18)
    )

    data object Base: Chain(
        8453,
        "base",
        "Base",
        Currency("Ether", "ETH", 18)
    )

    data object BaseGoerli: Chain(
        84531,
        "base-goerli",
        "Base Goerli",
        Currency("Goerli Ether", "ETH", 18)
    )

    data object BaseSepolia: Chain(
        84532,
        "base-sepolia",
        "Base Sepolia",
        Currency("Sepolia Ether", "ETH", 18)
    )
}
