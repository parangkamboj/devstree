package com.devstree.product

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.devstree.product.adapter.PlacesRecyclerviewAdapter
import com.devstree.product.databinding.ActivityMainBinding
import com.devstree.product.googlePlacesApi.PlacesApiInterface
import com.devstree.product.googlePlacesApi.PlacesListClass
import com.devstree.product.googlePlacesApi.PlacesMainModel
import com.devstree.product.map.MapData
import com.devstree.product.model.AddressSuggestModel
import com.devstree.product.model.PathData
import com.devstree.product.model.TransferPathList
import com.devstree.product.roomdb.AppDataBase
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.gson.Gson
import com.rapicue.patient.googlePlacesApi.PlacesDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityMainBinding
    private lateinit var supportMapFragment: SupportMapFragment
    private var apiInterface: PlacesApiInterface? = null
    private lateinit var placesList: ArrayList<PlacesListClass>
    private lateinit var appDb: AppDataBase
    private var mMap: GoogleMap? = null
    private var marker: Marker? = null
    private var addressModel: AddressSuggestModel? = null
    private var transferPathList: TransferPathList? = null
    var LATITUDE = 0.0
    var LONGITUDE = 0.0
    var PICK_ADDRESS = ""
    var address = ""
    var addressTwo = ""
    var city = ""
    var state = ""
    var postalCode = ""
    var country = ""
    var distance = 0.00
    var userId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView(this@MainActivity, R.layout.activity_main)

        appDb = AppDataBase.getDatabase(this)

        supportMapFragment = SupportMapFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.google_map_request_ride, supportMapFragment)
            .commit()

        supportMapFragment.getMapAsync(this)

        val data = intent.getParcelableExtra<TransferPathList>("Data")

        val id = intent.getIntExtra("id", 0)
        if (id != 0) {
            userId = id
        }

        val distanceCalculateData = intent.getParcelableExtra<TransferPathList>("DistanceData")

        if (distanceCalculateData != null) {
            transferPathList = distanceCalculateData
        }

        if (data != null) {
            binding.apply {
                headerTitle.text = "Route"
                etSearch.visibility = View.GONE
            }
            setPathAndHeader(data)
        }

        placesList = arrayListOf()

        if (!Places.isInitialized()) {
            Places.initialize(
                this,
                resources.getString(R.string.API_KEY_FOR_PLACES_API)
            )
        }
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://maps.googleapis.com/maps/api/")
            .build()

        apiInterface = retrofit.create(PlacesApiInterface::class.java)

        searchPlaces()

        clickSave()

    }

    suspend fun getRoadDistance(
        originLat: Double?,
        originLong: Double?,
        destLat: Double?,
        destLong: Double?
    ): Double? {
        val url = "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=${originLat},${originLong}" +
                "&destination=${destLat},${destLong}" +
                "&key=${resources.getString(R.string.API_KEY_FOR_PLACES_API)}" // Replace with your key

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()

        val json = response.body?.string() ?: return null
        val jsonObj = JSONObject(json)
        val routes = jsonObj.getJSONArray("routes")
        if (routes.length() == 0) return null

        val legs = routes.getJSONObject(0).getJSONArray("legs")
        val distance = legs.getJSONObject(0).getJSONObject("distance").getString("text")
        val numericDistance = distance.replace("[^\\d.]".toRegex(), "").toDoubleOrNull()
        return numericDistance
    }

    private fun setPathAndHeader(data: TransferPathList) {
        binding.apply {

            supportMapFragment!!.getMapAsync { googleMap ->
                mMap = googleMap

                val builder = LatLngBounds.Builder()
                val latLngList = mutableListOf<LatLng>()

                // Your list of PathData
                val pathList: List<PathData> = data.list

                // Add markers
                for (pathData in pathList) {
                    val latLng = LatLng(pathData.lat ?: 0.0, pathData.long ?: 0.0)
                    latLngList.add(latLng)

                    val marker = mMap!!.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title(pathData.name ?: "")
                            .icon(BitmapEncoderNew(this@MainActivity, R.drawable.ic_location))
                    )
                    marker?.setAnchor(0.5f, 0.5f)
                    builder.include(latLng)
                }

                // Draw polylines between points
                if (latLngList.size > 1) {
                    for (i in 0 until latLngList.size - 1) {
                        val origin = latLngList[i]
                        val destination = latLngList[i + 1]
                        val url = getDirectionURLMap(
                            origin,
                            destination,
                            getString(R.string.API_KEY_FOR_PLACES_API)
                        )
                        GetDirection(url).execute()
                    }
                }
                // Move and zoom camera to fit all points
                val bounds = builder.build()
                val padding = 100
                mMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetDirection(val url: String) :
        AsyncTask<Void, Void, List<List<LatLng>>>() {
        override fun doInBackground(vararg params: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body!!.string()

            val result = ArrayList<List<LatLng>>()
            try {
                val respObj = Gson().fromJson(data, MapData::class.java)
                val path = ArrayList<LatLng>()
                path.clear()
                var disnt = 0.0
                var avgTime = 0.0
                for (i in 0 until respObj.routes[0].legs[0].steps.size) {
                    path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
                    disnt += respObj.routes[0].legs[0].steps[i].distance.value
                    avgTime += respObj.routes[0].legs[0].steps[i].duration.value
                }
                result.add(path)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return result
        }

        override fun onPostExecute(result: List<List<LatLng>>) {
            val lineoption = PolylineOptions()
            //   val pattern = Arrays.asList(Dot(), Gap(10f), Dash(30f), Gap(10f))
            for (i in result.indices) {
                lineoption.addAll(result[i])
                lineoption.width(10f)
                //  lineoption.pattern(pattern)
                lineoption.color(resources.getColor(R.color.black))
                lineoption.geodesic(true)
            }
            var hasPoints = false
            var maxLat: Double? = null
            var minLat: Double? = null
            var minLon: Double? = null
            var maxLon: Double? = null

            if (lineoption.getPoints() != null) {
                val pts: List<LatLng> = lineoption.getPoints()
                for (coordinate in pts) {
                    // Find out the maximum and minimum latitudes & longitudes
                    // Latitude
                    maxLat = if (maxLat != null) Math.max(
                        coordinate.latitude,
                        maxLat
                    ) else coordinate.latitude
                    minLat = if (minLat != null) Math.min(
                        coordinate.latitude,
                        minLat
                    ) else coordinate.latitude

                    // Longitude
                    maxLon = if (maxLon != null) Math.max(
                        coordinate.longitude,
                        maxLon
                    ) else coordinate.longitude
                    minLon = if (minLon != null) Math.min(
                        coordinate.longitude,
                        minLon
                    ) else coordinate.longitude
                    hasPoints = true
                }
            }

            if (hasPoints) {
                val builder = LatLngBounds.Builder()
                builder.include(LatLng(maxLat!!, maxLon!!))
                builder.include(LatLng(minLat!!, minLon!!))
                mMap!!.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100))
            }
            mMap!!.addPolyline(lineoption)
        }
    }


    fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val latLng = LatLng((lat.toDouble() / 1E5), (lng.toDouble() / 1E5))
            poly.add(latLng)
        }
        return poly
    }

    private fun getDirectionURLMap(origin: LatLng, dest: LatLng, secret: String): String {
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}" +
                "&destination=${dest.latitude},${dest.longitude}" +
                "&sensor=false" +
                "&mode=driving" +
                "&key=$secret"
    }

    private fun clickSave() {
        binding.apply {
            btnUpdate.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    val firstLat = transferPathList?.list?.get(0)?.lat
                    val firstLong = transferPathList?.list?.get(0)?.long
                    val destLat = addressModel?.latitude
                    val destLong = addressModel?.longitude

                    val distance = getRoadDistance(firstLat, firstLong, destLat, destLong)

                    if (distance != null) {
                        addressModel?.distance = distance
                    }

                    if (userId != 0) {
                        appDb.Dao().updateUser(
                            AddressSuggestModel(
                                id = userId,
                                address = addressModel?.address,
                                addressTwo = addressModel?.addressTwo,
                                city = addressModel?.city,
                                latitude = addressModel?.latitude,
                                longitude = addressModel?.longitude,
                                placeName = addressModel?.placeName,
                                state = addressModel?.state,
                                country = addressModel?.country,
                                distance = addressModel?.distance
                            )
                        )
                        withContext(Dispatchers.Main) {
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                    } else {
                        val rowId = appDb.Dao().insertUser(
                            AddressSuggestModel(
                                id = 0,
                                address = addressModel?.address,
                                addressTwo = addressModel?.addressTwo,
                                city = addressModel?.city,
                                latitude = addressModel?.latitude,
                                longitude = addressModel?.longitude,
                                placeName = addressModel?.placeName,
                                state = addressModel?.state,
                                country = addressModel?.country,
                                distance = addressModel?.distance
                            )
                        )
                        withContext(Dispatchers.Main) {
                            if (rowId > 0) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "User inserted successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                setResult(Activity.RESULT_OK)
                                finish()
                            } else {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Insert failed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                }
            }
        }
    }

    private fun searchPlaces() {
        binding.apply {
            etSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(
                    text: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    getData(text.toString())
                    updateListContainer.visibility = View.GONE
                }

                override fun afterTextChanged(s: Editable?) {
                    if (s.isNullOrEmpty()) {

                    }
                }
            })
        }
    }

    private fun getData(text: String) {
        apiInterface?.getPlace(text, getString(R.string.API_KEY_FOR_PLACES_API))
            ?.enqueue(object : Callback<PlacesMainModel?> {
                override fun onResponse(
                    call: Call<PlacesMainModel?>, response: Response<PlacesMainModel?>
                ) {
                    if (response.isSuccessful) {
                        //  binding.recyclerView.visibility = View.VISIBLE //TODO show suggestion recyclerview
                        response.body()?.predictions?.let {
                            placesList.addAll(it)
                        }

                        val linearLayout = LinearLayoutManager(
                            this@MainActivity,
                            LinearLayoutManager.VERTICAL,
                            false
                        )
                        binding.rvSearch.layoutManager = linearLayout
                        val recyclerviewAdapter =
                            response.body()?.predictions?.let {
                                PlacesRecyclerviewAdapter(
                                    it,
                                    object : PlacesRecyclerviewAdapter.OnClickListener {
                                        override fun onClick(places_id: String?) {
                                            getPlacesDetails(places_id.toString())
                                        }
                                    })
                            }
                        binding.rvSearch.adapter = recyclerviewAdapter
                    }
                }

                override fun onFailure(call: Call<PlacesMainModel?>, t: Throwable) {
                    Log.e("Failure", "onFailure: ${t.message}")
                }
            })
    }

    private fun getPlacesDetails(place_id: String) {
        apiInterface?.getDetails(getString(R.string.API_KEY_FOR_PLACES_API), place_id)
            ?.enqueue(object : Callback<PlacesDetails?> {
                override fun onResponse(
                    call: Call<PlacesDetails?>,
                    response: Response<PlacesDetails?>
                ) {
                    if (response.isSuccessful) {
                        val result = response.body()?.result
                        binding.apply {
                            var uAddress21 = ""
                            var uAddress22 = ""
                            var uAddress23 = ""
                            address = "${result?.name}"

                            LATITUDE = result?.geometry?.location?.lat!!
                            LONGITUDE = result.geometry.location.lng!!
                            PICK_ADDRESS = "${result.formatted_address}"

                            for (i in result?.address_components!!) {
                                for (postal in i.types!!) {
                                    if (postal == "postal_code") {
                                        postalCode = "${i.long_name}"
                                    } else if (postal == "locality") {
                                        city = "${i.long_name}"
                                    } else if (postal == "administrative_area_level_1") {
                                        state = "${i.long_name}"
                                    } else if (postal == "country") {
                                        country = "${i.long_name}"
                                    } else if (postal == "sublocality_level_1") {
                                        uAddress21 = "${i.long_name}"
                                    } else if (postal == "sublocality_level_2") {
                                        uAddress22 = "${i.long_name}"
                                    } else if (postal == "neighborhood") {
                                        uAddress23 = "${i.long_name}"
                                    }
                                }
                            }
                            addressTwo = "$uAddress21 $uAddress22 $uAddress23"
                            addressModel = null
                            val searchData = AddressSuggestModel(
                                id = 0,
                                address = PICK_ADDRESS,
                                addressTwo = addressTwo,
                                city = city,
                                state = state,
                                latitude = LATITUDE,
                                longitude = LONGITUDE,
                                country = country,
                                placeName = address
                            )
                            addressModel = searchData

                            val bundle = Bundle()
                            bundle.putParcelable("SearchData", searchData)

                            rvSearch.adapter = null
                            updateListContainer.visibility = View.VISIBLE

                            val originLocation =
                                LATITUDE.let { LONGITUDE.let { it1 -> LatLng(it, it1) } }
                            mMap!!.clear()

                            marker = mMap!!.addMarker(MarkerOptions().position(originLocation))
                            marker?.setAnchor(0.5f, 0.5f)
                            marker?.setIcon(
                                BitmapEncoderNew(
                                    this@MainActivity,
                                    R.drawable.ic_location
                                )
                            )

                            originLocation.let { CameraUpdateFactory.newLatLngZoom(it, 30F) }
                                ?.let { mMap!!.moveCamera(it) }


                        }
                    }
                }

                override fun onFailure(call: Call<PlacesDetails?>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "$call", Toast.LENGTH_LONG).show()
                }
            })

    }

    fun BitmapEncoderNew(
        applicationContext: Context?,
        icStartingPositionOnMap: Int
    ): BitmapDescriptor? {
        val vectorDrawable = ContextCompat.getDrawable(
            applicationContext!!, icStartingPositionOnMap
        )

        vectorDrawable!!.setBounds(
            0, 0, vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )

        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    override fun onMapReady(p0: GoogleMap) {
        val indiaLatLng = LatLng(20.5937, 78.9629) // India's center
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(indiaLatLng, 4.5f) // Zoom level

        mMap = p0

        p0.moveCamera(cameraUpdate)
        p0.uiSettings.isZoomControlsEnabled = true
    }
}