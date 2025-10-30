
package tr.warthunder.wttr

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity

class InAppBrowserActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_URL = "url"
        fun open(context: Context, url: String) {
            val i = Intent(context, InAppBrowserActivity::class.java)
            i.putExtra(EXTRA_URL, url)
            context.startActivity(i)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inapp_browser)

        val url = intent.getStringExtra(EXTRA_URL) ?: "about:blank"
        val titleView = findViewById<TextView>(R.id.title)
        val btnClose = findViewById<ImageButton>(R.id.btnClose)
        val btnExternal = findViewById<ImageButton>(R.id.btnOpenExternal)
        val wv = findViewById<WebView>(R.id.inappWebView)

        wv.settings.javaScriptEnabled = true
        wv.settings.domStorageEnabled = true

        wv.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                titleView.text = view?.title ?: ""
            }
        }
        wv.webChromeClient = WebChromeClient()

        wv.loadUrl(url)

        btnClose.setOnClickListener { finish() }
        btnExternal.setOnClickListener {
            val external = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(external)
        }
    }
}
