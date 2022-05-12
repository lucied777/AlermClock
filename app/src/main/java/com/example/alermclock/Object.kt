package com.example.alermclock

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RealmAlerm : RealmObject(){
    @PrimaryKey
    var id : Int = 0
    var alermSet : Boolean = false
    var alermType : Int? = null
    var year : Int = 2022
    var month : Int = 3
    var day: Int = 19
    var hour : Int = 0
    var minute : Int = 0
}

open class RealmAlarm : RealmObject(){
    @PrimaryKey
    var id : Int = 0
    var alarmSet : Boolean = false
    var alarmType : Int? = null
    var year : Int = 2022
    var month : Int = 3
    var day: Int = 19
    var hour : Int = 0
    var minute : Int = 0
}