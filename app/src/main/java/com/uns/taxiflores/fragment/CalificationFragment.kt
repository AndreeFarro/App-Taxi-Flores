package com.uns.taxiflores.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.uns.taxiflores.R
import com.uns.taxiflores.databinding.FragmentCalificationBinding
import com.uns.taxiflores.models.History
import com.uns.taxiflores.providers.HistoryProvider

class CalificationFragment : Fragment() {

    private var _binding: FragmentCalificationBinding? = null
    private val binding get() = _binding!!

    private var historyProvider = HistoryProvider()
    private var history: History? = null
    private var calification = 0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCalificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCalfication.setOnClickListener{
            if(history?.id != null){
                calification = binding.ratinBar.numStars.toFloat()
                updateCalificationDriver(history?.id!!)

            }else{
                Toast.makeText(context, "el id del historial es nulo", Toast.LENGTH_SHORT).show()
            }

        }
        binding.ratinBar.setOnRatingBarChangeListener { ratingBar, value, b ->
            calification = value
        }
        getHistory()
    }

    private fun getHistory(){
        historyProvider.getLastHistory().get().addOnSuccessListener { query ->
            if (query != null){
                if (query.documents.size > 0){
                    history = query.documents[0].toObject(History::class.java)
                    history?.id = query.documents[0].id

                    binding.textViewOrigin.text = history?.origin
                    binding.textViewDestination.text = history?.destination
                    binding.textViewPrice.text = format(history?.price!!)
                    binding.textViewTimeAndDistance.text = "${history?.time} Min - ${format(history?.km!!)}"
                    Log.d("FIRESTORE","HISTORIAL: $history")
                }

            }
        }
    }

    private fun updateCalificationDriver(id: String){
        historyProvider.updateCalificationToDriver(id, calification).addOnCompleteListener{
            if (it.isSuccessful){
                gotoMap()
            }else{
                Toast.makeText(context, "Error al actualziar calificacion", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun gotoMap(){
        findNavController().navigate(R.id.action_calificationFragment_to_map)

        //val fragmentManager: FragmentManager = requireFragmentManager()
        //this will clear the back stack and displays no animation on the screen
        //this will clear the back stack and displays no animation on the screen
        //fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }
    private fun format(value : Double): String{
        return String.format("%.1f",value)
    }

}