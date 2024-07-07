package com.goflash.dispatch.features.lastmile.tripCreation.ui.activity

import android.os.Bundle
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.completedItem
import com.goflash.dispatch.app_constants.status
import com.goflash.dispatch.data.TaskListDTO
import com.goflash.dispatch.databinding.ActivitySummaryDetailBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.DataUpdateListener
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.impl.SummaryDetailPresenter
import com.goflash.dispatch.features.lastmile.tripCreation.ui.adapter.TasksAttemptedAdapter
import com.goflash.dispatch.features.lastmile.tripCreation.view.SummaryDetailView
import com.goflash.dispatch.model.AddressDTO
import com.goflash.dispatch.type.TaskStatus
import com.goflash.dispatch.ui.activity.BaseActivity

import javax.inject.Inject

class SummaryDetailActivity : BaseActivity(), SummaryDetailView, DataUpdateListener {

    private val TAG = SummaryDetailActivity::class.java.simpleName

    @Inject
    lateinit var mPresenter: SummaryDetailPresenter

    //private var mList: ArrayList<TaskListDTO>? = null
    private var mList = mutableListOf<TaskListDTO>()

    private var mStatus: String? = null
    private var tripId: String? = null
    private var completeTask: Boolean = false

    private lateinit var binding: ActivitySummaryDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivitySummaryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mStatus = intent.getStringExtra(status)
        tripId = intent.getStringExtra("tripId")
        completeTask = intent.getBooleanExtra(completedItem, false)

        initDagger()
        initViews()
    }

    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this@SummaryDetailActivity)

        mPresenter.onAttach(this, this@SummaryDetailActivity)
    }

    private fun initViews() {

        //mList = intent.getParcelableArrayListExtra("")

        binding.toolBar.toolbarTitle.text = getString(R.string.summary)

        binding.rvTaskList.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        binding.toolBar.iVProfileHome.setOnClickListener { onBackPressed() }
        getTasks()
    }

    private fun getTasks() {
        if (mStatus != null) {
            mList = if (completeTask)
                RushSearch().whereEqual("tripId", tripId).and().whereEqual("type", mStatus).and()
                    .whereEqual(status, TaskStatus.COMPLETED.name).find(TaskListDTO::class.java)
            else
                RushSearch().whereEqual("tripId", tripId).and().whereEqual(status, mStatus).find(TaskListDTO::class.java)

               /* mList?.filter { it.type == mStatus }?.filter { it.status ==  TaskStatus.COMPLETED.name}
            else
                mList?.filter { it.type == mStatus }*/

        }
        binding.rvTaskList.adapter = TasksAttemptedAdapter(this, mList, this)
    }

    override fun onViewDetails(position: Int, data: TaskListDTO) {
        showProgress()
        mPresenter.getAddressDetails(position, data.shipmentId)
    }

    override fun onFailure(error: Throwable) {
        hideProgress()
        processError(error)
    }

    override fun updateList(position: Int, detail: AddressDTO) {
        hideProgress()

        mList[position].name = detail.name
        mList[position].address1 = detail.address1
        mList[position].address2 = detail.address2
        mList[position].address3 = detail.address3
        mList[position].city = detail.city
        mList[position].state = detail.state
        mList[position].pincode = detail.pincode?.toInt()

        binding.rvTaskList.adapter?.notifyDataSetChanged()
    }
}