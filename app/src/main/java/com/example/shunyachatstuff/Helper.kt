package com.example.shunyachatstuff

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*

class Helper {
    private val PERMISSIONS_REQUEST_CODE = 1

    var appPermissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO
    )

    fun checkAndRequestPermission(activity: Activity?): Boolean {
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                val listPermissionsNeeded: MutableList<String> =
                    ArrayList()
                for (perm in appPermissions) {
                    if (ContextCompat.checkSelfPermission(
                            activity!!,
                            perm
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        listPermissionsNeeded.add(perm)
                    }
                }
                if (listPermissionsNeeded.isNotEmpty()) {
                    ActivityCompat.requestPermissions(
                        activity!!, listPermissionsNeeded.toTypedArray(),
                        PERMISSIONS_REQUEST_CODE
                    )
                    return false
                }
            } catch (re: RuntimeException) {
            }
        }
        return true
    }

}