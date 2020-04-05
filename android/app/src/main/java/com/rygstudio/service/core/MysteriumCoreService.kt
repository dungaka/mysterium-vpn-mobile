/*
 * Copyright (C) 2019 The "mysteriumnetwork/mysterium-vpn-mobile" Authors.
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

package com.rygstudio.service.core

import android.content.Context
import android.os.IBinder
import androidx.lifecycle.MutableLiveData
import mysterium.MobileNode
import com.rygstudio.NotificationFactory
import com.rygstudio.ui.ProposalViewItem

interface MysteriumCoreService : IBinder {
    fun startNode(): MobileNode

    fun stopNode()

    fun startConnectivityChecker()

    fun networkConnState(): MutableLiveData<NetworkConnState>

    fun getActiveProposal(): ProposalViewItem?

    fun setActiveProposal(proposal: ProposalViewItem?)

    fun getContext(): Context

    fun startForegroundWithNotification(id: Int, notificationFactory: NotificationFactory)

    fun stopForeground()
}
