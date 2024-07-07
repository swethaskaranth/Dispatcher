package com.goflash.dispatch.ui.activity

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
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.CAMERA_REQ
import com.goflash.dispatch.app_constants.REQUEST_IMAGE_CAPTURE
import com.goflash.dispatch.app_constants.album_name2
import com.goflash.dispatch.databinding.ActivityRaiseTicketBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.listeners.OnItemSelected
import com.goflash.dispatch.presenter.RaiseTicketPresenter
import com.goflash.dispatch.presenter.views.RaiseTicketView
import com.goflash.dispatch.ui.adapter.CreditImageAdapter
import com.goflash.dispatch.util.*
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.*
import javax.inject.Inject

class RaiseTicketActivity : BaseActivity(), RaiseTicketView, View.OnClickListener, TextWatcher,
    OnItemSelected, View.OnFocusChangeListener, AdapterView.OnItemSelectedListener {

    private val TAG = RaiseTicketActivity::class.java.simpleName

    @Inject
    lateinit var mPresenter: RaiseTicketPresenter

    private var adapter: CreditImageAdapter? = null

    private var queryImageUrl: String = ""
    private var selectedImage: Uri? = null
    private var fileList = mutableListOf<File>()
    private var listImage = mutableListOf<String>()

    private var currentPhotoPath: String? = ""
    private var imageUri: Uri? = null
    private var fileName = ""

    private var emailIdList = mutableListOf<String>()
    private var autoAdapter: ArrayAdapter<String>? = null
    private var alertDialog: AlertDialog.Builder?  = null

    private var priorities = arrayOf("Select Severity of Issue", "Urgent", "Non Urgent")
    private var priority: String? = null

    private lateinit var binding: ActivityRaiseTicketBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        initDagger()
        super.onCreate(savedInstanceState)
        binding = ActivityRaiseTicketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()

    }

    private fun initDagger() {
        DaggerActivityComponents.builder().networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this@RaiseTicketActivity)

        mPresenter.onAttachView(this, this)
    }

    private fun initViews() {

        binding.toolBar.toolbarTitle.text = getString(R.string.raise_ticket)

        binding.layoutFeedback.btn.btnPayment.text = getString(R.string.submit)

        binding.layoutFeedback.ivAddImage.setOnClickListener(this)
        binding.toolBar.iVProfileHome.setOnClickListener(this)
        binding.layoutFeedback.btn.btnPayment.setOnClickListener(this)
        binding.layoutFeedback.layoutCc.editCc.onFocusChangeListener = this
        binding.layoutFeedback.ivAddImage.onFocusChangeListener = this
        binding.layoutFeedback.spinnerPriority.onItemSelectedListener = this

        binding.layoutFeedback.layoutCc.editCc.addTextChangedListener(this)

        binding.layoutFeedback.btn.btnPayment.isEnabled = false
        binding.layoutFeedback.btn.btnPayment.setBackgroundResource(R.drawable.disable_button)

        autoAdapter = ArrayAdapter<String>(
            this, android.R.layout.simple_dropdown_item_1line, emailIdList
        )

        binding.layoutFeedback.layoutCc.editCc.setAdapter<ArrayAdapter<String>>(autoAdapter)
        binding.layoutFeedback.layoutCc.editCc.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {   // KeyEvent: If triggered by an enter key, this is the event; otherwise, this is null.
                showView(v.text.toString() + " ")
                true
            } else {
                false
            }
        }

        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.layoutFeedback.spinnerPriority.adapter = arrayAdapter
    }

    private fun addChipToGroup(person: String, chipGroup: ChipGroup) {
        val chip = Chip(this)
        chip.text = person
        chip.isCloseIconEnabled = true
        chip.setChipIconTintResource(R.color.chipIconTint)
        // necessary to get single selection working
        chip.isClickable = true
        chip.isCheckable = false
        chipGroup.addView(chip as View)
        emailIdList.add(chip.text.toString())
        //Log.d(TAG, "On addChipToGroup ${chip.text}")
        chip.setOnCloseIconClickListener {
            chipGroup.removeView(chip as View)
            emailIdList.remove(chip.text.toString())
            autoAdapter?.notifyDataSetChanged()
            checkValidation()
        }
    }

    override fun afterTextChanged(s: Editable?) {
        showView(s.toString())
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    private fun showView(s: String) {
        var trimmed = s
        if (trimmed.length > 1 && trimmed.endsWith(",") || trimmed.endsWith(" ")) {
            trimmed = s.toString().trim { it <= ' ' }
            trimmed = trimmed.replace(",", "")
            if (isValidEmail(trimmed)) {
                addChipToGroup(trimmed, binding.layoutFeedback.layoutCc.chipGroup2)
                binding.layoutFeedback.layoutCc.editCc.text.clear()
            } else
                Toaster.show(this, "Please enter valid email Id")
        }
    }


    override fun onClick(v: View?) {
        when(v?.id) {

            R.id.iVProfileHome -> finish()

            R.id.btn_payment -> validation()

            R.id.iv_add_image -> showImageOption()
        }
    }

    private fun dispatchTakePictureIntent() {
        if(fileList.size < 5) {

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
                chooserIntent!!.putExtra(
                    Intent.EXTRA_INITIAL_INTENTS,
                    intentList.toTypedArray<Parcelable>()
                )
            }

            startActivityForResult(chooserIntent, REQUEST_IMAGE_CAPTURE)
        }else{
            Toaster.show(this, "You can upload maximum 5 images.")
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            showProgress()
            handleImageRequest(data)
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

            val clipData = data?.clipData
            if(clipData != null && clipData.itemCount < 5 && fileList.size < 5){
                for(i in 0 until clipData.itemCount){
                    selectedImage = clipData.getItemAt(i).uri
                    queryImageUrl = selectedImage?.path!!
                    queryImageUrl = compressImageFile(queryImageUrl, false, selectedImage!!, this@RaiseTicketActivity)

                    fileList.add(File(queryImageUrl))
                    listImage.add(queryImageUrl)
                }
            }else if(data?.data != null && fileList.size < 5){
                selectedImage = data.data
                queryImageUrl = selectedImage?.path!!
                queryImageUrl = compressImageFile(queryImageUrl, false, selectedImage!!, this@RaiseTicketActivity)
                fileList.add(File(queryImageUrl))
                listImage.add(queryImageUrl)

            }

            if(clipData != null && clipData.itemCount >= 5 || fileList.size >= 5){
                Toaster.show(baseContext, "You can upload maximum 5 images.")
            }

            if (data?.data == null || currentPhotoPath!!.isEmpty() && fileList.size < 5) {
                imageUri = Uri.fromFile(File(currentPhotoPath!!))
                fileName = String.format("ticket%d.jpg", System.currentTimeMillis())
                mPresenter.compressImage(
                    currentPhotoPath!!,
                    File(getAlbumStorageDir(this@RaiseTicketActivity, album_name2),fileName), imageUri!!)
                mPresenter.deleteTempFiles(applicationContext, temp_files)
                queryImageUrl = File(getAlbumStorageDir(this@RaiseTicketActivity,album_name2),fileName).toString()
                fileList.add(File(queryImageUrl))
                listImage.add(queryImageUrl)
                currentPhotoPath = null

            }

            hideProgress()
            binding.layoutFeedback.llImages.visibility = View.VISIBLE
            creditImage()
            checkValidation()
        }
    }

    private fun validation() {

        if(!checkValidation())
            return

        var emailId = ""
        if (emailIdList.isNotEmpty() && emailIdList.size <= 10){
            emailId = TextUtils.join(",", emailIdList)
        }
        //Log.d(TAG, emailId)
        showProgress()
        mPresenter.uploadFile(fileList, binding.layoutFeedback.editFeedback.text.toString(), binding.layoutFeedback.editTitle.text.toString(), emailId,
            priority?:return
        )
    }

    private fun checkValidation(): Boolean{

        if(binding.layoutFeedback.editTitle.text.isEmpty()) {
            Toaster.show(this, "Please enter title")
            return false
        }

        if(binding.layoutFeedback.editFeedback.text.isEmpty()) {
            Toaster.show(this, "Please enter description")
            return false
        }

        if(listImage.size == 0){
            return false
        }

        if(emailIdList.size > 10) {
            Toaster.show(this, "Not more than 10 email Id allowed.")
            return false
        }

        if(priority == null){
            Toaster.show(this, "Please select priority.")
            return false
        }

        binding.layoutFeedback.btn.btnPayment.isEnabled = true
        binding.layoutFeedback.editTitle.setBackgroundResource(R.drawable.blue_button_background)
        return true
    }

    private fun creditImage(){
        adapter = CreditImageAdapter(this, listImage, true, this)
        binding.layoutFeedback.rvCreditImage.layoutManager = GridLayoutManager(this, 4)
        binding.layoutFeedback.rvCreditImage.adapter = adapter
    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        if(error != null)
            processError(error)
    }

    override fun onSuccess() {
        hideProgress()
        message()
    }

    private fun message() {
        alertDialog = AlertDialog.Builder(this, R.style.CustomAlertDialog)

        val dialogView = layoutInflater.inflate(R.layout.layout_dialog, null)
        alertDialog?.setView(dialogView)
        val txtDone =  dialogView.findViewById<TextView>(R.id.txtDone)
        txtDone.setOnClickListener {
            val intent = Intent(this@RaiseTicketActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
        alertDialog?.create()?.show()
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQ -> {
                // If request is CANCELLED, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    dispatchTakePictureIntent()
                }
                return
            }
        }
    }

    override fun onStop() {
        super.onStop()
        alertDialog?.create()?.cancel()

    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.onDetachView()
    }

    private fun showImageOption(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (checkPermissions())
                dispatchTakePictureIntent()
            else
                dispatchTakePictureIntent()
    }


    override fun onItemSelected(position: Int) {
        fileList.removeAt(position)
        listImage.removeAt(position)
        checkValidation()
        binding.layoutFeedback.rvCreditImage.adapter?.notifyDataSetChanged()
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        when(v?.id){
            R.id.edit_cc -> {
                if(!hasFocus)
                    showView(binding.layoutFeedback.layoutCc.editCc.text.toString() + " ")
            }

            R.id.iv_add_image -> {
                if(hasFocus)
                    showImageOption()
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        priority = when (position) {
            1 -> "Urgent"
            2 -> "Non-Urgent"
            else -> null
        }

        checkValidation()
    }
}