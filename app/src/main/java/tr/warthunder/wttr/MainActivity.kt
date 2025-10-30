
package tr.warthunder.wttr

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.content.Intent
import android.provider.MediaStore
import android.os.Build
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.core.view.isVisible

class MainActivity : ComponentActivity() {

    override fun onNewIntent(intent: android.content.Intent?) {
        super.onNewIntent(intent)
        val url = intent?.getStringExtra("open_url")
        if (url != null && this::webView.isInitialized) {
            webView.loadUrl(url)
        }
    }

    private lateinit var webView: WebView
    private var filePathCallback: ValueCallback<Array<android.net.Uri>>? = null
    private var cameraPhotoUri: android.net.Uri? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        NotificationHelper.createChannel(this)
        RssScheduler.schedule(this)

        val swipe = findViewById<SwipeRefreshLayout>(R.id.swipe)
        webView = findViewById(R.id.webview)
        val progress = findViewById<View>(R.id.progress)

        val settings: WebSettings = webView.settings
        CookieManager.getInstance().setAcceptCookie(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
            settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.allowContentAccess = true
        settings.allowFileAccess = true
        settings.mediaPlaybackRequiresUserGesture = false
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.userAgentString = settings.userAgentString + " WTTR-App/1.0"

        swipe.setOnRefreshListener { webView.reload() }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?, fileChooserParams: FileChooserParams?): Boolean {
                this@MainActivity.filePathCallback = filePathCallback
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                var photoUri: Uri? = null
                try {
                    val photoFile = kotlin.runCatching {
                        java.io.File.createTempFile("wttr_cam_", ".jpg", cacheDir)
                    }.getOrNull()
                    if (photoFile != null) {
                        photoUri = androidx.core.content.FileProvider.getUriForFile(
                            this@MainActivity,
                            applicationContext.packageName + ".fileprovider",
                            photoFile
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    }
                } catch (_: Exception) {}
                cameraPhotoUri = photoUri

                val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
                contentSelectionIntent.type = "image/*"
                contentSelectionIntent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))

                val intentArray = if (takePictureIntent.resolveActivity(packageManager) != null) arrayOf(takePictureIntent) else arrayOf()
                val chooser = Intent(Intent.ACTION_CHOOSER)
                chooser.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                chooser.putExtra(Intent.EXTRA_TITLE, "Dosya seç")
                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
                startActivityForResult(chooser, 1001)
                return true
            }
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progress.isVisible = newProgress < 100
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url ?: return false
                return handleUrl(url)
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return url?.let { handleUrl(Uri.parse(it)) } ?: false
            }

            private fun handleUrl(uri: Uri): Boolean {
                val host = uri.host ?: return false
                // Stay inside main WebView for warthunder.tr, otherwise open our in-app browser
                return if (host.endsWith("warthunder.tr")) {
                    false
                } else {
                    InAppBrowserActivity.open(this@MainActivity, uri.toString())
                    true
                }
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                progress.isVisible = true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                progress.isVisible = false
            }
        }

        if (savedInstanceState == null) {
            val target = intent?.getStringExtra("open_url")
            if (target != null) {
                webView.loadUrl(target)
            } else {
                webView.loadUrl("https://warthunder.tr/")
            }
        } else {
            webView.restoreState(savedInstanceState)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001) {
            val callback = filePathCallback
            filePathCallback = null
            if (callback == null) return
            val results: Array<Uri>? = when {
                resultCode == RESULT_OK && data != null && data.data != null -> arrayOf(data.data!!)
                resultCode == RESULT_OK && cameraPhotoUri != null -> arrayOf(cameraPhotoUri!!)
                else -> null
            }
            callback.onReceiveValue(results)
        }
    }

    override fun onBackPressed() {
        if (this::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
