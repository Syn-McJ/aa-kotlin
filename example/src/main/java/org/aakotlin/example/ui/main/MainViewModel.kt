/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.example.ui.main

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.web3auth.core.Web3Auth
import com.web3auth.core.types.ExtraLoginOptions
import com.web3auth.core.types.LoginParams
import com.web3auth.core.types.Provider
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import org.aakotlin.alchemy.account.ModularAccountV2
import org.aakotlin.alchemy.middleware.AlchemyGasManagerConfig
import org.aakotlin.alchemy.middleware.erc7677Middleware
import org.aakotlin.alchemy.middleware.withAlchemyGasManager
import org.aakotlin.alchemy.provider.AlchemyProvider
import org.aakotlin.core.Address
import org.aakotlin.core.Chain
import org.aakotlin.core.UserOperationCallData
import org.aakotlin.core.auth.AccountMode
import org.aakotlin.core.provider.ConnectionConfig
import org.aakotlin.core.provider.ISmartAccountProvider
import org.aakotlin.core.provider.ProviderConfig
import org.aakotlin.core.provider.SmartAccountProviderOpts
import org.aakotlin.core.signer.LocalAccountSigner
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.datatypes.Function
import org.web3j.contracts.eip20.generated.ERC20
import org.web3j.crypto.Credentials
import org.web3j.tx.gas.DefaultGasProvider
import org.web3j.utils.Numeric
import java.math.BigDecimal
import java.math.BigInteger

enum class Step {
    NOT_STARTED,
    KEY,
    ADDRESS,
    READY,
    MINTING,
    CONFIRMING,
    DONE,
    ERROR
}

data class UIState(
    val step: Step = Step.NOT_STARTED,
    val address: String? = null,
    val error: String? = null,
    val balance: String? = null,
    val explorerLink: String? = null
)

class MainViewModel : ViewModel() {
    companion object {
        private val chain = Chain.Sepolia // Alchemy token is deployed on Sepolia

        private const val JIFFYSCAN_BASE_URL = "https://jiffyscan.xyz/userOpHash/"
        private const val ALCHEMY_TOKEN_SEPOLIA_ADDRESS = "0xCFf7C6dA719408113DFcb5e36182c6d5aa491443"

        // replace with your Alchemy API key
        private const val ALCHEMY_API_KEY = "VL04Y5WbMvKHO05PIKtTsmifkEaz8UYU"
        // replace with your Alchemy gas policy ID
        private const val ALCHEMY_GAS_POLICY_ID = "fc1342bf-c475-43b8-af42-d7b3cd189e59"

        // replace with your Web3Auth Client ID
        // these IDs are from Web3Auth example
        const val WEB3_AUTH_CLIENT_ID = "BPi5PB_UiIZ-cPz1GtV5i1I2iOSOHuimiXBI0e-Oe_u6X3oVAbCiAZOTEBtTXw4tsluTITPqA8zMsfxIKMjiqNQ"
        const val AUTH0_CLIENT_ID = "hUVVf4SEsZT7syOiL0gLU9hFEtm2gQ6O"
    }

    private lateinit var web3Auth: Web3Auth
    private var alchemyToken: ERC20? = null
    private var scaProvider: ISmartAccountProvider? = null

    private val _uiState = MutableLiveData(UIState())
    val uiState: LiveData<UIState> = _uiState

    fun init(web3Auth: Web3Auth) {
        this.web3Auth = web3Auth
    }

    fun login() {
        web3Auth.login(
            LoginParams(
                Provider.JWT,
                extraLoginOptions = ExtraLoginOptions(
                    domain = "https://web3auth.au.auth0.com",
                    verifierIdField = "sub"
                )
            )
        ).whenComplete { _, error ->
            setKeyState(true, error)
        }
    }

    fun logout() {
        val logoutCompletableFuture = web3Auth.logout()
        logoutCompletableFuture.whenComplete { _, error ->
            setKeyState(false, error)
        }
    }

    fun setResultUrl(data: Uri?) {
        web3Auth.setResultUrl(data)
    }

    fun checkSession() {
        web3Auth.initialize().whenComplete { _, error ->
            setKeyState(true, error)
        }
    }

    fun mint() {
        scaProvider?.let { provider ->
            _uiState.value = _uiState.value?.copy(step = Step.MINTING)
            viewModelScope.launch {
                try {
                    val resultHash = sendMintUserOperation(provider)
                    _uiState.value = _uiState.value?.copy(step = Step.CONFIRMING)
                    provider.waitForUserOperationTransaction(resultHash)
                    _uiState.value = _uiState.value?.copy(step = Step.DONE, explorerLink = JIFFYSCAN_BASE_URL + resultHash)
                    refreshAlchemyTokenBalance()
                } catch (ex: Exception) {
                    _uiState.value = _uiState.value?.copy(step = Step.ERROR, error = ex.message ?: "Error while minting")
                }
            }
        }
    }

    private fun setKeyState(loggedIn: Boolean, error: Throwable?) {
        if (error == null) {
            if (loggedIn && web3Auth.getPrivkey().isNotEmpty()) {
                setupSmartContractAccount(Credentials.create(web3Auth.getPrivkey()))
                viewModelScope.launch {
                    _uiState.postValue(
                        _uiState.value?.copy(
                            step = Step.READY,
                            address = scaProvider?.getAddress()
                        )
                    )
                }
            } else {
                _uiState.postValue(_uiState.value?.copy(step = Step.NOT_STARTED))
            }
        } else {
            _uiState.postValue(_uiState.value?.copy(step = Step.ERROR, error = error.message ?: "Error while fetching key"))
        }
    }

    private fun setupSmartContractAccount(credentials: Credentials) {
        val connectionConfig = ConnectionConfig(ALCHEMY_API_KEY, null, null)
        val provider = AlchemyProvider(
            ProviderConfig(
                chain,
                connectionConfig,
                SmartAccountProviderOpts(50, 500)
            )
        ).withAlchemyGasManager(
            AlchemyGasManagerConfig(ALCHEMY_GAS_POLICY_ID, connectionConfig)
        )
        .erc7677Middleware(policyId = ALCHEMY_GAS_POLICY_ID)

        val signer = LocalAccountSigner()
        val account = ModularAccountV2(
            provider.rpcClient,
            null,
            signer,
            chain,
            mode = AccountMode.EIP7702
        )

        signer.setCredentials(credentials)
        scaProvider = provider.apply { connect(account) }
        alchemyToken = ERC20.load(
            ALCHEMY_TOKEN_SEPOLIA_ADDRESS,
            provider.rpcClient,
            credentials,
            DefaultGasProvider()
        )
    }

    private suspend fun sendMintUserOperation(provider: ISmartAccountProvider): String {
        val address = provider.getAddress()
        val function = Function(
            "mint",
            listOf(
                org.web3j.abi.datatypes.Address(address),
                org.web3j.abi.datatypes.generated.Uint256(BigInteger("11700000000000000000000"))
            ),
            listOf()
        )

        val encoded = FunctionEncoder.encode(function)
        return provider.sendUserOperation(
            UserOperationCallData(
                Address(ALCHEMY_TOKEN_SEPOLIA_ADDRESS),
                Numeric.hexStringToByteArray(encoded),
            )
        ).hash
    }

    private suspend fun refreshAlchemyTokenBalance() {
        val balance = alchemyToken?.balanceOf(
            scaProvider?.getAddress()
        )?.sendAsync()?.await()?.toBigDecimal()
        val balanceStr = balance?.divide(BigDecimal.TEN.pow(18))?.toPlainString()
        _uiState.postValue(_uiState.value?.copy(balance = balanceStr))
    }
}