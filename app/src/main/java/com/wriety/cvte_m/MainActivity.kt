package com.wriety.cvte_m

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.wriety.cvte_m.accel.AccelDemoActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, AccelDemoActivity::class.java))
        finish()
    }
}