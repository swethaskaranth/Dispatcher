package com.goflash.dispatch.ui.customView

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Typeface
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.content.ContextCompat
import com.goflash.dispatch.R
import java.util.*


class MultiSpinnerSearch : AppCompatSpinner, DialogInterface.OnCancelListener {
	private var highlightSelected = false
	private var highlightColor: Int = ContextCompat.getColor(getContext(), R.color.list_selected)
	private var textColor: Int = Color.GRAY
	private var limit = -1
	private var selected = 0
	private var defaultText = ""
	private var spinnerTitle = ""
	private var emptyTitle = "Not Found!"
	private var searchHint = "Type to search"
	private var clearText = "Clear All"
	private var colorSeparation = false
	private var isShowSelectAllButton = false
	private var listener: MultiSpinnerListener? = null
	private var limitListener: LimitExceedListener? = null
	private var adapter: MyAdapter? = null
	private var items: List<KeyPairBoolData>? = null
	private var isSearchEnabled = true

	constructor(context: Context) : super(context) {}
	constructor(arg0: Context, arg1: AttributeSet?) : super(arg0, arg1) {
		val a: TypedArray = arg0.obtainStyledAttributes(arg1, R.styleable.MultiSpinnerSearch)
		for (i in 0 until a.indexCount) {
			val attr: Int = a.getIndex(i)
			if (attr == R.styleable.MultiSpinnerSearch_hintText) {
				a.getString(attr)?.let { setHintText(it) }
				spinnerTitle = getHintText()
				defaultText = spinnerTitle
				break
			} else if (attr == R.styleable.MultiSpinnerSearch_highlightSelected) {
				highlightSelected = a.getBoolean(attr, false)
			} else if (attr == R.styleable.MultiSpinnerSearch_highlightColor) {
				highlightColor =
					a.getColor(attr, ContextCompat.getColor(getContext(), R.color.list_selected))
			} else if (attr == R.styleable.MultiSpinnerSearch_textColor) {
				textColor = a.getColor(attr, Color.GRAY)
			} else if (attr == R.styleable.MultiSpinnerSearch_clearText) {
				a.getString(attr)?.let { setClearText(it) }
			}
		}

		a.recycle()
	}

	constructor(arg0: Context, arg1: AttributeSet?, arg2: Int) : super(arg0, arg1, arg2) {}

	fun isSearchEnabled(): Boolean {
		return isSearchEnabled
	}

	fun setSearchEnabled(searchEnabled: Boolean) {
		isSearchEnabled = searchEnabled
	}

	fun isColorSeparation(): Boolean {
		return colorSeparation
	}

	fun setColorSeparation(colorSeparation: Boolean) {
		this.colorSeparation = colorSeparation
	}

	fun getHintText(): String {
		return spinnerTitle
	}

	fun setHintText(hintText: String) {
		spinnerTitle = hintText
		defaultText = spinnerTitle
	}

	fun setClearText(clearText: String) {
		this.clearText = clearText
	}

	fun setLimit(limit: Int, listener: LimitExceedListener?) {
		this.limit = limit
		limitListener = listener
		isShowSelectAllButton = false // if its limited, select all default false.
	}

	fun getSelectedItems(): List<KeyPairBoolData> {
		val selectedItems: MutableList<KeyPairBoolData> = ArrayList()
		for (item in items!!) {
			if (item.isSelected) {
				selectedItems.add(item)
			}
		}
		return selectedItems
	}

	fun getSelectedIds(): List<Long> {
		val selectedItemsIds: MutableList<Long> = ArrayList()
		for (item in items!!) {
			if (item.isSelected) {
				selectedItemsIds.add(item.id)
			}
		}
		return selectedItemsIds
	}

	override fun onCancel(dialog: DialogInterface?) {
		onDetachedFromWindow()
		// refresh text on spinner
		val spinnerBuffer = StringBuilder()
		val selectedData: ArrayList<KeyPairBoolData> = ArrayList()
		for (i in items!!.indices) {
			val currentData = items!![i]
			if (currentData.isSelected) {
				selectedData.add(currentData)
				spinnerBuffer.append(currentData.name)
				spinnerBuffer.append(", ")
			}
		}
		var spinnerText = spinnerBuffer.toString()
		spinnerText =
			if (spinnerText.length > 2) spinnerText.substring(
				0,
				spinnerText.length - 2
			) else getHintText()
		val adapterSpinner: ArrayAdapter<String> =
			ArrayAdapter(getContext(), R.layout.textview_for_spinner, arrayOf<String>(spinnerText))
		setAdapter(adapterSpinner)
		adapter?.notifyDataSetChanged()
		listener!!.onItemsSelected(selectedData)
	}

	@SuppressLint("MissingInflatedId")
	override fun performClick(): Boolean {
		super.performClick()
		builder = AlertDialog.Builder(getContext())
		builder?.setTitle(spinnerTitle)
		val inflater: LayoutInflater =
			context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
		val view: View = inflater.inflate(R.layout.service_type_alert_dialog_view, null)
		builder?.setView(view)
		val listView: ListView = view.findViewById(R.id.list)
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE)
		listView.setFastScrollEnabled(false)
		adapter = items?.let { MyAdapter(getContext(), it) }
		listView.setAdapter(adapter)
		val emptyText: TextView = view.findViewById(R.id.emptyTV)
		emptyText.setText(emptyTitle)
		listView.setEmptyView(emptyText)

		/*
		 * For selected items
		 */selected = 0
		for (i in items!!.indices) {
			if (items!![i].isSelected) selected++
		}

		val save: TextView = view.findViewById(R.id.save)
		save.setOnClickListener {
			ad?.cancel()
		}
		val clear: TextView = view.findViewById(R.id.clear)
		clear.setOnClickListener {
			for (i in adapter!!.arrayList.indices) {
				adapter!!.arrayList[i].isSelected = false
			}
			adapter?.notifyDataSetChanged()
		}

		/*
        Added Select all Dialog Button.
         *//*if (isShowSelectAllButton && limit == -1) {
			builder?.setNeutralButton(R.string.select_all) { dialog, which ->
				for (i in adapter!!.arrayList.indices) {
					adapter!!.arrayList[i].isSelected = true
				}
				adapter?.notifyDataSetChanged()
				// To call onCancel listner and set title of selected items.
				dialog.cancel()
			}
		}
		builder?.setPositiveButton(R.string.ok) { dialog, which ->
			//Log.i(TAG, " ITEMS : " + items.size());
			dialog.cancel()
		}
		builder?.setNeutralButton(clearText) { dialog, which ->
			//Log.i(TAG, " ITEMS : " + items.size());
			for (i in adapter!!.arrayList.indices) {
				adapter!!.arrayList[i].isSelected = false
			}
			adapter?.notifyDataSetChanged()
			//dialog.cancel()
		}*/
		builder?.setOnCancelListener(this)
		ad = builder?.show()
		Objects.requireNonNull(ad?.window)?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
		return true
	}

	fun setItems(items: List<KeyPairBoolData>, listener: MultiSpinnerListener?) {
		this.items = items
		this.listener = listener
		val spinnerBuffer = StringBuilder()
		for (i in items.indices) {
			if (items[i].isSelected) {
				spinnerBuffer.append(items[i].name)
				spinnerBuffer.append(", ")
			}
		}
		if (spinnerBuffer.length > 2) defaultText =
			spinnerBuffer.toString().substring(0, spinnerBuffer.toString().length - 2)
		val adapterSpinner: ArrayAdapter<String> =
			ArrayAdapter(getContext(), R.layout.textview_for_spinner, arrayOf<String>(defaultText))
		setAdapter(adapterSpinner)
	}

	fun setEmptyTitle(emptyTitle: String) {
		this.emptyTitle = emptyTitle
	}

	fun setSearchHint(searchHint: String) {
		this.searchHint = searchHint
	}

	fun isShowSelectAllButton(): Boolean {
		return isShowSelectAllButton
	}

	fun setShowSelectAllButton(showSelectAllButton: Boolean) {
		isShowSelectAllButton = showSelectAllButton
	}

	interface LimitExceedListener {
		fun onLimitListener(data: KeyPairBoolData?)
	}

	//Adapter Class
	inner class MyAdapter internal constructor(
		context: Context?,
		var arrayList: List<KeyPairBoolData>
	) :
		BaseAdapter(), Filterable {
		val mOriginalValues : List<KeyPairBoolData> = arrayList
		val inflater: LayoutInflater

		init {
			inflater = LayoutInflater.from(context)
		}

		override fun getCount(): Int {
			return arrayList.size
		}

		override fun getItem(position: Int): Any {
			return position
		}

		override fun getItemId(position: Int): Long {
			return position.toLong()
		}

		override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
//            //Log.i(TAG, "getView() enter");
			var convertView: View? = convertView
			val holder: ViewHolder
			if (convertView == null) {
				holder = ViewHolder()
				convertView = inflater.inflate(R.layout.item_listview_multiple, parent, false)
				holder.textView = convertView?.findViewById(R.id.alertTextView)
				holder.checkBox = convertView?.findViewById(R.id.alertCheckbox)
				convertView?.setTag(holder)
			} else {
				holder = convertView.getTag() as ViewHolder
			}
			var background: Int = R.color.white
			if (colorSeparation) {
				val backgroundColor: Int =
					if (position % 2 == 0) R.color.list_even else R.color.list_odd
				background = backgroundColor
				convertView?.setBackgroundColor(
					ContextCompat.getColor(
						getContext(),
						backgroundColor
					)
				)
			}
			val data = arrayList[position]
			holder.textView?.setText(data.name)
			holder.checkBox?.setChecked(data.isSelected)
			convertView?.setOnClickListener { v ->
				if (data.isSelected) { // deselect
					selected--
				} else { // selected
					selected++
					if (selected > limit && limit > 0) {
						--selected // select with limit
						if (limitListener != null) limitListener!!.onLimitListener(data)
						return@setOnClickListener
					}
				}
				val temp =
					v.getTag() as ViewHolder
				temp.checkBox?.setChecked(!temp.checkBox!!.isChecked())
				data.isSelected = !data.isSelected
				//Log.i(TAG, "On Click Selected Item : " + data.getName() + " : " + data.isSelected());
				notifyDataSetChanged()
			}
			if (data.isSelected) {
				holder.textView?.setTextColor(textColor)
				if (highlightSelected) {
					holder.textView?.setTypeface(null, Typeface.BOLD)
					convertView?.setBackgroundColor(highlightColor)
				} else {
					convertView?.setBackgroundColor(Color.WHITE)
				}
			} else {
				holder.textView?.setTypeface(null, Typeface.NORMAL)
				convertView?.setBackgroundColor(ContextCompat.getColor(getContext(), background))
			}
			holder.checkBox?.setTag(holder)
			return convertView
		}

		@SuppressLint("DefaultLocale")
		override fun getFilter(): Filter {
			return object : Filter() {
				override fun publishResults(constraint: CharSequence?, results: FilterResults) {
					arrayList = results.values as List<KeyPairBoolData> // has the filtered values
					notifyDataSetChanged() // notifies the data with new filtered values
				}

				override fun performFiltering(constraint: CharSequence?): FilterResults {
					var constraint = constraint
					val results =
						FilterResults() // Holds the results of a filtering operation in values
					val FilteredArrList: MutableList<KeyPairBoolData> = ArrayList()


					/*
					 *
					 *  If constraint(CharSequence that is received) is null returns the mOriginalValues(Original) values
					 *  else does the Filtering and returns FilteredArrList(Filtered)
					 *
					 **/if (constraint == null || constraint.length == 0) {

						// set the Original result to return
						results.count = mOriginalValues.size
						results.values = mOriginalValues
					} else {
						constraint = constraint.toString().lowercase(Locale.getDefault())
						for (i in mOriginalValues.indices) {
							//Log.i(TAG, "Filter : " + mOriginalValues.get(i).getName() + " -> " + mOriginalValues.get(i).isSelected());
							val data = mOriginalValues[i].name
							if (data.lowercase(Locale.getDefault())
									.contains(constraint.toString())
							) {
								FilteredArrList.add(mOriginalValues[i])
							}
						}
						// set the Filtered result to return
						results.count = FilteredArrList.size
						results.values = FilteredArrList
					}
					return results
				}
			}
		}

		private inner class ViewHolder {
			var textView: TextView? = null
			var checkBox: CheckBox? = null
		}
	}

	companion object {
		var builder: AlertDialog.Builder? = null
		var ad: AlertDialog? = null
	}
}