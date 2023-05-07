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
import com.nareshchocha.filepickerlibray.models.ImageCaptureConfig
import com.nareshchocha.filepickerlibray.permission.PermissionUtils.checkPermission
import com.nareshchocha.filepickerlibray.picker.PickerUtils.createFileGetUri
import com.nareshchocha.filepickerlibray.picker.PickerUtils.createMediaFileFolder
import com.nareshchocha.filepickerlibray.picker.PickerUtils.selectFile
import com.nareshchocha.filepickerlibray.utilities.appConst.Const
import com.nareshchocha.filepickerlibray.utilities.extentions.getImageCaptureIntent
import com.nareshchocha.filepickerlibray.utilities.extentions.getSettingIntent
import com.nareshchocha.filepickerlibray.utilities.extentions.setCanceledResult
import com.nareshchocha.filepickerlibray.utilities.extentions.setSuccessResult
import com.nareshchocha.filepickerlibray.utilities.extentions.showMyDialog
import timber.log.Timber
import java.io.File

class ImageCaptureActivity : AppCompatActivity() {
    private var imageFileUri: Uri? = null
    private var imageFile: File? = null

    private val mImageCaptureConfig: ImageCaptureConfig? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(
                Const.BundleExtras.IMAGE_CAPTURE,
                ImageCaptureConfig::class.java,
            )
        } else {
            intent.getParcelableExtra(Const.BundleExtras.IMAGE_CAPTURE) as ImageCaptureConfig?
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

    private val imageCapture =
        selectFile(ActivityResultContracts.StartActivityForResult(), resultCallBack = { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                Timber.tag(Const.LogTag.FILE_RESULT)
                    .w("File Uri ::: ${imageFileUri?.toString()}")
                Timber.tag(Const.LogTag.FILE_RESULT).w("filePath ::: ${imageFile?.absoluteFile}")
                Timber.tag(Const.LogTag.FILE_RESULT).w("file read:: ${imageFile?.canRead()}")
                setSuccessResult(imageFileUri, imageFile?.absolutePath)
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
        imageFileUri = if (mImageCaptureConfig != null) {
            imageFile = createMediaFileFolder(
                folderFile = mImageCaptureConfig!!.mFolder,
                fileName = mImageCaptureConfig!!.fileName,
            )
            createFileGetUri(imageFile!!)
        } else {
            imageFile = createMediaFileFolder(
                folderFile = Const.DefaultPaths.defaultFolder,
                fileName = Const.DefaultPaths.defaultImageFile,
            )
            createFileGetUri(imageFile!!)
        }
        imageFileUri?.let { imageCapture.launch(getImageCaptureIntent(it)) }
    }

    private fun showAskDialog() {
        showMyDialog(
            getString(R.string.err_permission_denied),
            getString(
                R.string.err_write_storage_permission,
                PERMISSION.split(".").lastOrNull() ?: "",
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
        fun getInstance(mContext: Context, mImageCaptureConfig: ImageCaptureConfig?): Intent {
            val filePickerIntent = Intent(mContext, ImageCaptureActivity::class.java)
            mImageCaptureConfig?.let {
                filePickerIntent.putExtra(Const.BundleExtras.IMAGE_CAPTURE, it)
            }
            return filePickerIntent
        }
    }
}
