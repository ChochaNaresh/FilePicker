package com.nareshchocha.filepickerlibrary.utilities

import android.util.Log
import com.nareshchocha.filepickerlibrary.FilePickerResultContracts
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const

fun log(
    message: String,
    priority: LogPriority = LogPriority.DEBUG_LOG,
    customTag: String = Const.LogTag.FILE_PICKER_ERROR,
    throwable: Throwable? = null
) {
    if (FilePickerResultContracts.isLoggingEnabled) {
        message.printToLog(
            priority = priority,
            customTag = customTag,
            throwable = throwable
        )
    }
}

fun Any?.printToLog(
    customTag: String? = null,
    priority: LogPriority = LogPriority.DEBUG_LOG,
    throwable: Throwable? = null
) {
    printToLog(customTag, priority, throwable) { this?.toString() ?: "null" }
}

fun printToLog(
    customTag: String? = null,
    priority: LogPriority = LogPriority.DEBUG_LOG,
    throwable: Throwable? = null,
    messageProvider: () -> String
) {
    if (!FilePickerResultContracts.isLoggingEnabled) return
    val tag = customTag ?: priority.getString()
    val message = messageProvider()

    when (priority) {
        LogPriority.DEBUG_LOG -> Log.d(tag, message, throwable)
        LogPriority.ERROR_LOG -> Log.e(tag, message, throwable)
        LogPriority.INFO_LOG -> Log.i(tag, message, throwable)
        LogPriority.WARNING_LOG -> Log.w(tag, message, throwable)
    }
}

enum class LogPriority {
    DEBUG_LOG,
    ERROR_LOG,
    INFO_LOG,
    WARNING_LOG
}

private fun LogPriority.getString(): String =
    when (this) {
        LogPriority.DEBUG_LOG -> "DEBUG =>"
        LogPriority.ERROR_LOG -> "ERROR =>"
        LogPriority.INFO_LOG -> "INFO =>"
        LogPriority.WARNING_LOG -> "WARNING =>"
    }
