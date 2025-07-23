package com.example.htmlviewer.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.htmlviewer.WebActivity
import com.example.htmlviewer.adapter.AppAdapter
import com.example.htmlviewer.databinding.FragmentProfileBinding
import com.example.htmlviewer.model.AppItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

class ProfileFragment : Fragment() {
    
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var favoriteAdapter: AppAdapter
    private val favoriteList = mutableListOf<AppItem>()
    
    companion object {
        fun newInstance() = ProfileFragment()
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUserInfo()
        setupFavoriteRecyclerView()
        loadFavoriteApps()
    }
    
    private fun setupUserInfo() {
        binding.tvUserName.text = "HTML应用爱好者"
    }
    
    private fun setupFavoriteRecyclerView() {
        favoriteAdapter = AppAdapter(favoriteList) { appItem ->
            openWebActivity(appItem)
        }
        
        binding.recyclerViewFavorites.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = favoriteAdapter
        }
    }
    
    private fun loadFavoriteApps() {
        try {
            val jsonString = loadJSONFromAsset(requireContext(), "apps.json")
            if (jsonString != null) {
                val gson = Gson()
                val listType = object : TypeToken<List<AppItem>>() {}.type
                val apps: List<AppItem> = gson.fromJson(jsonString, listType)
                
                // Filter favorite apps
                val prefs = requireContext().getSharedPreferences("favorites", Context.MODE_PRIVATE)
                val favorites = apps.filter { app ->
                    prefs.getBoolean(app.appName, false)
                }.map { app ->
                    app.copy(isFavorite = true)
                }
                
                favoriteList.clear()
                favoriteList.addAll(favorites)
                favoriteAdapter.notifyDataSetChanged()
                
                updateFavoriteView()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            updateFavoriteView()
        }
    }
    
    private fun updateFavoriteView() {
        if (favoriteList.isEmpty()) {
            binding.tvNoFavorites.visibility = View.VISIBLE
            binding.recyclerViewFavorites.visibility = View.GONE
        } else {
            binding.tvNoFavorites.visibility = View.GONE
            binding.recyclerViewFavorites.visibility = View.VISIBLE
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
    
    private fun openWebActivity(appItem: AppItem) {
        val intent = WebActivity.newIntent(requireContext(), appItem)
        startActivity(intent)
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh the favorite list when returning to this fragment
        loadFavoriteApps()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}