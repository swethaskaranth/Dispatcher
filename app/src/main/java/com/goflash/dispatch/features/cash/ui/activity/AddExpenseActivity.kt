package com.goflash.dispatch.features.cash.ui.activity

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.view.View
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
import com.goflash.dispatch.app_constants.STORAGE_REQ
import com.goflash.dispatch.app_constants.album_name2
import com.goflash.dispatch.data.ExpenseDTO
import com.goflash.dispatch.databinding.LayoutAddAllExpensesBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.cash.listeners.AddExpenseListener
import com.goflash.dispatch.features.cash.listeners.FileTypeSelectListener
import com.goflash.dispatch.features.cash.presenter.AddExpensePresenter
import com.goflash.dispatch.features.cash.ui.adapter.ExpenseAdapter
import com.goflash.dispatch.features.cash.ui.fragment.BottomSheetChooseFileFragment
import com.goflash.dispatch.features.cash.view.AddExpenseView
import com.goflash.dispatch.ui.activity.BaseActivity
import com.goflash.dispatch.ui.itemDecoration.VerticalMarginItemDecoration
import com.goflash.dispatch.util.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.*
import javax.inject.Inject

class AddExpenseActivity : BaseActivity(), AddExpenseView, View.OnClickListener,
    AddExpenseListener, FileTypeSelectListener {

    @Inject
    lateinit var mPresenter: AddExpensePresenter

    private val PDF_SELECTION_CODE = 2

    private var imagePosition = -1

    private var currentPhotoPath: String? = ""

    private lateinit var binding: LayoutAddAllExpensesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutAddAllExpensesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initDagger()
        initViews()
    }

    private fun initViews() {
        binding.toolbar.toolbarTitle.text = getString(R.string.add_expense)

        binding.rvExpense.layoutManager = LinearLayoutManager(this)
        binding.rvExpense.addItemDecoration(
            VerticalMarginItemDecoration(
                resources.getDimension(R.dimen.margin_10).toInt()
            )
        )

        binding.btnSubmit.btnSubmit.text = getString(R.string.submit)
        binding.btnSubmit.btnSubmit.isEnabled = false
        binding.btnSubmit.btnSubmit.setBackgroundResource(R.drawable.disable_button)

        binding.btnSubmit.btnSubmit.setOnClickListener(this)
        binding.tvAddExpense.setOnClickListener(this)
        binding.toolbar.iVProfileHome.setOnClickListener(this)

        mPresenter.getExpenses()
    }

    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this)

        mPresenter.onAttachView(this, this)

    }

    override fun onExpenseFetched(expenses: MutableList<ExpenseDTO>) {
        hideProgress()
        imagePosition = -1
        binding.rvExpense.adapter = ExpenseAdapter(this, expenses, this)

    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.tvAddExpense -> mPresenter.addNewExpense()
            R.id.btn_submit -> mPresenter.submitExpenses()
            R.id.iVProfileHome -> finish()

        }
    }

    override fun onUpload(position: Int) {

        imagePosition = position

        val bottomSheetFragment = BottomSheetChooseFileFragment()
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)

        /*val intent = Intent(this,PreviewActivity::class.java)
        intent.putExtra("position",position)
        startActivityForResult(intent, PREVIEW)*/

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

    private fun openPDFIntent(){
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
                val file = getFile(this@AddExpenseActivity,documentUri)//use pdf as file
                mPresenter.uploadFile(file,imagePosition,"PDF")
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
                    this@AddExpenseActivity
                )

            } else if (data?.data != null) {
                val selectedImage = data.data
                queryImageUrl = selectedImage?.path!!
                queryImageUrl = compressImageFile(
                    queryImageUrl,
                    false,
                    selectedImage!!,
                    this@AddExpenseActivity
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
                    File(getAlbumStorageDir(this@AddExpenseActivity, album_name2), fileName),
                    imageUri!!
                )
                mPresenter.deleteTempFiles(applicationContext, temp_files)
                queryImageUrl = File(
                    getAlbumStorageDir(this@AddExpenseActivity, album_name2),
                    fileName
                ).toString()
                currentPhotoPath = null

            }

            mPresenter.uploadFile(File(queryImageUrl), imagePosition, "IMAGE")


        }
    }

    override fun deleteImage(position: Int) {
        mPresenter.deleteVoucherImage(position)
    }

    override fun onAmountEntered() {
        mPresenter.getTotalExpenses()
    }

    override fun deleteExpense(position: Int) {
        mPresenter.deleteExpense(position)
    }

    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_CANCELED && requestCode == PREVIEW && data?.getStringExtra(
                "filePath"
            ) != null
        ) {
            try {
                mPresenter.uploadFile(
                    File(
                        getAlbumStorageDir(this@AddExpenseActivity, album_name),
                        data.getStringExtra("filePath")
                    ), data.getIntExtra("position", -1)
                )

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }*/

    override fun setTotalExpense(expenseCount: Int, expense: Long, enable: Boolean) {
        binding.btnSubmit.labelTotalExpense.text = String.format(getString(R.string.total_expenses), expenseCount)
        binding.btnSubmit.tvTotalExpense.text = String.format(getString(R.string.cash_in_hand_text_formatted), getDecimalFormat(expense))

        if (enable) {
            binding.btnSubmit.btnSubmit.isEnabled = true
            binding.btnSubmit.btnSubmit.setBackgroundResource(R.drawable.blue_button_background)
        } else {
            binding.btnSubmit.btnSubmit.isEnabled = false
            binding.btnSubmit.btnSubmit.setBackgroundResource(R.drawable.disable_button)
        }
    }

    override fun finishActivity() {
        finish()
    }

    override fun onShowProgress() {
        showProgress()
    }

    override fun onHideProgress() {
        hideProgress()
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.onDetachView()
    }


}