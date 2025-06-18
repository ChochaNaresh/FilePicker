package com.nareshchocha.filepickerlibrary.utilities

import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.nareshchocha.filepickerlibrary.utilities.extensions.getRequestedPermissions

interface PermissionsManager {
    fun shouldShowRationale(
        context: ComponentActivity,
        permission: String
    ) = ActivityCompat.shouldShowRequestPermissionRationale(
        context as Activity,
        permission
    )

    fun getRequestedPermissions(context: ComponentActivity): Array<String>? = context.getRequestedPermissions()

    fun isPermissionsAdded(
        context: ComponentActivity,
        permissions: List<String>
    ): Boolean {
        val requestedPermissions = getRequestedPermissions(context) ?: return false
        return permissions.all { requestedPermissions.contains(it) }
    }

    fun isPermissionAdded(
        context: ComponentActivity,
        permission: String
    ): Boolean {
        val requestedPermissions = getRequestedPermissions(context) ?: return false
        return requestedPermissions.contains(permission)
    }

    @Composable
    fun Check()
}

class SinglePermissionManager(
    private val activity: ComponentActivity,
    private val permission: String,
    private val onPermissionMissing: (permission: String) -> Unit = { },
    private val onPermissionGranted: () -> Unit = {},
    private val onPermissionDenied: () -> Unit = { },
    private val onShowRationale: () -> Unit = { }
) : PermissionsManager {
    lateinit var requestPermissionLauncher: ManagedActivityResultLauncher<String, Boolean>

    fun getDeniedPermissions(): List<String> =
        listOf(permission).filter {
            ContextCompat.checkSelfPermission(
                activity,
                it
            ) != PackageManager.PERMISSION_GRANTED
        }

    fun getRationalePermissions(): List<String> =
        listOf(permission).filter {
            shouldShowRationale(activity, it)
        }

    fun getMissingPermissions(): List<String> =
        listOf(permission).filterNot {
            isPermissionAdded(activity, it)
        }

    @Composable
    fun RegisterForSinglePermission() {
        requestPermissionLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    onPermissionGranted()
                } else if (shouldShowRationale(activity, permission)) {
                    onShowRationale()
                } else {
                    onPermissionDenied()
                }
            }
    }

    @Composable
    override fun Check() {
        if (permission.isBlank()) {
            onPermissionGranted()
        } else if (!isPermissionsAdded(activity, listOf(permission))) {
            onPermissionMissing(permission)
        } else {
            RegisterForSinglePermission()
            LaunchedEffect(Unit) {
                permissionsCheck()
            }
        }
    }

    fun permissionsCheck() {
        if (::requestPermissionLauncher.isInitialized) {
            requestPermissionLauncher.launch(permission)
        }
    }
}

class MultiplePermissionManager(
    private val activity: ComponentActivity,
    private val permissions: List<String>,
    private val onPermissionsMissing: (permissions: List<String>) -> Unit = { },
    private val onMultiplePermissionsGranted: () -> Unit = {},
    private val onMultiplePermissionsDenied: () -> Unit = {},
    private val onShowMultipleRationale: () -> Unit = { }
) : PermissionsManager {
    lateinit var requestMultiplePermissionsLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>

    fun getDeniedPermissions(): List<String> =
        permissions.filter {
            ContextCompat.checkSelfPermission(
                activity,
                it
            ) != PackageManager.PERMISSION_GRANTED
        }

    fun getRationalePermissions(): List<String> =
        permissions.filter {
            shouldShowRationale(activity, it)
        }

    fun getMissingPermissions(): List<String> =
        permissions.filterNot {
            isPermissionAdded(activity, it)
        }

    @Composable
    fun RegisterForMultiplePermissions() {
        requestMultiplePermissionsLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsMap ->
                val isAllPermissionsGranted = permissionsMap.all { it.value }
                val permissionsRequiringRationale =
                    permissions.any { shouldShowRationale(activity, it) }
                if (isAllPermissionsGranted) {
                    onMultiplePermissionsGranted()
                } else if (permissionsRequiringRationale) {
                    onShowMultipleRationale()
                } else {
                    onMultiplePermissionsDenied()
                }
            }
    }

    @Composable
    override fun Check() {
        if (permissions.isEmpty()) {
            onMultiplePermissionsGranted()
        } else if (!isPermissionsAdded(activity, permissions)) {
            onPermissionsMissing(getMissingPermissions())
        } else {
            RegisterForMultiplePermissions()
            LaunchedEffect(Unit) {
                permissionsCheck()
            }
        }
    }

    fun permissionsCheck() {
        if (::requestMultiplePermissionsLauncher.isInitialized) {
            requestMultiplePermissionsLauncher.launch(permissions.toTypedArray())
        }
    }
}
