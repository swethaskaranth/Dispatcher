package com.goflash.dispatch.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.goflash.dispatch.R
import com.goflash.dispatch.features.audit.presenter.AuditSummaryPresenter
import com.goflash.dispatch.presenter.views.SummaryRowView

class AuditSummaryAdapter(
    private val context: Context,
    private val presenter: AuditSummaryPresenter
) : RecyclerView.Adapter<AuditSummaryAdapter.SummaryHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SummaryHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.layout_audit_summary_list, parent, false)
        return SummaryHolder(view, presenter)
    }

    override fun getItemCount(): Int {
        return presenter.getCount()
    }

    override fun onBindViewHolder(holder: SummaryHolder, position: Int) {
        presenter.onBindSummaryRowView(position, holder)
    }


    class SummaryHolder(view: View, private val presenter: AuditSummaryPresenter) :
        RecyclerView.ViewHolder(view), SummaryRowView {

        val tvDate = view.findViewById<TextView>(R.id.tvDate)
        val tvName = view.findViewById<TextView>(R.id.tvName)


        override fun setDate(date: String?) {
            tvDate.text = date
        }

        override fun setName(name: String?) {
            tvName.text = name
        }

        override fun setOnClickListener() {
            itemView.setOnClickListener{
                presenter.onItemClicked(adapterPosition)
            }
        }


    }
}