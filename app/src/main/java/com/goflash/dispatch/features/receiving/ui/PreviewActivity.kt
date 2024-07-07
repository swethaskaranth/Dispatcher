package com.goflash.dispatch.features.receiving.ui

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.view.View
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.album_name
import com.goflash.dispatch.app_constants.temp_files
import com.goflash.dispatch.databinding.ActivityPreviewBinding
import com.goflash.dispatch.features.receiving.presenter.PreviewPresenter
import com.goflash.dispatch.features.receiving.view.PreviewView
import com.goflash.dispatch.ui.activity.BaseActivity
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.util.getAlbumStorageDir
import java.io.File
import java.io.IOException
import javax.inject.Inject



class PreviewActivity : BaseActivity(), PreviewView, View.OnClickListener {

    private val TAG: String = PreviewActivity::class.java.name
    private val CAMERA_REQ = 100
    private var mImageBitmap: Bitmap? = null

    @Inject
    lateinit var mPresenter: PreviewPresenter

    private val REQUEST_IMAGE_CAPTURE = 1
    private var currentPhotoPath: String? = null

    private var fileName = ""

    private var position = -1

    private lateinit var binding: ActivityPreviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        position = intent.getIntExtra("position",-1)

        initDagger()
        initViews()
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun initViews() {
        binding.toolBar1.toolbarTitle.text = getString(R.string.preview)

        binding.ivClick.setOnClickListener(this)
        binding.ivRetry.setOnClickListener(this)
        binding.ivUpload.setOnClickListener(this)

        val toolbar = findViewById<View>(R.id.toolBar1) as Toolbar
        setSupportActionBar(toolbar)

        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back_button_circular)
            setDisplayShowTitleEnabled(false)
        }

        if(checkPermissions())
            dispatchTakePictureIntent()
    }

    private fun initDagger() {
        DaggerActivityComponents.builder().networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this@PreviewActivity)

        mPresenter.onAttachView(this, this)
    }

    override fun onBackPressed() {
        upload()
    }

    private fun upload(){
        val intent = Intent()
        intent.putExtra("filePath", fileName)
        intent.putExtra("position",position)
        setResult(Activity.RESULT_OK, intent)

        finish()
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onClick(v: View?) {

        when(v?.id){

            R.id.iv_click -> {
                if(checkPermissions())
                    dispatchTakePictureIntent()}

            R.id.iv_retry -> {
                if(checkPermissions())
                    dispatchTakePictureIntent()}

            R.id.iv_upload -> upload()
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    mPresenter.createImageFile(applicationContext, temp_files).apply {
                        currentPhotoPath = absolutePath
                    }
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.goflash.dispatch.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            try {

                if (currentPhotoPath != null) {
                    fileName = String.format("Acknowledgement%d.jpg", System.currentTimeMillis())
                    mPresenter.compressImage(
                        currentPhotoPath!!,
                        File(getAlbumStorageDir(this@PreviewActivity,album_name),fileName )
                    )
                    mPresenter.deleteTempFiles(this@PreviewActivity, temp_files)

                    displayImage()
                    showRetry()
                }

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun displayImage() {

        //val dir = getStorageDirectory(applicationContext, album_name)
        val file = File(getAlbumStorageDir(this@PreviewActivity,album_name),fileName)
        if (file.exists()) {
            mImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, Uri.fromFile(file))
            binding.imageView.setImageBitmap(mImageBitmap)
        }


    }

    private fun showRetry(){
        binding.ivRetry.visibility = View.VISIBLE
        binding.ivUpload.visibility = View.VISIBLE
        binding.ivClick.visibility = View.INVISIBLE
    }

    override fun showImagePath(path: String) {
        currentPhotoPath = path
    }

    /**
     * Method to check permission for camera and storage, put in login time due to need at multiple places so asked in advance
     * */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermissions(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),
                CAMERA_REQ)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_REQ -> {
                // If request is CANCELLED, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    dispatchTakePictureIntent()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
    }

    override fun onSuccess() {
        hideProgress()
    }

    override fun onShowProgress() {

    }

    override fun onHideProgress() {

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.onDetachView()
    }
}
