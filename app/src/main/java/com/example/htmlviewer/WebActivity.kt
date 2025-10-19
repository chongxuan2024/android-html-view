package com.example.htmlviewer

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.htmlviewer.data.FavoritesManager
import com.example.htmlviewer.data.UserStatsManager
import com.example.htmlviewer.databinding.ActivityWebBinding
import com.example.htmlviewer.model.AppItem
import com.example.htmlviewer.webview.GameJavaScriptInterface

class WebActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityWebBinding
    private lateinit var appItem: AppItem
    private lateinit var favoritesManager: FavoritesManager
    private lateinit var userStatsManager: UserStatsManager
    private lateinit var gameJSInterface: GameJavaScriptInterface
    private var isFavorite = false
    
    companion object {
        private const val EXTRA_APP_ITEM = "extra_app_item"
        
        fun newIntent(context: Context, appItem: AppItem): Intent {
            return Intent(context, WebActivity::class.java).apply {
                putExtra(EXTRA_APP_ITEM, appItem)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        appItem = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_APP_ITEM, AppItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_APP_ITEM)
        } ?: run {
            finish()
            return
        }
        
        // 初始化FavoritesManager和UserStatsManager
        favoritesManager = FavoritesManager.getInstance(this)
        userStatsManager = UserStatsManager.getInstance(this)
        
        // 创建JavaScript接口
        gameJSInterface = GameJavaScriptInterface(this, appItem.appName)
        
        // 记录应用打开
        userStatsManager.recordAppOpen(appItem.appName)
        
        setupFullScreen()
        setupWebView()
        setupFloatingButtons()
        loadHtmlFile()
    }
    
    private fun setupFullScreen() {
        // Hide action bar for full screen experience
        supportActionBar?.hide()
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Hide status bar and navigation bar for immersive experience
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.apply {
            // Hide system bars
            hide(WindowInsetsCompat.Type.systemBars())
            // Set behavior for when user swipes to show system bars
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        
        // Set status bar and navigation bar to transparent
        window.statusBarColor = getColor(android.R.color.transparent)
        window.navigationBarColor = getColor(android.R.color.transparent)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = appItem.getDisplayName()
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }
    
    private fun setupWebView() {
        binding.webView.apply {
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    binding.progressBar.visibility = android.view.View.VISIBLE
                    Log.d("WebActivity", "页面开始加载: $url")
                }
                
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    binding.progressBar.visibility = android.view.View.GONE
                    Log.d("WebActivity", "页面加载完成: $url")
                    
                    // 页面加载完成后，测试JavaScript接口
//                    testJavaScriptInterface()
                }
                
                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                    super.onReceivedError(view, request, error)
                    Log.e("WebActivity", "WebView加载错误: ${error?.description}")
                }
            }
            
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    binding.progressBar.progress = newProgress
                }
                
                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                    consoleMessage?.let {
                        Log.d("WebView-Console", "[${it.messageLevel()}] ${it.message()} (${it.sourceId()}:${it.lineNumber()})")
                    }
                    return true
                }
            }
            
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                
                // 添加调试支持
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    android.webkit.WebView.setWebContentsDebuggingEnabled(true)
                }
            }
            
            // 注册JavaScript接口
            addJavascriptInterface(gameJSInterface, GameJavaScriptInterface.INTERFACE_NAME)
            Log.d("WebActivity", "JavaScript接口已注册: ${GameJavaScriptInterface.INTERFACE_NAME}")
        }
    }
    
    private fun setupFloatingButtons() {
        // Setup back button with icon options
        setupBackIcon()
        binding.backButton.setOnClickListener {
            onBackPressed()
        }
        
        // Setup favorite button
        updateFabIcon()
        binding.fabFavorite.setOnClickListener {
            toggleFavorite()
        }
    }
    
    private fun setupBackIcon() {
        // You can change this to any of these options:
        val backIcons = listOf(
            "↩️",  // Current: Curved arrow (recommended)
            "⬅️",  // Left arrow emoji
            "◀",   // Solid left triangle
            "←",   // Simple left arrow
            "🔙",  // Back emoji
            "⤴️",  // Up-left arrow
            "↖️"   // Up-left diagonal arrow
        )
        
        // Use the first icon (↩️) - you can change the index to try different icons
        binding.backIcon.text = backIcons[0]
    }
    
    private fun loadHtmlFile() {
        val htmlFile = "file:///android_asset/${appItem.getHtmlFileName()}"
        binding.webView.loadUrl(htmlFile)
    }
    
    private fun toggleFavorite() {
        isFavorite = !isFavorite
        appItem.isFavorite = isFavorite
        updateFabIcon()
        
        // Save using FavoritesManager for persistent storage
        favoritesManager.setFavorite(appItem.appName, isFavorite)
    }
    
    private fun updateFabIcon() {
        val iconText = if (isFavorite) "💖" else "🤍"
        binding.favoriteIcon.text = iconText
    }
    
    private fun loadFavoriteStatus() {
        // Load using FavoritesManager for persistent storage
        isFavorite = favoritesManager.isFavorite(appItem.appName)
        appItem.isFavorite = isFavorite
        updateFabIcon()
    }

    
    override fun onResume() {
        super.onResume()
        loadFavoriteStatus()
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}