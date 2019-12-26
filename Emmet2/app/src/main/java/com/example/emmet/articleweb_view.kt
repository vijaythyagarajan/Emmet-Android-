package com.example.emmet

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient

class articleweb_view : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_articleweb_view)

        webView = findViewById(R.id.webview)

        var link = intent.getStringExtra("url")

        val webSettings = webView.settings
        webSettings.javaScriptEnabled = false
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url)
                return true
            }
        }
        webView.loadUrl(link)
        WebView.setWebContentsDebuggingEnabled(false)
    }


}
