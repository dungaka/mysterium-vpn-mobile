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

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.rygstudio.logging.BugReporter
import com.rygstudio.service.core.NodeRepository

enum class IdentityRegistrationStatus(val status: String) {
    UNKNOWN("Unknown"),
    REGISTERED_CONSUMER("RegisteredConsumer"),
    UNREGISTERED("Unregistered"),
    IN_PROGRESS("InProgress"),
    PROMOTING("Promoting"),
    REGISTRATION_ERROR("RegistrationError");

    companion object {
        fun parse(status: String): IdentityRegistrationStatus {
            return values().find { it.status == status } ?: UNKNOWN
        }
    }
}

class IdentityModel(
        val address: String,
        val channelAddress: String,
        var status: IdentityRegistrationStatus
) {
    val registered: Boolean
        get() {
            return status == IdentityRegistrationStatus.REGISTERED_CONSUMER
        }

    val registrationFailed: Boolean
        get() {
            return status == IdentityRegistrationStatus.REGISTRATION_ERROR
        }
}

class BalanceModel(val balance: TokenModel)

class TokenModel(token: Long = 0) {
    var displayValue = ""
    var value = 0.00

    init {
        value = token / 100_000_000.00
        displayValue = "%.3f MYSTT".format(value)
    }
}

class AccountViewModel(private val nodeRepository: NodeRepository, private val bugReporter: BugReporter) : ViewModel() {
    val balance = MutableLiveData<BalanceModel>()
    val identity = MutableLiveData<IdentityModel>()

    suspend fun load() {
        initListeners()
        loadIdentity {
            CoroutineScope(Dispatchers.Main).launch { loadBalance() }
        }
    }

    suspend fun topUp() {
        try {
            val currentIdentity = identity.value ?: return
            nodeRepository.topUpBalance(currentIdentity.address)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to top-up balance", e)
        }
    }

    fun needToTopUp(): Boolean {
        if (balance.value == null) {
            return false
        }
        return balance.value!!.balance.value < 0.01
    }

    fun isIdentityRegistered(): Boolean {
        val currentIdentity = identity.value ?: return false
        return currentIdentity.registered
    }

    suspend fun loadIdentity(done: () -> Unit) {
        try {
            // Load node identity and it's registration status.
            val nodeIdentity = nodeRepository.getIdentity()
            val identityResult = IdentityModel(
                    address = nodeIdentity.address,
                    channelAddress = nodeIdentity.channelAddress,
                    status = IdentityRegistrationStatus.parse(nodeIdentity.registrationStatus)
            )
            identity.value = identityResult
            bugReporter.setUserIdentifier(nodeIdentity.address)
            Log.i(TAG, "Loaded identity ${nodeIdentity.address}, channel addr: ${nodeIdentity.channelAddress}")

            // Register identity if not registered or failed.
            if (identityResult.status == IdentityRegistrationStatus.UNREGISTERED || identityResult.status == IdentityRegistrationStatus.REGISTRATION_ERROR) {
                val registrationFees = nodeRepository.identityRegistrationFees()
                if (identity.value != null) {
                    nodeRepository.registerIdentity(identity.value!!.address, registrationFees.fee)
                }
            }
        } catch (e: Exception) {
            identity.value = IdentityModel(address = "", channelAddress = "", status = IdentityRegistrationStatus.REGISTRATION_ERROR)
            Log.e(TAG, "Failed to load account identity", e)
        } finally {
            done()
        }
    }

    private suspend fun loadBalance() {
        if (identity.value == null) {
            return
        }
        val balance = nodeRepository.balance(identity.value!!.address)
        handleBalanceChange(balance)
    }

    private suspend fun initListeners() {
        nodeRepository.registerBalanceChangeCallback {
            handleBalanceChange(it)
        }

        nodeRepository.registerIdentityRegistrationChangeCallback {
            handleIdentityRegistrationChange(it)
        }
    }

    private fun handleIdentityRegistrationChange(status: String) {
        val currentIdentity = identity.value ?: return
        viewModelScope.launch {
            currentIdentity.status = IdentityRegistrationStatus.parse(status)
            identity.value = currentIdentity
        }
    }

    private fun handleBalanceChange(changedBalance: Long) {
        viewModelScope.launch {
            balance.value = BalanceModel(TokenModel(changedBalance))
        }
    }

    companion object {
        const val TAG = "AccountViewModel"
    }
}
