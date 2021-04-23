package edu.coe.krambeer.a15maze

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

class TileView(context: Context?, private val tile: Tile): View(context), View.OnTouchListener {

    private val painter = Paint()
    private var touchOffsetX = 0f
    private var touchOffsetY = 0f

    private var prevX = 0f
    private var prevY = 0f

    private var directionSet = false
    private var moveXFlag = false
    private var colFlag = false

    private val otherTiles: ArrayList<TileView> = ArrayList()

    init{
        setOnTouchListener(this)
        prevX = tile.getX()
        prevY = tile.getY()
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

        if(event.action == MotionEvent.ACTION_UP){
            //directionSet = false
            return true
        }

        if(!view.tile.isClickInBounds(touchX, touchY))
            return false

        if(event.action == MotionEvent.ACTION_DOWN){
            touchOffsetX = touchX - prevX
            touchOffsetY = touchY - prevY
        }
        else if(event.action == MotionEvent.ACTION_MOVE){
            if(!directionSet){
                moveXFlag = abs(touchX - prevX) > abs(touchY - prevY)
                directionSet = true
            }
            if(moveXFlag){
                val newX = touchX - touchOffsetX
                view.tile.setX(view.tile.boundedX(newX))
//                for(oTile in otherTiles){
//                    if(oTile.isCollision(this))
//                        colFlag = true
//                }
                if(colFlag)
                    view.tile.setX(view.tile.boundedX(prevX))
                else
                    prevX = newX
            }
            else{
                val newY = touchY - touchOffsetY
                view.tile.setY(view.tile.boundedY(newY))
//                for(oTile in otherTiles){
//                    if(oTile.isCollision(this))
//                        colFlag = true
//                }
                if(colFlag)
                    view.tile.setY(view.tile.boundedY(prevY))
                else
                    prevY = newY
            }
        }

        view.invalidate()
        return true
    }

    private fun isCollision(movingTile: TileView):Boolean{
        val mXMin = movingTile.tile.getX()
        val mXMax = mXMin + movingTile.tile.getSize()
        val mYMin = movingTile.tile.getY()
        val mYMax = mYMin + movingTile.tile.getSize()

        val thisXMin = tile.getX()
        val thisXMax = thisXMin + tile.getSize()
        val thisYMin = tile.getY()
        val thisYMax = thisYMin + tile.getSize()

        if(mXMin >= thisXMax || mXMax <= thisXMin || mYMin >= thisYMax || mYMax <= thisYMin)
            return false

        return true
    }

    fun addOtherTile(tile: TileView){
        otherTiles.add(tile)
    }
}