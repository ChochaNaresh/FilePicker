package com.nareshchocha.filepickerlibrary.utilities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.nareshchocha.filepickerlibrary.utilities.extensions.getRequestedPermissions
import com.nareshchocha.filepickerlibrary.utilities.extensions.getSettingIntent

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
    private val onPermissionMissing: (permission: List<String>) -> Unit = { },
    private val onPermissionGranted: () -> Unit = {},
    private val onPermissionDenied: () -> Unit = { },
    private val onShowRationale: () -> Unit = { }
) : PermissionsManager {
    lateinit var requestPermissionLauncher: ManagedActivityResultLauncher<String, Boolean>
    lateinit var settingLauncherLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>

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
    fun RegisterForSettingLauncher() {
        settingLauncherLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                permissionsCheck()
            }
    }

    @Composable
    override fun Check() {
        if (permission.isBlank()) {
            onPermissionGranted()
        } else if (!isPermissionsAdded(activity, listOf(permission))) {
            onPermissionMissing(getMissingPermissions())
        } else {
            RegisterForSinglePermission()
            RegisterForSettingLauncher()
            LaunchedEffect(Unit) {
                permissionsCheck()
            }
        }
    }

    fun openAppSettings() {
        if (::settingLauncherLauncher.isInitialized) {
            settingLauncherLauncher.launch(activity.getSettingIntent())
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
    lateinit var settingLauncherLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>

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
    fun RegisterForSettingLauncher() {
        settingLauncherLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                permissionsCheck()
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
            RegisterForSettingLauncher()
            LaunchedEffect(Unit) {
                permissionsCheck()
            }
        }
    }

    fun openAppSettings() {
        if (::settingLauncherLauncher.isInitialized) {
            settingLauncherLauncher.launch(activity.getSettingIntent())
        }
    }

    fun permissionsCheck() {
        if (::requestMultiplePermissionsLauncher.isInitialized) {
            requestMultiplePermissionsLauncher.launch(permissions.toTypedArray())
        }
    }
}

class MediaMultiplePermissionManager(
    private val activity: ComponentActivity,
    private val permissions: List<String>,
    private val onPermissionsMissing: (permissions: List<String>) -> Unit = { },
    private val onMultiplePermissionsGranted: () -> Unit = {},
    private val onMultiplePermissionsDenied: () -> Unit = {},
    private val onShowMultipleRationale: () -> Unit = { }
) : PermissionsManager {
    lateinit var requestMultiplePermissionsLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>
    lateinit var settingLauncherLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>

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
                val isReadMediaVisualUserSelected =
                    permissionsMap
                        .filter {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                it.key == android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                            } else {
                                false
                            }
                        }.values
                        .firstOrNull() == true
                val permissionsRequiringRationale =
                    permissions.any { shouldShowRationale(activity, it) }

                if (isReadMediaVisualUserSelected && !isAllPermissionsGranted) {
                    onMultiplePermissionsGranted()
                } else if (isAllPermissionsGranted) {
                    onMultiplePermissionsGranted()
                } else if (permissionsRequiringRationale) {
                    onShowMultipleRationale()
                } else {
                    onMultiplePermissionsDenied()
                }
            }
    }

    @Composable
    fun RegisterForSettingLauncher() {
        settingLauncherLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                permissionsCheck()
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
            RegisterForSettingLauncher()
            LaunchedEffect(Unit) {
                permissionsCheck()
            }
        }
    }

    fun openAppSettings() {
        if (::settingLauncherLauncher.isInitialized) {
            settingLauncherLauncher.launch(activity.getSettingIntent())
        }
    }

    fun permissionsCheck() {
        if (::requestMultiplePermissionsLauncher.isInitialized) {
            requestMultiplePermissionsLauncher.launch(permissions.toTypedArray())
        }
    }
}
