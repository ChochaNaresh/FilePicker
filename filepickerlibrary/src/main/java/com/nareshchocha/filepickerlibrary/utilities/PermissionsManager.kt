package com.nareshchocha.filepickerlibrary.utilities

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionsManager(private val context: Context) {

    // Single Permission Request
    fun requestPermission(
        activityResultCaller: ActivityResultCaller,
        permission: String,
        onPermissionGranted: () -> Unit,
        onPermissionDenied: () -> Unit,
        onShowRationale: (permissions: String) -> Unit
    ) {
        val permissionsRequiringRationale = permission.let {
            ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, it)
        }

        if (permissionsRequiringRationale) {
            onShowRationale(permission)
        } else {
            val requestPermissionLauncher =
                activityResultCaller.let { activity ->
                    activity.registerForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        if (isGranted) {
                            onPermissionGranted()
                        } else {
                            onPermissionDenied()
                        }
                    }
                }

            // Check if permission is already granted
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                onPermissionGranted()
            } else {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    // Multiple Permission Request with Rationale
    fun requestMultiplePermissionsWithRationale(
        activityResultCaller: ActivityResultCaller,
        permissions: Array<String>,
        onPermissionsGranted: () -> Unit,
        onPermissionsDenied: () -> Unit,
        onShowRationale: (permissions: List<String>) -> Unit
    ) {
        // Check for rationale for each permission
        val permissionsRequiringRationale = permissions.filter {
            ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, it)
        }

        // If any permission requires rationale, show rationale
        if (permissionsRequiringRationale.isNotEmpty()) {
            onShowRationale(permissionsRequiringRationale)
        } else {
            // If no rationale is required, request permissions directly
            requestMultiplePermissions(
                activityResultCaller,
                permissions,
                onPermissionsGranted,
                onPermissionsDenied
            )
        }
    }

    // Request multiple permissions without rationale
    private fun requestMultiplePermissions(
        activityResultCaller: ActivityResultCaller,
        permissions: Array<String>,
        onPermissionsGranted: () -> Unit,
        onPermissionsDenied: () -> Unit
    ) {
        val requestMultiplePermissionsLauncher = activityResultCaller.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissionsMap ->
            val isAllPermissionsGranted = permissionsMap.all { it.value }

            if (isAllPermissionsGranted) {
                onPermissionsGranted()
            } else {
                onPermissionsDenied()
            }
        }

        // Check if all permissions are already granted
        if (permissions.all {
                ContextCompat.checkSelfPermission(
                    context,
                    it
                ) == PackageManager.PERMISSION_GRANTED
            }) {
            onPermissionsGranted()
        } else {
            requestMultiplePermissionsLauncher.launch(permissions)
        }
    }

    // Optional: Show rationale for requesting permission
    fun shouldShowRationale(permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, permission)
    }
}
