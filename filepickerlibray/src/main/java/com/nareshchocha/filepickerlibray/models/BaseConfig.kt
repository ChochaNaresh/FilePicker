package com.nareshchocha.filepickerlibray.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
open class BaseConfig(
    open val popUpIcon: Int,
    open val popUpText: String,
) : Parcelable
