package com.example.htmlviewer.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.htmlviewer.WebActivity
import com.example.htmlviewer.adapter.AppAdapter
import com.example.htmlviewer.databinding.FragmentHallBinding
import com.example.htmlviewer.model.AppItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

class HallFragment : Fragment() {
    
    private var _binding: FragmentHallBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var appAdapter: AppAdapter
    private val appList = mutableListOf<AppItem>()
    
    companion object {
        fun newInstance() = HallFragment()
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHallBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupSwipeRefresh()
        loadAppsData()
    }
    
    private fun setupRecyclerView() {
        appAdapter = AppAdapter(appList) { appItem ->
            openWebActivity(appItem)
        }
        
        binding.recyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = appAdapter
        }
    }
    
    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            loadAppsData()
        }
    }
    
    private fun loadAppsData() {
        try {
            val jsonString = loadJSONFromAsset(requireContext(), "apps.json")
            if (jsonString != null) {
                val gson = Gson()
                val listType = object : TypeToken<List<AppItem>>() {}.type
                val apps: List<AppItem> = gson.fromJson(jsonString, listType)
                
                // Load favorite status for each app
                val prefs = requireContext().getSharedPreferences("favorites", Context.MODE_PRIVATE)
                apps.forEach { app ->
                    app.isFavorite = prefs.getBoolean(app.appName, false)
                }
                
                appList.clear()
                appList.addAll(apps)
                appAdapter.notifyDataSetChanged()
                
                binding.emptyView.visibility = if (appList.isEmpty()) View.VISIBLE else View.GONE
            } else {
                showError("无法加载应用数据")
            }
        } catch (e: Exception) {
            showError("加载应用数据失败: ${e.message}")
        } finally {
            binding.swipeRefresh.isRefreshing = false
        }
    }
    
    private fun loadJSONFromAsset(context: Context, fileName: String): String? {
        return try {
            val inputStream = context.assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        }
    }
    
    private fun showError(message: String) {
        binding.emptyView.visibility = View.VISIBLE
        // Note: emptyView is now a LinearLayout, not TextView
        // The error message is handled by the layout itself
    }
    
    private fun openWebActivity(appItem: AppItem) {
        val intent = WebActivity.newIntent(requireContext(), appItem)
        startActivity(intent)
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh the list to update favorite status
        loadAppsData()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}