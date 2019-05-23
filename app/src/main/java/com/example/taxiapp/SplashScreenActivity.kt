package com.example.taxiapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class SplashScreenActivity : AppCompatActivity() {
    private var savedToken: String? = null
    private var savedLogin: String? = null
    private var lastLogin: Long? = null
    private var loginTime: Long? = null
    private var sPref: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        var intent: Intent
        sPref = getSharedPreferences("TaxiService", Context.MODE_PRIVATE)
        savedToken = sPref!!.getString("auth_token", "")
        savedLogin = sPref!!.getString("login", "")
        lastLogin = sPref!!.getLong("last_login", 0)
        loginTime = Date().time
        if (savedToken != "" || savedLogin != "" || (loginTime!! - lastLogin!!) / 2592000000.0 < 30) { // If last_login timestamp is older than 30 days
            GlobalScope.launch {
                val managedChannel = ManagedChannelBuilder.forAddress(getString(R.string.server_address), resources.getInteger(R.integer.server_port)).usePlaintext().build()
                val blockingStub = taxiServiceGrpc.newBlockingStub(managedChannel)
                val tokenCheckRequest = TokenCheckRequest.newBuilder()
                        .setApi("v1")
                        .setUserType(0)
                        .setAuthToken(savedToken)
                        .setLogin(savedLogin)
                        .build()
                val tokenCheckResponse: TokenCheckResponse
                try {
                    tokenCheckResponse = blockingStub.tokenCheck(tokenCheckRequest) // Запрос на создание
                    managedChannel.shutdown()
                    intent = if (tokenCheckResponse.isValidToken) {
                        Intent(applicationContext, MainActivity::class.java)
                    } else {
                        Intent(applicationContext, SignInActivity::class.java)
                    }
                    startActivity(intent)
                    finish()
                } catch (e: StatusRuntimeException) {
                    if (e.status.cause is java.net.ConnectException) {
                        runOnUiThread { Toast.makeText(this@SplashScreenActivity, R.string.internet_connection, Toast.LENGTH_LONG).show() }
                    }
                    //                                logger.log(Level.WARNING, "RPC failed: " + e.getStatus());
                    managedChannel.shutdown()
                    intent = Intent(applicationContext, SignInActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        } else {
            intent = Intent(applicationContext, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}
