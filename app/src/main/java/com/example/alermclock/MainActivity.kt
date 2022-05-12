package com.example.alermclock

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.core.os.HandlerCompat
import com.example.alermclock.databinding.ActivityMainBinding
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.concurrent.timer

class MainActivity : AppCompatActivity(),DatePickerFragment.OnDateSlectedListener, TimePickerFragment.OnTimeSelectedListener,
AlertDialogFlagment.OnAlertListener{

    private lateinit var binding: ActivityMainBinding
    private lateinit var realm: Realm

    var alermDate = ""
    private var alarmType : Int? = null
    private lateinit var realmAlarm: RealmAlarm
    private lateinit var mediaPlayer: MediaPlayer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent?.getBooleanExtra("OnReceive",false) == true){

//            when {
//                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ->
//                    window.addFlags(WindowManager.LayoutParams.ftu)
//            }

            mediaPlayer = MediaPlayer.create(this,R.raw.kimiga)
            mediaPlayer.isLooping = true
            mediaPlayer.start()

            intent.putExtra("OnReceive",false)
            AlertDialogFlagment().show(supportFragmentManager,"アラーム")

        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        realm = Realm.getDefaultInstance()



        //アラームデータを作成

        val id = 0
        var realmAlarm2 = realm.where<RealmAlarm>().equalTo("id",id).findFirst()
        if (realmAlarm2 == null) realmAlarm2 = realm.createObject(id)
        realmAlarm = realmAlarm2

        binding.apply {

            //時間表示

            val handler = HandlerCompat.createAsync(mainLooper)

            kotlin.concurrent.timer(period = 15000){
                handler.post{
                    val date = LocalDateTime.now()
                    var fomatter = DateTimeFormatter.ofPattern("yyyy/MM/dd E曜日")
                    textDate.text = fomatter.format(date)
                    fomatter = DateTimeFormatter.ofPattern("HH:mm")
                    textTime.text = fomatter.format(date)
                }
            }

            //日付アラームボタン

            //リスナー
            buttonDateAlarm.setOnClickListener {
                alarmType = 0
                DatePickerFragment().show(supportFragmentManager,"日付アラームセット")
            }

            //時間アラームボタン

            //リスナー
            buttonTimeAlarm.setOnClickListener {
                alarmType = 1
                TimePickerFragment().show(supportFragmentManager,"時間アラームセット")
            }

            //アラームスイッチ

            //アプリ立ち上げ時のスイッチの位置
            if(realmAlarm.alarmSet) switchAlarm.isChecked = true

            //リスナー
            switchAlarm.setOnCheckedChangeListener { button, isChecked ->

                //スイッチの状態を保存
                realm.executeTransaction{
                    realmAlarm.alarmSet = isChecked
                }

                //アラームスイッチONの時
                if (isChecked) checkAlarmSwich()

                //アラームスイッチOFFの時
                else{
                    val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val intent = Intent(this@MainActivity,AlarmBroadcastReceiver::class.java)
                    val pending = PendingIntent.getBroadcast(this@MainActivity,0,intent,0)
                    am.cancel(pending)
                    Toast.makeText(this@MainActivity,"アラームをキャンセルしました", Toast.LENGTH_LONG).show()
                }
            }
        }

        showTextAlarm()

    }

    ///////////////////////////////////////////////////////////////////////////////////////
    //アラームスイッチチェック

    fun checkAlarmSwich(){

        val calendar = Calendar.getInstance()

        realmAlarm.let {

            //アラームスイッチONの時
            if (it.alarmSet){

                when(it.alarmType){

                    //データが無い時(一度もアラームを設定したことがない初期状態)
                    null ->  Toast.makeText(this@MainActivity,"アラーム時刻がセットされてません", Toast.LENGTH_LONG).show()

                    //日付アラーム
                    0 -> {
                        calendar.set(it.year,it.month,it.day,it.hour,it.minute)
                        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        val intent = Intent(this@MainActivity,AlarmBroadcastReceiver::class.java)
                        val pending = PendingIntent.getBroadcast(this@MainActivity,0,intent,0)
                        val info = AlarmManager.AlarmClockInfo(calendar.timeInMillis,null)
                        am.setAlarmClock(info,pending)
                        Toast.makeText(this@MainActivity,"日付アラーム設定しました", Toast.LENGTH_LONG).show()
                    }

                    //時間アラーム
                    else -> {
                        calendar.apply {
                            timeInMillis = System.currentTimeMillis()
                            set(Calendar.HOUR_OF_DAY,it.hour)
                            set(Calendar.MINUTE, it.minute)
                        }
                        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        val intent = Intent(this,AlarmBroadcastReceiver::class.java)
                        val pending = PendingIntent.getBroadcast(this,0,intent,0)
                        am.setInexactRepeating(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            AlarmManager.INTERVAL_DAY,
                            pending
                        )

                        Toast.makeText(this@MainActivity,"時間アラーム設定しました", Toast.LENGTH_LONG).show()

                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    //設定したアラーム時刻の表示

    fun showTextAlarm(){

        binding.apply {

            val calendar = Calendar.getInstance()

            realmAlarm.let {

                when (it.alarmType){
                    //データなし
                    null -> {
                        textSetAlarm.text = ""
                    }
                    //日付アラーム
                    0 -> {
                        calendar.set(it.year,it.month,it.day,it.hour,it.minute)
                        textSetAlarm.text = "${android.text.format.DateFormat.format("yyyy/MM/dd E HH:mm",calendar)}"
                    }
                    //時間アラーム
                    1 ->{
                        val alarmTime =  "%02d:%02d".format(it.hour,it.minute)
                        textSetAlarm.text = "毎日 $alarmTime"
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    //日付アラームボタン処理

    override fun onSelectedClick(year: Int, month: Int, day: Int) {

        realm.executeTransaction {

            //日付をアラームデータに保存
            realmAlarm.let{
                it.year = year
                it.month = month
                it.day = day
            }
        }

        TimePickerFragment().show(supportFragmentManager,"時間セット")
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    //時間アラームボタン処理

    override fun onSelectedClick(hour: Int, minute: Int) {

        realm.executeTransaction {

            //時間とアラームタイプをアラームデータに保存
            realmAlarm.let{
                it.alarmType = alarmType
                it.hour = hour
                it.minute = minute
            }
        }

        checkAlarmSwich()
        showTextAlarm()
    }

    ///////////////////////////////////////////////////////////////////////////////////////

    override fun onDestroy() {
        super.onDestroy()

        if(mediaPlayer?.isPlaying)
            mediaPlayer.stop()
        mediaPlayer?.release()
        realm.close()
       // onPositiveClick()
    }

    override fun onPositiveClick() {

        if(mediaPlayer?.isPlaying)
            mediaPlayer.stop()
        mediaPlayer?.release()
    }
}