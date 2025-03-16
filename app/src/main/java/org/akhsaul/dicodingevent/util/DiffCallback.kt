package org.akhsaul.dicodingevent.util

import androidx.recyclerview.widget.DiffUtil
import org.akhsaul.dicodingevent.data.Event

object DiffCallback : DiffUtil.ItemCallback<Event>() {
    override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem == newItem
    }
}