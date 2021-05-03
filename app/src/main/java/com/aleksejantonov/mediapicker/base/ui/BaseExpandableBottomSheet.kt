package com.aleksejantonov.mediapicker.base.ui

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.annotation.LayoutRes
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.aleksejantonov.mediapicker.base.getColor
import com.aleksejantonov.mediapicker.base.getScreenHeight
import timber.log.Timber

abstract class BaseExpandableBottomSheet : AppCompatDialogFragment() {

    @get:LayoutRes
    protected abstract val layoutRes: Int
    protected abstract val slideOffsetListener: (Float) -> Unit

    private var behavior: BottomSheetBehavior<FrameLayout>? = null
    private var currentDim = DEFAULT_DIM

    private val bottomSheetCallback by lazy {
        object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN || newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    dismiss()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                slideOffsetListener.invoke(slideOffset)
                if (slideOffset == 1f) {
                    if (currentDim != EXPANDED_DIM) {
                        dialog?.window?.setDimAmount(EXPANDED_DIM)
                        currentDim = EXPANDED_DIM
                    }
                } else {
                    if (currentDim != DEFAULT_DIM) {
                        dialog?.window?.setDimAmount(DEFAULT_DIM)
                        currentDim = DEFAULT_DIM
                    }
                }
            }
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
                setupBehavior(bottomSheet)
                dialog?.window?.setDimAmount(DEFAULT_DIM)
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    private fun setupBehavior(bottomSheet: FrameLayout) {
        behavior = BottomSheetBehavior.from(bottomSheet)
        behavior?.state = BottomSheetBehavior.STATE_EXPANDED
        behavior?.peekHeight = context?.getScreenHeight()?.let { it / 2 } ?: BottomSheetBehavior.PEEK_HEIGHT_AUTO
        behavior?.addBottomSheetCallback(bottomSheetCallback)
    }

    override fun onDestroyView() {
        val viewGroup = (view as? ViewGroup)
        for (i in 0 until (viewGroup?.childCount ?: 0)) {
            if (viewGroup?.getChildAt(i) is RecyclerView) {
                (viewGroup.getChildAt(i) as? RecyclerView)?.adapter = null
                Timber.d("Bottom sheet recycler adapter released: ${viewGroup.getChildAt(i)}")
            }
        }
        behavior?.removeBottomSheetCallback(bottomSheetCallback)
        Timber.d("Bottom sheet behavior callback removed")
        super.onDestroyView()
    }

    companion object {
        private const val DEFAULT_DIM = 0.5f
        private const val EXPANDED_DIM = 0f
    }
}