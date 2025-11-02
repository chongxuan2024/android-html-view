package com.example.htmlviewer.adapter

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.htmlviewer.databinding.ItemVoiceRecordBinding
import com.example.htmlviewer.model.VoiceRecord

class VoiceRecordAdapter(
    private val onPlayClick: (VoiceRecord) -> Unit,
    private val onDeleteClick: (VoiceRecord) -> Unit
) : ListAdapter<VoiceRecord, VoiceRecordAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemVoiceRecordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemVoiceRecordBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(record: VoiceRecord) {
            binding.tvEmotionTag.text = record.emotionTag
            binding.tvDuration.text = formatDuration(record.duration)
            binding.tvTimestamp.text = formatTimestamp(record.timestamp)
            binding.tvTranscription.text = record.transcription

            binding.btnPlay.setOnClickListener {
                onPlayClick(record)
            }

            binding.btnDelete.setOnClickListener {
                onDeleteClick(record)
            }
        }

        private fun formatDuration(millis: Long): String {
            val seconds = (millis / 1000) % 60
            val minutes = (millis / (1000 * 60)) % 60
            return String.format("%d:%02d", minutes, seconds)
        }

        private fun formatTimestamp(timestamp: Long): String {
            return DateUtils.getRelativeTimeSpanString(
                timestamp,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            ).toString()
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<VoiceRecord>() {
        override fun areItemsTheSame(oldItem: VoiceRecord, newItem: VoiceRecord): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: VoiceRecord, newItem: VoiceRecord): Boolean {
            return oldItem == newItem
        }
    }
}

