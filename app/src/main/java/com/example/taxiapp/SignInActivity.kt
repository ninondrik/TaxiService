package com.example.taxiapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import android.widget.Toast
import io.grpc.ManagedChannelBuilder
import io.grpc.Status
import io.grpc.StatusRuntimeException
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

class SignInActivity : AppCompatActivity() {
    private var phoneEditText: EditText? = null
    private var passwordEditText: EditText? = null
    private var authToken = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        phoneEditText = phoneEdit
        passwordEditText = passwordEdit
        countryCodePicker.registerPhoneNumberTextView(phoneEditText)
    }

    fun signIn(view: View) {
        when {
            phoneEditText!!.toString().trim { it <= ' ' }.isEmpty() -> {
                phoneEditText!!.requestFocus()
                phoneEditText!!.error = "Phone can't be empty"
            }
            passwordEditText!!.text.toString().trim { it <= ' ' }.isEmpty() -> {
                passwordEditText!!.requestFocus()
                passwordEditText!!.error = "Password can't be empty"
            }
            else -> {
                GlobalScope.launch {
                    // Build connection and rpc objects
                    val managedChannel = ManagedChannelBuilder.forAddress(getString(R.string.server_address), resources.getInteger(R.integer.server_port)).usePlaintext().build()
                    val blockingStub = taxiServiceGrpc.newBlockingStub(managedChannel)
                    var phoneText: String = countryCodePicker.fullNumber.toString()
                    phoneText = phoneText.replace("\\D+".toRegex(), "")
                    val loginRequest = LoginRequest.newBuilder()
                            .setApi("v1")
                            .setLogin(phoneText)
                            .setPassword(passwordEditText!!.text.toString())
                            .setUserType(0)
                            .build()
                    val loginResponse: LoginResponse
                    try {
                        loginResponse = blockingStub.withDeadlineAfter(5000, TimeUnit.MILLISECONDS).loginUser(loginRequest) // Запрос на создание
                        authToken = loginResponse.authToken
                        managedChannel.shutdown()
                        if (authToken.isNotEmpty()) {
                            // Save data to preferences and start new activity
                            val sPref = getSharedPreferences("TaxiService", Context.MODE_PRIVATE)
                            sPref.edit().putLong("last_login", Date().time).apply() // Token saving into SharedPreferences
                            sPref.edit().putInt("customer_id", loginResponse.userId).apply() // Token saving into SharedPreferences
                            sPref.edit().putString("auth_token", authToken).apply() // Token saving into SharedPreferences
                            sPref.edit().putString("login", phoneText).apply() // Token saving into SharedPreferences
                            val intent = Intent(applicationContext, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            phoneEditText!!.error = ""
                            passwordEditText!!.error = ""
                        }
                    } catch (e: StatusRuntimeException) {
                        // Check exceptions
                        if (e.status.cause is java.net.ConnectException) {
                            runOnUiThread { Toast.makeText(this@SignInActivity, R.string.internet_connection, Toast.LENGTH_LONG).show() }
                        } else if (e.status.code == Status.Code.NOT_FOUND || e.status.code == Status.Code.PERMISSION_DENIED) {
                            runOnUiThread { Toast.makeText(this@SignInActivity, R.string.wrong_data, Toast.LENGTH_LONG).show() }
                        } else if (e.status.code == Status.Code.UNKNOWN) {
                            runOnUiThread { Toast.makeText(this@SignInActivity, R.string.server_error, Toast.LENGTH_LONG).show() }
                        }
                        //                                logger.log(Level.WARNING, "RPC failed: " + e.getStatus());
                        managedChannel.shutdown()
                    }
                }
            }
        }

    }

    fun changeForm(view: View) {
        redirectToRegister.setTextColor(resources.getColor(R.color.design_default_color_primary))
        val intent = Intent(applicationContext, SignUpActivity::class.java)
        startActivity(intent)
        finish()
    }
}
