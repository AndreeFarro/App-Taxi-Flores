package com.uns.taxiflores.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.uns.taxiflores.databinding.FragmentSearchBinding
import com.uns.taxiflores.models.Booking
import com.uns.taxiflores.providers.AuthProvider
import com.uns.taxiflores.providers.BookingProvider
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
    private var extraTime :Double= 0.0
    private var extraDistance :Double= 0.0

    private var originLatLng: LatLng? = null
    private var destinationLatLng: LatLng? = null

    private val geoProvider = GeoProvider()
    private val authProvider = AuthProvider()
    private var radius = 0.1
    private var idDriver =""
    private var isDriverFound=false
    private var driverLatLng: LatLng? = null
    private var limitRadius = 20

    private val bookingProvider = BookingProvider()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        arguments?.let {
            extraOrigin = it.getString("origin").toString()
            extraDestination = it.getString("destination").toString()

            extraOrigin_lat = it.getDouble("origin_lat")
            extraOrigin_lng = it.getDouble("origin_lng")
            extraDestination_lat = it.getDouble("destination_lat")
            extraDestination_lng = it.getDouble("destination_lng")
            extraTime = it.getDouble("time")
            extraDistance = it.getDouble("distance")

            originLatLng = LatLng(extraOrigin_lat, extraOrigin_lng)
            destinationLatLng = LatLng(extraDestination_lat, extraDestination_lng)
            getClosestDriver()
        }

        return binding.root


    }

    private fun createBooking(idDriver: String){
        val booking = Booking(
        idClient = authProvider.getId(),
            idDriver = idDriver,
            origin = extraOrigin,
            destination = extraDestination,
            status = "create",
            time = extraTime,
            km = extraDistance,
            originLat = extraOrigin_lat,
            originLng = extraOrigin_lat,
            destinationLat = extraDestination_lat,
            destinationLng = extraDestination_lng
        )
        bookingProvider.create(booking).addOnCompleteListener{
            if (it.isSuccessful){
                Toast.makeText(context, "Datos del viaje creados", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(context, "error al traer datos", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun getClosestDriver(){
        if (originLatLng == null) return
        geoProvider.getNearbyDrivers(originLatLng!!,radius).addGeoQueryEventListener(object : GeoQueryEventListener{
            override fun onKeyEntered(documentID: String, location: GeoPoint) {
                if(!isDriverFound){
                    isDriverFound = true
                    idDriver = documentID
                    Log.d("FIRESTORE","Conductor id: $idDriver")
                    driverLatLng = LatLng(location.latitude,location.longitude)
                    binding.textViewSearch.text = "CONDUCTOR ENCONTRADO\nESPERANDO RESPUESTA"
                    createBooking(documentID)
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