package com.uns.taxiflores.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.uns.taxiflores.R
import com.uns.taxiflores.databinding.FragmentHistoriesBinding
import com.uns.taxiflores.databinding.FragmentHistoriesDetailBinding
import com.uns.taxiflores.models.Client
import com.uns.taxiflores.models.Driver
import com.uns.taxiflores.models.History
import com.uns.taxiflores.providers.ClientProvider
import com.uns.taxiflores.providers.DriverProvider
import com.uns.taxiflores.providers.HistoryProvider
import com.uns.taxiflores.utils.RelativeTime

class HistoriesDetailFragment : Fragment() {

    private var _binding: FragmentHistoriesDetailBinding? = null
    private val binding get() = _binding!!
    private val historyProvider = HistoryProvider()
    private val driverProvider = DriverProvider()
    private var extraId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoriesDetailBinding.inflate(inflater, container, false)

        arguments.let {
            extraId = it?.getString("id").toString()
            getHistory()
        }
        binding.imageViewBack.setOnClickListener{finish()}

        return binding.root
    }

    private fun getHistory(){
        historyProvider.getHistoryById(extraId).addOnSuccessListener { document ->
            if (document.exists()){
                val history = document.toObject(History::class.java)
                binding.textViewOrigin.text = history?.origin
                binding.textViewDestination.text = history?.destination
                binding.textViewDate.text = RelativeTime.getTimeAgo(history?.timestamp!!)
                binding.textViewPrice.text = "S/.${String.format("%.1f",history.price)}"
                binding.textViewClientCalification.text = "${history.calificationToDriver}"
                binding.textViewClientCalification.text = "${history.calificationToCliente}"
                binding.textViewTimeAndDistance.text = "${history.time} Min - ${String.format("%.1f",history.km)} Km"
                driverInfo(history?.idDriver!!)
            }
        }
    }

    private fun driverInfo(id: String){
        driverProvider.getDriver(id).addOnSuccessListener{ document ->
            if (document.exists()){
                val driver = document.toObject(Driver::class.java)

                binding.textViewEmail.text = driver?.email
                binding.textViewName.text = "${driver?.name} ${driver?.lastName}"
                if (driver?.image != null){
                    Glide.with(this).load(driver?.image).into(binding.circleImageProfile)
                }
            }
        }
    }

    private fun finish(){
        findNavController().navigate(R.id.action_historiesDetailFragment_to_historiesFragment)
    }

}