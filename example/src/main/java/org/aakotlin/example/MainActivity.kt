/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.example

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.web3auth.core.Web3Auth
import com.web3auth.core.types.BuildEnv
import com.web3auth.core.types.LoginConfigItem
import com.web3auth.core.types.Network
import com.web3auth.core.types.TypeOfLogin
import com.web3auth.core.types.Web3AuthOptions
import org.aakotlin.example.ui.main.MainFragment
import org.aakotlin.example.ui.main.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        viewModel.init(Web3Auth(
            Web3AuthOptions(
                context = this,
                clientId = MainViewModel.WEB3_AUTH_CLIENT_ID,
                network = Network.SAPPHIRE_MAINNET,
                buildEnv = BuildEnv.PRODUCTION,
                redirectUrl = Uri.parse("com.sbz.web3authdemoapp://auth"), // Your app's scheme (see manifest)
                loginConfig = hashMapOf(
                    "jwt" to LoginConfigItem(
                        verifier = "w3a-auth0-demo",
                        typeOfLogin = TypeOfLogin.JWT,
                        name = "Auth0 Login",
                        clientId = MainViewModel.AUTH0_CLIENT_ID
                    )
                )
            )
        ))

        viewModel.setResultUrl(intent?.data)
        viewModel.checkSession()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        viewModel.setResultUrl(intent?.data)
    }
}