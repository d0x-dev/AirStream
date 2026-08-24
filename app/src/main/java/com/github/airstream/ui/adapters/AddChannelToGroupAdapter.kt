package com.github.airstream.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.github.airstream.databinding.AddChannelToGroupRowBinding
import com.github.airstream.db.obj.SubscriptionGroup
import com.github.airstream.ui.adapters.callbacks.DiffUtilItemCallback
import com.github.airstream.ui.viewholders.AddChannelToGroupViewHolder

class AddChannelToGroupAdapter(
    private val channelId: String
) : ListAdapter<SubscriptionGroup, AddChannelToGroupViewHolder>(DiffUtilItemCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddChannelToGroupViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = AddChannelToGroupRowBinding.inflate(layoutInflater, parent, false)
        return AddChannelToGroupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddChannelToGroupViewHolder, position: Int) {
        val channelGroup = getItem(holder.bindingAdapterPosition)

        holder.binding.apply {
            groupName.text = channelGroup.name
            groupCheckbox.isChecked = channelGroup.channels.contains(channelId)

            groupCheckbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    channelGroup.channels += channelId
                } else {
                    channelGroup.channels -= channelId
                }
            }
            root.setOnClickListener {
                groupCheckbox.isChecked = !groupCheckbox.isChecked
            }
        }
    }
}
