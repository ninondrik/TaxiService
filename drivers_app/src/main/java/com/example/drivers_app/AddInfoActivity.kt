package com.example.drivers_app

import android.app.Activity
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaRecorder.VideoSource.CAMERA
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_add_info.*
import java.io.IOException


class AddInfoActivity : AppCompatActivity() {

    private var validation: Validation? = null
    private var passportEdit: EditText? = null
    private var drivingLicenseEdit: EditText? = null
    private var expiryDateEdit: EditText? = null
    private var passportPhoto: Bitmap? = null
    private var drivingLicensePhoto: Bitmap? = null
    private var registrationCertificatePhoto: Bitmap? = null
    private var carPhoto: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_info)
        passportEdit = passportEditText
        drivingLicenseEdit = drivingLicenseEditText
        expiryDateEdit = expiryDate
        validation = Validation(this@AddInfoActivity)

        // Validate passport field
        passportEdit!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validation!!.isPassportValid(passportEdit)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })

        expiryDateEdit!!.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                pickDate()
            }
        }

        loadPassportImageButton.setOnClickListener {

        }
    }

    private fun pickDate() {
        val datePickerDialog = DatePickerDialog(this@AddInfoActivity)
        datePickerDialog.setOnDateSetListener { _, year, month, dayOfMonth ->
            expiryDateEdit!!.text = SpannableStringBuilder("${dayOfMonth.toString().padStart(2, '0')}.${month.toString().padStart(2, '0')}.$year") // format: MM.DD.Y
        }
        datePickerDialog.setOnDismissListener {
            validation!!.isDateValid(expiryDateEdit)
        }
        datePickerDialog.show()
    }

    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Action");
        val pictureDialogItems = arrayOf(
                "Select photo from gallery",
                "Capture photo from camera")
        pictureDialog.setItems(pictureDialogItems,
                DialogInterface.OnClickListener { _, choice ->

                    when (choice) {
                        0 -> choosePhotoFromGallary()
                        1 -> takePhotoFromCamera()
                    }

                })
        pictureDialog.show()
    }

    // TODO: choose or make photos
    // TODO: saving photos to variables

    private fun choosePhotoFromGallary() {
        val galleryIntent = Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        startActivityForResult(galleryIntent, 0)
    }

    private fun takePhotoFromCamera() {
        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_CANCELED) {
            return
        }
        if (requestCode == 0) {
            if (data != null) {
                val contentURI = data.data
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                    Toast.makeText(this@AddInfoActivity, "Image Saved!", Toast.LENGTH_SHORT).show()

                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this@AddInfoActivity, "Failed!", Toast.LENGTH_SHORT).show()
                }

            }

        } else if (requestCode == CAMERA) {
            val thumbnail = data!!.extras!!.get("data") as Bitmap
            Toast.makeText(this@AddInfoActivity, "Image Saved!", Toast.LENGTH_SHORT).show()
        }
    }
}
