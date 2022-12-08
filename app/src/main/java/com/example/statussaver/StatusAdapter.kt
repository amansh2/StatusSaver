package com.example.statussaver

import android.app.Application
import android.content.Context
import android.location.GnssAntennaInfo
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Glide.with
import com.example.statussaver.databinding.GridItemBinding

class StatusAdapter(private val listener:ButtonClicked, val context: Context):
    RecyclerView.Adapter<StatusAdapter.StatusViewHolder>() {

     val myList=ArrayList<Status>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
        val binding:GridItemBinding=DataBindingUtil.inflate(LayoutInflater.from(parent.context),R.layout.grid_item, parent, false)
        return StatusViewHolder(binding)

    }

    override fun onBindViewHolder(holder: StatusViewHolder, position: Int) {

        Glide.with(context).load(myList[position].fileUri).placeholder(android.R.drawable.progress_indeterminate_horizontal).error(android.R.drawable.stat_notify_error).into(holder.binding.listImage)
        holder.binding.listImage.setOnClickListener {
            listener.imageClicked(myList[position])
        }
        if(myList[position].video){
            holder.binding.video.visibility= View.VISIBLE
        }


    }

    override fun getItemCount(): Int {
       return myList.size
    }

    fun updateList(list:ArrayList<Status>){
        myList.clear()
        myList.addAll(list)
        notifyDataSetChanged()

    }

    class StatusViewHolder( val binding:GridItemBinding): RecyclerView.ViewHolder(binding.root) {


    }


    interface ButtonClicked {
        fun imageClicked(status: Status)

    }
}


