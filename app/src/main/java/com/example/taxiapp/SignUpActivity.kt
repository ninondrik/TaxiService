package com.example.taxiapp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusRuntimeException
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {
    private var nameEditText: EditText? = null
    private var phoneEditText: EditText? = null
    private var emailEditText: EditText? = null
    private var passwordEditText: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        nameEditText = nameEdit
        phoneEditText = phoneEdit
        emailEditText = emailEdit
        passwordEditText = passwordEdit
        countryCodePicker.registerPhoneNumberTextView(phoneEditText)
        val policy = Html.fromHtml(getString(R.string.agree_terms_privacy), Html.FROM_HTML_MODE_LEGACY)
        val termsOfUse = agree_terms_privacy
        termsOfUse.text = policy
        termsOfUse.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun isValidEmail(sequence: CharSequence): Boolean {
        return !TextUtils.isEmpty(sequence)
    }

    private fun isValidPhone(sequence: CharSequence): Boolean {
        return !TextUtils.isEmpty(sequence)
    }

    fun register(view: View) {
        // TODO: fields validation
        if (nameEditText!!.text.toString().trim { it <= ' ' }.isEmpty()) {
            nameEditText!!.requestFocus()
            nameEditText!!.error = "It's just a formality"
        } else if (!isValidPhone(phoneEditText!!.toString())) {
            phoneEditText!!.requestFocus()
            phoneEditText!!.error = "Phone can't be empty"
        } else if (!isValidEmail(emailEditText!!.toString())) {
            emailEditText!!.requestFocus()
            emailEditText!!.error = "Email can't be empty"
        } else if (passwordEditText!!.text.toString().trim { it <= ' ' }.isEmpty()) {
            passwordEditText!!.requestFocus()
            passwordEditText!!.error = "Password can't be empty"
        } else {
            GlobalScope.launch {
                val managedChannel = ManagedChannelBuilder.forAddress(getString(R.string.server_address), resources.getInteger(R.integer.server_port)).usePlaintext().build()
                val blockingStub = taxiServiceGrpc.newBlockingStub(managedChannel)
                var phoneText: String = countryCodePicker.fullNumber.toString()
                phoneText = phoneText.replace("\\D+".toRegex(), "")
                val customer = Customer.newBuilder()
                        .setName(nameEditText!!.text.toString())
                        .setPhoneNumber(phoneText)
                        .setEmail(emailEditText!!.text.toString())
                        .setPassword(passwordEditText!!.text.toString())
                        .build()
                val createCustomerRequest = CreateCustomerRequest.newBuilder()
                        .setApi("v1")
                        .setCustomer(customer)
                        .build()
                val createCustomerResponse: CreateCustomerResponse
                try {
                    createCustomerResponse = blockingStub.createCustomer(createCustomerRequest) // Запрос на создание
                    Log.v("Response", createCustomerResponse.authToken)
                    managedChannel.shutdown()
                    val intent = Intent(applicationContext, SignInActivity::class.java)
                    startActivity(intent)
                    finish()
                } catch (e: StatusRuntimeException) {
                    if (e.status.cause is java.net.ConnectException) {
                        runOnUiThread { Toast.makeText(this@SignUpActivity, R.string.internet_connection, Toast.LENGTH_LONG).show() }
                    }
                    //                                logger.log(Level.WARNING, "RPC failed: " + e.getStatus());
                    managedChannel.shutdown()
                }
            }

        }

    }

    fun changeForm(view: View) {
        redirectToLogin.setTextColor(resources.getColor(R.color.design_default_color_primary))
        val intent = Intent(applicationContext, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }
}