package com.aleksejantonov.mediapicker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.aleksejantonov.mediapicker.picker.MediaPickerDialogFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val permissions: Array<String> = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private val bottomSheetRouter by lazy { SL.bottomSheetRouter }

    override fun onResume() {
        super.onResume()
        bottomSheetRouter.attach(this)
        pick.setOnClickListener {
            if (Build.VERSION.SDK_INT < 23 || Build.VERSION.SDK_INT >= 23 && permissionsGranted()) {
                bottomSheetRouter.openModalBottomSheet(MediaPickerDialogFragment.newInstance(false, "test123"))
            } else {
                requestPermissions(permissions, 1212)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun permissionsGranted(): Boolean {
        for (permission in permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) return false
        }
        return true
    }
}
