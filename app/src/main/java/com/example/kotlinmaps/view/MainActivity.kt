package com.example.kotlinmaps.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.kotlinmaps.adapter.PlaceAdapter
import com.example.kotlinmaps.databinding.ActivityMainBinding
import com.example.kotlinmaps.model.place
import com.example.kotlinmaps.room.db.placeDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MainActivity : AppCompatActivity() {
    val compositeDisposable = CompositeDisposable()
    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        intentToMapActivity()

        val db = Room.databaseBuilder(applicationContext,placeDatabase::class.java,"Places").build()
        val placeDao = db.placeDao()

        compositeDisposable.addAll(
            placeDao.allget()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleRespose)
        )

    }
    fun handleRespose(placeList : List<place>){
        binding.recyclerview.layoutManager = LinearLayoutManager(this)
        val adapter = PlaceAdapter(placeList)
        binding.recyclerview.adapter  = adapter

    }

    fun intentToMapActivity(){
        binding.floattingbutton.setOnClickListener {
            val intent  = Intent(this@MainActivity, MapsActivity::class.java)
            intent.putExtra("info","new")
            startActivity(intent)
        }
    }
}