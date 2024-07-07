package com.goflash.dispatch.features.cash.ui.activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.*
import com.goflash.dispatch.data.CashPickupDTO
import com.goflash.dispatch.databinding.ActivityCashPickupBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.cash.listeners.FileTypeSelectListener
import com.goflash.dispatch.features.cash.presenter.AddCashPickupPresenter
import com.goflash.dispatch.features.cash.ui.fragment.BottomSheetChooseFileFragment
import com.goflash.dispatch.features.cash.view.AddCashPickupView
import com.goflash.dispatch.features.receiving.ui.PreviewActivity
import com.goflash.dispatch.type.DepositType
import com.goflash.dispatch.ui.activity.BaseActivity
import com.goflash.dispatch.util.*
import com.goflash.dispatch.util.temp_files
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.ArrayList
import javax.inject.Inject

class AddCashPickupActivity : BaseActivity(), AddCashPickupView, View.OnClickListener,
    FileTypeSelectListener {

    @Inject
    lateinit var mPresenter: AddCashPickupPresenter

    private var cashPickup: CashPickupDTO? = null

    private val PDF_SELECTION_CODE = 2

    private var currentPhotoPath: String? = ""

    private var selectedPosition = 0

    private lateinit var binding: ActivityCashPickupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCashPickupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initDagger()
        initViews()
    }

    private fun initViews() {

        binding.toolbar.toolbarTitle.text = getString(R.string.add_cash_pick_up)
        binding.bottomLayout.labelTotalExpense.text = getString(R.string.total_cash_picked_up)
        binding.bottomLayout.btnSubmit.text = getString(R.string.submit)

        binding.bottomLayout.btnSubmit.isEnabled = false
        binding.bottomLayout.btnSubmit.setBackgroundResource(R.drawable.disable_button)

        mPresenter.getData()

        initEditTexts()

        binding.toolbar.iVProfileHome.setOnClickListener(this)
        binding.bottomLayout.btnSubmit.setOnClickListener(this)
        binding.layoutAddCashPickup.tvAddPhoto.setOnClickListener(this)
        binding.layoutAddCashPickup.ivRemove.setOnClickListener(this)

        val depositTypes = DepositType.toStringList()
        depositTypes.add(0, "Select Deposit Type")

        binding.layoutAddCashPickup.spDepositType.adapter = ArrayAdapter(this@AddCashPickupActivity, R.layout.layout_reason_spinner_item,
            R.id.textReason, depositTypes)

        binding.layoutAddCashPickup.spDepositType.setSelection(0)

        binding.layoutAddCashPickup.spDepositType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedPosition = position
                when(position){
                    0 -> {
                       enableOrDisableUploadAndReceipt(false)
                    }
                    1,4 -> {
                        enableOrDisableUploadAndReceipt(true)
                        binding.layoutAddCashPickup.tvReceipt.text = getString(R.string.cash_receipt_number)
                        binding.layoutAddCashPickup.edtAtmId.text.clear()
                        binding.layoutAddCashPickup.edtAtmId.visibility = View.GONE
                        binding.layoutAddCashPickup.tvAtmId.visibility = View.GONE
                    }
                    2 -> {
                        enableOrDisableUploadAndReceipt(true)
                        binding.layoutAddCashPickup.tvReceipt.text = getString(R.string.reference_number)
                        binding.layoutAddCashPickup.edtAtmId.visibility = View.VISIBLE
                        binding.layoutAddCashPickup.tvAtmId.visibility = View.VISIBLE
                    }
                    3 -> {
                        enableOrDisableUploadAndReceipt(true)
                        binding.layoutAddCashPickup.tvReceipt.text = getString(R.string.reference_number)
                        binding.layoutAddCashPickup.edtAtmId.text.clear()
                        binding.layoutAddCashPickup.edtAtmId.visibility = View.GONE
                        binding.layoutAddCashPickup.tvAtmId.visibility = View.GONE
                    }
                    5 -> {
                        enableOrDisableUploadAndReceipt(true)
                        binding.layoutAddCashPickup.tvReceipt.text = getString(R.string.upi_reference_number)
                        binding.layoutAddCashPickup.edtAtmId.text.clear()
                        binding.layoutAddCashPickup.edtAtmId.visibility = View.GONE
                        binding.layoutAddCashPickup.tvAtmId.visibility = View.GONE
                    }
                }
                enableOrDisableSubmit()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }

    }

    private fun enableOrDisableUploadAndReceipt(enabled: Boolean){
        binding.layoutAddCashPickup.edtCashReceipt.isEnabled = enabled
        binding.layoutAddCashPickup.tvAddPhoto.isEnabled = enabled

        if(enabled){
            binding.layoutAddCashPickup.tvAddPhoto.setBackgroundResource(R.drawable.border_upload_photo)
        }else{
            binding.layoutAddCashPickup.tvAddPhoto.setBackgroundResource(R.drawable.upload_disabled)
        }
    }

    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this)

        mPresenter.onAttachView(this, this)
    }

    private fun initEditTexts() {
        //2000
        binding.layoutAddCashPickup.edt2000.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.isNotEmpty() == true) {
                    cashPickup?.twoThousand = s.toString().toLong()
                    setData(cashPickup)
                } else {
                    cashPickup?.twoThousand = null
                    setData(cashPickup)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })

        //500
        binding.layoutAddCashPickup.edt500.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.isNotEmpty() == true) {
                    cashPickup?.fiveHundred = s.toString().toLong()
                    setData(cashPickup)
                } else {
                    cashPickup?.fiveHundred = null
                    setData(cashPickup)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })

        //200
        binding.layoutAddCashPickup.edt200.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.isNotEmpty() == true) {
                    cashPickup?.twoHundred = s.toString().toLong()
                    setData(cashPickup)
                } else {
                    cashPickup?.twoHundred = null
                    setData(cashPickup)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })

        //100
        binding.layoutAddCashPickup.edt100.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.isNotEmpty() == true) {
                    cashPickup?.hundred = s.toString().toLong()
                    setData(cashPickup)
                } else {
                    cashPickup?.hundred = null
                    setData(cashPickup)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })

        //50
        binding.layoutAddCashPickup.edt50.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.isNotEmpty() == true) {
                    cashPickup?.fifty = s.toString().toLong()
                    setData(cashPickup)
                } else {
                    cashPickup?.fifty = null
                    setData(cashPickup)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })

        //20
        binding.layoutAddCashPickup.edt20.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.isNotEmpty() == true) {
                    cashPickup?.twenty = s.toString().toLong()
                    setData(cashPickup)
                } else {
                    cashPickup?.twenty = null
                    setData(cashPickup)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })

        //10
        binding.layoutAddCashPickup.edt10.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.isNotEmpty() == true) {
                    cashPickup?.ten = s.toString().toLong()
                    setData(cashPickup)
                } else {
                    cashPickup?.ten = null
                    setData(cashPickup)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })

        //5
        binding.layoutAddCashPickup.edt5.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.isNotEmpty() == true) {
                    cashPickup?.five = s.toString().toLong()
                    setData(cashPickup)
                } else {
                    cashPickup?.five = null
                    setData(cashPickup)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })

        //2
        binding.layoutAddCashPickup.edt2.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.isNotEmpty() == true) {
                    cashPickup?.two = s.toString().toLong()
                    setData(cashPickup)
                } else {
                    cashPickup?.two = null
                    setData(cashPickup)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })

        //1
        binding.layoutAddCashPickup.edt1.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.isNotEmpty() == true) {
                    cashPickup?.one = s.toString().toLong()
                    setData(cashPickup)
                } else {
                    cashPickup?.one = null
                    setData(cashPickup)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })

        //cash_receipt
        binding.layoutAddCashPickup.edtCashReceipt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.isNotEmpty() == true) {
                    cashPickup?.cashDepositReceiptNumber = s.toString()
                    setData(cashPickup)
                } else {
                    cashPickup?.cashDepositReceiptNumber = null
                    setData(cashPickup)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })

        binding.layoutAddCashPickup.edtAtmId.addTextChangedListener {
            enableOrDisableSubmit()
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.iVProfileHome -> finish()
            R.id.btn_submit -> {
                hideKeyboard()
                if(selectedPosition == 0){
                    processError(Throwable("Please select Deposit Type."))
                    return
                }
                cashPickup?.cashDepositReceiptNumber = binding.layoutAddCashPickup.edtCashReceipt.text.toString()
                if (!validateCashPickup()) {
                    binding.layoutAddCashPickup.txtInvalid.visibility = View.VISIBLE
                    return
                }
                binding.layoutAddCashPickup.txtInvalid.visibility = View.GONE
                cashPickup?.atmId = binding.layoutAddCashPickup.edtAtmId.text?.toString()
                cashPickup?.depositType = DepositType.values()[selectedPosition-1].name
                mPresenter.submitCashPickup(cashPickup)
            }
            R.id.tv_add_photo -> uploadPhoto()
            R.id.iv_remove -> removeImageFromCashPickup()
        }
    }

    private fun validateCashPickup(): Boolean {

        val checkFile = !(cashPickup?.cashDepositFileUrl.isNullOrEmpty())
        val checkReceipt = !(cashPickup?.cashDepositReceiptNumber.isNullOrEmpty() || (selectedPosition == 5 && cashPickup?.cashDepositReceiptNumber?.length?:0 != 12)
                || (selectedPosition == 2 && cashPickup?.cashDepositReceiptNumber?.length?:0 != 4))

        val checkAtmId = !(selectedPosition == 2 && (binding.layoutAddCashPickup.edtAtmId.text.isNullOrEmpty() || binding.layoutAddCashPickup.edtAtmId.text?.length?:0 != 8))


        val checkAmount = ((cashPickup?.twoThousand != null && cashPickup?.twoThousand!! > 0L) || (cashPickup?.fiveHundred != null && cashPickup?.fiveHundred!! > 0L) || (cashPickup?.twoHundred != null && cashPickup?.twoHundred!! > 0L)
                || (cashPickup?.hundred != null && cashPickup?.hundred!! > 0L) || (cashPickup?.fifty != null && cashPickup?.fifty!! > 0L) || (cashPickup?.twenty != null && cashPickup?.twenty!! > 0L)
                || (cashPickup?.ten != null && cashPickup?.ten!! > 0L) || (cashPickup?.five != null && cashPickup?.five!! > 0L) || (cashPickup?.two != null && cashPickup?.two!! > 0L)
                || (cashPickup?.one != null && cashPickup?.one!! > 0L))

        return checkFile && checkReceipt && checkAmount && checkAtmId


    }

    private fun uploadPhoto() {
        /*val intent = Intent(this, PreviewActivity::class.java)
        startActivityForResult(intent, PREVIEW)*/

        val bottomSheetFragment = BottomSheetChooseFileFragment()
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
    }

    override fun onFileTypeSelected(type: String) {
        if (type == "IMAGE") {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                if (checkPermissions())
                    dispatchTakePictureIntent()
                else
                    dispatchTakePictureIntent()

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                if (checkStoragePermissions())
                    openPDFIntent()
                else
                    openPDFIntent()

        }
    }


    private fun openPDFIntent() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "application/pdf"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

        startActivityForResult(intent, PDF_SELECTION_CODE)
    }

    /**
     * Method to check permission for camera and storage, put in login time due to need at multiple places so asked in advance
     * */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermissions(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.CAMERA
                ),
                CAMERA_REQ
            )
            return false
        }
        return true
    }

    /**
     * Method to check permission for camera and storage, put in login time due to need at multiple places so asked in advance
     * */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkStoragePermissions(): Boolean {
        if ((ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED) ||
            (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                STORAGE_REQ
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            CAMERA_REQ -> {
                // If request is CANCELLED, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    dispatchTakePictureIntent()
                }
                return
            }
            STORAGE_REQ -> {
                // If request is CANCELLED, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    openPDFIntent()
                }
                return
            }

        }
    }

    private fun dispatchTakePictureIntent() {

        lateinit var chooserIntent: Intent

        var intentList: MutableList<Intent> = ArrayList()

        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, setImageUri())

        intentList = addIntentsToList(this, intentList, pickIntent)
        intentList = addIntentsToList(this, intentList, takePhotoIntent)

        if (intentList.size > 0) {
            chooserIntent = Intent.createChooser(
                intentList.removeAt(intentList.size - 1),
                getString(R.string.select_capture_image)
            )
            chooserIntent.putExtra(
                Intent.EXTRA_INITIAL_INTENTS,
                intentList.toTypedArray<Parcelable>()
            )
        }

        startActivityForResult(chooserIntent, REQUEST_IMAGE_CAPTURE)
    }

    private fun setImageUri(): Uri {

        val photoFile: File? = try {
            mPresenter.createImageFile(applicationContext, temp_files).apply {
                currentPhotoPath = absolutePath

            }
        } catch (ex: IOException) {
            // Error occurred while creating the File
            null
        }

        val imageUri = FileProvider.getUriForFile(
            this,
            "com.goflash.dispatch.fileprovider",
            photoFile!!
        )

        currentPhotoPath = photoFile.absolutePath
        return imageUri!!


    }

    private fun addIntentsToList(
        context: Context,
        list: MutableList<Intent>,
        intent: Intent
    ): MutableList<Intent> {
        val resInfo = context.packageManager.queryIntentActivities(intent, 0)
        for (resolveInfo in resInfo) {
            val packageName = resolveInfo.activityInfo.packageName
            val targetedIntent = Intent(intent)
            targetedIntent.setPackage(packageName)
            list.add(targetedIntent)
        }
        return list
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            showProgress()
            handleImageRequest(data)
        }
        if (requestCode == PDF_SELECTION_CODE && resultCode == Activity.RESULT_OK) {
            showProgress()
            handlePdfRequest(data)
        }
    }

    private fun handlePdfRequest(data: Intent?) {
        val exceptionHandler = CoroutineExceptionHandler { _, t ->
            t.printStackTrace()
            hideProgress()
            Toast.makeText(
                this,
                t.localizedMessage ?: getString(R.string.pdf_error),
                Toast.LENGTH_SHORT
            ).show()
        }

        GlobalScope.launch(Dispatchers.Main + exceptionHandler) {
            data?.data?.also { documentUri ->
                contentResolver?.takePersistableUriPermission(
                    documentUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                val file = getFile(this@AddCashPickupActivity, documentUri)//use pdf as file
                mPresenter.uploadFile(file, "PDF")
            }

        }
    }

    private fun handleImageRequest(data: Intent?) {
        val exceptionHandler = CoroutineExceptionHandler { _, t ->
            t.printStackTrace()
            hideProgress()
            Toast.makeText(
                this,
                t.localizedMessage ?: getString(R.string.comm_error),
                Toast.LENGTH_SHORT
            ).show()
        }

        GlobalScope.launch(Dispatchers.Main + exceptionHandler) {

            var queryImageUrl = ""

            val clipData = data?.clipData
            if (clipData != null && clipData.itemCount == 1) {

                val selectedImage = clipData.getItemAt(0).uri
                queryImageUrl = selectedImage?.path!!
                queryImageUrl = compressImageFile(
                    queryImageUrl,
                    false,
                    selectedImage!!,
                    this@AddCashPickupActivity
                )

            } else if (data?.data != null) {
                val selectedImage = data.data
                queryImageUrl = selectedImage?.path!!
                queryImageUrl = compressImageFile(
                    queryImageUrl,
                    false,
                    selectedImage!!,
                    this@AddCashPickupActivity
                )

            }

            if (clipData != null && clipData.itemCount > 1) {
                Toaster.show(baseContext, "You can upload a single image.")
            }

            if (data?.data == null || currentPhotoPath!!.isEmpty()) {
                val imageUri = Uri.fromFile(File(currentPhotoPath!!))
                val fileName = String.format("expense%d.jpg", System.currentTimeMillis())
                mPresenter.compressImage(
                    currentPhotoPath!!,
                    File(getAlbumStorageDir(this@AddCashPickupActivity, album_name2), fileName),
                    imageUri!!
                )
                mPresenter.deleteTempFiles(applicationContext, temp_files)
                queryImageUrl = File(
                    getAlbumStorageDir(this@AddCashPickupActivity, album_name2),
                    fileName
                ).toString()
                currentPhotoPath = null

            }

            mPresenter.uploadFile(File(queryImageUrl), "IMAGE")


        }
    }

    override fun onShowProgress() {
        showProgress()
    }

    override fun onHideProgress() {
        hideProgress()
    }

    override fun setFileUrl(url: String, type: String) {
        hideProgress()
        cashPickup?.cashDepositFileUrl = url
        if (type == "IMAGE")
            Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_add_image)
                .into(binding.layoutAddCashPickup.ivShowImage)
        else
            binding.layoutAddCashPickup.ivShowImage.setImageResource(R.drawable.ic_pdf)

        binding.layoutAddCashPickup.ivShowImage.visibility = View.VISIBLE
        binding.layoutAddCashPickup.ivRemove.visibility = View.VISIBLE

        setData(cashPickup)

        enableOrDisableSubmit()

    }

    private fun removeImageFromCashPickup() {
        cashPickup?.cashDepositFileUrl = null

        binding.layoutAddCashPickup.ivShowImage.setImageResource(R.drawable.ic_add_image)
        binding.layoutAddCashPickup.ivShowImage.visibility = View.GONE
        binding.layoutAddCashPickup.ivRemove.visibility = View.GONE

        enableOrDisableSubmit()

    }

    override fun setData(cashPickupDTO: CashPickupDTO?) {
        cashPickup = cashPickupDTO ?: CashPickupDTO()

        if (cashPickup?.cashDepositFileUrl != null) {
            binding.layoutAddCashPickup.ivShowImage.visibility = View.VISIBLE
            binding.layoutAddCashPickup.ivRemove.visibility = View.VISIBLE
        }

        var totalCash = 0L

        binding.layoutAddCashPickup.tvAmount2000.text = "${(cashPickup?.twoThousand ?: 0) * 2000}"
        totalCash += (cashPickup?.twoThousand ?: 0) * 2000

        binding.layoutAddCashPickup.tvAmount500.text = "${(cashPickup?.fiveHundred ?: 0) * 500}"
        totalCash += (cashPickup?.fiveHundred ?: 0) * 500

        binding.layoutAddCashPickup.tvAmount200.text = "${(cashPickup?.twoHundred ?: 0) * 200}"
        totalCash += (cashPickup?.twoHundred ?: 0) * 200

        binding.layoutAddCashPickup.tvAmount100.text = "${(cashPickup?.hundred ?: 0) * 100}"
        totalCash += (cashPickup?.hundred ?: 0) * 100

        binding.layoutAddCashPickup.tvAmount50.text = "${(cashPickup?.fifty ?: 0) * 50}"
        totalCash += (cashPickup?.fifty ?: 0) * 50

        binding.layoutAddCashPickup.tvAmount20.text = "${(cashPickup?.twenty ?: 0) * 20}"
        totalCash += (cashPickup?.twenty ?: 0) * 20

        binding.layoutAddCashPickup.tvAmount10.text = "${(cashPickup?.ten ?: 0) * 10}"
        totalCash += (cashPickup?.ten ?: 0) * 10

        binding.layoutAddCashPickup.tvAmount5.text = "${(cashPickup?.five ?: 0) * 5}"
        totalCash += (cashPickup?.five ?: 0) * 5

        binding.layoutAddCashPickup.tvAmount2.text = "${(cashPickup?.two ?: 0) * 2}"
        totalCash += (cashPickup?.two ?: 0) * 2

        binding.layoutAddCashPickup.tvAmount1.text = "${(cashPickup?.one ?: 0) * 1}"
        totalCash += (cashPickup?.one ?: 0) * 1

        binding.bottomLayout.tvTotalExpense.text = String.format(
            getString(R.string.cash_in_hand_text_formatted),
            getDecimalFormat(totalCash)
        )

        enableOrDisableSubmit()

    }

    private fun enableOrDisableSubmit() {
        if (checkCashPickupData() || !validateCashPickup()) {
            binding.bottomLayout.btnSubmit.isEnabled = false
            binding.bottomLayout.btnSubmit.setBackgroundResource(R.drawable.disable_button)
        } else {
            binding.bottomLayout.btnSubmit.isEnabled = true
            binding.bottomLayout.btnSubmit.setBackgroundResource(R.drawable.blue_button_background)
        }
    }

    private fun checkCashPickupData(): Boolean {
        return ((cashPickup?.twoThousand == null || cashPickup?.twoThousand == 0L)
                && (cashPickup?.fiveHundred == null || cashPickup?.fiveHundred == 0L)
                && (cashPickup?.twoHundred == null || cashPickup?.twoHundred == 0L)
                && (cashPickup?.hundred == null || cashPickup?.hundred == 0L)
                && (cashPickup?.fifty == null || cashPickup?.fifty == 0L)
                && (cashPickup?.twenty == null || cashPickup?.twenty == 0L)
                && (cashPickup?.ten == null || cashPickup?.ten == 0L)
                && (cashPickup?.five == null || cashPickup?.five == 0L)
                && (cashPickup?.two == null || cashPickup?.two == 0L)
                && (cashPickup?.one == null || cashPickup?.one == 0L)
                && cashPickup?.cashDepositFileUrl.isNullOrEmpty()
                && cashPickup?.cashDepositReceiptNumber.isNullOrEmpty())
    }

    override fun setEditTextData(cashPickup: CashPickupDTO?) {
        binding.layoutAddCashPickup.edt2000.setText(if (cashPickup?.twoThousand == null) "" else cashPickup.twoThousand?.toString())
        binding.layoutAddCashPickup.edt500.setText(if (cashPickup?.fiveHundred == null) "" else cashPickup.fiveHundred.toString())
        binding.layoutAddCashPickup.edt200.setText(if (cashPickup?.twoHundred == null) "" else cashPickup.twoHundred.toString())
        binding.layoutAddCashPickup.edt100.setText(if (cashPickup?.hundred == null) "" else cashPickup.hundred.toString())
        binding.layoutAddCashPickup.edt50.setText(if (cashPickup?.fifty == null) "" else cashPickup.fifty.toString())
        binding.layoutAddCashPickup.edt20.setText(if (cashPickup?.twenty == null) "" else cashPickup.twenty.toString())
        binding.layoutAddCashPickup.edt10.setText(if (cashPickup?.ten == null) "" else cashPickup.ten.toString())
        binding.layoutAddCashPickup.edt5.setText(if (cashPickup?.five == null) "" else cashPickup.five.toString())
        binding.layoutAddCashPickup.edt2.setText(if (cashPickup?.two == null) "" else cashPickup.two.toString())
        binding.layoutAddCashPickup.edt1.setText(if (cashPickup?.one == null) "" else cashPickup.one.toString())

        binding.layoutAddCashPickup.edtCashReceipt.setText(cashPickup?.cashDepositReceiptNumber)
    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)
    }

    override fun finishActivity() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.onDetachView()
    }
}