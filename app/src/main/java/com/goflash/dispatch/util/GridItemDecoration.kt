package com.goflash.dispatch.util

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GridItemDecoration(private val spacing: Int):
    RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % 2
        outRect.left = column * spacing / 2; // column * ((1f / spanCount) * spacing)
        outRect.right = spacing - (column + 1) * spacing / 2; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
        if (position >= 2) {
            outRect.top = spacing; // item top
        }
    }
}