package com.example.drivers_app

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.SearchView
import android.widget.Toast
import com.example.drivers_app.search_adapter.CarItem
import com.example.drivers_app.search_adapter.CarModelAdapter
import com.example.drivers_app.search_adapter.ColorAdapter
import com.example.drivers_app.search_adapter.ColorItem
import com.example.taxiapp.*
import com.github.pinball83.maskededittext.MaskedEditText
import com.google.protobuf.ByteString
import com.google.protobuf.Timestamp
import io.grpc.ManagedChannelBuilder
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.okhttp.internal.Platform.logger
import kotlinx.android.synthetic.main.activity_sign_up_add_info.*
import kotlinx.android.synthetic.main.activity_sign_up_search_car.view.*
import kotlinx.android.synthetic.main.activity_sign_up_search_car.view.searchView
import kotlinx.android.synthetic.main.activity_sign_up_search_color.view.*
import kotlinx.android.synthetic.main.activity_sign_up_set_car.view.*
import kotlinx.android.synthetic.main.search_car_item.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Level


class AddInfoActivity : AppCompatActivity() {

    private var validation: Validation? = null
    private var passportEdit: MaskedEditText? = null
    private var drivingLicenseEdit: MaskedEditText? = null

    private var expiryDateEdit: MaskedEditText? = null
    private var passportPhoto: ByteString? = null
    private var drivingLicensePhoto: ByteString? = null
    private var registrationCertificatePhoto: ByteString? = null
    private val carBrands: MutableMap<Int, String>? = mutableMapOf()
    private val carModels: MutableList<CarItem> = mutableListOf()
    private val colorsArray: MutableList<ColorItem> = mutableListOf()
    private var alertDialog: AlertDialog.Builder? = null
    private var searchCarDialog: AlertDialog? = null

    private var searchCarDialogView: View? = null
    private var searchColorDialogView: View? = null

    private var searchButton: Button? = null
    private var searchColor: Button? = null
    private var setCarDialog: AlertDialog.Builder? = null
    private var setCarDialogView: View? = null

    private var carModelRecyclerView: RecyclerView? = null
    private var carColorRecyclerView: RecyclerView? = null

    private var carCardItem: View? = null

    companion object {
        private const val REQUEST_PASSPORT_IMAGE = 0
        private const val REQUEST_DRIVING_LICENSE_IMAGE = 1
        private const val REQUEST_STS_IMAGE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_add_info)
        passportEdit = passportEditText
        drivingLicenseEdit = drivingLicenseEditText
        expiryDateEdit = expiryDate

        carCardItem = layoutInflater.inflate(R.layout.search_car_item, null)

        setCarDialogView = this@AddInfoActivity.layoutInflater.inflate(R.layout.activity_sign_up_set_car, null)
        setCarDialog = AlertDialog.Builder(this@AddInfoActivity).setView(setCarDialogView)

        searchCarDialogView = this.layoutInflater.inflate(R.layout.activity_sign_up_search_car, null)
        searchColorDialogView = this.layoutInflater.inflate(R.layout.activity_sign_up_search_color, null)

        searchCarDialog = AlertDialog.Builder(this@AddInfoActivity).setView(searchCarDialogView).create()
        alertDialog = AlertDialog.Builder(this@AddInfoActivity)

        carModelRecyclerView = searchCarDialogView!!.carModelItems
        carColorRecyclerView = searchColorDialogView!!.carColorItems

        validation = Validation(this@AddInfoActivity)

        // fill brands array on background
        GlobalScope.launch {
            loadCarBrands()
            loadCarModels()
        }
        GlobalScope.launch {
            loadCarColor()
        }

        val datePickerDialog = DatePickerDialog(this@AddInfoActivity)
        datePickerDialog.setOnDateSetListener { _, year, month, dayOfMonth ->
            expiryDateEdit!!.text = SpannableStringBuilder("${dayOfMonth.toString().padStart(2, '0')}.${month.toString().padStart(2, '0')}.$year") // format: MM.DD.Y
        }
        datePickerDialog.setOnDismissListener {
            validation!!.isDateValid(expiryDateEdit)
        }

        // Validate passport field
        passportEdit!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validation!!.isPassportValid(passportEdit)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })

        drivingLicenseEdit!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validation!!.isDrivingLicenseValid(drivingLicenseEdit)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })

        // Show datePicker dialog after editText focus
        expiryDateEdit!!.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                datePickerDialog.show()
            }
        }

        loadPassportImageButton.setOnClickListener {
            choosePhotoFromGallery(REQUEST_PASSPORT_IMAGE)
        }
        loadDrivingLicenceButton.setOnClickListener {
            choosePhotoFromGallery(REQUEST_DRIVING_LICENSE_IMAGE)
        }
        loadSTSButton.setOnClickListener {
            choosePhotoFromGallery(REQUEST_STS_IMAGE)
        }
    }

    // Fill carBrands [0: name1, 1: name2, ...]
    private fun loadCarBrands() {
        val managedChannel = ManagedChannelBuilder.forAddress(getString(R.string.server_address),
                resources.getInteger(R.integer.server_port)).usePlaintext().build()
        val blockingStub = taxiServiceGrpc.newBlockingStub(managedChannel)
        val readAllCarBrandsRequest = ReadAllCarBrandsRequest.newBuilder()
                .setApi(getString(R.string.api_version))
                .build()
        val readAllCarBrandsResponse: ReadAllCarBrandsResponse
        try {
            readAllCarBrandsResponse = blockingStub.readAllCarBrands(readAllCarBrandsRequest)
            readAllCarBrandsResponse.carBrandList.forEach {
                carBrands!![it.id] = it.brandName
            }

        } catch (e: StatusRuntimeException) {
            if (e.status.cause is java.net.ConnectException) {
                runOnUiThread { Toast.makeText(this@AddInfoActivity, R.string.error_internet_connection, Toast.LENGTH_LONG).show() }
            } else if (e.status == Status.UNKNOWN) {
                runOnUiThread { Toast.makeText(this@AddInfoActivity, R.string.error_cannot_load_car_brands, Toast.LENGTH_LONG).show() }
            }
            logger.log(Level.WARNING, "RPC failed: " + e.status)
        }
        managedChannel.shutdown()
    }

    // Fill carModels {brand -> [name1, name2, name3, ...], brand2 -> [...]}
    private fun loadCarModels() {
        val managedChannel = ManagedChannelBuilder.forAddress(getString(R.string.server_address),
                resources.getInteger(R.integer.server_port)).usePlaintext().build()
        val blockingStub = taxiServiceGrpc.newBlockingStub(managedChannel)
        var readAllCarModelsRequest = ReadAllCarModelsRequest.newBuilder().build()
        var readAllCarModelsResponse: ReadAllCarModelsResponse
//        var carModelsArray: MutableList<String>
        try {
            carBrands!!.forEach { carBrand ->
                readAllCarModelsRequest = readAllCarModelsRequest.toBuilder()
                        .setApi(getString(R.string.api_version))
                        .setCarBrandId(carBrand.key)
                        .build()
//                carModelsArray = mutableListOf()
                readAllCarModelsResponse = blockingStub.readAllCarModels(readAllCarModelsRequest)
                readAllCarModelsResponse.carModelsList.forEach { carModel ->
                    carModels.add(CarItem(carModel.modelName, carBrand.value))
                }
//                carModels!![carBrand.value] = carModelsArray
            }
        } catch (e: StatusRuntimeException) {
            if (e.status.cause is java.net.ConnectException) {
                runOnUiThread { Toast.makeText(this@AddInfoActivity, R.string.error_internet_connection, Toast.LENGTH_LONG).show() }
            } else if (e.status == Status.UNKNOWN) {
                runOnUiThread { Toast.makeText(this@AddInfoActivity, R.string.error_cannot_load_car_models, Toast.LENGTH_LONG).show() }
            }
            logger.log(Level.WARNING, "RPC failed: " + e.status)
        }

        managedChannel.shutdown()
    }

    private fun loadCarColor() {
        val managedChannel = ManagedChannelBuilder.forAddress(getString(R.string.server_address),
                resources.getInteger(R.integer.server_port)).usePlaintext().build()
        val blockingStub = taxiServiceGrpc.newBlockingStub(managedChannel)
        val readAllColorsRequest = ReadAllColorsRequest.newBuilder()
                .setApi(getString(R.string.api_version))
                .build()
        val readAllColorsResponse: ReadAllColorsResponse
        try {
            readAllColorsResponse = blockingStub.getColors(readAllColorsRequest)
            readAllColorsResponse.colorList.forEach { color ->
                colorsArray.add(ColorItem(color.description))
            }

        } catch (e: StatusRuntimeException) {
            if (e.status.cause is java.net.ConnectException) {
                runOnUiThread { Toast.makeText(this@AddInfoActivity, R.string.error_internet_connection, Toast.LENGTH_LONG).show() }
            } else if (e.status == Status.UNKNOWN) {
                runOnUiThread { Toast.makeText(this@AddInfoActivity, R.string.error_cannot_load_car_brands, Toast.LENGTH_LONG).show() }
            }
            logger.log(Level.WARNING, "RPC failed: " + e.status)
        }
        managedChannel.shutdown()

    }

    fun addCarDialog(view: View) {
        alertDialog!!.setMessage(R.string.dialog_message_add_auto)
                .setPositiveButton(R.string.dialog_message_add_now) { _, _ ->
                    setCarDialog()
                }
                .setNegativeButton(R.string.dialog_message_add_later) { dialog, _ ->
                    registerDriver()
                    dialog.dismiss()
                }
        alertDialog!!.create()
        alertDialog!!.show()
    }

    private fun setCarDialog() {
        val licensePlateEditText = setCarDialogView!!.licensePlate
        var modelAdapter: CarModelAdapter? = null
        var colorAdapter: ColorAdapter? = null

        searchButton = setCarDialogView!!.searchModel
        searchColor = setCarDialogView!!.chooseColorButton

        carModelRecyclerView!!.layoutManager = LinearLayoutManager(searchCarDialogView!!.context)
        carColorRecyclerView!!.layoutManager = LinearLayoutManager(searchCarDialogView!!.context)

        searchButton!!.setOnClickListener {
            searchCarDialog = AlertDialog.Builder(this@AddInfoActivity).setView(searchCarDialogView).create()
            modelAdapter = CarModelAdapter(carModels)
            carModelRecyclerView!!.adapter = modelAdapter
            searchCarDialog!!.show()
        }
        searchColor!!.setOnClickListener {
            searchCarDialog = AlertDialog.Builder(this@AddInfoActivity).setView(searchColorDialogView).create()
            colorAdapter = ColorAdapter(colorsArray)
            carColorRecyclerView!!.adapter = colorAdapter
            searchCarDialog!!.show()
        }

        setCarDialog!!.setView(setCarDialogView)
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                    if (licensePlateEditText.text.isNotEmpty() && searchColor!!.text.isNotEmpty() && searchButton!!.text.isNotEmpty()) {
                        registerDriver()
                    }
                }
                .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }
                .setOnCancelListener {

                }.create()
        setCarDialog!!.show()


        searchCarDialogView!!.searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchCarDialogView!!.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                modelAdapter!!.filter.filter(newText)
                return false
            }
        })

    }

    fun chooseColor(view: View) {
        searchColor!!.text = SpannableStringBuilder("${view.carModel.text} ${view.carBrand.text}")
        searchCarDialog!!.dismiss()
    }

    fun chooseModel(view: View) {
        searchButton!!.text = SpannableStringBuilder("${view.carModel.text} ${view.carBrand.text}")
        searchCarDialog!!.dismiss()
    }

    private fun registerDriver() {
        val fieldsValidation = mapOf(
                "passport" to validation!!.isPassportValid(passportEdit),
                "drivingLicense" to validation!!.isDrivingLicenseValid(drivingLicenseEdit),
                "expiryDate" to validation!!.isDateValid(expiryDateEdit)
        )
        if (!fieldsValidation.values.contains(false)) {
            GlobalScope.launch {
                val managedChannel = ManagedChannelBuilder.forAddress(getString(R.string.server_address),
                        resources.getInteger(R.integer.server_port)).usePlaintext().build()
                val blockingStub = taxiServiceGrpc.newBlockingStub(managedChannel)
                val birthDateParse = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(intent.getStringExtra("birthDate")).time
                val expiryDateParse = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(expiryDateEdit!!.text.toString()).time
                // TODO: if car was set add it to DB
                val driver = Driver.newBuilder()
                        .setFirstName(intent.getStringExtra("name"))
                        .setSurname(intent.getStringExtra("surname"))
                        .setPartronymic(intent.getStringExtra("patronymic"))
                        .setBirthDate(Timestamp.newBuilder().setSeconds(birthDateParse))
                        .setPhoneNumber(intent.getStringExtra("phone"))
                        .setEmail(intent.getStringExtra("email"))
                        .setPassword(intent.getStringExtra("password"))
                        .build()
                val driverDocuments = DriverDocuments.newBuilder()
                        .setPassportNumber(passportEdit!!.text.toString())
                        .setPassportImage(passportPhoto)
                        .setDrivingLicenseNumber(drivingLicenseEdit!!.text.toString())
                        .setExpiryDate(Timestamp.newBuilder().setSeconds(expiryDateParse))
                        .setDrivingLicenseImage(drivingLicensePhoto)
                        .setStsPhoto(registrationCertificatePhoto)
                        .build()
                val createDriverRequest = CreateDriverRequest.newBuilder()
                        .setApi(getString(R.string.api_version))
                        .setDriver(driver)
                        .setDriverDocuments(driverDocuments)
                        .build()
                val createDriverResponse: CreateDriverResponse
                try {
                    createDriverResponse = blockingStub.createDriver(createDriverRequest)
                    Log.v("Response: ", createDriverResponse.authToken)
                    managedChannel.shutdown()
                    val intent = Intent(applicationContext, SignInActivity::class.java)
                    startActivity(intent)
                    finishAffinity()
                    finish()
                } catch (e: StatusRuntimeException) {
                    if (e.status.cause is java.net.ConnectException) {
                        runOnUiThread { Toast.makeText(this@AddInfoActivity, R.string.error_internet_connection, Toast.LENGTH_LONG).show() }
                    } else if (e.status == Status.UNKNOWN) {
                        runOnUiThread { Toast.makeText(this@AddInfoActivity, R.string.user_is_already_exists, Toast.LENGTH_LONG).show() }
                    }
                    logger.log(Level.WARNING, "RPC failed: " + e.status)
                    managedChannel.shutdown()
                }
            }
        }
    }

    private fun choosePhotoFromGallery(requestImage: Int) {
        Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also { choosePhotoIntent ->
            choosePhotoIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(choosePhotoIntent, requestImage)
            }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_CANCELED) {
            return
        } else if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val contentURI = data.data
                try {
                    // Save data to bitmap
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                    GlobalScope.launch {
                        // Compress bitmap
                        val out = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.WEBP, 70, out)
                        val bitmapByteArray = ByteString.copyFrom(out.toByteArray())
                        when (requestCode) {
                            0 -> {
                                passportPhoto = bitmapByteArray
                                loadPassportImageButton!!.background = ContextCompat.getDrawable(this@AddInfoActivity, R.color.btn_success)
                                arePhotosLoaded()
                            }
                            1 -> {
                                drivingLicensePhoto = bitmapByteArray
                                loadDrivingLicenceButton!!.background = ContextCompat.getDrawable(this@AddInfoActivity, R.color.btn_success)
                                arePhotosLoaded()
                            }
                            2 -> {
                                registrationCertificatePhoto = bitmapByteArray
                                loadSTSButton!!.background = ContextCompat.getDrawable(this@AddInfoActivity, R.color.btn_success)
                                arePhotosLoaded()
                            }
                        }
                    }
                    Toast.makeText(this@AddInfoActivity, R.string.image_saved, Toast.LENGTH_SHORT).show()
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this@AddInfoActivity, R.string.error_failed, Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    private fun arePhotosLoaded() {
        if (passportPhoto != null && drivingLicensePhoto != null && registrationCertificatePhoto != null) {
            runOnUiThread {
                registerButton.isEnabled = true
            }
        }
    }
}
