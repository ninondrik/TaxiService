package com.example.drivers_app

import android.support.v7.app.AppCompatActivity
import android.widget.EditText
import com.github.pinball83.maskededittext.MaskedEditText
import java.util.regex.Pattern

class Validation(private val activity: AppCompatActivity) {

    fun isFieldValid(editText: EditText?): Boolean {
        val text = editText!!.text.toString()
        val pattern = Pattern.compile("[a-zА-Я]+", Pattern.CASE_INSENSITIVE)
        /*
         * If not empty field is empty and don't matches to pattern
         * then set error and return false
         */
        return if (!pattern.matcher(text).matches()) {
            editText.error = activity.getString(R.string.error_invalid_value)
            false
        } else {
            editText.error = null
            true
        }
    }

    fun isEmailValid(emailEdit: EditText?): Boolean {
        val pattern = android.util.Patterns.EMAIL_ADDRESS
        val text = emailEdit!!.text.toString()
        /*
         * If not empty field is empty and don't matches to pattern
         * then set error and return false
         */
        if (text.isNotEmpty()) {
            if (pattern.matcher(text).matches()) {
                emailEdit.error = null
                return true
            }
        }
        emailEdit.error = activity.getString(R.string.error_invalid_value)
        return false

    }

    fun isPasswordValid(passwordEdit: EditText?, checkStrength: Boolean = false): Boolean {
        val password = passwordEdit!!.text.toString()
        val length = password.length

        val disallowedSymbols = Pattern.compile("[а-я]+", Pattern.CASE_INSENSITIVE)

        // Checking disallowed symbols in password

        if (length == 0) {
            passwordEdit.error = activity.getString(R.string.error_invalid_value)
            return false
        }
        if (disallowedSymbols.matcher(password).find()) {
            passwordEdit.error = activity.getString(R.string.use_latin_and_next_symbols)
            return false
        }
        // Checking passwords strength
        else if (checkStrength) {
            // Minimal length check
            if (length < 6) {
                passwordEdit.setBackgroundColor(activity.getColor(R.color.quantum_vanillaredA400))
                passwordEdit.error = activity.getString(R.string.warning_weak_password)
                return false
            }
            var strengthPass = 0
            val matchedCases = arrayOf("[$@$!%*#?&]+", "[A-Z]+", "\\d+", "[a-z]+")
            matchedCases.forEach {
                if (Pattern.compile(it).matcher(password).find()) {
                    strengthPass++
                }
            }
            /* FIXME: improve checking passwords strength
            * quantum_vanillaredA400 - for weak password
            * quantum_yellowA700 - for medium strength password
            * quantum_googgreen - for strong password
            */
            when (strengthPass) {
                0 -> return false
                1 -> {
                    when (length) {
                        in 16..31 -> passwordEdit.setBackgroundColor(activity.getColor(R.color.quantum_vanillaredA400))
                        in 32..63 -> passwordEdit.setBackgroundColor(activity.getColor(R.color.quantum_yellowA700))
                        in 64..128 -> passwordEdit.setBackgroundColor(activity.getColor(R.color.quantum_googgreen))
                    }
                    return true
                }
                2 -> {
                    when (length) {
                        in 8..24 -> passwordEdit.setBackgroundColor(activity.getColor(R.color.quantum_vanillaredA400))
                        in 25..48 -> passwordEdit.setBackgroundColor(activity.getColor(R.color.quantum_yellowA700))
                        in 49..128 -> passwordEdit.setBackgroundColor(activity.getColor(R.color.quantum_googgreen))
                    }
                    return true
                }
                3 -> {
                    when (length) {
                        in 8..12 -> passwordEdit.setBackgroundColor(activity.getColor(R.color.quantum_vanillaredA400))
                        in 13..20 -> passwordEdit.setBackgroundColor(activity.getColor(R.color.quantum_yellowA700))
                        in 21..128 -> passwordEdit.setBackgroundColor(activity.getColor(R.color.quantum_googgreen))
                    }
                    return true
                }
                4 -> {
                    when (length) {
                        in 6..10 -> passwordEdit.setBackgroundColor(activity.getColor(R.color.quantum_vanillaredA400))
                        in 11..15 -> passwordEdit.setBackgroundColor(activity.getColor(R.color.quantum_yellowA700))
                        in 16..128 -> passwordEdit.setBackgroundColor(activity.getColor(R.color.quantum_googgreen))
                    }
                    return true
                }

            }
        }
        return true
    }

    fun isPhoneValid(phoneEdit: EditText?): Boolean {
        val length = phoneEdit!!.text.length
        return if (length < 10) {
            phoneEdit.error = activity.getString(R.string.error_invalid_value)
            false

        } else {
            phoneEdit.error = null
            true
        }
    }

    // TODO: check expiryDate is greater than now
    // TODO: check birthDate is greater than 18
    fun isDateValid(dateEdit: MaskedEditText?): Boolean {
        val pattern = Pattern.compile("^\\d{2}.\\d{2}.\\d{4}$")
        val text = dateEdit!!.text
        return if (text!!.isNotEmpty() && pattern.matcher(text).matches()) {
            dateEdit.error = null
            true
        } else {
            dateEdit.error = activity.getString(R.string.error_invalid_value)
            false
        }
    }

    fun isPassportValid(passportEdit: MaskedEditText?): Boolean {
        val pattern = Pattern.compile("^\\d{4}\\s\\d{6}$")
        val text = passportEdit!!.text
        return if (text!!.isNotEmpty() && pattern.matcher(text).matches()) {
            passportEdit.error = null
            true
        } else {
            passportEdit.error = activity.getString(R.string.error_invalid_value)
            false
        }

    }

    fun isDrivingLicenseValid(drivingLicenseEdit: MaskedEditText?): Boolean {
        val pattern = Pattern.compile("^\\d{2}\\s\\d{2}\\s\\d{6}$")
        val text = drivingLicenseEdit!!.text
        return if (text!!.isNotEmpty() && pattern.matcher(text).matches()) {
            drivingLicenseEdit.error = null
            true
        } else {
            drivingLicenseEdit.error = activity.getString(R.string.error_invalid_value)
            false
        }

    }
}