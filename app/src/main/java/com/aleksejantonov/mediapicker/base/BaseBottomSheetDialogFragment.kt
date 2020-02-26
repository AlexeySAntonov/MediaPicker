package com.aleksejantonov.mediapicker.base

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.annotation.LayoutRes
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView

abstract class BaseBottomSheetDialogFragment : DialogFragment() {

    @get:LayoutRes
    protected abstract val layoutRes: Int

    private var behavior: BottomSheetBehavior<FrameLayout>? = null

    private val bottomSheetCallback by lazy {
        object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) dismiss()
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(layoutRes, container, false)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(checkNotNull(context), theme)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val bottomSheet = dialog?.findViewById(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
                context?.let { bottomSheet.background = ColorDrawable(getColor(android.R.color.transparent)) }
                behavior = BottomSheetBehavior.from(bottomSheet)
                behavior?.state = BottomSheetBehavior.STATE_COLLAPSED
                behavior?.peekHeight = context?.getScreenHeight()?.let { it / 2 } ?: BottomSheetBehavior.PEEK_HEIGHT_AUTO
                behavior?.addBottomSheetCallback(bottomSheetCallback)
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    override fun onDestroyView() {
        val viewGroup = (view as? ViewGroup)
        for (i in 0 until (viewGroup?.childCount ?: 0)) {
            if (viewGroup?.getChildAt(i) is RecyclerView) {
                (viewGroup.getChildAt(i) as? RecyclerView)?.adapter = null
                Log.d("Bottom sheet: ", "recycler adapter released: ${viewGroup.getChildAt(i)}")
            }
        }
        behavior?.removeBottomSheetCallback(bottomSheetCallback)
        Log.d("Bottom sheet: ", "behavior callback removed")
        super.onDestroyView()
    }
}