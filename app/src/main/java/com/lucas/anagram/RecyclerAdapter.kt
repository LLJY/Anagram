package com.lucas.anagram

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recycler_item.view.*

class RecyclerAdapter(val items : ArrayList<String>, val context: Context) : RecyclerView.Adapter<ViewHolder>(){
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder?.anagram.text = items.get(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

}
class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    // Holds the TextView that will add each animal to
    val anagram = view.Anagram
}
