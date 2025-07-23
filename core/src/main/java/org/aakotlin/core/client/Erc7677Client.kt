package org.aakotlin.core.client

import org.aakotlin.core.PaymasterDataParams
import org.web3j.protocol.core.Request

interface Erc7677Client {
    fun getPaymasterStubData(
        params: PaymasterDataParams
    ): Request<*, PaymasterData>
}