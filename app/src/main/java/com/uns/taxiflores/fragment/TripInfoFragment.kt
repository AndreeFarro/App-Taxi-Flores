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
import androidx.navigation.fragment.findNavController
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.example.easywaylocation.draw_path.DirectionUtil
import com.example.easywaylocation.draw_path.PolyLineDataBean
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.uns.taxiflores.R
import com.uns.taxiflores.databinding.FragmentTripInfoBinding
import com.uns.taxiflores.models.Prices
import com.uns.taxiflores.providers.ConfigProvider


class TripInfoFragment : Fragment() ,OnMapReadyCallback, Listener, DirectionUtil.DirectionCallBack{
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
    private var time :Double= 0.0
    private var distance :Double= 0.0

    private var originLatLng: LatLng? = null
    private var destinationLatLng: LatLng? = null

    private var wayPoints: ArrayList<LatLng> = ArrayList()
    private val WAY_POINT_TAG ="way_point_tag"
    private lateinit var directionUtil : DirectionUtil

    private var markerOrigin: Marker? = null
    private var markerDestination: Marker? = null

    private var configProvider = ConfigProvider()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTripInfoBinding.inflate(inflater, container, false)

        arguments?.let {
            extraOrigin = it.getString("origin").toString()
            extraDestination = it.getString("destination").toString()

            extraOrigin_lat = it.getDouble("origin_lat")
            extraOrigin_lng = it.getDouble("origin_lng")
            extraDestination_lat = it.getDouble("destination_lat")
            extraDestination_lng = it.getDouble("destination_lng")

            originLatLng = LatLng(extraOrigin_lat,extraOrigin_lng)
            destinationLatLng = LatLng(extraDestination_lat,extraDestination_lng)

            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
            mapFragment!!.getMapAsync(this)
        }

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

        binding.bntConfirmRequest.setOnClickListener {
            gotToSearchDriver()
        }


    }

    private fun gotToSearchDriver(){

        if (originLatLng!=null && destinationLatLng!=null){
            //findNavController().navigate(R.id.action_tripInfo_to_search)
            val bundle = Bundle()

            bundle.putString("origin", extraOrigin)
            bundle.putString("destination", extraDestination)
            bundle.putDouble("origin_lat", originLatLng?.latitude!!)
            bundle.putDouble("origin_lng", originLatLng?.longitude!!)
            bundle.putDouble("destination_lat", destinationLatLng?.latitude!!)
            bundle.putDouble("destination_lng", destinationLatLng?.longitude!!)
            bundle.putDouble("time", time)
            bundle.putDouble("distance", distance)

            val searchFragment = SearchFragment()
            searchFragment.arguments = bundle
            //findNavController().navigate(R.id.action_tripInfo_to_search)
            fragmentManager?.beginTransaction()?.replace(R.id.fragment_content_main,searchFragment)?.commit()

        }
        else{
            Toast.makeText(context,"Debes seleccionar el origen y el destino",Toast.LENGTH_LONG).show()
        }
    }



    private fun getPrices(distance : Double, time : Double){
        configProvider.getPrices().addOnSuccessListener { document ->
            if (document.exists()){
                val prices = document.toObject(Prices::class.java) //DOCUMENTO CON LA INFORMACION
                val totalDistance = distance * prices?.km!!
                val totalTime = time * prices.min!!
                var total =totalDistance + totalTime
                total = if (total < 5.0) prices.minValue!! else total

                val minTotal = total - prices.difference!!
                val maxTotal = total + prices.difference

                val minTotalString = String.format("%.1f",minTotal)
                val maxTotalString = String.format("%.1f",maxTotal)

                binding.textViewPrecio.text = "S/$minTotalString - S/$maxTotalString"
            }
        }
    }


    private fun addOriginMarker(){
        if (originLatLng == null) return
        markerOrigin=googleMap?.addMarker(MarkerOptions().position(originLatLng!!).title("Mi posicion")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_location_person)))
    }

    private fun addDestinationMarker(){
        if (destinationLatLng == null) return
        markerDestination=googleMap?.addMarker(MarkerOptions().position(destinationLatLng!!).title("Llegada")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_pin)))
    }



    private fun easyDrawRoute(){
        if (originLatLng== null || destinationLatLng==null) return
        wayPoints.add(originLatLng!!)
        wayPoints.add(destinationLatLng!!)
        directionUtil=DirectionUtil.Builder()
            .setDirectionKey(resources.getString(R.string.google_maps_key))
            .setOrigin(originLatLng!!)
            .setWayPoints(wayPoints)
            .setGoogleMap(googleMap!!)
            .setPolyLinePrimaryColor(R.color.back)
            .setPolyLineWidth(10)
            .setPathAnimation(true)
            .setCallback(this)
            .setDestination(destinationLatLng!!)
            .build()

        directionUtil.initPath()

    }


    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true

        if(originLatLng != null){
            googleMap?.moveCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.builder().target(originLatLng!!).zoom(14f).build()
                ))
        }

        easyDrawRoute()
        addOriginMarker()
        addDestinationMarker()

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

    override fun pathFindFinish(
        polyLineDetailsMap: HashMap<String, PolyLineDataBean>,
        polyLineDetailsArray: ArrayList<PolyLineDataBean>
    ) {
        distance = polyLineDetailsArray[1].distance.toDouble()
        time = polyLineDetailsArray[1].time.toDouble()

        distance = if(distance < 1000.0) 1000.0 else distance
        time = if(time < 60.0) 60.0 else time

        distance = distance / 1000
        time = time / 60

        val timeFormat = String.format("%.2f",time)
        val distanceFormat = String.format("%.2f",distance)

        getPrices(distance,time)

        binding.textViewTimeAndDistance.text = "$timeFormat mins\n$distanceFormat km"


        directionUtil.drawPath(WAY_POINT_TAG)
    }
}