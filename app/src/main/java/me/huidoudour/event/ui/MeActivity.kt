package me.huidoudour.event.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import me.huidoudour.event.R
import me.huidoudour.event.utils.LocaleHelper

class MeActivity : AppCompatActivity() {
    
    override fun attachBaseContext(newBase: Context?) {
        // 应用语言设置
        super.attachBaseContext(LocaleHelper.applyLanguage(newBase!!))
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_me)
        
        // 设置状态栏样式以适配白色背景
        setupStatusBar()
        
        // 网站按钮点击事件
        val btnWebsite = findViewById<Button>(R.id.btn_website)
        btnWebsite.setOnClickListener {
            val url = "https://github.com/huidoudour"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
    }
    
    private fun setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 及以上版本
            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
            windowInsetsController.isAppearanceLightStatusBars = true // 设置状态栏文字为深色
        } else {
            // Android 10 及以下版本
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }
}