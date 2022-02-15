package edu.washington.awty

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

lateinit var alarmManager : AlarmManager
private lateinit var pendingIntent : PendingIntent
var interval : Long = 0
var btnState : String = ""
var msgState : String = ""
var phoneState : String = ""
var minState : String = ""

class IntentListener : BroadcastReceiver() {
    init {
        Log.i("BroadcastReceiver", "Created BroadcastReceiver")
    }
    override fun onReceive(p0: Context?, p1: Intent?) {
        Toast.makeText(p0, "${p1?.getStringExtra("phone")}:Are we there yet?", Toast.LENGTH_LONG).show()
        Log.i("BroadcastReceiver", "${p1?.getStringExtra("phone")}   $interval")
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, pendingIntent)
    }
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btn = findViewById<Button>(R.id.statusBtn)
        if (savedInstanceState != null) {
            btnState = savedInstanceState.getString("btn") as String
            msgState = savedInstanceState.getString("msg") as String
            phoneState = savedInstanceState.getString("phone") as String
            minState = savedInstanceState.getString("min") as String

            btn.text = btnState
            findViewById<EditText>(R.id.msgInput).setText(msgState)
            findViewById<EditText>(R.id.phoneInput).setText(phoneState)
            findViewById<EditText>(R.id.timeInput).setText(minState)
        }

        val receiver = IntentListener()
        val intFilter = IntentFilter()
        registerReceiver(receiver, intFilter)

        btn.setOnClickListener {
            val msg = findViewById<EditText>(R.id.msgInput).text
            val phone = findViewById<EditText>(R.id.phoneInput).text
            val min = findViewById<EditText>(R.id.timeInput).text

            if (btn.text == "Start") {
                if (msg.isEmpty()) {
                    Toast.makeText(this, R.string.form_alert_msg, Toast.LENGTH_SHORT).show()
                } else if (!phone.matches(("^[+]?[0-9]{10}$".toRegex()))) {
                    Toast.makeText(this, R.string.form_alert_phone, Toast.LENGTH_SHORT).show()
                } else if (min.isEmpty() || min.toString().toInt() <= 0) {
                    Toast.makeText(this, R.string.form_alert_time, Toast.LENGTH_SHORT).show()
                } else {
                    btn.text = getString(R.string.stop)

                    val formattedPhone = "(" + phone.subSequence(0, 3) + ") " + phone.subSequence(3, 6) + "-" + phone.subSequence(6,10)
                    interval = min.toString().toLong() * 1000 * 60
                    Log.i("BroadcastReceiver", interval.toString())

                    intFilter.addAction(formattedPhone)
                    Log.i("BroadcastReceiver", "Set intent")
                    alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val intent = Intent(this, IntentListener::class.java)
                    intent.putExtra("phone", formattedPhone)
                    intent.putExtra("interval", interval.toString())
                    pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, pendingIntent)
                    Log.i("BroadcastReceiver", "Set alarm")
                }
            } else {
                btn.text = getString(R.string.start)
                alarmManager.cancel(pendingIntent)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val btn = findViewById<Button>(R.id.statusBtn).text.toString()
        val msg = findViewById<EditText>(R.id.msgInput).text.toString()
        val phone = findViewById<EditText>(R.id.phoneInput).text.toString()
        val min = findViewById<EditText>(R.id.timeInput).text.toString()
        outState.putString("btn", btn)
        outState.putString("msg", msg)
        outState.putString("phone", phone)
        outState.putString("min", min)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        btnState = savedInstanceState.getString("btn") as String
        msgState = savedInstanceState.getString("msg") as String
        phoneState = savedInstanceState.getString("phone") as String
        minState = savedInstanceState.getString("min") as String

        findViewById<Button>(R.id.statusBtn).text = btnState
        findViewById<EditText>(R.id.msgInput).setText(msgState)
        findViewById<EditText>(R.id.phoneInput).setText(phoneState)
        findViewById<EditText>(R.id.timeInput).setText(minState)
    }
}