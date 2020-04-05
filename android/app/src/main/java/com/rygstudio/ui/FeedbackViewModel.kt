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

package com.rygstudio.ui

import androidx.lifecycle.ViewModel
import com.rygstudio.service.core.NodeRepository

enum class FeedbackType(val type: Int) {
    BUG(0),
    CONNECTIVITY_ISSUE(1),
    POSITIVE_FEEDBACK(2);

    override fun toString(): String {
        return when(this) {
            BUG -> "bug"
            CONNECTIVITY_ISSUE -> "connectivity"
            POSITIVE_FEEDBACK -> "positive"
        }
    }

    companion object {
        fun parse(type: Int): FeedbackType {
            return values().find { it.type == type } ?: BUG
        }
    }
}

class FeedbackViewModel(private val nodeRepository: NodeRepository): ViewModel() {
    private var feedbackType = FeedbackType.BUG
    private var message = ""

    fun setFeedbackType(type: Int) {
        feedbackType = FeedbackType.parse(type)
    }

    fun setMessage(msg: String) {
        message = msg
    }

    fun isMessageSet(): Boolean {
        return message != ""
    }

    suspend fun submit() {
        val description = "Platform: Android, Feedback Type: $feedbackType, Message: $message"
        nodeRepository.sendFeedback(description)
    }
}
