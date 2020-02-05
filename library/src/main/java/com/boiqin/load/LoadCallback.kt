package com.boiqin.load

interface LoadCallback {
    fun onSuccuss()

    fun onFailure(errorCode: Int, errorMsg: String)
}