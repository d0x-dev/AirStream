package com.github.airstream.ui.sheets

import android.app.Dialog
import android.content.res.Configuration
import android.os.Bundle
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.fragment.app.FragmentManager
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.PEEK_HEIGHT_AUTO
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

open class ExpandedBottomSheet(@LayoutRes layoutResId: Int) :
    BottomSheetDialogFragment(layoutResId) {
    private val bottomSheet: FrameLayout? get() = dialog?.findViewById(R.id.design_bottom_sheet)
    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                window.addFlags(android.view.WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                window.attributes.blurBehindRadius = 60
            }
            window.setDimAmount(0.3f)
            window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        }

        val designBottomSheet = dialog?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
        designBottomSheet?.post {
            designBottomSheet.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        }
        
        view?.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        val standardBottomSheet = view?.findViewById<android.view.View>(com.github.airstream.R.id.standard_bottom_sheet)
        standardBottomSheet?.setBackgroundResource(com.github.airstream.R.drawable.bg_bottom_sheet_rounded)
        standardBottomSheet?.backgroundTintList = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) return dialog

        dialog.setOnShowListener { expand() }

        return dialog
    }

    fun show(fragmentManager: FragmentManager) = show(fragmentManager, null)

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        // ensure that the sheet doesn't hide parts of the video
        dialog?.dismiss()
    }

    fun expand(collapse: Boolean = false) {
        bottomSheet?.let { fl ->
            val bottomSheetInfoBehavior = BottomSheetBehavior.from(fl)
            if (collapse) {
                bottomSheetInfoBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                bottomSheetInfoBehavior.setPeekHeight(0, true)
            } else {
                bottomSheetInfoBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                bottomSheetInfoBehavior.setPeekHeight(PEEK_HEIGHT_AUTO, true)
            }
        }
    }
}
