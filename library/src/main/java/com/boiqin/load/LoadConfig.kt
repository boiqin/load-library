package com.boiqin.load

class LoadConfig private constructor() {
    var mLibraryPath //so存放路径
            : String? = null
    var mRemoteLibraryPath // 服务器端so下载地址
            : String? = null

    private fun setLibraryPath(libraryPath: String?) {
        mLibraryPath = libraryPath
    }

    private fun setRemoteLibraryPath(remoteLibraryPath: String?) {
        mRemoteLibraryPath = remoteLibraryPath
    }

    class Builder {
        private var mLibraryPath: String = ""
        private var mRemoteLibraryPath: String = ""
        fun setLibraryPath(libraryPath: String): Builder {
            mLibraryPath = libraryPath
            return this
        }

        fun setRemoteLibraryPath(remoteLibraryPath: String): Builder {
            mRemoteLibraryPath = remoteLibraryPath
            return this
        }

        fun build(): LoadConfig {
            val config = LoadConfig()
            config.setLibraryPath(mLibraryPath)
            config.setRemoteLibraryPath(mRemoteLibraryPath)
            return config
        }
    }
}