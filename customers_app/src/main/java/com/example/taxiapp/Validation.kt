package com.example.taxiapp

import android.support.v7.app.AppCompatActivity
import android.widget.EditText
import java.util.regex.Pattern

class Validation(private val activity: AppCompatActivity) {

    fun isNameValid(editText: EditText?): Boolean {
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

    fun isEmailValid(editText: EditText?): Boolean {
        val pattern = android.util.Patterns.EMAIL_ADDRESS
        val text = editText!!.text.toString()
        /*
         * If not empty field is empty and don't matches to pattern
         * then set error and return false
         */
        if (text.isNotEmpty()) {
            if (pattern.matcher(text).matches()) {
                editText.error = null
                return true
            }
        } else {
            editText.error = null
            return true
        }
        editText.error = activity.getString(R.string.error_invalid_value)
        return false

    }

    fun isPasswordValid(editText: EditText?, checkStrength: Boolean = false): Boolean {
        val password = editText!!.text.toString()
        val length = password.length

        val disallowedSymbols = Pattern.compile("[а-я]+", Pattern.CASE_INSENSITIVE)

        // Checking disallowed symbols in password

        if (length == 0) {
            editText.error = activity.getString(R.string.error_invalid_value)
            return false
        }
        if (disallowedSymbols.matcher(password).find()) {
            editText.error = activity.getString(R.string.use_latin_and_next_symbols)
            return false
        }
        // Checking passwords strength
        else if (checkStrength) {
            // Minimal length check
            if (length < 6) {
                editText.setBackgroundColor(activity.getColor(R.color.quantum_vanillaredA400))
                editText.error = activity.getString(R.string.warning_weak_password)
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
                        in 16..31 -> editText.setBackgroundColor(activity.getColor(R.color.quantum_vanillaredA400))
                        in 32..63 -> editText.setBackgroundColor(activity.getColor(R.color.quantum_yellowA700))
                        in 64..128 -> editText.setBackgroundColor(activity.getColor(R.color.quantum_googgreen))
                    }
                    return true
                }
                2 -> {
                    when (length) {
                        in 8..24 -> editText.setBackgroundColor(activity.getColor(R.color.quantum_vanillaredA400))
                        in 25..48 -> editText.setBackgroundColor(activity.getColor(R.color.quantum_yellowA700))
                        in 49..128 -> editText.setBackgroundColor(activity.getColor(R.color.quantum_googgreen))
                    }
                    return true
                }
                3 -> {
                    when (length) {
                        in 8..12 -> editText.setBackgroundColor(activity.getColor(R.color.quantum_vanillaredA400))
                        in 13..20 -> editText.setBackgroundColor(activity.getColor(R.color.quantum_yellowA700))
                        in 21..128 -> editText.setBackgroundColor(activity.getColor(R.color.quantum_googgreen))
                    }
                    return true
                }
                4 -> {
                    when (length) {
                        in 6..10 -> editText.setBackgroundColor(activity.getColor(R.color.quantum_vanillaredA400))
                        in 11..15 -> editText.setBackgroundColor(activity.getColor(R.color.quantum_yellowA700))
                        in 16..128 -> editText.setBackgroundColor(activity.getColor(R.color.quantum_googgreen))
                    }
                    return true
                }

            }
        }
        return true
    }

    fun isPhoneValid(editText: EditText?): Boolean {
        val length = editText!!.text.length
        return if (length < 10) {
            editText.error = activity.getString(R.string.error_invalid_value)
            false

        } else {
            editText.error = null
            true
        }
    }

}