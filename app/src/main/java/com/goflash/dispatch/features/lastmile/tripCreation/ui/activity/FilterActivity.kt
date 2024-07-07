package com.goflash.dispatch.features.lastmile.tripCreation.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.RadioButton
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.*
import com.goflash.dispatch.data.UnassignedDTO
import com.goflash.dispatch.databinding.ActivityFilterBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.FilterPresenter
import com.goflash.dispatch.features.lastmile.tripCreation.view.FilterView
import com.goflash.dispatch.type.FilterTags
import com.goflash.dispatch.type.PaymentStatus
import com.goflash.dispatch.type.ServiceType
import com.goflash.dispatch.type.ShipmentType
import com.goflash.dispatch.ui.activity.BaseActivity
import com.goflash.dispatch.ui.customView.KeyPairBoolData
import com.goflash.dispatch.ui.customView.MultiSpinnerListener
import com.goflash.dispatch.util.RelativeRadioGroup
import com.goflash.dispatch.util.getTimestampFutureString
import com.goflash.dispatch.util.getTimestampString
import com.goflash.dispatch.util.getTimestampString2
import org.jetbrains.anko.toast
import javax.inject.Inject


class FilterActivity : BaseActivity(), FilterView, View.OnClickListener,
    RelativeRadioGroup.OnCheckedChangeListener {

    @Inject
    lateinit var mPresenter: FilterPresenter

    private var type: String? = null

    private var getEDD: String? = null
    private var tag: String? = null
    private var tagSelected: Int = -1
    private var eddSelected: Int = -1
    private var serviceTypeSelected: MutableList<ServiceType> = mutableListOf()

    val listArray1 = ArrayList<KeyPairBoolData>()

    private var smartTrips: Boolean? = false

    private lateinit var binding: ActivityFilterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)


        initDagger()
        initViews()
    }

    private fun initViews() {
        binding.toolBar.toolbarTitle.text = getString(R.string.filters)
        binding.layoutFilter.radioGroup1.setOnCheckedChangeListener(this)
        binding.toolBar.ivClear.setOnClickListener(this)
        binding.include.btnClear.setOnClickListener(this)
        binding.include.btnSave.setOnClickListener(this)

        binding.toolBar.ivClear.visibility = View.VISIBLE
        binding.include.btnClear.text = getString(R.string.reset)

        smartTrips = intent.getBooleanExtra("smartTrips", false)

        if (intent.hasExtra(SERVICETYPE)) {
            val shipmentType = intent.getStringExtra(SERVICETYPE)
            shipmentType?.let {
                val selectedItems = it.split(",")
                serviceTypeSelected.clear()
                selectedItems.forEach {selected ->
                    ServiceType.getEnumNameByName(selected)?.let {
                        serviceTypeSelected.add(it)
                    }
                }
                if(serviceTypeSelected.isEmpty())
                    binding.layoutFilter.spServiceType.setHintText("Select Service Type")
            }?:  binding.layoutFilter.spServiceType.setHintText("Select Service Type")
        }else binding.layoutFilter.spServiceType.setHintText("Select Service Type")

        setupSpinner()


        binding.layoutFilter.rbAllDate.isChecked = true

        eddSelected = 3
        tagSelected = 3

        if (intent.extras != null) {
            if (intent.getBooleanExtra(pickup_onFly, false)) {
                binding.layoutFilter.rgTaskType.visibility = View.GONE
                binding.layoutFilter.labelTaskType.visibility = View.GONE
            }

            if (intent.hasExtra(TASKTYPE)) {
                //var taskType = intent.getStringExtra(TASKTYPE)
                type = intent.getStringExtra(TASKTYPE)
                when(type){
                    ShipmentType.FORWARD.name -> binding.layoutFilter.checkDelivery.isChecked = true
                    ShipmentType.RETURN.name -> binding.layoutFilter.checkPickup.isChecked = true
                    ShipmentType.FMPICKUP.name -> binding.layoutFilter.checkFmPickup.isChecked = true
                    ShipmentType.RTO.name -> binding.layoutFilter.checkRTO.isChecked = true
                    else -> binding.layoutFilter.checkAll.isChecked = true
                }
            }

            if (intent.hasExtra(PAYMENT_STATUS)) {
                val paymentStatus = intent.getStringExtra(PAYMENT_STATUS)
                if (paymentStatus == null || paymentStatus == "both") {
                    binding.layoutFilter.cbDone.isChecked = true
                    binding.layoutFilter.cbPending.isChecked = true
                    } else if (paymentStatus.contains(PaymentStatus.DONE.type)) {
                    binding.layoutFilter.cbDone.isChecked = true
                    } else if (paymentStatus.contains(PaymentStatus.PENDING.type)) {
                    binding.layoutFilter.cbPending.isChecked = true
                    }

                 }else{
                binding.layoutFilter.cbDone.isChecked = true
                binding.layoutFilter.cbPending.isChecked = true
                }


            eddSelected = intent.getIntExtra(EDD, 3)

            when (intent.getIntExtra(EDD, 3)) {
                0 -> {
                    binding.layoutFilter.rbYesterday.isChecked = true
                    getEDD = getTimestampString(1)
                }
                1 -> {
                    binding.layoutFilter.rbTomorrow.isChecked = true
                    getEDD = getTimestampFutureString(1)
                }
                2 -> {
                    binding.layoutFilter.rbToday.isChecked = true
                    getEDD = getTimestampString2()
                }
                3 -> {
                    binding.layoutFilter.rbAllDate.isChecked = true
                    getEDD = getTimestampString(60)
                }
            }

            tagSelected = intent.getIntExtra(TAGS, 3)

            when (intent.getIntExtra(TAGS, 3)) {
                0 -> {
                    binding.layoutFilter.rbAll.isChecked = true
                    tag = null
                }
                1 -> {
                    binding.layoutFilter.rbBlocked.isChecked = true
                    tag = FilterTags.BLOCKED.name
                }
                2 -> {
                    binding.layoutFilter.rbHigh.isChecked = true
                    tag = FilterTags.HIGH_PRIORITY.name
                }
                3 -> {
                    binding.layoutFilter.rbPostponed.isChecked = true
                    tag = FilterTags.POSTPONED.name
                }
            }

        } else {

            binding.layoutFilter.checkAll.isChecked = true

        }
    }

    private fun setupSpinner(){
        listArray1.clear()
        val serviceTypes = ServiceType.values().toList()

        binding.layoutFilter.spServiceType.setHintText("Select Service Type")
        for (i in serviceTypes.indices) {
            val h = KeyPairBoolData((i + 1).toLong(), serviceTypes[i].displayName, serviceTypeSelected.any { it.name == serviceTypes[i].name }, null)
            listArray1.add(h)
        }

        binding.layoutFilter.spServiceType.setSearchEnabled(false)

        binding.layoutFilter.spServiceType.setItems(listArray1, object: MultiSpinnerListener{
            override fun onItemsSelected(selectedItems: List<KeyPairBoolData?>?) {
                serviceTypeSelected.clear()
                selectedItems?.forEach {selected ->
                    selected?.name?.let {
                        ServiceType.getEnumNameByDisplayName(it)?.let {
                            serviceTypeSelected.add(it)
                        }
                    }
                }

                if(serviceTypeSelected.isEmpty())
                    binding.layoutFilter.spServiceType.setHintText("Select Service Type")
            }
        })
    }

    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this@FilterActivity)

        mPresenter.onAttach(this, this@FilterActivity)
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.btn_clear -> clearFilters()

            R.id.btn_save -> getFilters()

            R.id.ivClear -> {
                setResult(Activity.RESULT_CANCELED, intent)
                finish()
            }

        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED, intent)
        finish()
    }

    override fun onCheckedChanged(group: RelativeRadioGroup?, checkedId: Int) {
        //getEDD(group!!, checkedId)
    }

    override fun onStop() {
        super.onStop()
        mPresenter.onDetach()
    }

    private fun getFilters() {
        type = when{
            binding.layoutFilter.checkDelivery.isChecked -> ShipmentType.FORWARD.name
            binding.layoutFilter.checkPickup.isChecked -> ShipmentType.RETURN.name
            binding.layoutFilter.checkFmPickup.isChecked -> ShipmentType.FMPICKUP.name
            binding.layoutFilter.checkRTO.isChecked -> ShipmentType.RTO.name
            binding.layoutFilter.checkAll.isChecked -> ""
            else -> null
        }



        if (binding.layoutFilter.rbAllDate.isChecked)
            getEDD = getTimestampString(60)

        if (type == null) {
            toast("Please select any one Delivery/Pickup.")
            return
        }

        if (getEDD == null) {
            toast("Please choose EDD.")
            return
        }



        val paymentStatus = if(binding.layoutFilter.cbDone.isChecked &&binding.layoutFilter. cbPending.isChecked)
            "both"
        else if (binding.layoutFilter.cbDone.isChecked)
            PaymentStatus.DONE.type
        else if (binding.layoutFilter.cbPending.isChecked)
            PaymentStatus.PENDING.type
        else
        ""


        /*if (serviceType.isEmpty()) {
            toast("Please select any one Service Types.")
            return
        }*/

        if (paymentStatus.isEmpty()) {
            toast("Please select any one Payment Status.")
            return
            }



        callActivity(type, getEDD!!,paymentStatus)
    }

    private fun callActivity(type: String?, getEDD: String?, paymentStatus: String) {
        val intent = Intent()
        intent.putExtra("type", type)
        intent.putExtra("getEDD", getEDD)
        intent.putExtra("tag", tag)
        intent.putExtra("selectedEDD", eddSelected)
        intent.putExtra("selectedTag", tagSelected)
        intent.putExtra(SERVICETYPE, serviceTypeSelected.joinToString(",") { it.name })
        intent.putExtra(PAYMENT_STATUS,paymentStatus)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun clearFilters() {

        binding.layoutFilter.checkAll.isChecked = true

        serviceTypeSelected.clear()
        setupSpinner()

        binding.layoutFilter.cbDone.isChecked = true
        binding.layoutFilter.cbPending.isChecked = true

        binding.layoutFilter.rbYesterday.isChecked = false
        binding.layoutFilter.rbTomorrow.isChecked = false
        binding.layoutFilter.rbToday.isChecked = false
        binding.layoutFilter.rbAllDate.isChecked = true
        eddSelected = 3

        binding.layoutFilter.rbBlocked.isChecked = false
        binding.layoutFilter.rbHigh.isChecked = false
        binding.layoutFilter.rbPostponed.isChecked = false
        binding.layoutFilter.rbAll.isChecked = true
        tag = null
        tagSelected = 0
    }

    override fun onSuccess(list: List<UnassignedDTO>) {

    }

    override fun onFailure(error: Throwable?) {

    }

    fun onCheckboxClicked(view: View) {
        if (view is CheckBox) {
            val checked: Boolean = view.isChecked

            /* when (view.id) {
                 R.id.checkDelivery -> {
                     value1 = if (checked) {
                         checkDelivery.text.toString()
                     } else {
                         ""
                     }
                 }
                 R.id.checkPickup -> {
                     value2 = if (checked) {
                         checkPickup.text.toString()
                     } else {
                         ""
                     }
                 }
             }*/
        }
    }

    fun onRadioButton(view: View) {

        if (view is RadioButton) {
            val checked: Boolean = view.isChecked

            when {
                binding.layoutFilter.rbYesterday.isChecked -> {
                    eddSelected = 0
                    getEDD = getTimestampString(1)
                }
                binding.layoutFilter.rbTomorrow.isChecked -> {
                    eddSelected = 1
                    getEDD = getTimestampFutureString(1)
                }
                binding.layoutFilter.rbToday.isChecked -> {
                    eddSelected = 2
                    getEDD = getTimestampString2()
                }
                binding.layoutFilter.rbAllDate.isChecked -> {
                    eddSelected = 3
                    getEDD = getTimestampString(60)
                }
            }
        }


    }

    fun onTagSelect(view: View) {
        if (view is RadioButton) {
            when {
                binding.layoutFilter.rbAll.isChecked -> {
                    tagSelected = 0
                    tag = null
                }
                binding.layoutFilter.rbBlocked.isChecked -> {
                    tagSelected = 1
                    tag = FilterTags.BLOCKED.name
                }
                binding.layoutFilter.rbHigh.isChecked -> {
                    tagSelected = 2
                    tag = FilterTags.HIGH_PRIORITY.name
                }
                binding.layoutFilter.rbPostponed.isChecked -> {
                    tagSelected = 3
                    tag = FilterTags.POSTPONED.name
                }
            }
        }

    }

    private fun getTags() {

    }

    private fun getType(type: Int): String {
        return ""
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.onDetach()
    }
}
