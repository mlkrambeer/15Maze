package edu.coe.krambeer.a15maze

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View

class TileView(context: Context?, private val tile: Tile) : View(context), View.OnTouchListener {

    private val painter = Paint()

    init{
        setOnTouchListener(this)
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        painter.color = Color.BLACK
        painter.style = Paint.Style.STROKE
        painter.strokeWidth = 20f
        val left = tile.getX()
        val right = tile.getX() + tile.getSize()
        val top = tile.getY()
        val bottom = tile.getY() + tile.getSize()
        canvas!!.drawRect(left, top, right, bottom, painter)

        painter.textSize = 70F
        painter.textAlign = Paint.Align.LEFT
        painter.strokeWidth = 5f
        canvas.drawText(tile.getNumber().toString(), (right + left) / 2 - 25f, (top + bottom) / 2 + 25f, painter)

    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        val view = v as TileView
        val touchX = event!!.x
        val touchY = event.y

        view.tile.setX(view.tile.boundedX(touchX))
        view.tile.setY(view.tile.boundedY(touchY))
        Log.i("test", tile.getX().toString())
        Log.i("test", tile.getY().toString())

        view.invalidate()
        return true
    }

}