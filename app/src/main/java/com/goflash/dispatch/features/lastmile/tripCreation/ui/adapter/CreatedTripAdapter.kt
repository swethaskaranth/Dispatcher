package com.goflash.dispatch.features.lastmile.tripCreation.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.data.TripDTO
import com.goflash.dispatch.features.lastmile.tripCreation.presenter.CreatedPresenter
import com.goflash.dispatch.features.lastmile.tripCreation.view.StatusRowView

class CreatedTripAdapter(private val context: Context, private val mPresenter: CreatedPresenter) :
    RecyclerView.Adapter<CreatedTripAdapter.CreatedTripHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreatedTripHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.layout_trip_item_created, parent, false)
        return CreatedTripHolder(view, mPresenter, context)
    }

    override fun getItemCount(): Int {
        return mPresenter.getCount()
    }

    override fun onBindViewHolder(holder: CreatedTripHolder, position: Int) {
        mPresenter.onStatusRowView(position, holder)
    }

    class CreatedTripHolder(
        view: View,
        private val mPresenter: CreatedPresenter,
        private val context: Context
    ) : RecyclerView.ViewHolder(view), StatusRowView {

        val v = view
        val tripId : TextView= v.findViewById(R.id.tvTripId!!)
        private val tvTaskCount : TextView = v.findViewById(R.id.tvTaskCount)
        private val tvSprinter : TextView = v.findViewById(R.id.tvSprinter)
        private val tvBin : TextView = v.findViewById(R.id.tvBin)
        private val tvAddEditSprinter : TextView = v.findViewById(R.id.tvAddEditSprinter)
        private val tvEditSprinter : TextView= v.findViewById(R.id.tvEditSprinter)
        private val tvMergeTo : TextView = v.findViewById(R.id.tvMergeTo)
        private val div: View = v.findViewById(R.id.view2)
        private val tvRouteName: TextView = v.findViewById(R.id.tvRouteName)

        private val text_color by lazy{
            ContextCompat.getColor(context,R.color.unassigned_count_text_color)
        }

        private val grey by lazy{
            ContextCompat.getColor(context, R.color.color_c7)
        }

        override fun bagId(id: Long) {
            tripId.text = "Trip $id"
        }

        override fun onListClick(position: Int) {
            v.setOnClickListener {
                mPresenter.onClickListner(position, 1)
            }
        }

        override fun setCount(id: String) {
            tvTaskCount.text = id
        }

        override fun setName(name: String?, routeId: String?) {

            if (name.isNullOrEmpty())
                tvSprinter.visibility = View.GONE
            else{
                tvSprinter.visibility = View.VISIBLE
                tvSprinter.text = name
            }
            if(routeId.isNullOrEmpty()){
                tvRouteName.visibility = View.GONE
            }else{
                tvRouteName.visibility = View.VISIBLE
                tvRouteName.text = routeId
            }
        }

        override fun setBin(id: String?) {

            if (id.isNullOrEmpty()) {
                tvBin.visibility = View.GONE
            }else {
                tvBin.visibility = View.VISIBLE
                tvBin.text = id
            }
        }

        override fun onClickListner(position: Int, data: TripDTO) {

            if (data.agentName != null) {
                tvEditSprinter.visibility = View.VISIBLE
                tvAddEditSprinter.visibility = View.GONE
            } else {
                tvAddEditSprinter.visibility = View.VISIBLE
                tvEditSprinter.visibility = View.GONE
            }

            tvAddEditSprinter.setOnClickListener {
                mPresenter.onClickListner(position, 2)
            }

            tvEditSprinter.setOnClickListener {
                mPresenter.onClickListner(position, 2)
            }

            tvMergeTo.setOnClickListener {
                mPresenter.onMergeClicked(position)
            }
        }

        override fun enableOrDisableClicks(enable: Boolean) {
            if(enable){
                tvAddEditSprinter.setTextColor(text_color)
                tvEditSprinter.setTextColor(text_color)
            }else{
                tvAddEditSprinter.setTextColor(grey)
                tvEditSprinter.setTextColor(grey)
            }
        }

        override fun hideMerge() {
            tvMergeTo.visibility = View.GONE
            div.visibility = View.GONE
        }
    }


}