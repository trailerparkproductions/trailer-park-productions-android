package net.trailerparkproductions.app

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var errorView: LinearLayout

    private val siteHost = "trailerparkproductions.net"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        progressBar = findViewById(R.id.progressBar)
        errorView = findViewById(R.id.errorView)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress = newProgress
                progressBar.visibility = if (newProgress in 1..99) View.VISIBLE else View.GONE
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url ?: return false
                return handleUrl(url)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                errorView.visibility = View.GONE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                swipeRefresh.isRefreshing = false
                // Hide the website's own header/nav inside the app — the bottom tabs are the only navigation.
                view?.evaluateJavascript(
                    "var s=document.createElement('style');s.innerHTML='header.topbar{display:none!important}';document.head.appendChild(s);",
                    null
                )
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                if (request?.isForMainFrame == true) {
                    swipeRefresh.isRefreshing = false
                    errorView.visibility = View.VISIBLE
                }
            }
        }

        swipeRefresh.setOnRefreshListener { webView.reload() }

        findViewById<Button>(R.id.retryButton).setOnClickListener {
            errorView.visibility = View.GONE
            webView.reload()
        }

        findViewById<ImageButton>(R.id.shareButton).setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "https://trailerparkproductions.net")
            }
            startActivity(Intent.createChooser(intent, "Share Trailer Park Productions"))
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            val url = when (item.itemId) {
                R.id.nav_home -> "https://trailerparkproductions.net"
                R.id.nav_stories -> "https://trailerparkproductions.net/#stories"
                R.id.nav_media -> "https://trailerparkproductions.net/#media"
                R.id.nav_podcast -> "https://trailerparkproductions.net/#podcast"
                R.id.nav_store -> "https://trailerparkproductions.net/#store"
                else -> "https://trailerparkproductions.net"
            }
            webView.loadUrl(url)
            true
        }

        if (savedInstanceState == null) {
            webView.loadUrl("https://trailerparkproductions.net")
        } else {
            webView.restoreState(savedInstanceState)
        }
    }

    private fun handleUrl(url: Uri): Boolean {
        val scheme = url.scheme ?: return false
        if (scheme == "mailto") {
            startActivity(Intent(Intent.ACTION_SENDTO, url))
            return true
        }
        if (scheme == "tel") {
            startActivity(Intent(Intent.ACTION_DIAL, url))
            return true
        }
        val host = url.host ?: return false
        if (!host.endsWith(siteHost)) {
            startActivity(Intent(Intent.ACTION_VIEW, url))
            return true
        }
        return false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
}
