package com.goflash.dispatch.features.lastmile.tripCreation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import co.uk.rushorm.core.RushCore
import co.uk.rushorm.core.RushSearch
import com.goflash.dispatch.R
import com.goflash.dispatch.SortationApplication
import com.goflash.dispatch.app_constants.completedItem
import com.goflash.dispatch.app_constants.pickup_onFly
import com.goflash.dispatch.app_constants.status
import com.goflash.dispatch.app_constants.trip_id
import com.goflash.dispatch.data.CdsCashCollection
import com.goflash.dispatch.data.ReceiveCdsCash
import com.goflash.dispatch.data.TaskListDTO
import com.goflash.dispatch.databinding.LayoutTripSummaryBinding
import com.goflash.dispatch.di.components.DaggerActivityComponents
import com.goflash.dispatch.features.lastmile.tripCreation.listeners.OnSummaryListener
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.LastMileSummaryPresenter
import com.goflash.dispatch.features.lastmile.tripCreation.ui.adapter.SummaryAdapter
import com.goflash.dispatch.features.lastmile.tripCreation.ui.fragments.BottomSheetCashDepositFragment
import com.goflash.dispatch.features.lastmile.tripCreation.ui.fragments.BottomSheetCashFragment
import com.goflash.dispatch.features.lastmile.tripCreation.view.LastMileSummaryView
import com.goflash.dispatch.model.SummaryItem
import com.goflash.dispatch.ui.activity.BaseActivity
import com.goflash.dispatch.util.SimpleDividerItemDecoration
import com.goflash.dispatch.util.taskListClear
import javax.inject.Inject
import kotlin.math.max


class LastMileSummaryActivity : BaseActivity(), LastMileSummaryView, OnSummaryListener,
    View.OnClickListener {

    @Inject
    lateinit var mPresenter: LastMileSummaryPresenter

    private var tripId: String? = null
    private var sprinterName: String? = null
    private var amountStr: Int? = 0

    private val complete_summary_list: MutableList<SummaryItem> = ArrayList()
    private val incomplete_summary_list: MutableList<SummaryItem> = ArrayList()

    private var receiveCdsCash: ReceiveCdsCash? = null

    private lateinit var binding: LayoutTripSummaryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutTripSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initDagger()
        initViews()
    }

    private fun initViews() {

       // ivDelete.visibility = View.INVISIBLE

        binding.fab.setOnClickListener(this)
        binding.toolBar.ivBack.setOnClickListener(this)
        binding.linearLayout.tvCashInHand.setOnClickListener(this)
        binding.toolBar.ivBack.setOnClickListener(this)
        binding.linearLayout.tvCashDeposit.setOnClickListener(this)

        binding.rvCompleteTasks.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        binding.rvInCompleteTasks.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)

        binding.rvCompleteTasks.addItemDecoration(SimpleDividerItemDecoration(this))
        binding.rvInCompleteTasks.addItemDecoration(SimpleDividerItemDecoration(this))

        tripId = intent.getStringExtra("tripId")
        sprinterName = intent.getStringExtra("sprinterName")
        receiveCdsCash = intent.getParcelableExtra("cdsCashCollection")
        if (intent?.getBooleanExtra("OFD", false) == true)
            binding.fab.visibility = View.VISIBLE
        binding.toolBar.toolbarTitle.text = getString(R.string.trip_summary)
        binding.toolBar.tvSprinter.text =
            "${String.format(getString(R.string.trip_number, tripId?.toLong()))} - $sprinterName"


    }

    private fun initDagger() {
        DaggerActivityComponents.builder()
            .networkComponent((application as SortationApplication).getNetworkComponent())
            .build().inject(this@LastMileSummaryActivity)

        mPresenter.onAttachView(this, this@LastMileSummaryActivity)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iVProfileHome -> {
                taskListClear()
                finish()
            }
            R.id.fab -> startAddShipmentActivity()
            R.id.tvCashInHand -> {
                if(amountStr != 0)
                    showCashBreakup()}
            R.id.ivBack -> finish()
            R.id.tvCashDeposit -> {
                if (receiveCdsCash != null && receiveCdsCash!!.total > 0)
                    showTransactions(ArrayList(receiveCdsCash!!.currentTripBreakUp))
            }

        }
    }

    override fun onResume() {
        super.onResume()
        showProgress()
        mPresenter.getTasksByTripId(tripId!!,receiveCdsCash)
    }

    override fun onTasksFetched(
        completeTasks: LinkedHashMap<String, List<TaskListDTO>>,
        inCompleteTasks: LinkedHashMap<String, List<TaskListDTO>>
    ) {
        hideProgress()
        complete_summary_list.clear()
        incomplete_summary_list.clear()

        var count = 0
        for (task in inCompleteTasks.entries) {
            count += task.value.size
            incomplete_summary_list.add(SummaryItem(task.key, task.value.size))
        }

        var completetaskCount = 0
        for (task in completeTasks.entries) {
            complete_summary_list.add(SummaryItem(task.key, task.value.size))
            completetaskCount += task.value.size
        }

        binding.linearLayout.completeCount.text = completetaskCount.toString()

        binding.linearLayout.inCompleteCount.text = count.toString()

        if (complete_summary_list.size == 0) {
            binding.llAttempted.visibility = View.GONE
            binding.rvCompleteTasks.visibility = View.GONE
            binding.compTask.visibility = View.GONE
        }else{
            binding.llAttempted.visibility = View.VISIBLE
            binding.rvCompleteTasks.visibility = View.VISIBLE
            binding. compTask.visibility = View.VISIBLE
        }

        if (incomplete_summary_list.size == 0) {
            binding.llUnAttempted.visibility = View.INVISIBLE
            binding.rvInCompleteTasks.visibility = View.INVISIBLE
            binding.incompTask.visibility = View.GONE
        }else {
            binding.llUnAttempted.visibility = View.VISIBLE
            binding.rvInCompleteTasks.visibility = View.VISIBLE
            binding.incompTask.visibility = View.VISIBLE
        }

        binding.rvCompleteTasks.adapter = SummaryAdapter(this, complete_summary_list, this)
        binding.rvInCompleteTasks.adapter = SummaryAdapter(this, incomplete_summary_list, this)

        binding.rvCompleteTasks!!.adapter?.notifyDataSetChanged()
        binding.rvInCompleteTasks!!.adapter?.notifyDataSetChanged()

    }

    override fun onItemSelected(position: Int, complete: Boolean) {
        val intent = Intent(this, SummaryDetailActivity::class.java)
        if (complete) {
            intent.putExtra(status, complete_summary_list[position].status)
        } else {
            intent.putExtra(status, incomplete_summary_list[position].status)
        }
        intent.putExtra(completedItem, complete)
        intent.putExtra("tripId", tripId)
        startActivity(intent)

    }

    override fun onCashFetched(cash_value : Int, cds_cash: Int) {
        amountStr = cash_value

        if(amountStr == 0) {
            binding.linearLayout.tvCashInHand.isEnabled = false
            binding.linearLayout.tvCashInHand.setTextColor(resources.getColor(R.color.gray_line))
        }else {
            binding.linearLayout.tvCashInHand.isEnabled = true
            binding.linearLayout.tvCashInHand.setTextColor(resources.getColor(R.color.unassigned_count_text_color))
        }

        binding.linearLayout.tvCashInHand.text = String.format(getString(R.string.cash_in_hand_text), max(cash_value.minus(cds_cash), 0))
        binding.linearLayout.tvCashCollected.text = String.format(getString(R.string.cash_in_hand_text), cash_value)
        binding.linearLayout.tvCashDeposit.text = String.format(getString(R.string.cash_in_hand_text), cds_cash)
    }

    override fun onFailure(error: Throwable?) {
        hideProgress()
        processError(error)
    }

    override fun hideProgressBar() {
        hideProgress()
    }

    private fun startAddShipmentActivity() {
        val intent = Intent(this, AddShipmentActivity::class.java)
        intent.putExtra(trip_id, tripId?.toLong())
        intent.putExtra(pickup_onFly, true)
        startActivity(intent)
    }

    private fun showCashBreakup() {
        val frag = BottomSheetCashFragment()
        val args = Bundle()
        args.putString("tripId", tripId)
        args.putString("sprinterName", sprinterName)
        args.putBoolean("summary", true)
        frag.arguments = args
        frag.isCancelable = false
        frag.show(supportFragmentManager, frag.tag)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.onDetachView()
    }

    fun showTransactions(list: ArrayList<CdsCashCollection?>?) {
        val fragment = BottomSheetCashDepositFragment()
        val args = Bundle()
        args.putSerializable("transactions", list)
        fragment.arguments = args
        fragment.show(supportFragmentManager, fragment.tag)
    }

}