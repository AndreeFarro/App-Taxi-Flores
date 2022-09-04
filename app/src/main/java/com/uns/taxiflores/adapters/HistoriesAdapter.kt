package com.uns.taxiflores.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.uns.taxiflores.R
import com.uns.taxiflores.models.History
import com.uns.taxiflores.utils.RelativeTime

class HistoriesAdapter(val context : Fragment, val histories: ArrayList<History>):RecyclerView.Adapter<HistoriesAdapter.HistoriesAdapterViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoriesAdapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardview_history,parent,false)
        return HistoriesAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoriesAdapterViewHolder, position: Int) {
        val history = histories[position]
        holder.textViewOrigin.text = history.origin
        holder.textViewDestination.text = history.destination
        if (history.timestamp != null){
            holder.textViewDate.text = RelativeTime.getTimeAgo(history.timestamp!!)
        }
        holder.itemView.setOnClickListener{goToDetail(history?.id!!)}

    }

    private fun goToDetail(idHistory: String){
        val bundle = Bundle()
        bundle.putString("id", idHistory)
        context.findNavController().navigate(R.id.action_historiesFragment_to_historiesDetailFragment,bundle)
    }

    //TAMAÑO DE LA LISTA
    override fun getItemCount(): Int {
        return histories.size
    }

    class HistoriesAdapterViewHolder(view : View): RecyclerView.ViewHolder(view){
        val textViewOrigin: TextView
        val textViewDestination: TextView
        val textViewDate : TextView

        init {
            textViewOrigin = view.findViewById(R.id.textViewOrigin)
            textViewDestination = view.findViewById(R.id.textViewDestination)
            textViewDate = view.findViewById(R.id.textViewDate)
        }
    }


}