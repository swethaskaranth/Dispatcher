package com.goflash.dispatch.ui.itemDecoration

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BoundaryItemDecoartion (private val context: Context) : RecyclerView.ItemDecoration(){

    private var paint : Paint? = null
    private val dividerHeight = 2

    private var spaceHeight: Int = 0

    private var layoutOrientation = -1

    constructor(context: Context,color : Int, height: Int) : this(context) {
        paint = Paint()
        paint!!.color = color
        paint!!.style = Paint.Style.STROKE
        paint!!.strokeWidth = dividerHeight.toFloat()
        spaceHeight = height
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.set(dividerHeight, spaceHeight+dividerHeight, dividerHeight, dividerHeight)


    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)

        if (parent.layoutManager is LinearLayoutManager && layoutOrientation == -1) {
            layoutOrientation =
                (parent.layoutManager as LinearLayoutManager).orientation
        }
        if (layoutOrientation == LinearLayoutManager.HORIZONTAL) {
            horizontal(c, parent)
        } else {
            vertical(c, parent)
        }
    }

    private fun horizontal(c: Canvas, parent: RecyclerView) {
        val top = parent.paddingTop
        val bottom = parent.height - parent.paddingBottom
        val itemCount = parent.childCount
        for (i in 0 until itemCount) {
            val child = parent.getChildAt(i)
            val params = child
                .layoutParams as RecyclerView.LayoutParams
            val left = child.right + params.rightMargin
            val right = child.left + dividerHeight
            c.drawRoundRect(RectF(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat()), 5.0f, 5.0f, paint!!);
        }
    }

    private fun vertical(c: Canvas, parent: RecyclerView) {
        val left = parent.paddingStart
        val right = parent.width - parent.paddingEnd
        val itemCount = parent.childCount
        for (i in 0 until itemCount) {
            val child = parent.getChildAt(i)
            val params = child
                .layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin
            val bottom = child.top + dividerHeight
            c.drawRoundRect(RectF(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat()), 5.0f, 5.0f, paint!!);


        }
    }





}