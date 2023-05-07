package com.nareshchocha.filepickerlibray.ui.activitys

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.nareshchocha.filepickerlibray.R
import com.nareshchocha.filepickerlibray.models.VideoCaptureConfig
import com.nareshchocha.filepickerlibray.permission.PermissionUtils.checkPermission
import com.nareshchocha.filepickerlibray.picker.PickerUtils.createFileGetUri
import com.nareshchocha.filepickerlibray.picker.PickerUtils.createMediaFileFolder
import com.nareshchocha.filepickerlibray.picker.PickerUtils.selectFile
import com.nareshchocha.filepickerlibray.utilities.appConst.Const
import com.nareshchocha.filepickerlibray.utilities.extentions.getSettingIntent
import com.nareshchocha.filepickerlibray.utilities.extentions.getVideoCaptureIntent
import com.nareshchocha.filepickerlibray.utilities.extentions.setCanceledResult
import com.nareshchocha.filepickerlibray.utilities.extentions.setSuccessResult
import com.nareshchocha.filepickerlibray.utilities.extentions.showMyDialog
import timber.log.Timber
import java.io.File

class VideoCaptureActivity : AppCompatActivity() {
    private var videoFileUri: Uri? = null
    private var videoFile: File? = null

    private val mVideoCaptureConfig: VideoCaptureConfig? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(
                Const.BundleExtras.VIDEO_CAPTURE,
                VideoCaptureConfig::class.java,
            )
        } else {
            intent.getParcelableExtra(Const.BundleExtras.VIDEO_CAPTURE) as VideoCaptureConfig?
        }
    }
    private val checkPermission =
        checkPermission(ActivityResultContracts.RequestPermission(), resultCallBack = {
            if (it) {
                launchCamera()
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSION)) {
                showAskDialog()
            } else {
                showGotoSettingDialog()
            }
        })

    private fun getPermission() {
        checkPermission.launch(PERMISSION)
    }

    private val videoCapture =
        selectFile(ActivityResultContracts.StartActivityForResult(), resultCallBack = { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                Timber.tag(Const.LogTag.FILE_RESULT)
                    .w("File Uri ::: ${videoFileUri?.toString()}")
                Timber.tag(Const.LogTag.FILE_RESULT).w("filePath ::: ${videoFile?.absoluteFile}")
                Timber.tag(Const.LogTag.FILE_RESULT).w("file read:: ${videoFile?.canRead()}")
                setSuccessResult(videoFileUri, videoFile?.absolutePath)
            } else {
                Timber.tag(Const.LogTag.FILE_PICKER_ERROR).e("capture Error")
                setCanceledResult("capture Error")
            }
        })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            getPermission()
        } else {
            launchCamera()
        }
    }

    private fun launchCamera() {
        videoFileUri = if (mVideoCaptureConfig != null) {
            videoFile = createMediaFileFolder(
                folderFile = mVideoCaptureConfig!!.mFolder,
                fileName = mVideoCaptureConfig!!.fileName,
            )
            createFileGetUri(videoFile!!)
        } else {
            videoFile = createMediaFileFolder(
                folderFile = Const.DefaultPaths.defaultFolder,
                fileName = Const.DefaultPaths.defaultVideoFile,
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
            getString(R.string.err_permission_denied),
            getString(R.string.err_write_storage_permission, PERMISSION.split(".").lastOrNull() ?: ""),
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
            getString(R.string.err_permission_denied),
            getString(R.string.err_write_storage_setting, PERMISSION.split(".").lastOrNull() ?: ""),
            positiveButtonText = "Go To Setting",
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
            if (ActivityCompat.checkSelfPermission(
                    this,
                    PERMISSION,
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                launchCamera()
            } else {
                setCanceledResult(getString(R.string.err_permission_result))
            }
        }

    companion object {
        private const val PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE
        fun getInstance(mContext: Context, mVideoCaptureConfig: VideoCaptureConfig?): Intent {
            val filePickerIntent = Intent(mContext, VideoCaptureActivity::class.java)
            mVideoCaptureConfig?.let {
                filePickerIntent.putExtra(Const.BundleExtras.VIDEO_CAPTURE, it)
            }
            return filePickerIntent
        }
    }
}
