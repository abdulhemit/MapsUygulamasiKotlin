package com.example.kotlinmaps.view

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.kotlinmaps.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.kotlinmaps.databinding.ActivityMapsBinding
import com.example.kotlinmaps.model.place
import com.example.kotlinmaps.room.db.placeDao
import com.example.kotlinmaps.room.db.placeDatabase
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableCompletableObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.schedulers.Schedulers.io

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,GoogleMap.OnMapLongClickListener{

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager : LocationManager
    private lateinit var locationListener : LocationListener
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var sharedPreferences: SharedPreferences
    var trackBoolean : Boolean? = null
    private var selectedLatitude : Double? = null
    private var selectedLongitude : Double? = null
    private lateinit var db : placeDatabase
    private lateinit var placeDao: placeDao
    val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        registerLauncher()
        sharedPreferences = this.getSharedPreferences("com.example.kotlinmaps", MODE_PRIVATE)
        trackBoolean = false
        db = Room.databaseBuilder(applicationContext,placeDatabase::class.java,"Places",)
            //.allowMainThreadQueries()
            .build()
        placeDao = db.placeDao()

        binding.buttonSave.setOnClickListener {
            save()
        }
        binding.buttonDelete.setOnClickListener {
            delete()
        }

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(this)
        binding.buttonSave.isEnabled = false
        val intent = intent
        val info = intent.getStringExtra("info")
        if (info.equals("new")){

            binding.buttonSave.visibility = View.VISIBLE
            binding.buttonDelete.visibility = View.GONE

            locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager

            locationListener = object : LocationListener{
                override fun onLocationChanged(location: Location) {
                    trackBoolean = sharedPreferences.getBoolean("trackBoolean",false)
                    if (!trackBoolean!!){
                        val userLocation = LatLng(location.latitude,location.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15f))
                        sharedPreferences.edit().putBoolean("trackBoolean",true).apply()
                    }

                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                    super.onStatusChanged(provider, status, extras)
                }

            }

            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(this@MapsActivity,Manifest.permission.ACCESS_FINE_LOCATION)){
                    Snackbar.make(binding.root,"Permission Needed for location ",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission"){
                        // request Permission
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }.show()
                }else{
                    // request Permission
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }else{
                // Permission granted
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0F,locationListener)
                val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastLocation != null){
                    val alLastLocation = LatLng(lastLocation.latitude,lastLocation.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(alLastLocation,15f))
                }
                // benim konumumu etkinlestirdik mi ?
                mMap.isMyLocationEnabled = true
            }
        }else{
            binding.buttonDelete.visibility = View.VISIBLE
            binding.buttonSave.visibility = View.GONE
            val placeFromMainActivity = intent.getSerializableExtra("selectPlaces") as? place
            placeFromMainActivity?.let {
                val LatLng = LatLng(it.latitude,it.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng,15f))
                mMap.addMarker(MarkerOptions().position(LatLng).title(it.name))
                binding.placeText.setText(it.name)
            }


        }



    }


    private fun registerLauncher(){
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result ->
            if (result){
                // permission granted
                if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0F,locationListener)

                    val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (lastLocation != null){
                        val alLastLocation = LatLng(lastLocation.latitude,lastLocation.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(alLastLocation,15f))

                    }
                    // benim konumumu etkinlestirdikmi ?
                    mMap.isMyLocationEnabled = true
                }


            }else{
                Toast.makeText(this@MapsActivity,"Permession Needed!",Toast.LENGTH_LONG).show()
            }
        }

    }

    override fun onMapLongClick(location: LatLng) {
        mMap.clear()

        binding.buttonSave.isEnabled = true
        mMap.addMarker(MarkerOptions().position(location))
        selectedLatitude = location.latitude
        selectedLongitude = location.longitude

    }
    fun save (){
        println("save")
        if (selectedLatitude != null && selectedLongitude != null){
            println("this save")
            val place = place(binding.placeText.text.toString(),selectedLatitude!!,selectedLongitude!!)
            placeDao.insert(place)
            compositeDisposable.addAll(
                placeDao.insert(place)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleRespose)
            )
        }

    }
    fun handleRespose(){
        val intent = Intent(this@MapsActivity,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
    fun delete(){
        val placeFromMainActivity = intent.getSerializableExtra("selectPlaces") as? place
        placeFromMainActivity?.let {
            //val deletePlace= place(it.name,it.latitude,it.longitude)
            compositeDisposable.addAll(
                placeDao.delete(it)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleRespose)
            )
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}



/*
      // Lat 37.1850219,Log 79.8936868
      val Lat = 37.1850219
      val Log = 79.8936868
      val hoten = LatLng(Lat,Log)
      mMap.addMarker(MarkerOptions().position(hoten).title("Yurtum Hoten"))
      mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hoten,13f))

       */