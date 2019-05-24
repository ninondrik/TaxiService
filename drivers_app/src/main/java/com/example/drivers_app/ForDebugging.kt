package com.example.drivers_app

import android.os.StrictMode

class ForDebugging {
    fun turnOnStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                    .penaltyLog()
                    .detectAll()
                    .build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                    .penaltyLog()
                    .detectAll()
//                    .penaltyDeath()
                    .build())
        }
    }
}