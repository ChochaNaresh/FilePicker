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
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.nareshchocha.filepickerlibrary.R
import com.nareshchocha.filepickerlibrary.models.DocumentFilePickerConfig
import com.nareshchocha.filepickerlibrary.permission.PermissionUtils.checkPermission
import com.nareshchocha.filepickerlibrary.picker.PickerUtils.selectFile
import com.nareshchocha.filepickerlibrary.utilities.FileUtils
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const
import com.nareshchocha.filepickerlibrary.utilities.extentions.getDocumentFilePick
import com.nareshchocha.filepickerlibrary.utilities.extentions.getSettingIntent
import com.nareshchocha.filepickerlibrary.utilities.extentions.setCanceledResult
import com.nareshchocha.filepickerlibrary.utilities.extentions.setSuccessResult
import com.nareshchocha.filepickerlibrary.utilities.extentions.showMyDialog
import timber.log.Timber

internal class DocumentFilePickerActivity : AppCompatActivity() {

    private val mDocumentFilePickerConfig: DocumentFilePickerConfig? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(
                Const.BundleInternalExtras.PICK_DOCUMENT,
                DocumentFilePickerConfig::class.java,
            )
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Const.BundleInternalExtras.PICK_DOCUMENT) as DocumentFilePickerConfig?
        }
    }
    private val checkPermission =
        checkPermission(ActivityResultContracts.RequestPermission(), resultCallBack = {
            if (it) {
                launchFilePicker()
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    getPermission(),
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
                if (mDocumentFilePickerConfig?.allowMultiple == true && result.data?.clipData != null) {
                    val uris = result.data?.getClipDataUris()
                    val filePaths = uris?.getFilePathList(this)
                    Timber.tag(Const.LogTag.FILE_RESULT).v("File Uri ::: $uris")
                    Timber.tag(Const.LogTag.FILE_RESULT).v("filePath ::: $filePaths")
                    setSuccessResult(uris, filePath = filePaths)
                } else if (result.data?.data != null) {
                    val data = result.data?.data
                    val filePath = data?.let { FileUtils.getRealPath(this, it) }
                    Timber.tag(Const.LogTag.FILE_RESULT).v("File Uri ::: ${data?.toString()}")
                    Timber.tag(Const.LogTag.FILE_RESULT).v("filePath ::: $filePath")
                    setSuccessResult(data, filePath)
                }
            } else {
                Timber.tag(Const.LogTag.FILE_PICKER_ERROR)
                    .v(getString(R.string.err_document_pick_error))
                setCanceledResult(getString(R.string.err_document_pick_error))
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
        if (mDocumentFilePickerConfig != null) {
            selectFile.launch(getDocumentFilePick(mDocumentFilePickerConfig!!))
        } else {
            setCanceledResult(
                getString(
                    R.string.err_config_null,
                    this::mDocumentFilePickerConfig::class.java.name,
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
            mDocumentFilePickerConfig?.askPermissionTitle
                ?: getString(R.string.err_permission_denied),
            mDocumentFilePickerConfig?.askPermissionMessage ?: getString(
                R.string.err_write_storage_permission,
                getPermission().split(".").lastOrNull() ?: "",
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
        if (mDocumentFilePickerConfig != null) {
            showMyDialog(
                mDocumentFilePickerConfig?.settingPermissionTitle
                    ?: getString(R.string.err_permission_denied),
                mDocumentFilePickerConfig?.settingPermissionMessage ?: getString(
                    R.string.err_write_storage_setting,
                    getPermission().split(".").lastOrNull() ?: "",
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
                    this::mDocumentFilePickerConfig::class.java.name,
                ),
            )
        }
    }

    private val settingCameraResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (mDocumentFilePickerConfig != null) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        getPermission(),
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
                        this::mDocumentFilePickerConfig::class.java.name,
                    ),
                )
            }
        }

    private fun checkPermission() {
        if (mDocumentFilePickerConfig != null) {
            checkPermission.launch(
                getPermission(),
            )
        } else {
            setCanceledResult(
                getString(
                    R.string.err_config_null,
                    this::mDocumentFilePickerConfig::class.java.name,
                ),
            )
        }
    }

    companion object {

        private fun getPermission(): String {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_VIDEO
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
        }

        @Keep
        fun getInstance(
            mContext: Context,
            mDocumentFilePickerConfig: DocumentFilePickerConfig?,
        ): Intent {
            val filePickerIntent = Intent(mContext, DocumentFilePickerActivity::class.java)
            mDocumentFilePickerConfig?.let {
                filePickerIntent.putExtra(Const.BundleInternalExtras.PICK_DOCUMENT, it)
            }
            return filePickerIntent
        }
    }
}
