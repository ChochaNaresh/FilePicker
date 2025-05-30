package com.nareshchocha.filepickerlibrary.models

import android.graphics.drawable.shapes.Shape
import android.os.Build
import android.os.Parcelable
import android.provider.MediaStore
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import com.nareshchocha.filepickerlibrary.R
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const
import kotlinx.parcelize.Parcelize
import java.io.File

@Keep
@Parcelize
data class PickerData(
    val mPopUpConfig: PopUpConfig? = null,
    val listIntents: List<BaseConfig> = emptyList(),
) : Parcelable

@Keep
@Parcelize
data class PopUpConfig(
    val chooserTitle: String? = null,
    val mPopUpType: PopUpType? = null,
    val mOrientation: Orientation? = null,
    val cornerSize: Float = 8f,
    val title: @Composable ((title: String) -> Unit)? = null,
    val item: @Composable ((item: BaseConfig) -> Unit)? = null,
) : Parcelable

enum class Orientation {
    HORIZONTAL,
    VERTICAL;

    fun isHorizontal() = this == HORIZONTAL
    fun isVertical() = this == VERTICAL
}

@Parcelize
data class ImageCaptureConfig(
    @DrawableRes override val popUpIcon: Int? = R.drawable.ic_camera,
    override val popUpText: String? = "Camera",
    val mFolder: File? = null,
    val fileName: String? = Const.DefaultPaths.defaultImageFile(),
    /**
     * It is not working correctly. However, it will Work on the same devices.
     */
    val isUseRearCamera: Boolean? = true,
    override val askPermissionTitle: String? = null,
    override val askPermissionMessage: String? = null,
    override val settingPermissionTitle: String? = null,
    override val settingPermissionMessage: String? = null,
) : Parcelable, BaseConfig(
    popUpIcon,
    popUpText,
    askPermissionTitle,
    askPermissionMessage,
    settingPermissionTitle,
    settingPermissionMessage,
)

@Parcelize
data class VideoCaptureConfig(
    @DrawableRes override val popUpIcon: Int? = R.drawable.ic_video,
    override val popUpText: String? = "Video",
    val mFolder: File? = null,
    val fileName: String? = Const.DefaultPaths.defaultVideoFile(),
    val maxSeconds: Int? = null,
    val maxSizeLimit: Long? = null,
    val isHighQuality: Boolean? = null,
    override val askPermissionTitle: String? = null,
    override val askPermissionMessage: String? = null,
    override val settingPermissionTitle: String? = null,
    override val settingPermissionMessage: String? = null,
) : Parcelable, BaseConfig(
    popUpIcon,
    popUpText,
    askPermissionTitle,
    askPermissionMessage,
    settingPermissionTitle,
    settingPermissionMessage,
)

@Parcelize
data class PickMediaConfig(
    @DrawableRes override val popUpIcon: Int? = R.drawable.ic_media,
    override val popUpText: String? = "Pick Media",
    val allowMultiple: Boolean? = false,
    /**
     *  MaxFiles work after SDK 33 or above versions
     */
    val maxFiles: Int? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        MediaStore.getPickImagesMaxLimit()
    } else {
        Int.MAX_VALUE
    },
    val mPickMediaType: PickMediaType? = PickMediaType.ImageAndVideo,
    override val askPermissionTitle: String? = null,
    override val askPermissionMessage: String? = null,
    override val settingPermissionTitle: String? = null,
    override val settingPermissionMessage: String? = null,
) : Parcelable, BaseConfig(
    popUpIcon,
    popUpText,
    askPermissionTitle,
    askPermissionMessage,
    settingPermissionTitle,
    settingPermissionMessage,
) {
    fun getPickMediaType(input: PickMediaType?): String? {
        return when (input) {
            PickMediaType.ImageOnly -> "image/*"
            PickMediaType.VideoOnly -> "video/*"
            PickMediaType.ImageAndVideo -> null
            else -> null
        }
    }
}

@Parcelize
data class DocumentFilePickerConfig(
    @DrawableRes override val popUpIcon: Int? = R.drawable.ic_file,
    override val popUpText: String? = "File Media",
    val allowMultiple: Boolean? = false,
    /**
     *  MaxFiles work after SDK 33 or above versions
     */
    val maxFiles: Int? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        MediaStore.getPickImagesMaxLimit()
    } else {
        Int.MAX_VALUE
    },
    val mMimeTypes: List<String>? = listOf("*/*"),
    override val askPermissionTitle: String? = null,
    override val askPermissionMessage: String? = null,
    override val settingPermissionTitle: String? = null,
    override val settingPermissionMessage: String? = null,
) : Parcelable, BaseConfig(
    popUpIcon,
    popUpText,
    askPermissionTitle,
    askPermissionMessage,
    settingPermissionTitle,
    settingPermissionMessage,
)

@Keep
@Parcelize
enum class PopUpType : Parcelable {
    BOTTOM_SHEET, DIALOG;

    fun isDialog() = this == DIALOG
}

@Parcelize
enum class PickMediaType : Parcelable {
    ImageOnly, VideoOnly, ImageAndVideo
}
