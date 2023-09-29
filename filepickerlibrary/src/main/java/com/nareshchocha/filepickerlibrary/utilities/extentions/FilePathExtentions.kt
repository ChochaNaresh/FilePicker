package com.nareshchocha.filepickerlibrary.utilities.extentions

import android.net.Uri

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is ExternalStorageProvider.
 */
internal fun Uri.isExternalStorageDocument(): Boolean {
    return "com.android.externalstorage.documents" == authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is DownloadsProvider.
 */
internal fun Uri.isDownloadsDocument(): Boolean {
    return "com.android.providers.downloads.documents" == authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is MediaProvider.
 */
internal fun Uri.isMediaDocument(): Boolean {
    return "com.android.providers.media.documents" == authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is Google Photos.
 */
internal fun Uri.isGooglePhotosUri(): Boolean {
    return "com.google.android.apps.photos.content" == authority
}

internal fun Uri.isGoogleDriveUri(): Boolean {
    return "com.google.android.apps.docs.storage" == authority || "com.google.android.apps.docs.storage.legacy" == authority
}
