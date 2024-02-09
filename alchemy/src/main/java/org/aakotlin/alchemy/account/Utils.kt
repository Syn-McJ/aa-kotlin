/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.alchemy.account

import org.aakotlin.core.Address
import org.aakotlin.core.Chain

fun Chain.defaultLightAccountFactoryAddress(): Address {
    return when (id) {
        Chain.MainNet.id,
        Chain.Sepolia.id,
        Chain.Goerli.id,
        Chain.Polygon.id,
        Chain.PolygonMumbai.id,
        Chain.Optimism.id,
        Chain.OptimismGoerli.id,
        Chain.Arbitrum.id,
        Chain.ArbitrumGoerli.id,
        Chain.Base.id,
        Chain.BaseGoerli.id -> Address("0x000000893A26168158fbeaDD9335Be5bC96592E2")

        else -> throw Error("no default light account factory contract exists for $name")
    }
}