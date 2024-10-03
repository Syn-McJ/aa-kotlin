/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.alchemy.account

import org.aakotlin.core.Address
import org.aakotlin.core.Chain

sealed class LightAccountVersion(val version: String, val factoryAddress: Address, val implAddress: Address) {
    @Deprecated("This version does not support 1271 signature validation", ReplaceWith("LightAccountVersion.V1_1_0"))
    data object V1_0_1 : LightAccountVersion(
        version = "v1.0.1",
        factoryAddress = Address("0x000000893A26168158fbeaDD9335Be5bC96592E2"),
        implAddress = Address("0xc1b2fc4197c9187853243e6e4eb5a4af8879a1c0")
    )

    @Deprecated("This version has a known issue with 1271 validation", ReplaceWith("LightAccountVersion.V1_1_0"))
    data object V1_0_2 : LightAccountVersion(
        version = "v1.0.2",
        factoryAddress = Address("0x00000055C0b4fA41dde26A74435ff03692292FBD"),
        implAddress = Address("0x5467b1947F47d0646704EB801E075e72aeAe8113")
    )

    data object V1_1_0 : LightAccountVersion(
        version = "v1.1.0",
        factoryAddress = Address("0x00004EC70002a32400f8ae005A26081065620D20"),
        implAddress = Address("0xae8c656ad28F2B59a196AB61815C16A0AE1c3cba")
    )
}

fun Chain.defaultLightAccountFactoryAddress(version: LightAccountVersion = LightAccountVersion.V1_1_0): Address {
    return when (id) {
        Chain.MainNet.id,
        Chain.Sepolia.id,
        Chain.Polygon.id,
        Chain.Optimism.id,
        Chain.OptimismGoerli.id,
        Chain.Arbitrum.id,
        Chain.ArbitrumGoerli.id,
        Chain.Base.id,
        Chain.BaseGoerli.id,
        Chain.BaseSepolia.id -> version.factoryAddress

        else -> throw Error("no default light account factory contract exists for $name")
    }
}