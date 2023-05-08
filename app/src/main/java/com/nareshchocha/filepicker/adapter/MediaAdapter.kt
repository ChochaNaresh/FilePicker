package com.nareshchocha.filepicker.adapter

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.MediaController
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nareshchocha.filepicker.databinding.ItemMediaBinding

class MediaAdapter(
    private val mContext: Context,
    private var items: List<Uri>,
) : RecyclerView.Adapter<MediaAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder =
        ItemViewHolder(ItemMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        bind(holder, position)
    }

    private fun bind(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        val mimeTypeInfo = item.getMimeType(mContext)
        if (!mimeTypeInfo.isNullOrEmpty()) {
            if (mimeTypeInfo.contains("image")) {
                holder.binding.ivVideo.visibility = View.GONE
                holder.binding.ivImage.visibility = View.VISIBLE
                holder.binding.ivImage.load(item)
                holder.binding.ivImage.requestFocus()
                holder.binding.ivVideo.clearFocus()
            } else {
                holder.binding.ivVideo.visibility = View.VISIBLE
                holder.binding.ivImage.visibility = View.GONE
                val videoMediaController = MediaController(holder.binding.ivVideo.context)
                holder.binding.ivVideo.setVideoURI(item)
                videoMediaController.setMediaPlayer(holder.binding.ivVideo)
                holder.binding.ivVideo.setMediaController(videoMediaController)
                holder.binding.ivImage.clearFocus()
                holder.binding.ivVideo.requestFocus()
                holder.binding.ivVideo.start()
            }
        }
    }

    class ItemViewHolder(val binding: ItemMediaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemCount() = items.size
}

fun Uri.getMimeType(mContext: Context): String? {
    val cR: ContentResolver = mContext.contentResolver
    val mime = MimeTypeMap.getSingleton()
    val extension = mime.getExtensionFromMimeType(cR.getType(this))
    return mime.getMimeTypeFromExtension(extension)
}
