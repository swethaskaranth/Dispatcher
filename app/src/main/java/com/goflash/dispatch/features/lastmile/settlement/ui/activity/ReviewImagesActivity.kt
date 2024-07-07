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
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.CAMERA_REQ
import com.goflash.dispatch.app_constants.REQUEST_IMAGE_CAPTURE
import com.goflash.dispatch.app_constants.album_name2
import com.goflash.dispatch.data.AckSlipDto
import com.goflash.dispatch.databinding.LayoutSelectImagesBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.lastmile.settlement.presenter.ReviewImagesPresenter
import com.goflash.dispatch.features.lastmile.settlement.ui.adapter.ReviewImageAdapter
import com.goflash.dispatch.features.lastmile.settlement.view.ReviewImagesView
import com.goflash.dispatch.listeners.ImageClickListener
import com.goflash.dispatch.type.AckStatus
import com.goflash.dispatch.ui.activity.BaseActivity
import com.goflash.dispatch.util.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.*
import javax.inject.Inject

class ReviewImagesActivity : BaseActivity(), ReviewImagesView, View.OnClickListener,
    ImageClickListener {

    @Inject
    lateinit var mPresenter: ReviewImagesPresenter

    private var tripId: Long? = null
    private var sprinterName: String? = null
    lateinit var lbn: String

    private var currentPhotoPath: String? = ""

    private lateinit var reviewImageAdapter: ReviewImageAdapter

    private lateinit var binding: LayoutSelectImagesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutSelectImagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tripId = intent.getLongExtra("tripId", -1)
        sprinterName = intent.getStringExtra("sprinterName")
        lbn = intent.getStringExtra("lbn") ?: ""

        initDagger()
        initViews()
    }

    private fun initViews() {
        binding.toolBar.toolbarTitle.text = lbn
        binding.toolBar.tvSprinter.text =
            String.format(getString(R.string.trip_id_with_sprinter), tripId, sprinterName)
        binding.toolBar.ivBack.setOnClickListener(this)
        binding.btnApprove.btnApporve.setOnClickListener(this)
        binding.btnApprove.btnUpload.setOnClickListener(this)
        setApproveButton(0)
    }

    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent()).build()
            .inject(this@ReviewImagesActivity)

        mPresenter.onAttach(this, this)

    }

    override fun onResume() {
        super.onResume()

        mPresenter.getAckSlipsForLBN(tripId!!, lbn)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivBack -> finish()
            R.id.btn_apporve -> mPresenter.approveImages()
            R.id.btn_upload -> uploadImages()
        }
    }

    override fun onAckSlipsFetched(slips: MutableList<AckSlipDto>) {
        binding.rvReviewOrders.layoutManager = GridLayoutManager(this, 2)
        reviewImageAdapter =  ReviewImageAdapter(this, slips, this)
        binding.rvReviewOrders.adapter = reviewImageAdapter

        binding.rvReviewOrders.addItemDecoration(GridItemDecoration(resources.getDimension(R.dimen.margin_8).toInt()))

    }


    override fun onImageClicked(position: Int) {
        mPresenter.onItemClicked(position)
    }

    override fun onImageApproved(position: Int) {
        mPresenter.onItemSelected(position, AckStatus.ACCEPTED)
    }

    override fun onImageRejected(position: Int) {
        mPresenter.onItemSelected(position, AckStatus.REJECTED)
    }
    override fun setApproveButton(count: Int) {
        if (count > 0) {
            //btn_apporve.text = String.format(getString(R.string.approve_with_count), count)
            binding.btnApprove.btnApporve.setBackgroundResource(R.drawable.blue_button_background)
            binding.btnApprove.btnApporve.isEnabled = true
        } else {
            //btn_apporve.text = String.format(getString(R.string.approve))
            binding.btnApprove.btnApporve.setBackgroundResource(R.drawable.disable_button)
            binding.btnApprove.btnApporve.isEnabled = false
        }
    }

    override fun onImagesApproved(count: Int) {
        val intent = Intent(this@ReviewImagesActivity, Step4VerifyImagesActivity::class.java)
        intent.putExtra("count", count)
        intent.putExtra("tripId", tripId.toString())
        intent.putExtra("sprinterName", sprinterName)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)

    }

    private fun uploadImages(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (checkPermissions())
                dispatchTakePictureIntent()
            else
                dispatchTakePictureIntent()
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
                    this@ReviewImagesActivity
                )

            } else if (data?.data != null) {
                val selectedImage = data.data
                queryImageUrl = selectedImage?.path!!
                queryImageUrl = compressImageFile(
                    queryImageUrl,
                    false,
                    selectedImage!!,
                    this@ReviewImagesActivity
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
                    File(getAlbumStorageDir(this@ReviewImagesActivity, album_name2), fileName),
                    imageUri!!
                )
                mPresenter.deleteTempFiles(applicationContext, temp_files)
                queryImageUrl = File(
                    getAlbumStorageDir(this@ReviewImagesActivity, album_name2),
                    fileName
                ).toString()
                currentPhotoPath = null

            }

            mPresenter.uploadFile(File(queryImageUrl), lbn)


        }
    }

    override fun onAckSlipUploaded(ackSlipDto: AckSlipDto) {
        hideProgress()
        reviewImageAdapter.addAckSLip(ackSlipDto)
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
    ){
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

    override fun onShowProgress() {
        showProgress()
    }

    override fun onHideProgress() {
        hideProgress()
    }

    override fun onFailure(error: Throwable?) {
        processError(error)
    }

    override fun startApproveActivity(position: Int, lbn: String) {
        val intent = Intent(this@ReviewImagesActivity, ApproveImageActivity::class.java)
        intent.putExtra("position", position)
        intent.putExtra("tripId", tripId)
        intent.putExtra("sprinterName", sprinterName)
        intent.putExtra("lbn", lbn)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.onDetach()
    }
}