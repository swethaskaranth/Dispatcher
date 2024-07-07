package com.goflash.dispatch.features.lastmile.settlement.ui.activity

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
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.CAMERA_REQ
import com.goflash.dispatch.app_constants.REQUEST_IMAGE_CAPTURE
import com.goflash.dispatch.app_constants.album_name2
import com.goflash.dispatch.app_constants.sprinter_name
import com.goflash.dispatch.data.AckForRecon
import com.goflash.dispatch.databinding.LayoutVerifyImagesBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.lastmile.settlement.presenter.VerifyImagesPresenter
import com.goflash.dispatch.features.lastmile.settlement.ui.adapter.PODSpinnerAdapter
import com.goflash.dispatch.features.lastmile.settlement.ui.adapter.VerifyImageAdapter
import com.goflash.dispatch.features.lastmile.settlement.view.VerifyImagesView
import com.goflash.dispatch.listeners.VerifyImageListener
import com.goflash.dispatch.ui.activity.BaseActivity
import com.goflash.dispatch.ui.itemDecoration.MarginItemDecoration
import com.goflash.dispatch.util.Toaster
import com.goflash.dispatch.util.compressImageFile
import com.goflash.dispatch.util.getAlbumStorageDir
import com.goflash.dispatch.util.temp_files
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.*
import javax.inject.Inject

class Step4VerifyImagesActivity : BaseActivity(), VerifyImagesView, View.OnClickListener,
    VerifyImageListener {

    @Inject
    lateinit var mPresenter: VerifyImagesPresenter

    private var tripId: Long? = null
    private var sprinterName: String? = null

    private var selectedOrderId: String? = null

    private var currentPhotoPath: String? = ""

    private lateinit var binding: LayoutVerifyImagesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutVerifyImagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tripId = intent.getStringExtra("tripId")?.toLong()
        sprinterName = intent.getStringExtra("sprinterName")

        initDagger()
        initViews()

        val count = intent?.getIntExtra("count", 0)
        count?.let {
            if (count > 0)
                Snackbar.make(binding.btnPaymentLayout.flSave, "$count Images Approved", Snackbar.LENGTH_SHORT)
                    .show()
        }
    }

    private fun initViews() {
        binding.toolBar.toolbarTitle.text = getString(R.string.verify_pod_images)
        binding.toolBar.tvSprinter.text =
            String.format(getString(R.string.trip_id_with_sprinter), tripId, sprinterName)
        binding.btnPaymentLayout.btnPayment.text = getString(R.string.proceed)

        binding.btnPaymentLayout.btnPayment.setOnClickListener(this)
        binding.toolBar.ivBack.setOnClickListener(this)
        binding.clUploadImages.setOnClickListener(this)

        binding.rvReviewOrders.layoutManager = LinearLayoutManager(this)
        binding.rvReviewOrders.addItemDecoration(
            MarginItemDecoration(
                resources.getDimension(R.dimen.margin_10).toInt()
            )
        )

        binding.spOrder.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (selectedOrderId == null && position != 0) {
                    selectedOrderId = (parent?.getItemAtPosition(position) as AckForRecon).lbn
                }

            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }
    }

    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent()).build()
            .inject(this@Step4VerifyImagesActivity)

        mPresenter.onAttachView(this, this)
        mPresenter.setTripId(tripId!!)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivBack -> finish()
            R.id.clUploadImages -> selectedOrderId?.let {
                uploadImages()
            }
            R.id.btn_payment -> mPresenter.onNext(tripId!!)
        }
    }

    override fun onResume() {
        super.onResume()
        mPresenter.getData()
    }

    override fun onAckSlipsFetched(ackSlips: MutableList<AckForRecon>) {
        binding.rvReviewOrders.adapter = VerifyImageAdapter(this, ackSlips, this)
        val orderList: MutableList<AckForRecon> = mutableListOf()
        orderList.addAll(ackSlips)
        orderList.add(0, AckForRecon("Select Order", null, null))
        binding.spOrder.adapter = PODSpinnerAdapter(orderList)

    }

    override fun enableOrDisableProceed(disable: Boolean) {
        if (disable) {
            binding.btnPaymentLayout.btnPayment.isEnabled = false
            binding.btnPaymentLayout.btnPayment.setBackgroundResource(R.drawable.disable_button)
        } else {
            binding.btnPaymentLayout.btnPayment.isEnabled = true
            binding.btnPaymentLayout.btnPayment.setBackgroundResource(R.drawable.blue_button_background)
        }
    }

    override fun onOrderSelected(lbn: String) {
        mPresenter.onOrderSelected(lbn)

    }

    override fun startReviewActivity(lbn: String) {
        val intent = Intent(this@Step4VerifyImagesActivity, ReviewImagesActivity::class.java)
        intent.putExtra("lbn", lbn)
        intent.putExtra("tripId", tripId)
        intent.putExtra("sprinterName", sprinterName)
        startActivity(intent)
    }

    private fun uploadImages() {
        if (selectedOrderId != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                if (checkPermissions())
                    dispatchTakePictureIntent()
                else
                    dispatchTakePictureIntent()
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
                    this@Step4VerifyImagesActivity
                )

            } else if (data?.data != null) {
                val selectedImage = data.data
                queryImageUrl = selectedImage?.path!!
                queryImageUrl = compressImageFile(
                    queryImageUrl,
                    false,
                    selectedImage!!,
                    this@Step4VerifyImagesActivity
                )

            }

            if (clipData != null && clipData.itemCount > 1) {
                Toaster.show(baseContext, "You can upload a single image.")
            }

            if (data?.data == null || currentPhotoPath!!.isEmpty()) {
                val imageUri = Uri.fromFile(File(currentPhotoPath!!))
                val fileName = String.format("AckSlip%d.jpg", System.currentTimeMillis())
                mPresenter.compressImage(
                    currentPhotoPath!!,
                    File(getAlbumStorageDir(this@Step4VerifyImagesActivity, album_name2), fileName),
                    imageUri!!
                )
                mPresenter.deleteTempFiles(applicationContext, temp_files)
                queryImageUrl = File(
                    getAlbumStorageDir(this@Step4VerifyImagesActivity, album_name2),
                    fileName
                ).toString()
                currentPhotoPath = null

            }

            mPresenter.uploadFile(File(queryImageUrl), selectedOrderId!!)


        }
    }

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

    override fun onAckSlipUploaded() {
        onHideProgress()
        selectedOrderId = null
        mPresenter.getData()
        binding.spOrder.setSelection(0)
    }

    override fun onShowProgress() {
        showProgress()
    }

    override fun onHideProgress() {
        hideProgress()
    }

    override fun onFailure(error: Throwable?) {
        processError(error)
    }

    override fun goToStep3Activity() {
        val intent = Intent(this, Step3CashCollectionActivity::class.java)
        intent.putExtra("tripId", tripId.toString())
        intent.putExtra(sprinter_name, sprinterName)
        startActivity(intent)
    }

    override fun startAckDeliverySlipReconActivity() {
        val intent = Intent(this, AckDeliverySlipReconActivity::class.java)
        intent.putExtra("tripId", tripId.toString())
        intent.putExtra(sprinter_name, sprinterName)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.onDetachView()
    }
}