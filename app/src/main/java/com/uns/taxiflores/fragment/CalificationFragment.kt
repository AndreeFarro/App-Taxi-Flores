package com.uns.taxiflores.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.uns.taxiflores.databinding.FragmentCalificationBinding
import com.uns.taxiflores.models.History
import com.uns.taxiflores.providers.HistoryProvider

class CalificationFragment : Fragment() {

    private var _binding: FragmentCalificationBinding? = null
    private val binding get() = _binding!!

    private var historyProvider = HistoryProvider()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCalificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getHistory()
    }

    private fun getHistory(){
        historyProvider.getLastHistory().get().addOnSuccessListener { query ->
            if (query != null){
                if (query.documents.size > 0){
                    val document = query.documents[0].toObject(History::class.java)
                    Log.d("FIRESTORE","HISTORIAL: $document")
                }

            }
        }
    }

}