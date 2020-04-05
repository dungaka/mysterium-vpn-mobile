/*
 * Copyright (C) 2020 The "mysteriumnetwork/mysterium-vpn-mobile" Authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.rygstudio.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.rygstudio.AppContainer
import com.rygstudio.smartvpn.R
import android.content.Intent
import android.net.Uri

class AccountFragment : Fragment() {
    private lateinit var accountViewModel: AccountViewModel
    private lateinit var toolbar: Toolbar
    private lateinit var accountBalanceCard: MaterialCardView
    private lateinit var accountBalanceText: TextView
    private lateinit var accountIdentityText: TextView
    private lateinit var accountIdentityRegistrationLayout: ConstraintLayout
    private lateinit var accountIdentityRegistrationLayoutCard: MaterialCardView
    private lateinit var accountIdentityRegistrationLayoutRetryCard: MaterialCardView
    private lateinit var accountIdentityChannelAddressText: TextView
    private lateinit var accountTopUpButton: Button
    private lateinit var accountIdentityRegistrationRetryButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val root = inflater.inflate(R.layout.fragment_account, container, false)
        accountViewModel = AppContainer.from(activity).accountViewModel

        // Initialize UI elements.
        toolbar = root.findViewById(R.id.account_toolbar)
        accountBalanceCard = root.findViewById(R.id.account_balance_card)
        accountBalanceText = root.findViewById(R.id.account_balance_text)
        accountIdentityText = root.findViewById(R.id.account_identity_text)
        accountIdentityRegistrationLayout = root.findViewById(R.id.account_identity_registration_layout)
        accountIdentityRegistrationLayoutCard = root.findViewById(R.id.account_identity_registration_layout_card)
        accountIdentityRegistrationLayoutRetryCard = root.findViewById(R.id.account_identity_registration_layout_retry_card)
        accountIdentityChannelAddressText = root.findViewById(R.id.account_identity_channel_address_text)
        accountTopUpButton = root.findViewById(R.id.account_topup_button)
        accountIdentityRegistrationRetryButton = root.findViewById(R.id.account_identity_registration_retry_button)

        // Handle back press.
        toolbar.setNavigationOnClickListener {
            navigateTo(root, Screen.MAIN)
        }

        onBackPress {
            navigateTo(root, Screen.MAIN)
        }

        accountViewModel.identity.observe(this, Observer {
            handleIdentityChange(it)
        })

        accountViewModel.balance.observe(this, Observer {
            accountBalanceText.text = it.balance.displayValue
        })

        accountTopUpButton.setOnClickListener { handleTopUp(root) }

        accountIdentityChannelAddressText.setOnClickListener { openKovanChannelDetails() }
        accountIdentityText.setOnClickListener { openKovanIdentityDetails() }

        accountIdentityRegistrationRetryButton.setOnClickListener { handleRegistrationRetry() }

        return root
    }

    private fun handleRegistrationRetry() {
        accountIdentityRegistrationRetryButton.isEnabled = false
        CoroutineScope(Dispatchers.Main).launch {
            accountViewModel.loadIdentity {}
            accountIdentityRegistrationRetryButton.isEnabled = true
        }
    }

    private fun openKovanChannelDetails() {
        val channelAddress = accountViewModel.identity.value!!.channelAddress
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://goerli.etherscan.io/address/$channelAddress"))
        startActivity(browserIntent)
    }

    private fun openKovanIdentityDetails() {
        val identityAddress = accountViewModel.identity.value!!.address
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://goerli.etherscan.io/address/$identityAddress"))
        startActivity(browserIntent)
    }

    private fun handleIdentityChange(identity: IdentityModel) {
        accountIdentityText.text = identity.address
        accountIdentityChannelAddressText.text = identity.channelAddress

        if (identity.registered) {
            accountIdentityRegistrationLayout.visibility = View.GONE
            accountBalanceCard.visibility = View.VISIBLE
        } else {
            accountIdentityRegistrationLayout.visibility = View.VISIBLE
            accountIdentityRegistrationLayoutCard.visibility = View.VISIBLE
            accountIdentityRegistrationLayoutRetryCard.visibility = View.GONE
            accountBalanceCard.visibility = View.GONE

            // Show retry button.
            if (identity.registrationFailed) {
                accountIdentityRegistrationLayoutRetryCard.visibility = View.VISIBLE
                accountIdentityRegistrationLayoutCard.visibility = View.GONE
            }
        }
    }

    private fun handleTopUp(root: View) {
        accountTopUpButton.isEnabled = false
        CoroutineScope(Dispatchers.Main).launch {
            accountViewModel.topUp()
            showMessage(root.context, "Balance will be updated in a few moments.")
            accountTopUpButton.isEnabled = true
        }
    }
}
