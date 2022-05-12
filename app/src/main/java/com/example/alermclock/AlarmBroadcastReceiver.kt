package com.example.alermclock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class AlarmBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, p1: Intent?) {

        val intnet = Intent(context,MainActivity::class.java)
            .putExtra("OnReceive",true)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context?.startActivity(intnet)
    }
}