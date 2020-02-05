package com.boiqin.load

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build.CPU_ABI
import android.os.Build.SUPPORTED_32_BIT_ABIS
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.LOLLIPOP
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import java.io.File
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


object LoadUtils {
    private val TAG = LoadUtils::class.java.simpleName
    const val LOAD_LIBRARY_SP = "load_library_sp"
    const val LOAD_SPLIT = "|"

    const val LIBRARY_DIR = "remote_lib"

    @SuppressLint("UnsafeDynamicallyLoadedCode")
    fun loadLibrary(path: String?, libraryList: List<String>): Boolean {
        try {
            for (so in libraryList) {
                System.load(path + File.separator + so)
            }
        } catch (e: UnsatisfiedLinkError) {
            e.printStackTrace()
            return false
        }
        return true
    }

    fun showLoading(context: Context?) {
        Handler(Looper.getMainLooper()).post {
            //LoadingWhiteUtils.show(context, "加载中...", false, null);
        }
    }

    fun showLoading(context: Context?, handler: Handler) {
        handler.post {
            // LoadingWhiteUtils.show(context, "加载中...", false, null);
        }
    }

    fun showLoading(context: Context?, msg: String?) {
        Handler(Looper.getMainLooper()).post {
            // LoadingWhiteUtils.show(context, msg, false, null);
        }
    }

    fun showLoading(
        context: Context?,
        handler: Handler,
        msg: String?
    ) {
        handler.post {
            //LoadingWhiteUtils.show(context, msg, false, null);
        }
    }

    fun hideLoading() {
        Handler(Looper.getMainLooper()).post {
            //LoadingWhiteUtils.dismiss();
        }
    }

    fun hideLoading(handler: Handler) {
        handler.post {
            //LoadingWhiteUtils.dismiss();
        }
    }

    fun getSpString(context: Context, key: String): String {
        val sharedPreferences = context.getSharedPreferences(LOAD_LIBRARY_SP, Context.MODE_PRIVATE)
        return sharedPreferences.getString(key, "") ?: ""
    }

    fun saveSpString(context: Context, key: String, value: String) {
        val sharedPreferences = context.getSharedPreferences(LOAD_LIBRARY_SP, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun listDirectory(dir: File): ArrayList<String> {
        require(dir.exists()) { "$dir not exists" }
        require(dir.isDirectory) { "$dir is not directory" }
        val fileNameList = ArrayList<String>()
        val files = dir.listFiles()
        if (files != null && files.isNotEmpty()) {
            for (file in files) {
                if (!file.isDirectory) {
                    fileNameList.add(file.name)
                }
            }
        }
        return fileNameList
    }

    fun isARMv7Compatible(): Boolean {
        try {
            if (SDK_INT >= LOLLIPOP) {
                for (abi in SUPPORTED_32_BIT_ABIS) {
                    if (abi == "armeabi-v7a") {
                        return true
                    }
                }
            } else {
                if (CPU_ABI == "armeabi-v7a" || CPU_ABI == "arm64-v8a") {
                    return true
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return false
    }

    fun md5(string: String?): String {
        if (TextUtils.isEmpty(string)) {
            return ""
        }
        val md5: MessageDigest?
        try {
            md5 = MessageDigest.getInstance("MD5")
            val bytes: ByteArray = md5.digest(string!!.toByteArray())
            val result = StringBuilder()
            for (b in bytes) {
                var temp = Integer.toHexString(b.toInt() and 0xff)
                if (temp.length == 1) {
                    temp = "0$temp"
                }
                result.append(temp)
            }
            return result.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }
}