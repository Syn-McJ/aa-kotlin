/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.core.util

import org.aakotlin.core.Chain
import org.aakotlin.core.EntryPoint

object Defaults {
    /**
     * Utility method returning the entry point contract address given a {@link Chain} object
     *
     * @param chain - a {@link Chain} object
     * @returns a {@link abi.Address} for the given chain
     * @throws if the chain doesn't have an address currently deployed
     */
    fun getDefaultEntryPoint(chain: Chain): EntryPoint = getV6EntryPoint(chain)

    fun getV6EntryPoint(chain: Chain): EntryPoint {
        return when (chain.id) {
            Chain.MainNet.id,
            Chain.Sepolia.id,
            Chain.Polygon.id,
            Chain.Optimism.id,
            Chain.OptimismGoerli.id,
            Chain.Arbitrum.id,
            Chain.ArbitrumGoerli.id,
            Chain.Base.id,
            Chain.BaseGoerli.id,
            Chain.BaseSepolia.id -> EntryPoint(
                "0x5FF137D4b0FDCD49DcA30c7CF57E578a026d2789",
                "0.6.0",
                chain
            )

            else -> throw Error("no default entry point contract exists for ${chain.name}")
        }
    }

    fun getV7EntryPoint(chain: Chain): EntryPoint {
        return when (chain.id) {
            Chain.MainNet.id,
            Chain.Sepolia.id,
            Chain.Polygon.id,
            Chain.Optimism.id,
            Chain.OptimismGoerli.id,
            Chain.Arbitrum.id,
            Chain.ArbitrumGoerli.id,
            Chain.Base.id,
            Chain.BaseGoerli.id,
            Chain.BaseSepolia.id ->
                EntryPoint(
                    "0x0000000071727De22E5E9d8BAf0edAc6f37da032",
                    "0.7.0",
                    chain
                )
            else -> throw Error("no default entry point contract exists for ${chain.name}")
        }
    }

    /**
     * Utility method returning the default simple account factory address given a {@link Chain} object
     *
     * @param chain - a {@link Chain} object
     * @returns a {@link abi.Address} for the given chain
     * @throws if the chain doesn't have an address currently deployed
     */
    fun getDefaultSimpleAccountFactoryAddress(chain: Chain): String {
        return when (chain.id) {
            Chain.MainNet.id,
            Chain.Polygon.id,
            Chain.Optimism.id,
            Chain.Arbitrum.id,
            Chain.Base.id,
            Chain.BaseGoerli.id,
            Chain.BaseSepolia.id -> "0x15Ba39375ee2Ab563E8873C8390be6f2E2F50232"

            Chain.Sepolia.id,
            Chain.OptimismGoerli.id,
            Chain.ArbitrumGoerli.id -> "0x9406Cc6185a346906296840746125a0E44976454"

            else -> throw Error("no default simple account factory contract exists for ${chain.name}")
        }
    }
}
