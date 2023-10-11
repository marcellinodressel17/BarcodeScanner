package com.ridwanharts.qrcodebarcodescanner

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_scan_result.view.*

class ResultAdapter(
    private val context: Context,
    private val listResultScan: ArrayList<ResultScan>,
    private val listener: RecylerViewClickListener
): RecyclerView.Adapter<ResultAdapter.MyViewHolder>() {

    private var newListSize: Int? = null

    class MyViewHolder(val view: View): RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_scan_result, parent, false)
        )
    }

    override fun getItemCount(): Int = listResultScan.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val item = listResultScan[position]
        newListSize = listResultScan.size
        holder.view.tv_title.text = item.title
        holder.view.tv_date.text = item.date
        holder.view.tv_result.text = item.result

        listener.onRecylerViewItemClick(holder.view, item, position)

        if (item.title == "LINK"){
            holder.view.iv_link.visibility = View.VISIBLE
            holder.view.iv_text.visibility = View.INVISIBLE
        }else{
            holder.view.iv_link.visibility = View.INVISIBLE
            holder.view.iv_text.visibility = View.VISIBLE
        }

    }

    fun updateList(position: Int){
        notifyDataSetChanged()
        listResultScan.removeAt(position)
        notifyItemRemoved(position)
    }
}