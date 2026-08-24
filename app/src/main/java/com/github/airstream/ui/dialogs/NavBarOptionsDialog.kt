package com.github.airstream.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.airstream.R
import com.github.airstream.databinding.SimpleOptionsRecyclerBinding
import com.github.airstream.extensions.setOnDraggedListener
import com.github.airstream.helpers.NavBarHelper
import com.github.airstream.ui.adapters.NavBarOptionsAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder

data class NavBarItem(
    val itemId: Int,
    val title: String?,
    var isVisible: Boolean
)

class NavBarOptionsDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = SimpleOptionsRecyclerBinding.inflate(layoutInflater)
        val options = NavBarHelper.getNavBarItemPreference(requireContext())
        val adapter = NavBarOptionsAdapter(
            options.map { (itemId, isVisible) ->
                NavBarItem(
                    itemId,
                    NavBarHelper.getNavBarItemTitle(requireContext(), itemId),
                    isVisible
                )
            }.toMutableList(),
            NavBarHelper.getStartFragmentId(requireContext())
        )

        binding.optionsRecycler.layoutManager = LinearLayoutManager(context)
        binding.optionsRecycler.adapter = adapter
        binding.optionsRecycler.setOnDraggedListener { from, to ->
            val itemToMove = adapter.items[from]
            adapter.items.remove(itemToMove)
            adapter.items.add(to, itemToMove)

            adapter.notifyItemMoved(from, to)
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.navigation_bar)
            .setView(binding.root)
            .setPositiveButton(R.string.okay) { _, _ ->
                NavBarHelper.setNavBarItemsPreference( requireContext(), adapter.items)
                NavBarHelper.setStartFragment(requireContext(), adapter.selectedHomeTabId)
                RequireRestartDialog()
                    .show(requireParentFragment().childFragmentManager, null)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
