package com.uns.taxiflores.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.SphericalUtil
import com.uns.taxiflores.R
import com.uns.taxiflores.databinding.FragmentMapBinding
import com.uns.taxiflores.models.DriverLocation
import com.uns.taxiflores.providers.AuthProvider
import com.uns.taxiflores.providers.GeoProvider
import com.uns.taxiflores.utils.CarMoveAnim
import org.imperiumlabs.geofirestore.callbacks.GeoQueryEventListener


class MapFragment : Fragment(), OnMapReadyCallback, Listener {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private var googleMap: GoogleMap? = null
    private var easyWayLocation: EasyWayLocation? = null
    private var myLocationLatLng: LatLng? = null
    private val geoProvider= GeoProvider()
    private val authProvider= AuthProvider()

    //VARIABLES PARA GOOGLE PLACES
    private var places: PlacesClient? = null
    private var autocompleteOrigin: AutocompleteSupportFragment? = null
    private var autocompleteDestination: AutocompleteSupportFragment? = null
    private var originName= ""
    private var destinationName= ""
    private var originLatLng: LatLng? = null
    private var destinationLatLng: LatLng? = null


    private var isLocationEnabled = false

    private val driverMarkers = ArrayList<Marker>()
    private val driversLocation = ArrayList<DriverLocation>()

    private var mapView: MapView? = null



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)

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
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))

        startGooglePlaces()

        binding.bntRequestTrip.setOnClickListener { goToTripInfo() }
    }




    val locationPermissions =registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
            permission ->
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            when{
                permission.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false)->{
                    Log.d("LOCALIZACION","Permiso Concedido")
                    easyWayLocation?.startLocation();
                }
                permission.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false)->{
                    Log.d("LOCALIZACION","Permiso Concedido con Limitación")
                    easyWayLocation?.startLocation();
                }
                else ->{
                    Log.d("LOCALIZACION","Permiso no Concedido")
                }
            }
        }
    }

    private fun getNearbyDrivers(){
        if(myLocationLatLng == null) return
        geoProvider.getNearbyDrivers(myLocationLatLng!!,20.0).addGeoQueryEventListener(object :
            GeoQueryEventListener {
            override fun onKeyEntered(documentID: String, location: GeoPoint) {
                for (marker in driverMarkers){
                    if (marker.tag != null){
                        if (marker.tag == documentID){
                            return
                        }
                    }
                }
                //creamos un nuevo marcador para el conductor conectado
                val driverLatLng = LatLng(location.latitude, location.longitude)
                val marker = googleMap?.addMarker(
                    MarkerOptions().position(driverLatLng).title("Conductor disponible").icon(
                        BitmapDescriptorFactory.fromResource(R.drawable.car)
                    )
                )
                marker?.tag = documentID
                driverMarkers.add(marker!!)

                val dl = DriverLocation()
                dl.id = documentID
                driversLocation.add(dl)
            }

            override fun onKeyExited(documentID: String) {
                for (marker in driverMarkers){
                    if (marker.tag != null){
                        if (marker.tag == documentID){
                            marker.remove()
                            driverMarkers.remove(marker)
                            driversLocation.removeAt(getPositionDriver(documentID))
                            return
                        }
                    }
                }
            }

            override fun onKeyMoved(documentID: String, location: GeoPoint) {
                for (marker in driverMarkers){

                    val start = LatLng(location.latitude, location.longitude)
                    var end: LatLng? = null
                    val position = getPositionDriver(marker.tag.toString())

                    if (marker.tag != null){
                        if (marker.tag == documentID){
                            //marker.position = LatLng(location.latitude, location.longitude)
                            if (driversLocation[position].latlng != null){
                                end = driversLocation[position].latlng
                            }
                            driversLocation[position].latlng = LatLng(location.latitude, location.longitude)
                            if (end != null){
                                CarMoveAnim.carAnim(marker,end,start)
                            }

                        }
                    }
                }
            }

            override fun onGeoQueryReady() {

            }

            override fun onGeoQueryError(exception: Exception){

            }


        })
    }


    private fun goToTripInfo(){
        if (originLatLng!=null && destinationLatLng!=null){
            //findNavController().navigate(R.id.action_map_to_tripInfo)
            val bundle = Bundle()

            bundle.putString("origin", originName)
            bundle.putString("destination", destinationName)
            bundle.putDouble("origin_lat", originLatLng?.latitude!!)
            bundle.putDouble("origin_lng", originLatLng?.longitude!!)
            bundle.putDouble("destination_lat", destinationLatLng?.latitude!!)
            bundle.putDouble("destination_lng", destinationLatLng?.longitude!!)

            val tripInfoFragment = TripInfoFragment()
            tripInfoFragment.arguments = bundle
            fragmentManager?.beginTransaction()?.replace(R.id.fragment_content_main,tripInfoFragment)?.commit()
        }
        else{
            Toast.makeText(context,"Debes seleccionar el origen y el destino",Toast.LENGTH_LONG).show()
        }

    }

    private fun getPositionDriver(id: String) : Int{
        var position = 0
        for (i in driversLocation.indices){
            if (id == driversLocation[i].id){
                position = i
                break
            }
        }
        return position
    }


    private fun onCameraMove(){
        googleMap?.setOnCameraIdleListener {
            try {
                val geocoder = Geocoder(context)
                originLatLng = googleMap?.cameraPosition?.target

                if (originLatLng != null){
                    val addressList = geocoder.getFromLocation(originLatLng?.latitude!!,originLatLng?.longitude!!,1)
                    if (addressList.size > 0){
                        val location = addressList[0]

                        val city = location.locality
                        val country = location.countryName
                        val address = location.getAddressLine(0)
                        originName = "$address $city"
                        autocompleteOrigin?.setText(originName)
                    }
                }
            }catch (e: Exception){
                Log.d("MOVE", "ERROR: ${e.message}")
            }
        }
    }

    private fun startGooglePlaces(){
        if(!Places.isInitialized()){
            Places.initialize(requireContext(), resources.getString(R.string.google_maps_key))
        }

        places= Places.createClient(requireContext())
        instanceAutocompleteOrigin()
        instanceAutocompleteDestination()
    }


    private fun limitSearch(){
        val northSide = SphericalUtil.computeOffset(myLocationLatLng,5000.0,0.0 )
        val southSide = SphericalUtil.computeOffset(myLocationLatLng,5000.0,180.0 )

        autocompleteOrigin?.setLocationBias(RectangularBounds.newInstance(southSide,northSide))
        autocompleteDestination?.setLocationBias(RectangularBounds.newInstance(southSide,northSide))
    }


    private fun instanceAutocompleteOrigin(){
        autocompleteOrigin = getChildFragmentManager().findFragmentById(R.id.placesAutocompleteOrigin) as AutocompleteSupportFragment
        autocompleteOrigin?.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS,
            )
        )
        autocompleteOrigin?.setHint("Lugar de recogida")
        autocompleteOrigin?.setCountry("PE")
        autocompleteOrigin?.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                originName = place.name!!
                originLatLng = place.latLng
                Log.d("PLACES","Address: $originName")
                Log.d("PLACES","Lat: ${originLatLng?.latitude}")
                Log.d("PLACES","Lat: ${originLatLng?.longitude}")
            }

            override fun onError(p0: Status) {

            }
        })

    }
    private fun instanceAutocompleteDestination(){
        autocompleteDestination = getChildFragmentManager().findFragmentById(R.id.placesAutocompleteDestination) as AutocompleteSupportFragment
        autocompleteDestination?.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS,
            )
        )
        autocompleteDestination?.setHint("Destino")
        autocompleteDestination?.setCountry("PE")
        autocompleteDestination?.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                destinationName = place.name!!
                destinationLatLng = place.latLng
                Log.d("PLACES","Address: $destinationName")
                Log.d("PLACES","Lat: ${destinationLatLng?.latitude}")
                Log.d("PLACES","Lat: ${destinationLatLng?.longitude}")
            }

            override fun onError(p0: Status) {

            }
        })

    }



    //ejecuta cada ves que se abre la aplicacio
    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {//CUANDO CERRAMOS LA APP O PASAMOS A OTRA ACTIVYTI
        super.onDestroy()
        easyWayLocation?.endUpdates();
    }


    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true
        onCameraMove()
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
        googleMap?.isMyLocationEnabled = true



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
        myLocationLatLng = LatLng(location.latitude, location.longitude) //latitud y longitud de la posicion actual



        if (!isLocationEnabled){
            isLocationEnabled = true
            googleMap?.moveCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.builder().target(myLocationLatLng!!).zoom(15f).build()
                ))
            getNearbyDrivers()
            limitSearch()
        }
    }

    override fun locationCancelled() {

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}