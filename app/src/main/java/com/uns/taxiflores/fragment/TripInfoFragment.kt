package com.uns.taxiflores.fragment

import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.uns.taxiflores.R
import com.uns.taxiflores.databinding.FragmentTripInfoBinding


class TripInfoFragment : Fragment() ,OnMapReadyCallback, Listener{
    private var _binding: FragmentTripInfoBinding? = null
    private val binding get() = _binding!!
    private var googleMap: GoogleMap? = null
    private var easyWayLocation: EasyWayLocation? = null

    private var extraOrigin = ""
    private var extraDestination = ""
    private var extraOrigin_lat :Double= 0.0
    private var extraOrigin_lng :Double= 0.0
    private var extraDestination_lat :Double= 0.0
    private var extraDestination_lng :Double= 0.0

    private var originLatLng: LatLng? = null
    private var destinationLatLng: LatLng? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTripInfoBinding.inflate(inflater, container, false)

        val bundle = this.arguments
        if (bundle != null) {
            extraOrigin = bundle.getString("origin").toString()
            extraDestination = bundle.getString("destination").toString()
            extraOrigin_lat = bundle.getDouble("origin_lat")
            extraOrigin_lng = bundle.getDouble("origin_lng")
            extraDestination_lat = bundle.getDouble("destination_lat")
            extraDestination_lng = bundle.getDouble("destination_lng")
            originLatLng = LatLng(extraOrigin_lat,extraOrigin_lng)
            destinationLatLng = LatLng(extraDestination_lat,extraDestination_lng)
        }else{
            Toast.makeText(context, "Error we", Toast.LENGTH_SHORT).show()
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val locationRequest = LocationRequest.create().apply{
            interval = 0
            fastestInterval = 0
            priority = Priority.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 1f
        }
        easyWayLocation = EasyWayLocation(context, locationRequest, false, false, this)

        binding.textViewOrigin.text = extraOrigin
        binding.textViewDestination.text = extraDestination


        binding.imageViewBack.setOnClickListener{ activity?.finish() }



    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true

        try {
            val success = googleMap?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.style)
            )
            if (!success!!) {
                Log.d("MAPAS", "No se pudo encontrar el estilo")
            }
        } catch (e: Resources.NotFoundException) {
            Log.d("MAPAS", "Erro: ${e.toString()}")
        }
    }

    override fun locationOn() {

    }

    override fun currentLocation(location: Location?) {

    }

    override fun locationCancelled() {

    }

    override fun onDestroy() {//CUANDO CERRAMOS LA APP O PASAMOS A OTRA ACTIVYTI
        super.onDestroy()
        easyWayLocation?.endUpdates();
    }
}