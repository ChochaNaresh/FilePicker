package com.nareshchocha.filepickerlibray.utilities.extentions

import android.net.Uri

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is ExternalStorageProvider.
 */
fun Uri.isExternalStorageDocument(): Boolean {
    return "com.android.externalstorage.documents" == authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is DownloadsProvider.
 */
fun Uri.isDownloadsDocument(): Boolean {
    return "com.android.providers.downloads.documents" == authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is MediaProvider.
 */
fun Uri.isMediaDocument(): Boolean {
    return "com.android.providers.media.documents" == authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is Google Photos.
 */
fun Uri.isGooglePhotosUri(): Boolean {
    return "com.google.android.apps.photos.content" == authority
}
