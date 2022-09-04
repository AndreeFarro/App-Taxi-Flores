package com.uns.taxiflores.fragment

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.uns.taxiflores.R
import com.uns.taxiflores.models.Booking
import com.uns.taxiflores.models.Client
import com.uns.taxiflores.providers.AuthProvider
import com.uns.taxiflores.providers.BookingProvider
import com.uns.taxiflores.providers.ClientProvider
import com.uns.taxiflores.providers.GeoProvider

class ModalBottomSheetMenu: BottomSheetDialogFragment() {

    val clientProvider = ClientProvider()
    val authProvider = AuthProvider()

    var textViewUserName : TextView? = null
    var linearLayoutLogout : LinearLayout? = null
    var linearLayoutProfile : LinearLayout? = null
    var linearLayoutHistory : LinearLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        val view = inflater.inflate(R.layout.modal_bottom_sheet_menu,container,false)
        textViewUserName = view.findViewById(R.id.textViewUserName)
        linearLayoutLogout = view.findViewById(R.id.linearLayoutLogout)
        linearLayoutProfile = view.findViewById(R.id.linearLayoutProfile)
        linearLayoutHistory = view.findViewById(R.id.linearLayoutHistory)

        getClient()
        linearLayoutLogout?.setOnClickListener{goToMain()}
        linearLayoutProfile?.setOnClickListener { goMapToProfile() }
        linearLayoutHistory?.setOnClickListener { goToHistories()}

        return view
    }


    private fun goProfileToMap(){
        findNavController().navigate(R.id.action_profileFragment_to_map)
    }

    private fun goMapToProfile(){
        findNavController().navigate(R.id.action_map_to_profileFragment)
    }

    private fun goToHistories(){
        findNavController().navigate(R.id.action_map_to_historiesFragment)
    }

    private fun goToMain(){
        authProvider.logout()
        findNavController().navigate(R.id.action_map_to_login)

    }



    private fun getClient(){
        clientProvider.getClientById(authProvider.getId()).addOnSuccessListener { document ->
            if (document.exists()){
                val client = document.toObject(Client::class.java)
                textViewUserName?.text = "${client?.name} ${client?.lastName}"
            }
        }
    }

    companion object{
        const val TAG = "ModalBottomSheetMenu"
    }



}