package com.goflash.dispatch.features.dispatch.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.goflash.dispatch.R
import com.goflash.dispatch.features.dispatch.presenter.InvoiceListPresenter
import com.goflash.dispatch.features.dispatch.ui.interfaces.InvoiceListController
import com.goflash.dispatch.features.dispatch.view.InvoiceRowView


class InvoiceAdapter(private val context: Context, private val presenter: InvoiceListController) : androidx.recyclerview.widget.RecyclerView.Adapter<InvoiceAdapter.InvoiceHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceHolder {

        val view = LayoutInflater.from(context).inflate(R.layout.invoice_item, parent, false)
        return InvoiceHolder(view, presenter)

    }

    override fun getItemCount(): Int {
        return presenter.getCount()
    }


    override fun onBindViewHolder(holder: InvoiceHolder, position: Int) {
        presenter.onBindInvoiceRowView(position,holder)
    }


    class InvoiceHolder(private val view: View, private  val presenter : InvoiceListController) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view), InvoiceRowView {

        val invoice_number: TextView = view.findViewById(R.id.invoice_number)
        val invoice_time: TextView = view.findViewById(R.id.invoice_time)
        val invoice_print: TextView = view.findViewById(R.id.print_invoice)
        val eway_bill_print: TextView = view.findViewById(R.id.print_eway_bill)
        val divider: View = view.findViewById(R.id.div)

        override fun setInvoiceId(invoiceId: String?) {
            invoice_number.text = invoiceId
        }

        override fun setTime(time: String) {
            invoice_time.text = time
        }

        override fun setOnClickListeners() {
            invoice_print.setOnClickListener{
                presenter.onPrintInvoiceClicked(adapterPosition)
            }

            eway_bill_print.setOnClickListener{
                presenter.onEwayBillClicked(adapterPosition)
            }
        }

        override fun hideEwayBill() {
            eway_bill_print.visibility = View.GONE
            divider.visibility = View.GONE
        }

    }


}