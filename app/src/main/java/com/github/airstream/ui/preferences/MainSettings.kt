package com.github.airstream.ui.preferences

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.github.airstream.R
import com.github.airstream.ui.activities.MainActivity

class MainSettings : Fragment(R.layout.fragment_custom_settings) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        (activity as? MainActivity)?.findViewById<View>(R.id.toolbar)?.visibility = View.GONE
        
        view.findViewById<View>(R.id.btnBack)?.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? MainActivity)?.findViewById<View>(R.id.toolbar)?.visibility = View.VISIBLE
    }
}