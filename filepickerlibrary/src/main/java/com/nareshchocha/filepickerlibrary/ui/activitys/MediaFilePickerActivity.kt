package com.nareshchocha.filepickerlibrary.ui.activitys

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
import com.nareshchocha.filepickerlibrary.R
import com.nareshchocha.filepickerlibrary.models.PickMediaConfig
import com.nareshchocha.filepickerlibrary.models.PickMediaType
import com.nareshchocha.filepickerlibrary.permission.PermissionUtils.checkPermission
import com.nareshchocha.filepickerlibrary.picker.PickerUtils.selectFile
import com.nareshchocha.filepickerlibrary.utilities.FileUtils
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const
import com.nareshchocha.filepickerlibrary.utilities.extentions.getMediaIntent
import com.nareshchocha.filepickerlibrary.utilities.extentions.getSettingIntent
import com.nareshchocha.filepickerlibrary.utilities.extentions.setCanceledResult
import com.nareshchocha.filepickerlibrary.utilities.extentions.setSuccessResult
import com.nareshchocha.filepickerlibrary.utilities.extentions.showMyDialog
import timber.log.Timber

internal class MediaFilePickerActivity : AppCompatActivity() {

    private val mPickMediaConfig: PickMediaConfig? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(
                Const.BundleInternalExtras.PICK_MEDIA,
                PickMediaConfig::class.java,
            )
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Const.BundleInternalExtras.PICK_MEDIA) as PickMediaConfig?
        }
    }
    private val checkPermission =
        checkPermission(ActivityResultContracts.RequestPermission(), resultCallBack = {
            if (it) {
                launchFilePicker()
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    getPermission(
                        mPickMediaConfig = mPickMediaConfig!!,
                    ),
                )
            ) {
                showAskDialog()
            } else {
                showGotoSettingDialog()
            }
        })
    private val selectFile =
        selectFile(ActivityResultContracts.StartActivityForResult(), resultCallBack = { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                if (mPickMediaConfig?.allowMultiple == true && result.data?.clipData != null) {
                    val uris = result.data?.getClipDataUris()
                    Timber.tag(Const.LogTag.FILE_RESULT).v("File Uri ::: $uris")
                    val filePaths = uris?.getFilePathList(this)
                    Timber.tag(Const.LogTag.FILE_RESULT).v("filePath ::: $filePaths")
                    setSuccessResult(uris, filePath = filePaths)
                } else if (result.data?.data != null) {
                    val data = result.data?.data
                    Timber.tag(Const.LogTag.FILE_RESULT).v("File Uri ::: ${data?.toString()}")
                    val filePath = data?.let { FileUtils.getRealPath(this, it) }
                    Timber.tag(Const.LogTag.FILE_RESULT).v("filePath ::: $filePath")
                    setSuccessResult(data, filePath)
                }
            } else {
                Timber.tag(Const.LogTag.FILE_PICKER_ERROR)
                    .v(getString(R.string.err_media_pick_error))
                setCanceledResult(getString(R.string.err_media_pick_error))
            }
        })

    private fun Intent.getClipDataUris(): ArrayList<Uri> {
        val resultSet = LinkedHashSet<Uri>()
        data?.let { data ->
            resultSet.add(data)
        }
        val clipData = clipData
        if (clipData == null && resultSet.isEmpty()) {
            return ArrayList()
        } else if (clipData != null) {
            for (i in 0 until clipData.itemCount) {
                val uri = clipData.getItemAt(i).uri
                if (uri != null) {
                    resultSet.add(uri)
                }
            }
        }
        return ArrayList(resultSet)
    }

    private fun List<Uri>.getFilePathList(context: Context): ArrayList<String> {
        val filePathList = ArrayList<String>()
        forEach { uri ->
            FileUtils.getRealPath(context, uri)?.also { filePath ->
                filePathList.add(filePath)
            }
        }
        return filePathList
    }

    private fun launchFilePicker() {
        if (mPickMediaConfig != null) {
            selectFile.launch(getMediaIntent(mPickMediaConfig!!))
        } else {
            setCanceledResult(
                getString(
                    R.string.err_config_null,
                    this::mPickMediaConfig::class.java.name,
                ),
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        title = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission()
        } else {
            launchFilePicker()
        }
    }

    private fun showAskDialog() {
        showMyDialog(
            mPickMediaConfig?.askPermissionTitle ?: getString(R.string.err_permission_denied),
            mPickMediaConfig?.askPermissionMessage ?: getString(
                R.string.err_write_storage_permission,
                getPermission(mPickMediaConfig!!).split(".").lastOrNull() ?: "",
            ),
            negativeClick = {
                setCanceledResult(getString(R.string.err_permission_result))
            },
            positiveClick = {
                checkPermission()
            },
        )
    }

    private fun showGotoSettingDialog() {
        if (mPickMediaConfig != null) {
            showMyDialog(
                mPickMediaConfig?.settingPermissionTitle
                    ?: getString(R.string.err_permission_denied),
                mPickMediaConfig?.settingPermissionMessage ?: getString(
                    R.string.err_write_storage_setting,
                    getPermission(mPickMediaConfig!!).split(".").lastOrNull() ?: "",
                ),
                positiveButtonText = getString(R.string.str_go_to_setting),
                negativeClick = {
                    setCanceledResult(getString(R.string.err_permission_result))
                },
                positiveClick = {
                    settingCameraResultLauncher.launch(getSettingIntent())
                },
            )
        } else {
            setCanceledResult(
                getString(
                    R.string.err_config_null,
                    this::mPickMediaConfig::class.java.name,
                ),
            )
        }
    }

    private val settingCameraResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (mPickMediaConfig != null) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        getPermission(mPickMediaConfig!!),
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    launchFilePicker()
                } else {
                    setCanceledResult(getString(R.string.err_permission_result))
                }
            } else {
                setCanceledResult(
                    getString(
                        R.string.err_config_null,
                        this::mPickMediaConfig::class.java.name,
                    ),
                )
            }
        }

    private fun checkPermission() {
        if (mPickMediaConfig != null) {
            checkPermission.launch(
                getPermission(
                    mPickMediaConfig = mPickMediaConfig!!,
                ),
            )
        } else {
            setCanceledResult(
                getString(
                    R.string.err_config_null,
                    this::mPickMediaConfig::class.java.name,
                ),
            )
        }
    }

    companion object {

        private fun getPermission(mPickMediaConfig: PickMediaConfig): String {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (mPickMediaConfig.mPickMediaType == PickMediaType.ImageOnly) {
                    Manifest.permission.READ_MEDIA_IMAGES
                } else {
                    Manifest.permission.READ_MEDIA_VIDEO
                }
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
        }

        fun getInstance(mContext: Context, mPickMediaConfig: PickMediaConfig?): Intent {
            val filePickerIntent = Intent(mContext, MediaFilePickerActivity::class.java)
            mPickMediaConfig?.let {
                filePickerIntent.putExtra(Const.BundleInternalExtras.PICK_MEDIA, it)
            }
            return filePickerIntent
        }
    }
}
