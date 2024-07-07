package com.goflash.dispatch.ui.itemDecoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class VerticalMarginItemDecoration (private val spaceHeight: Int) :
    RecyclerView.ItemDecoration() {


    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val dataSize = state.itemCount
        val position = parent.getChildAdapterPosition(view)
        with(outRect) {
            if (parent.getChildAdapterPosition(view) != 0)
                this.top = spaceHeight
        }
    }

}