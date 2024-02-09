/*
 * Copyright (c) 2024 aa-kotlin
 *
 * This file is part of the aa-kotlin project: https://github.com/syn-mcj/aa-kotlin,
 * and is released under the MIT License: https://opensource.org/licenses/MIT
 */
package org.aakotlin.example.ui.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import org.aakotlin.example.R


class MainFragment : Fragment() {
    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            view.findViewById<ProgressBar>(R.id.progress_bar).isVisible = state.step == Step.KEY || state.step == Step.ADDRESS
            view.findViewById<Button>(R.id.login_btn).isVisible = state.step == Step.NOT_STARTED
            view.findViewById<Button>(R.id.logout_btn).isVisible = state.step >= Step.READY
            view.findViewById<Button>(R.id.mint_btn).isVisible = state.step == Step.READY

            val stepText = view.findViewById<TextView>(R.id.step_txt)
            stepText.isVisible = state.step != Step.NOT_STARTED

            if (state.step == Step.ERROR) {
                stepText.text = state.error
                stepText.setTextColor(Color.RED)
            } else {
                stepText.text = when (state.step) {
                    Step.KEY -> "Fetching your key..."
                    Step.ADDRESS -> "Fetching your smart contract account address..."
                    Step.READY -> "Your account is ready: ${state.address} (Sepolia network)"
                    Step.MINTING -> "Minting Alchemy tokens..."
                    Step.CONFIRMING -> "Confirming transaction..."
                    Step.DONE -> "Done! Alchemy Token balance: ${state.balance}"
                    else -> ""
                }
                stepText.setTextColor(Color.BLACK)
            }

            stepText.setOnClickListener {
                val clipboardManager = view.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboardManager.setPrimaryClip(
                    ClipData.newPlainText(
                        "address",
                        state.address ?: ""
                    )
                )
                Toast.makeText(requireContext(), "Address copied", Toast.LENGTH_SHORT).show()
            }

            val linkText = view.findViewById<TextView>(R.id.explorer_link)
            linkText.isVisible = state.step == Step.DONE

            if (!state.explorerLink.isNullOrEmpty()) {
                val spannableStringBuilder = SpannableStringBuilder(state.explorerLink)
                val clickOnPrivacy = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setData(Uri.parse(state.explorerLink))
                        startActivity(intent)
                    }
                }

                val endIndex = state.explorerLink.length
                spannableStringBuilder.setSpan(clickOnPrivacy, 0, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                linkText.text = spannableStringBuilder
                linkText.movementMethod = LinkMovementMethod.getInstance()
            }
        }

        view.findViewById<Button>(R.id.login_btn).setOnClickListener {
            viewModel.login()
        }
        view.findViewById<Button>(R.id.logout_btn).setOnClickListener {
            viewModel.logout()
        }
        view.findViewById<Button>(R.id.mint_btn).setOnClickListener {
            viewModel.mint()
        }
    }
}