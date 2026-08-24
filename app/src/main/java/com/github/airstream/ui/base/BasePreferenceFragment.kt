package com.github.airstream.ui.base

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.core.view.updatePadding
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.github.airstream.R
import com.github.airstream.databinding.DialogTextPreferenceBinding
import com.github.airstream.ui.extensions.onSystemInsets
import com.github.airstream.ui.preferences.EditNumberPreference
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * PreferenceFragmentCompat using the [MaterialAlertDialogBuilder] instead of the old dialog builder
 */
abstract class BasePreferenceFragment : PreferenceFragmentCompat() {

    /**
     * Whether any preference dialog is currently visible to the user.
     */
    var isDialogVisible = false

        override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val originalView = super.onCreateView(inflater, container, savedInstanceState)
        val wrapper = android.widget.LinearLayout(requireContext())
        wrapper.orientation = android.widget.LinearLayout.VERTICAL
        wrapper.setBackgroundColor(android.graphics.Color.parseColor("#F2F2F7"))
        
        val header = inflater.inflate(com.github.airstream.R.layout.layout_settings_header, wrapper, false)
        wrapper.addView(header)
        
        val lp = android.widget.LinearLayout.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT, 
            0, 
            1f
        )
        originalView.layoutParams = lp
        wrapper.addView(originalView)
        
        return wrapper
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (requireActivity() as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        view.findViewById<android.widget.TextView>(com.github.airstream.R.id.tvHeaderTitle)?.text = preferenceScreen?.title ?: "Settings"
        view.findViewById<android.view.View>(com.github.airstream.R.id.btnBack)?.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        
        (requireActivity() as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.hide()

        view.onSystemInsets { v, systemInsets ->
            v.updatePadding(top = systemInsets.top)
            listView.updatePadding(bottom = listView.paddingBottom + systemInsets.bottom)
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        // can be set to true here since we only use the following preferences with dialogs
        isDialogVisible = true

        when (preference) {
            /**
             * Show a [MaterialAlertDialogBuilder] when the preference is a [ListPreference]
             */
            is ListPreference -> {
                // get the index of the previous selected item
                val prefIndex = preference.entryValues.indexOf(preference.value)
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(preference.title)
                    .setSingleChoiceItems(preference.entries, prefIndex) { dialog, index ->
                        // get the new ListPreference value
                        val newValue = preference.entryValues[index].toString()
                        // invoke the on change listeners
                        if (preference.callChangeListener(newValue)) {
                            preference.value = newValue
                        }
                        dialog.dismiss()
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .setOnDismissListener { isDialogVisible = false }
                    .show()
            }

            is MultiSelectListPreference -> {
                val selectedItems = preference.entryValues.map {
                    preference.values.contains(it)
                }.toBooleanArray()
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(preference.title)
                    .setMultiChoiceItems(preference.entries, selectedItems) { _, _, _ ->
                        val newValues = preference.entryValues
                            .filterIndexed { index, _ -> selectedItems[index] }
                            .map { it.toString() }
                            .toMutableSet()
                        if (preference.callChangeListener(newValues)) {
                            preference.values = newValues
                        }
                    }
                    .setPositiveButton(R.string.okay, null)
                    .setOnDismissListener { isDialogVisible = false }
                    .show()
            }

            is EditTextPreference -> {
                val binding = DialogTextPreferenceBinding.inflate(layoutInflater)
                binding.input.setText(preference.text)

                if (preference is EditNumberPreference) {
                    binding.input.inputType = InputType.TYPE_NUMBER_FLAG_SIGNED
                }

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(preference.title)
                    .setView(binding.root)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        val newValue = binding.input.text.toString()
                        if (preference is EditNumberPreference && newValue.toIntOrNull() == null) {
                            Toast.makeText(context, R.string.invalid_input, Toast.LENGTH_LONG).show()
                            return@setPositiveButton
                        }

                        if (preference.callChangeListener(newValue)) {
                            preference.text = newValue
                        }
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .setOnDismissListener { isDialogVisible = false }
                    .show()
            }
            /**
             * Otherwise show the normal dialog, dialogs for other preference types are not supported yet,
             * nor used anywhere inside the app
             */
            else -> super.onDisplayPreferenceDialog(preference)
        }
    }
}
