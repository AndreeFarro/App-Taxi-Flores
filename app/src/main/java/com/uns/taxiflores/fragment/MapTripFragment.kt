package com.uns.taxiflores.fragments

import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.text.method.TextKeyListener.clear
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.google.firebase.firestore.ListenerRegistration
import com.uns.taxiflores.R
import com.uns.taxiflores.databinding.FragmentMapTripBinding
import com.uns.taxiflores.models.Booking
import com.uns.taxiflores.providers.AuthProvider
import com.uns.taxiflores.providers.BookingProvider
import com.uns.taxiflores.providers.GeoProvider
import com.uns.taxiflores.utils.CarMoveAnim


class MapTripFragment : Fragment(), OnMapReadyCallback, Listener, DirectionUtil.DirectionCallBack {

    private var listenerDriverLocation: ListenerRegistration? = null
    private var driverLocation: LatLng? = null
    private var endLatLng: LatLng? = null
    private var listenerBooking: ListenerRegistration? = null
    private var markerDestination: Marker? = null
    private var originLatLng: LatLng? = null
    private var destinationLatLng: LatLng? = null
    private var booking: Booking? = null
    private var markerOrigin: Marker? = null
    private var bookingListener: ListenerRegistration? =null
    private var _binding: FragmentMapTripBinding? = null
    private val binding get() = _binding!!

    private var googleMap: GoogleMap? = null
    var easyWayLocation: EasyWayLocation? = null
    private var myLocationLatLng: LatLng? = null
    private var markerDriver: Marker? = null
    private val geoProvider= GeoProvider()
    private val authProvider= AuthProvider()
    private val bookingProvider= BookingProvider()


    private var wayPoints: ArrayList<LatLng> = ArrayList()
    private val WAY_POINT_TAG ="way_point_tag"
    private lateinit var directionUtil : DirectionUtil

    private var isDriverLocationFound = false
    private var isBookingLoaded = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapTripBinding.inflate(inflater, container, false)

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

        locationPermissions.launch(arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ))

    }

    val locationPermissions =registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
            permission ->
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            when{
                permission.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false)->{
                    Log.d("LOCALIZACION","Permiso Concedido")

                }
                permission.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false)->{
                    Log.d("LOCALIZACION","Permiso Concedido con Limitación")

                }
                else ->{
                    Log.d("LOCALIZACION","Permiso no Concedido")
                }
            }
        }
    }

    private fun getLocationDriver(){
        if (booking != null){
            listenerDriverLocation = geoProvider.getLocationWorking(booking?.idDriver!!).addSnapshotListener{document, e ->
                if (e != null){
                    Log.d("FIRESTORE","ERROR: ${e.message}")
                    return@addSnapshotListener
                }

                if(document?.get("l") == null) return@addSnapshotListener


                if (driverLocation != null){
                    endLatLng = driverLocation
                }

                if (document?.exists()!!){
                    var l = document?.get("l") as List<*>
                    val lat = l[0] as Double
                    val lng = l[1] as Double


                    driverLocation = LatLng(lat,lng)
                    //markerDriver?.remove()

                    if (!isDriverLocationFound){
                        isDriverLocationFound=true
                        addDriverMarker(driverLocation!!)
                        easyDrawRoute(driverLocation!!,originLatLng!!)
                    }

                    if (endLatLng != null){
                        CarMoveAnim.carAnim(markerDriver!!, endLatLng!!,driverLocation!!)
                    }


                    Log.d("FIRESTORE","Location $l")
                }

            }
        }
    }

   private fun getBooking(){
        listenerBooking = bookingProvider.getBooking().addSnapshotListener{ document, e ->
            if (e != null){
                Log.d("FIRESTORE","ERROR: ${e.message}")
                return@addSnapshotListener
            }

            booking = document?.toObject(Booking::class.java)
            if(booking == null) return@addSnapshotListener

            if(!isBookingLoaded){
                isBookingLoaded =true
                originLatLng = LatLng(booking?.originLat!!,booking?.originLng!!)
                destinationLatLng = LatLng(booking?.destinationLat!!,booking?.destinationLng!!)
                googleMap?.moveCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.builder().target(originLatLng!!).zoom(17f).build()
                    )
                )
                getLocationDriver()
                addOriginMarker(originLatLng!!)
            }


            when(booking?.status){
                "accept" -> binding.textViewStatus.text = "Aceptado"
                "started" -> startedTrip()
                "finished" -> finishedTrip()
            }


        }
    }

    private fun finishedTrip(){
        listenerDriverLocation?.remove()
        binding.textViewStatus.text ="Finalizado"
        findNavController().navigate(R.id.action_mapTripFragment_to_calificationFragment)
    }

    private fun startedTrip(){
        googleMap?.clear()
        if(driverLocation != null){
            addDriverMarker(driverLocation!!)
            addDestinationMarker()
            easyDrawRoute(driverLocation!!,destinationLatLng!!)
        }
        binding.textViewStatus.text = "Iniciado"
    }

    private fun addOriginMarker(position: LatLng){
        markerOrigin=googleMap?.addMarker(MarkerOptions().position(position).title("Recoger aqui!")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_location_person)))
    }

    private fun addDriverMarker(position: LatLng){
        markerDriver=googleMap?.addMarker(MarkerOptions().position(position).title("Tu conductor!")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)))
    }

    private fun addDestinationMarker(){
        if (destinationLatLng != null){
            markerDestination=googleMap?.addMarker(MarkerOptions().position(destinationLatLng!!).title("Recoger aqui!")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icons_pin24)))
        }
    }

    private fun easyDrawRoute(originLatLng: LatLng, destinationLatLng: LatLng){

        wayPoints.clear()
        wayPoints.add(originLatLng)
        wayPoints.add(destinationLatLng)

        directionUtil=DirectionUtil.Builder()
            .setDirectionKey(resources.getString(R.string.google_maps_key))
            .setOrigin(originLatLng)
            .setWayPoints(wayPoints)
            .setGoogleMap(googleMap!!)
            .setPolyLinePrimaryColor(R.color.back)
            .setPolyLineWidth(10)
            .setPathAnimation(true)
            .setCallback(this)
            .setDestination(destinationLatLng)
            .build()

        directionUtil.initPath()

    }


    //insercion de drawable
    private fun getMarkerFromDrawable(drawable: Drawable): BitmapDescriptor{
        val canvas = Canvas()

        val size = 1

        val width = 65 * size
        val heigh = 100 * size

        val bitmap = Bitmap.createBitmap(
            width,
            heigh,
            Bitmap.Config.ARGB_8888
        )
        canvas.setBitmap(bitmap)
        drawable.setBounds(0,0,width,heigh)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }


    override fun onDestroy() {//CUANDO CERRAMOS LA APP O PASAMOS A OTRA ACTIVYTI
        super.onDestroy()
        listenerBooking?.remove()
        listenerDriverLocation?.remove()
    }


    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true

        getBooking()
        //easyWayLocation?.startLocation();

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        googleMap?.isMyLocationEnabled = false

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

    /**
     * Actualizacion de la ubicacion en tiempo real
     */
    override fun currentLocation(location: Location) {

    }

    override fun locationCancelled() {

    }

    override fun pathFindFinish(
        polyLineDetailsMap: HashMap<String, PolyLineDataBean>,
        polyLineDetailsArray: ArrayList<PolyLineDataBean>
    ) = directionUtil.drawPath(WAY_POINT_TAG)
}