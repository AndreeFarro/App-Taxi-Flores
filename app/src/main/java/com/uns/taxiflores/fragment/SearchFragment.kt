package com.uns.taxiflores.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.uns.taxiflores.R
import com.uns.taxiflores.databinding.FragmentSearchBinding
import com.uns.taxiflores.databinding.FragmentTripInfoBinding
import com.uns.taxiflores.providers.AuthProvider
import com.uns.taxiflores.providers.GeoProvider
import org.imperiumlabs.geofirestore.callbacks.GeoQueryEventListener

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private var extraOrigin = ""
    private var extraDestination = ""
    private var extraOrigin_lat :Double= 0.0
    private var extraOrigin_lng :Double= 0.0
    private var extraDestination_lat :Double= 0.0
    private var extraDestination_lng :Double= 0.0

    private var originLatLng: LatLng? = null
    private var destinationLatLng: LatLng? = null

    private val geoProvider = GeoProvider()
    private val authProvider = AuthProvider()
    private var radius = 0.1
    private var idDriver =""
    private var isDriverFound=false
    private var driverLatLng: LatLng? = null
    private var limitRadius = 20

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        val bundle = this.arguments
        if (bundle != null) {
            extraOrigin = bundle.getString("origin").toString()
            extraDestination = bundle.getString("destination").toString()

            extraOrigin_lat = bundle.getDouble("origin_lat")
            extraOrigin_lng = bundle.getDouble("origin_lng")
            extraDestination_lat = bundle.getDouble("destination_lat")
            extraDestination_lng = bundle.getDouble("destination_lng")

            originLatLng = LatLng(extraOrigin_lat, extraOrigin_lng)
            destinationLatLng = LatLng(extraDestination_lat, extraDestination_lng)
            getClosestDriver()
        }
        return binding.root


    }

    private fun getClosestDriver(){
        geoProvider.getNearbyDrivers(originLatLng!!,radius).addGeoQueryEventListener(object : GeoQueryEventListener{
            override fun onKeyEntered(documentID: String, location: GeoPoint) {
                if(!isDriverFound){
                    isDriverFound = true
                    idDriver = documentID
                    Log.d("FIRESTORE","Conductor id: $idDriver")
                    driverLatLng = LatLng(location.latitude,location.longitude)
                    binding.textViewSearch.text = "CONDUCTOR ENCONTRADO\nESPERANDO RESPUESTA"
                }

            }

            override fun onKeyExited(documentID: String) {

            }

            override fun onKeyMoved(documentID: String, location: GeoPoint) {

            }

            override fun onGeoQueryError(exception: Exception) {

            }

            override fun onGeoQueryReady() {
                if (!isDriverFound){
                    radius = radius + 0.1

                    if (radius > limitRadius){
                        binding.textViewSearch.text = "NO SE ENCONTRO NINGUN CONDUCTOR"
                        return
                    }
                    else{
                        getClosestDriver()
                    }
                }

            }


        })
    }


}