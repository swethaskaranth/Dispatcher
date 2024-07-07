package com.goflash.dispatch.ui.itemDecoration

import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import android.view.View

class MarginItemDecoration(private val spaceHeight: Int) :
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

            if(dataSize > 0 && position == dataSize-1)
                this.bottom = 70

        }
    }

}