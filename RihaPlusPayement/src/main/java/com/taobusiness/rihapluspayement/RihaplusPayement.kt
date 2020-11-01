package com.taobusiness.rihapluspayement

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.ViewGroup
import android.webkit.*
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.airbnb.lottie.LottieAnimationView

class RihaplusPayement(): DialogFragment() {

    val TAG = "RechargeDialog"

    private lateinit var id: String
    private lateinit var montant: String
    private var page: String = "https://rihaplus.com"

    interface OnPayementListener{
        fun onFinished()
    }

    private lateinit var onPayementListener: OnPayementListener

    fun setId(id: String){
        this.id = id
    }

    fun setMontant(montant: String){
        this.montant = montant
    }

    fun setUrlPage(page: String){
        this.page = page
    }

    private lateinit var webView: WebView
    private lateinit var progressBar: LottieAnimationView

    private var url = "https://api.rihaplus.com/Api_rihaplus"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v: View = inflater.inflate(R.layout.dialog_payement, container, false)

        dialog!!.window!!.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.CENTER)
        dialog!!.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog!!.window!!.setBackgroundDrawableResource(R.color.transparent)
        dialog!!.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        retainInstance = true

        webView = v.findViewById(R.id.webView)
        progressBar = v.findViewById(R.id.pbLoader)

        var savedInstanceState = savedInstanceState
        if (savedInstanceState == null || savedInstanceState.isEmpty) {
            savedInstanceState = WorkaroundSavedState.savedInstanceState
        }

        Log.d(
            "TAG",
            "saved instance state oncretaeview: " + WorkaroundSavedState.savedInstanceState
        )
//        return super.onCreateView(inflater!!, container, savedInstanceState)

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val url2 = "/?Identifiant=$id&montant=$montant&page=$page"
        val apiUrl = url+url2
        assert(webView != null)
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.allowFileAccess = true

        webView.isHorizontalScrollBarEnabled = false;
        webView.isVerticalScrollBarEnabled = false;
        webView.isScrollbarFadingEnabled = false

        webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        webView.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY

        webView.settings.allowFileAccess
        webView.settings.allowUniversalAccessFromFileURLs
        webView.settings.allowContentAccess = true
        webView.settings.allowFileAccess = true
        webView.settings.builtInZoomControls = true
        webView.settings.domStorageEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
        webView.settings.pluginState = WebSettings.PluginState.ON
        webView.settings.useWideViewPort = true
        webView.settings.setSupportZoom(false)
        webView.settings.setAppCacheEnabled(true)

        webView.webViewClient = OurViewClient()
        webView.webChromeClient = OurChromeClient()

        webView.clearCache(false);
        webView.loadUrl(apiUrl)
        webView.requestFocus();

        val myJavaScriptInterface = MyJavaScriptInterface(requireContext())
        webView.addJavascriptInterface(myJavaScriptInterface, "AndroidFunction")
    }

    inner class MyJavaScriptInterface internal constructor(var context: Context) {
        @JavascriptInterface
        fun showToast(toast: String?) {
            Toast.makeText(context, toast, LENGTH_SHORT).show()
            webView.loadUrl("javascript:document.getElementById(\"Button3\").innerHTML = \"bye\";")
        }

        @JavascriptInterface
        fun openAndroidDialog() {
            val myDialog = AlertDialog.Builder(context)
            myDialog.setTitle("DANGER!")
            myDialog.setMessage("You can do what you want!")
            myDialog.setPositiveButton("ON", null)
            myDialog.show()
        }

    }

    inner class OurViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            CookieManager.getInstance().setAcceptCookie(true)
            return true
        }

        override fun onReceivedError(
            view: WebView,
            errorCode: Int,
            description: String,
            failingUrl: String
        ) {
            webView.loadUrl("file:///android_asset/error.html")
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
        }
    }

    open inner class OurChromeClient: WebChromeClient() {
        override fun onProgressChanged(view: WebView, progress: Int) {
            val webUrl = webView.url
            if (webUrl.startsWith(page)) {
                webView.stopLoading()
                onPayementListener.onFinished()
                dismiss()
            }

            if (progress < 100 ) {
                progressBar.visibility = View.VISIBLE
                webView.visibility = View.INVISIBLE
            }

            if (progress == 100) {
                progressBar.visibility = View.INVISIBLE
                webView.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        if (dialog != null && retainInstance) dialog!!.setOnDismissListener(null)
        super.onDestroyView()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        var savedInstanceState = savedInstanceState

        if (savedInstanceState == null || savedInstanceState.isEmpty) {
            savedInstanceState = WorkaroundSavedState.savedInstanceState
        }
        retainInstance = true
        Log.d("TAG", "saved instance state oncreate: " + WorkaroundSavedState.savedInstanceState)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var savedInstanceState = savedInstanceState
        if (savedInstanceState == null || savedInstanceState.isEmpty) {
            savedInstanceState = WorkaroundSavedState.savedInstanceState
        }

        Log.d(
            "TAG",
            "saved instance state oncretaedialog: " + WorkaroundSavedState.savedInstanceState
        )
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // ...
        super.onSaveInstanceState(outState!!)
        WorkaroundSavedState.savedInstanceState = outState
        Log.d("TAG", "saved instance state onsaveins: " + WorkaroundSavedState.savedInstanceState)
    }

    override fun onDestroy() {
        WorkaroundSavedState.savedInstanceState = null
        super.onDestroy()
    }

    fun onFinised(onPayementListener: OnPayementListener) {
        this.onPayementListener = onPayementListener
    }

    /**
     * Static class that stores the state of the task across orientation
     * changes. There is a bug in the compatibility library, at least as of the
     * 4th revision, that causes the save state to be null in the dialog's
     * onRestoreInstanceState.
     */
    object WorkaroundSavedState {
        var savedInstanceState: Bundle? = null
    }

}