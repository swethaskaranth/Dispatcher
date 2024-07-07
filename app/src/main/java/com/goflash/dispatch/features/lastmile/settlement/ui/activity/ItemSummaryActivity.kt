package com.goflash.dispatch.features.lastmile.settlement.ui.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.*
import com.goflash.dispatch.data.Item
import com.goflash.dispatch.databinding.LayoutReceivedItemSummaryBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.lastmile.settlement.listeners.ReconImageSelectListener
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.OnItemSelctedListener
import com.goflash.dispatch.features.lastmile.settlement.presenter.ItemSummaryPresenter
import com.goflash.dispatch.features.lastmile.settlement.ui.adapter.ItemSummaryAdapter
import com.goflash.dispatch.features.lastmile.settlement.ui.adapter.ReconImageAdapter
import com.goflash.dispatch.features.lastmile.settlement.view.ItemSummaryView
import com.goflash.dispatch.ui.activity.BaseActivity
import com.goflash.dispatch.ui.itemDecoration.MarginItemDecoration
import com.goflash.dispatch.util.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import javax.inject.Inject

class ItemSummaryActivity : BaseActivity(), ItemSummaryView, OnItemSelctedListener,
    View.OnClickListener, ReconImageSelectListener {

    @Inject
    lateinit var mPresenter: ItemSummaryPresenter

    private var tripId: Long? = null

    private var sprinter: String? = null

    private var refId: String? = null

    private var shipmentId: String? = null

    private var partialDelivery = false

    private var lastShipment = false

    private var currentPhotoPath: String? = ""

    private val reconImages: HashMap<String, String> = HashMap()

    private lateinit var binding: LayoutReceivedItemSummaryBinding

    private lateinit var adapter: ReconImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutReceivedItemSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tripId = intent.getLongExtra(trip_id, 0)
        sprinter = intent.getStringExtra(sprinter_name)
        refId = intent.getStringExtra(reference_id)
        shipmentId = intent?.getStringExtra(shipment_id)
        partialDelivery = intent?.getBooleanExtra(partial_delivery, false) ?: false

        initDagger()
        initViews()
    }

    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this@ItemSummaryActivity)

        mPresenter.onAttachView(this, this)

        mPresenter.setShipmentId(shipmentId!!, tripId!!, partialDelivery)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.getBooleanExtra(refresh, false) == true)
            mPresenter.getItems()
    }


    private fun initViews() {
        deleteTempFiles(this, album_name2)
        mPresenter.deleteImages(shipmentId!!)
        binding.toolBar.toolbarTitle.text = "Trip Settlement (2/3)"
        binding.toolBar.tvSprinter.text =
            String.format(getString(R.string.trip_id_with_sprinter), tripId, sprinter)
        binding.labelSummary.text =
            String.format(getString(R.string.item_received_summary_header), refId)

        binding.btnPaymentLayout.btnPayment.text = if (lastShipment)
            getString(R.string.proceed)
        else
            getString(R.string.scan_next_shipment)

        binding.toolBar.ivBack.setOnClickListener(this)
        binding.btnPaymentLayout.btnPayment.setOnClickListener(this)

        binding.rvItems.layoutManager = LinearLayoutManager(this)
        binding.rvItems.addItemDecoration(
            MarginItemDecoration(
                resources.getDimension(R.dimen.margin_10).toInt()
            )
        )

        binding.tvUploadImages.visibility = View.VISIBLE
        binding.rvImages.layoutManager = GridLayoutManager(this, 4)

        adapter = ReconImageAdapter(this, reconImages, this)
        binding.rvImages.adapter = adapter

    }

    override fun onItemsFetched(items: List<Item>) {
        binding.rvItems.adapter = ItemSummaryAdapter(this, items, this)
    }

    override fun enableOrDisableProceed(disable: Boolean) {
        binding.btnPaymentLayout.btnPayment.isEnabled = if (disable) {
            binding.btnPaymentLayout.btnPayment.setBackgroundResource(R.drawable.grey_button_background)
            false
        } else {
            binding.btnPaymentLayout.btnPayment.setBackgroundResource(R.drawable.blue_button_background)
            true
        }
    }


    override fun onItemSelected(position: Int) {
        mPresenter.reviewItem(position)
    }

    override fun goToReviewItemActivity(extras: Intent) {
        val intent = Intent(this, ReviewItemActivity::class.java)
        intent.putExtra(reference_id, refId)
        intent.putExtra(trip_id, tripId)
        intent.putExtra(partial_delivery, partialDelivery)
        intent.putExtras(extras)
        startActivity(intent)
    }

    override fun goToReceiveItemActivity(extras: Intent) {
        val intent = Intent(this, ReceiveItemActivity::class.java)
        intent.putExtra(reference_id, refId)
        intent.putExtra(trip_id, tripId)
        intent.putExtra(partial_delivery, partialDelivery)
        intent.putExtras(extras)
        startActivity(intent)
    }

    override fun goToScanShipmentActivity() {
        val intent = Intent(this, Step2PickedUpActivity::class.java)
        intent.putExtra(refresh, true)
        intent.putExtra(trip_id, tripId)
        intent.putExtra(sprinter_name, sprinter)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.ivBack -> finish()
            R.id.btn_payment -> {
                mPresenter.onScanNext(binding.btnPaymentLayout.btnPayment.text != getString(R.string.proceed))
            }
            R.id.clUploadImages -> {
                dispatchTakePictureIntent(this, setImageUri())
            }
        }
    }

    override fun setButtonToProceed() {
        lastShipment = true
        binding.btnPaymentLayout.btnPayment.text = getString(R.string.proceed)
    }

    override fun goToStep3Activity() {
        val intent = Intent(this, Step3CashCollectionActivity::class.java)
        intent.putExtra("tripId", tripId.toString())
        intent.putExtra(sprinter_name, sprinter)
        startActivity(intent)
    }

    override fun startReceiveFmPickupActivity() {
        val intent = Intent(this, ReceiveFmPickupShipmentActivity::class.java)
        intent.putExtra("tripId", tripId.toString())
        intent.putExtra(sprinter_name, sprinter)
        startActivity(intent)
    }

    override fun startVerifyImageActivity() {
        val intent = Intent(this, Step4VerifyImagesActivity::class.java)
        intent.putExtra("tripId", tripId.toString())
        intent.putExtra(sprinter_name, sprinter)
        startActivity(intent)
    }

    override fun startAckDeliverySlipReconActivity() {
        val intent = Intent(this, AckDeliverySlipReconActivity::class.java)
        intent.putExtra("tripId", tripId.toString())
        intent.putExtra(sprinter_name, sprinter)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()

        mPresenter.onDetachView()
    }

    private fun setImageUri(): Uri {

        val photoFile: File? = try {
            createImageFile(applicationContext, com.goflash.dispatch.util.temp_files).apply {
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

            var queryImageUrl = ""

            val clipData = data?.clipData
            if (clipData != null && clipData.itemCount == 1) {

                val selectedImage = clipData.getItemAt(0).uri
                queryImageUrl = selectedImage?.path!!
                queryImageUrl = compressImageFile(
                    queryImageUrl,
                    false,
                    selectedImage!!,
                    this@ItemSummaryActivity
                )

            } else if (data?.data != null) {
                val selectedImage = data.data
                queryImageUrl = selectedImage?.path!!
                queryImageUrl = compressImageFile(
                    queryImageUrl,
                    false,
                    selectedImage!!,
                    this@ItemSummaryActivity
                )

            }

            if (clipData != null && clipData.itemCount > 1) {
                Toaster.show(baseContext, "You can upload a single image.")
            }

            if (data?.data == null || currentPhotoPath!!.isEmpty()) {
                val imageUri = Uri.fromFile(File(currentPhotoPath!!))
                val fileName = String.format("AckSlip%d.jpg", System.currentTimeMillis())
                compressImage(
                    applicationContext,
                    currentPhotoPath!!,
                    File(getAlbumStorageDir(this@ItemSummaryActivity, album_name2), fileName),
                    imageUri!!
                )
                deleteTempFiles(applicationContext, com.goflash.dispatch.util.temp_files)
                queryImageUrl = File(
                    getAlbumStorageDir(this@ItemSummaryActivity, album_name2),
                    fileName
                ).toString()
                currentPhotoPath = null

            }

            mPresenter.uploadFile(File(queryImageUrl))


        }
    }

    override fun onShowProgress() {
        showProgress()
    }

    override fun onImageUploaded(queryImageUrl: String, key :String) {
        hideProgress()
        reconImages[key] = (queryImageUrl)
        adapter.setKeys()
        adapter.notifyDataSetChanged()
    }

    override fun onFailure(error: Throwable?) {
        processError(error)
    }

    override fun uploadImage() {
        if (mPresenter.getItemCount() != reconImages.size)
            dispatchTakePictureIntent(this, setImageUri())
        else
            showToast(this, "Max ${mPresenter.getItemCount()} Images allowed")
    }

    override fun removeImage(key: String) {
        mPresenter.removeReconImage(key)
        deleteFile(this, album_name2, reconImages[key]!!)
        reconImages.remove(key)
        adapter.notifyDataSetChanged()
    }

}