package com.uns.taxiflores.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.uns.taxiflores.R
import com.uns.taxiflores.models.Booking
import com.uns.taxiflores.models.Client
import com.uns.taxiflores.models.Driver
import com.uns.taxiflores.providers.*
import de.hdodenhof.circleimageview.CircleImageView

class ModalBottomSheetTripInfo: BottomSheetDialogFragment() {

    private var driver: Driver? = null
    private lateinit var booking: Booking
    val driverProvider = DriverProvider()
    val authProvider = AuthProvider()
    var textViewClientName : TextView? = null
    var textViewOrigin : TextView? = null
    var textViewDestination : TextView? = null
    var circleImageClient : CircleImageView? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        val view = inflater.inflate(R.layout.modal_bottom_sheet_trip_info,container,false)

        textViewClientName = view.findViewById(R.id.textViewClientName)
        textViewOrigin = view.findViewById(R.id.textViewOrigin)
        textViewDestination = view.findViewById(R.id.textViewDestination)
        circleImageClient = view.findViewById(R.id.circleImageClient)


        arguments.let { data ->
            booking = Booking.fromJson(data?.getString("booking")!!)!!
            textViewOrigin?.text = booking.origin
            textViewDestination?.text = booking.destination
        }


        getDriver()

        return view
    }





    @SuppressLint("SetTextI18n")
    private fun getDriver(){
        driverProvider.getDriver(booking.idDriver!!).addOnSuccessListener { document ->
            if (document.exists()){
                driver = document.toObject(Driver::class.java)
                textViewClientName?.text = "${driver?.name} ${driver?.lastName}"

                if (driver?.image != null){
                    if (driver?.image != ""){
                        Glide.with(requireContext()).load(driver?.image).into(circleImageClient!!)
                    }
                }

                // textViewUserName?.text = "${driver?.name} ${driver?.lastName}"
            }
        }
    }

    companion object{
        const val TAG = "ModalBottomSheetTripInfo"
    }



}