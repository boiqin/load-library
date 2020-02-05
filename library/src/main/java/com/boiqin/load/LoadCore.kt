package com.boiqin.load

import android.content.Context
import android.text.TextUtils
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

object LoadCore {
    private var mLoadConfig: LoadConfig? = null
    private var mContext: Context? = null
    private var mInitedLibraryList: List<String>? = null

    fun init(context: Context, loadConfig: LoadConfig) {
        mLoadConfig = loadConfig
        mContext = context.applicationContext
        mInitedLibraryList = ArrayList()
    }

    fun getContext(): Context? {
        return mContext
    }

    fun start(loadCallback: LoadCallback) {
        require(mLoadConfig != null) { "Please initialize LoadLibrary LoadConfig first" }
        require(mContext != null) { "Please initialize LoadLibrary Context first" }

        try {
            if (LoadUtils.isARMv7Compatible()) {
                //
                // 查看本地是否有so
                mLoadConfig?.run {
                    val remoteLibraryPathMd5 =
                        LoadUtils.md5(mRemoteLibraryPath)
                    if (TextUtils.isEmpty(mLibraryPath)) {
                        // 使用默认地址
                        mLibraryPath =
                            "${mContext!!.filesDir.absolutePath}/${LoadUtils.LIBRARY_DIR}"
                    }

                    val libraryDirectory = File(mLibraryPath)
                    if (libraryDirectory.exists() && libraryDirectory.isDirectory) {
                        // 查看本地文件是否完整，因为可能有多个so文件
                        val libraryFileList =
                            LoadUtils.getSpString(
                                mContext!!,
                                remoteLibraryPathMd5
                            )
                        val cacheLibraryFileNameList =
                            LoadUtils.listDirectory(
                                libraryDirectory
                            )
                        if (libraryFileList.isNotBlank()) {
                            val libraryFileNameList = libraryFileList.split(LoadUtils.LOAD_SPLIT)
                            if (cacheLibraryFileNameList.containsAll(libraryFileNameList)) {
                                // 已经包含了所有所需的so文件,直接加载
                                val success =
                                    LoadUtils.loadLibrary(
                                        mLibraryPath,
                                        libraryFileNameList
                                    )
                                if (success) {
                                    loadCallback.onSuccuss()
                                } else {
                                    loadCallback.onFailure(
                                        LoadErrorCode.LOAD_FAILURE,
                                        "load failure"
                                    )
                                }
                            } else {
                                // 否则去下载
                                mLoadConfig?.also {
                                    downloadLibrary(
                                        it,
                                        loadCallback
                                    )
                                }
                            }
                        }

                    }
                }
                //本地有，直接加载
                //本地没有，去下载
                mLoadConfig?.also {
                    downloadLibrary(it, loadCallback)
                }


            } else {
                loadCallback.onFailure(
                    LoadErrorCode.LOAD_NOT_SUPPORT,
                    "the device not support armv7"
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            loadCallback.onFailure(LoadErrorCode.LOAD_COMMON_ERROR, "load library error")
        }

    }

    private fun downloadLibrary(loadConfig: LoadConfig, loadCallback: LoadCallback) {
        DownloadHelper.download(
            loadConfig.mRemoteLibraryPath,
            loadConfig.mLibraryPath,
            DownloadHelper.getNameFromUrl(loadConfig.mRemoteLibraryPath),
            object : DownloadHelper.OnDownloadListener {
                override fun onDownloadSuccess() {
                    // 获取要下载文件名称
                    val zipFileName =
                        DownloadHelper.getNameFromUrl(loadConfig.mRemoteLibraryPath)
                    if (TextUtils.isEmpty(zipFileName)) {
                        // loadCallback.onFailure()
                        return
                    }
                    var unzip = false
                    try {
                        unzip = FileUtils.zipUncompress(
                            "${mContext!!.filesDir.absolutePath}/${LoadUtils.LIBRARY_DIR}/$zipFileName",
                            "${mContext!!.filesDir.absolutePath}/${LoadUtils.LIBRARY_DIR}"
                        )
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    if (unzip) {
                        // 加载并记录
                        val libraryFileNameList =
                            LoadUtils.listDirectory(
                                File(loadConfig.mLibraryPath)
                            )
                        val success = LoadUtils.loadLibrary(
                            loadConfig.mLibraryPath,
                            libraryFileNameList
                        )
                        if (success) {
                            val remoteLibraryPathMd5 =
                                LoadUtils.md5(loadConfig.mRemoteLibraryPath)
                            LoadUtils.saveSpString(
                                mContext!!,
                                remoteLibraryPathMd5,
                                libraryFileNameList.joinToString(separator = LoadUtils.LOAD_SPLIT) { i -> i })

                            loadCallback.onSuccuss()
                        } else {
                            loadCallback.onFailure(
                                LoadErrorCode.LOAD_FAILURE,
                                "load failure"
                            )
                        }
//                    val file = File("${mContext!!.getExternalFilesDir("")!!.absolutePath}/download/$zipFileName")
//                    if (file.exists()) {
//                        file.delete()
//                    }

                    }

                }

                override fun onDownloading(progress: Int) {
                }

                override fun onDownloadFailed() {
                    // 下载失败，清除已下载

                }

            })
    }
}