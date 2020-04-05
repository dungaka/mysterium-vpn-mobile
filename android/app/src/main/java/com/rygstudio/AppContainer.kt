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

package com.rygstudio

import android.app.NotificationManager
import android.content.Context
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentActivity
import androidx.room.Room
import kotlinx.coroutines.CompletableDeferred
import com.rygstudio.db.AppDatabase
import com.rygstudio.logging.BugReporter
import com.rygstudio.service.core.DeferredNode
import com.rygstudio.service.core.MysteriumCoreService
import com.rygstudio.service.core.NodeRepository
import com.rygstudio.ui.*

class AppContainer {
    lateinit var appDatabase: AppDatabase
    lateinit var nodeRepository: NodeRepository
    lateinit var sharedViewModel: SharedViewModel
    lateinit var proposalsViewModel: ProposalsViewModel
    lateinit var termsViewModel: TermsViewModel
    lateinit var accountViewModel: AccountViewModel
    lateinit var bugReporter: BugReporter
    lateinit var deferredMysteriumCoreService: CompletableDeferred<MysteriumCoreService>
    lateinit var drawerLayout: DrawerLayout
    lateinit var appNotificationManager: AppNotificationManager

    fun init(
            ctx: Context,
            deferredNode: DeferredNode,
            mysteriumCoreService: CompletableDeferred<MysteriumCoreService>,
            appDrawerLayout: DrawerLayout,
            notificationManager: NotificationManager
    ) {
        appDatabase = Room.databaseBuilder(
                ctx,
                AppDatabase::class.java, "mysteriumvpn"
        ).build()

        drawerLayout = appDrawerLayout
        deferredMysteriumCoreService = mysteriumCoreService
        bugReporter = BugReporter()
        nodeRepository = NodeRepository(deferredNode)
        appNotificationManager = AppNotificationManager(notificationManager, deferredMysteriumCoreService)
        accountViewModel = AccountViewModel(nodeRepository, bugReporter)
        sharedViewModel = SharedViewModel(nodeRepository, deferredMysteriumCoreService, appNotificationManager, accountViewModel)
        proposalsViewModel = ProposalsViewModel(sharedViewModel, nodeRepository, appDatabase)
        termsViewModel = TermsViewModel(appDatabase)
    }

    companion object {
        fun from(activity: FragmentActivity?): AppContainer {
            return (activity!!.application as MainApplication).appContainer
        }
    }
}
