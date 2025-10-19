package com.example.htmlviewer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.htmlviewer.databinding.ActivityWebBinding
import com.example.htmlviewer.model.AppItem

class WebActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityWebBinding
    private lateinit var appItem: AppItem
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
        
        setupFullScreen()
        setupWebView()
        setupFloatingButtons()
        loadHtmlFile()
    }
    
    private fun setupFullScreen() {
        // Hide action bar for full screen experience
        supportActionBar?.hide()
        
        // Set status bar color to match content
        window.statusBarColor = getColor(R.color.transparent)
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
                }
                
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    binding.progressBar.visibility = android.view.View.GONE
                }
            }
            
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    binding.progressBar.progress = newProgress
                }
            }
            
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                allowContentAccess = true
            }
        }
    }
    
    private fun setupFloatingButtons() {
        // Setup back button
        binding.backButton.setOnClickListener {
            onBackPressed()
        }
        
        // Setup favorite button
        updateFabIcon()
        binding.fabFavorite.setOnClickListener {
            toggleFavorite()
        }
    }
    
    private fun loadHtmlFile() {
        val htmlFile = "file:///android_asset/${appItem.getHtmlFileName()}"
        binding.webView.loadUrl(htmlFile)
    }
    
    private fun toggleFavorite() {
        isFavorite = !isFavorite
        appItem.isFavorite = isFavorite
        updateFabIcon()
        
        // Save to SharedPreferences
        val prefs = getSharedPreferences("favorites", Context.MODE_PRIVATE)
        prefs.edit().putBoolean(appItem.appName, isFavorite).apply()
    }
    
    private fun updateFabIcon() {
        val iconText = if (isFavorite) "💖" else "🤍"
        binding.favoriteIcon.text = iconText
    }
    
    private fun loadFavoriteStatus() {
        val prefs = getSharedPreferences("favorites", Context.MODE_PRIVATE)
        isFavorite = prefs.getBoolean(appItem.appName, false)
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