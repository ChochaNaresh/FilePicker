package com.nareshchocha.filepickerlibrary.ui.activitys

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.nareshchocha.filepickerlibrary.R
import com.nareshchocha.filepickerlibrary.models.VideoCaptureConfig
import com.nareshchocha.filepickerlibrary.permission.PermissionUtils.checkPermission
import com.nareshchocha.filepickerlibrary.picker.PickerUtils.createFileGetUri
import com.nareshchocha.filepickerlibrary.picker.PickerUtils.createMediaFileFolder
import com.nareshchocha.filepickerlibrary.picker.PickerUtils.selectFile
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const.DefaultPaths.defaultFolder
import com.nareshchocha.filepickerlibrary.utilities.extentions.getSettingIntent
import com.nareshchocha.filepickerlibrary.utilities.extentions.getVideoCaptureIntent
import com.nareshchocha.filepickerlibrary.utilities.extentions.setCanceledResult
import com.nareshchocha.filepickerlibrary.utilities.extentions.setSuccessResult
import com.nareshchocha.filepickerlibrary.utilities.extentions.showMyDialog
import timber.log.Timber
import java.io.File

internal class VideoCaptureActivity : AppCompatActivity() {
    private var videoFileUri: Uri? = null
    private var videoFile: File? = null

    private val mVideoCaptureConfig: VideoCaptureConfig? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(
                Const.BundleInternalExtras.VIDEO_CAPTURE,
                VideoCaptureConfig::class.java,
            )
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Const.BundleInternalExtras.VIDEO_CAPTURE) as VideoCaptureConfig?
        }
    }
    private val checkPermission =
        checkPermission(
            ActivityResultContracts.RequestMultiplePermissions(),
            resultCallBack = { input ->
                val allGranted = input.all { permission ->
                    ContextCompat.checkSelfPermission(
                        this,
                        permission.key,
                    ) == PackageManager.PERMISSION_GRANTED
                }
                val isShowRationale = input.any { permission ->
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        permission.key,
                    )
                }
                if (allGranted) {
                    launchCamera()
                } else if (isShowRationale) {
                    showAskDialog()
                } else {
                    showGotoSettingDialog()
                }
            },
        )

    private fun getPermission() {
        checkPermission.launch(getPermissionsList(this).toTypedArray())
    }

    private val videoCapture =
        selectFile(ActivityResultContracts.StartActivityForResult(), resultCallBack = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Timber.tag(Const.LogTag.FILE_RESULT)
                    .v("File Uri ::: ${videoFileUri?.toString()}")
                Timber.tag(Const.LogTag.FILE_RESULT).v("filePath ::: ${videoFile?.absoluteFile}")
                setSuccessResult(videoFileUri, videoFile?.absolutePath, true)
            } else {
                Timber.tag(Const.LogTag.FILE_PICKER_ERROR)
                    .v(getString(R.string.err_capture_error, "videoCapture"))
                setCanceledResult(getString(R.string.err_capture_error, "videoCapture"))
            }
        })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q || getPermissionsList(
                    this,
                ).isNotEmpty()
            ) {
                getPermission()
            } else {
                launchCamera()
            }
        } else {
            launchCamera()
        }
    }

    private fun launchCamera() {
        videoFileUri = if (mVideoCaptureConfig != null) {
            videoFile = createMediaFileFolder(
                folderFile = mVideoCaptureConfig!!.mFolder ?: this.defaultFolder(),
                fileName = mVideoCaptureConfig!!.fileName ?: Const.DefaultPaths.defaultVideoFile(),
            )
            createFileGetUri(videoFile!!)
        } else {
            videoFile = createMediaFileFolder(
                folderFile = this.defaultFolder(),
                fileName = Const.DefaultPaths.defaultVideoFile(),
            )
            createFileGetUri(videoFile!!)
        }
        videoFileUri?.let {
            videoCapture.launch(
                getVideoCaptureIntent(
                    it,
                    maxSeconds = mVideoCaptureConfig?.maxSeconds,
                    maxSizeLimit = mVideoCaptureConfig?.maxSizeLimit,
                    isHighQuality = mVideoCaptureConfig?.isHighQuality,
                ),
            )
        }
    }

    private fun showAskDialog() {
        showMyDialog(
            mVideoCaptureConfig?.askPermissionTitle ?: getString(R.string.err_permission_denied),
            mVideoCaptureConfig?.askPermissionMessage ?: getString(
                R.string.err_write_storage_permission,
                getPermissionsListString(),
            ),
            negativeClick = {
                setCanceledResult(getString(R.string.err_permission_result))
            },
            positiveClick = {
                getPermission()
            },
        )
    }

    private fun showGotoSettingDialog() {
        showMyDialog(
            mVideoCaptureConfig?.settingPermissionTitle
                ?: getString(R.string.err_permission_denied),
            mVideoCaptureConfig?.settingPermissionMessage ?: getString(
                R.string.err_write_storage_setting,
                getPermissionsListString(),
            ),
            positiveButtonText = getString(R.string.str_go_to_setting),
            negativeClick = {
                setCanceledResult(getString(R.string.err_permission_result))
            },
            positiveClick = {
                settingCameraResultLauncher.launch(getSettingIntent())
            },
        )
    }

    private val settingCameraResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val allGranted = getPermissionsList(this).all { permission ->
                ContextCompat.checkSelfPermission(
                    this,
                    permission,
                ) == PackageManager.PERMISSION_GRANTED
            }
            if (allGranted) {
                launchCamera()
            } else {
                setCanceledResult(getString(R.string.err_permission_result))
            }
        }

    private fun getPermissionsListString(): String {
        val listString = getPermissionsList(this).map {
            it.split(".").lastOrNull() ?: ""
        }.toString()
        return listString.substring(1, listString.length - 1).replace(",", " and ")
    }

    companion object {
        // private const val PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE
        private fun getPermissionsList(context: Context) = ArrayList<String>().also {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                it.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            val info: PackageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong()),
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_PERMISSIONS,
                )
            }
            val permissions = info.requestedPermissions
            if (permissions.contains(Manifest.permission.CAMERA)) {
                it.add(Manifest.permission.CAMERA)
            }
        }

        fun getInstance(mContext: Context, mVideoCaptureConfig: VideoCaptureConfig?): Intent {
            val filePickerIntent = Intent(mContext, VideoCaptureActivity::class.java)
            mVideoCaptureConfig?.let {
                filePickerIntent.putExtra(Const.BundleInternalExtras.VIDEO_CAPTURE, it)
            }
            filePickerIntent.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            return filePickerIntent
        }
    }
}
