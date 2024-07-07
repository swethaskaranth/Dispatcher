package com.goflash.dispatch.features.cash.ui.adapter

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.goflash.dispatch.R
import com.goflash.dispatch.data.ExpenseDTO
import com.goflash.dispatch.features.cash.listeners.AddExpenseListener


class ExpenseAdapter(
    private val context: Context,
    private val list: MutableList<ExpenseDTO>,
    private val listener: AddExpenseListener
) :
    RecyclerView.Adapter<ExpenseAdapter.ExpenseHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_expense_item, parent, false)
        return ExpenseHolder(view)

    }

    override fun onBindViewHolder(holder: ExpenseHolder, position: Int) {
        val expense = list[position]

        if (expense.isInvalid || !expense.isAdded)
            setEditView(holder, expense, position)
        else
            setExpenseView(holder, expense, position)


    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun setEditView(holder: ExpenseHolder, expense: ExpenseDTO, position: Int) {
        holder.editLayout.visibility = View.VISIBLE
        holder.addedLayout.visibility = View.GONE

        holder.txtInvalid.visibility = if (expense.isInvalid) View.VISIBLE else View.GONE

        holder.spExpType.adapter = ArrayAdapter(
            context,
            R.layout.layout_reason_spinner_item,
            R.id.textReason,
            context.resources.getStringArray(R.array.expense_type)
        )

        when (expense.expenseType) {
            null -> holder.spExpType.setSelection(0)
            "TEA_COFFEE" -> holder.spExpType.setSelection(1)
            "UTILITIES" -> holder.spExpType.setSelection(2)
            "STATIONARY" -> holder.spExpType.setSelection(3)
            "INFRA_SETUP" -> holder.spExpType.setSelection(4)
            "OTHERS" -> holder.spExpType.setSelection(5)
        }



        holder.spExpType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position != 0) {
                    when (holder.spExpType.selectedItem.toString()) {
                        "Tea/Coffee" -> expense.expenseType = "TEA_COFFEE"
                        "Utilities" -> expense.expenseType = "UTILITIES"
                        "Stationary" -> expense.expenseType = "STATIONARY"
                        "Infra Setup" -> expense.expenseType = "INFRA_SETUP"
                        "Others" -> expense.expenseType = "OTHERS"

                    }
                } else
                    expense.expenseType = null
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        if (expense.voucherFileUrl != null) {
            holder.image.visibility = View.VISIBLE
            holder.deleteImage.visibility = View.VISIBLE

            if (expense.fileType == "IMAGE")
                Glide.with(context)
                    .load(expense.voucherFileUrl)
                    .placeholder(R.drawable.ic_add_image)
                    .into(holder.image)
            else
                holder.image.setImageResource(R.drawable.ic_pdf_file_format_symbol)

        } else {
            holder.image.visibility = View.GONE
            holder.deleteImage.visibility = View.GONE
        }

        holder.tvVoucherNum.setText(expense.voucherNumber)
        holder.tvAmount.setText(if (expense.amount != null && expense.amount > 0) "${expense.amount}" else "")

        holder.tvVoucherNum.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s?.isNotEmpty() == true)
                    expense.voucherNumber = s.toString()
                else
                    expense.voucherNumber = null
            }
        })

        holder.tvAmount.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s?.isNotEmpty() == true) {
                    expense.amount = s.toString().toLong()
                    listener.onAmountEntered()

                    holder.tvAmount.removeTextChangedListener(this)

                    holder.tvAmount.setText(expense.amount.toString())
                    holder.tvAmount.setSelection(holder.tvAmount.text.length)

                    holder.tvAmount.addTextChangedListener(this)

                }else{
                    expense.amount = null
                    listener.onAmountEntered()
                }
            }
        })

        holder.tvUploadImg.setOnClickListener {
            listener.onUpload(position)
        }

        holder.deleteImage.setOnClickListener {
            listener.deleteImage(position)
        }

    }

    private fun setExpenseView(holder: ExpenseHolder, expense: ExpenseDTO, position: Int) {
        holder.addedLayout.visibility = View.VISIBLE
        holder.editLayout.visibility = View.GONE
        holder.tvVoucher.text = expense.voucherNumber

        holder.ivEdit.setOnClickListener { setEditView(holder, expense, position) }
        holder.ivDelete.setOnClickListener {
            listener.deleteExpense(position)
        }

    }

    class ExpenseHolder(view: View) : RecyclerView.ViewHolder(view) {

        val editLayout: View = view.findViewById(R.id.editItem)
        val spExpType: Spinner = view.findViewById(R.id.spExpType)
        val tvVoucherNum: EditText = view.findViewById(R.id.tvVoucherNum)
        val tvAmount: EditText = view.findViewById(R.id.tvAmount)
        val tvUploadImg: TextView = view.findViewById(R.id.tvUploadImg)
        val txtInvalid: TextView = view.findViewById(R.id.txtInvalid)
        val image: ImageView = view.findViewById(R.id.iv_show_image)
        val deleteImage: ImageView = view.findViewById(R.id.iv_remove)

        val addedLayout: View = view.findViewById(R.id.addedItem)
        val tvVoucher: TextView = view.findViewById(R.id.tvVoucher)
        val ivEdit: ImageView = view.findViewById(R.id.ivEdit)
        val ivDelete: ImageView = view.findViewById(R.id.ivDelete)


    }

}