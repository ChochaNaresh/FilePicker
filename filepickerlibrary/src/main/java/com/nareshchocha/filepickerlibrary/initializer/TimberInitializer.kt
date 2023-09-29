package com.nareshchocha.filepickerlibrary.initializer

import android.content.Context
import androidx.startup.Initializer
import com.nareshchocha.filepickerlibrary.utilities.appConst.Const.copyFileFolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.IOException

class TimberInitializer : Initializer<Unit> {
    companion object {
        private val TAG: String = TimberInitializer::class.java.name
    }

    override fun create(context: Context) {
        // if (BuildConfig.DEBUG) {
        Timber.plant(Timber.DebugTree())
        Timber.tag(TAG).v("TimberInitializer is initialized.")
        //Timber.tag(TAG).v("Delete copy Files")
        /*GlobalScope.launch(Dispatchers.IO) {
            try {
                deleteFiles(context.cacheDir.path+"/"+ copyFileFolder)
                *//*val isDeleted = File(context.cacheDir, copyFileFolder).deleteOnExit()
                Timber.tag(TAG).e("Delete Files isDeleted:: $isDeleted")*//*
            } catch (e: Exception) {
                Timber.tag(TAG).e("Delete Files Exception :: $e")
            }
            this.cancel()
        }.start()*/
    }
    /*fun deleteFiles(path: String) {
        val file = File(path)
        if (file.exists()) {
            val deleteCmd = "rm -r $path"
            val runtime = Runtime.getRuntime()
            try {
                Timber.tag(TAG).e(" BeFear Files Size:: ${File(path).list()?.size}")
                runtime.exec(deleteCmd)
                Timber.tag(TAG).e(" After Files Size:: ${File(path).list()?.size}")
                Timber.tag(TAG).e("Delete Files isDeleted:: Done")
            } catch (e: IOException) {
                Timber.tag(TAG).e("deleteFiles Exception :: $e")
            }
        }
    }*/

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
