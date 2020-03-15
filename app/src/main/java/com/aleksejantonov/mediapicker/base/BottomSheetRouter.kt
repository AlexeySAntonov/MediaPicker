package com.aleksejantonov.mediapicker.base

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import java.lang.ref.WeakReference

class BottomSheetRouter {
    private var activityRef: WeakReference<FragmentActivity>? = null

    fun attach(activity: FragmentActivity) {
        activityRef = WeakReference(activity)
    }

    fun openModalBottomSheet(fragment: DialogFragment) {
        activityRef?.get()?.let {
            fragment.show(it.supportFragmentManager, TAG_MODAL_BOTTOM_SHEET)
        }
    }

    fun close() {
        activityRef?.get()?.supportFragmentManager?.let {
            (it.findFragmentByTag(TAG_MODAL_BOTTOM_SHEET) as? DialogFragment)?.dialog?.dismiss()
        }
    }

    private companion object {
        const val TAG_MODAL_BOTTOM_SHEET = "TAG_MODAL_BOTTOM_SHEET"
    }
}