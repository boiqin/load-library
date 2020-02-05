package com.boiqin.load

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.boiqin.loaddemo.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.btn).setOnClickListener {
            val loadConfig = LoadConfig.Builder()
                .setRemoteLibraryPath("http://173.199.119.32/wp-content/uploads/2020/02/armeabi-v7a.zip")
                .build()
            LoadCore.init(this, loadConfig)
            LoadCore.start(object :
                LoadCallback {
                override fun onSuccuss() {
                    Toast.makeText(this@MainActivity, "加载成功", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(errorCode: Int, errorMsg: String) {
                    Toast.makeText(this@MainActivity, "加载失败", Toast.LENGTH_SHORT).show()
                }

            })
        }
    }
}
