package com.example.kotlinmaps.room.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.kotlinmaps.model.place

@Database(entities = [place::class], version = 1)
abstract class placeDatabase : RoomDatabase() {
    abstract fun placeDao(): placeDao
}