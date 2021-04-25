package edu.coe.krambeer.a15maze

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.MotionEvent
import android.view.View
import java.lang.Math.pow
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class TileView(context: Context?, private val tile: Tile, private var xCord: Int, var yCord: Int): View(context), View.OnTouchListener {

    private val painter = Paint()
    private var touchOffsetX = 0f
    private var touchOffsetY = 0f

    private var prevX = 0f
    private var prevY = 0f

    private var directionSet = false
    private var moveXFlag = false
    private var colFlag = false

    private val otherTiles: ArrayList<TileView> = ArrayList()

    private lateinit var activity: TileViewListener

    init{
        setOnTouchListener(this)
        prevX = tile.getX()
        prevY = tile.getY()
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        painter.color = Color.BLACK
        if(isCorrectSpot())
            painter.color = Color.RED
        painter.style = Paint.Style.STROKE
        painter.strokeWidth = 15f
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

    //this only allows changing one of x or y coords at a time
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        val view = v as TileView
        val touchX = event!!.x
        val touchY = event.y
        colFlag = false

        if(event.action == MotionEvent.ACTION_UP){
            directionSet = false
            snapToNearestSquare(view)
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
                moveXFlag = !moveDirection()
                directionSet = true
            }
            if(moveXFlag){
                val newX = touchX - touchOffsetX
                view.tile.setX(view.tile.boundedX(newX))
                for(oTile in otherTiles){
                    if(oTile.isCollision(this))
                        colFlag = true
                }
                if(colFlag)
                    view.tile.setX(view.tile.boundedX(prevX))
                else
                    prevX = newX
            }
            else{
                val newY = touchY - touchOffsetY
                view.tile.setY(view.tile.boundedY(newY))
                for(oTile in otherTiles){
                    if(oTile.isCollision(this))
                        colFlag = true
                }
                if(colFlag)
                    view.tile.setY(view.tile.boundedY(prevY))
                else
                    prevY = newY
            }
        }

        view.invalidate()
        return true
    }

    fun snapToNearestSquare(view: TileView){
        val spacing = 20f
        val offset = 7f

        var minDist = Float.MAX_VALUE
        var snapX = 0f
        var snapY = 0f
        var snapXCord = 0
        var snapYCord = 0

        for(i in 0..15){
            val xSpot = (i%4) * (view.tile.getSize() + spacing) + offset
            val ySpot = (i/4) * (view.tile.getSize() + spacing) + offset
            val dist = sqrt((view.getPrevX() - xSpot).toDouble().pow(2.0) + (view.getPrevY() - ySpot).toDouble().pow(2.0))
            if(dist < minDist){
                minDist = dist.toFloat()
                snapX = xSpot
                snapY = ySpot
                snapXCord = (i%4)
                snapYCord = i/4
            }
        }
        view.tile.setX(view.tile.boundedX(snapX))
        view.tile.setY(view.tile.boundedY(snapY))
        view.setPrevX(snapX)
        view.setPrevY(snapY)
        view.setXCoord(snapXCord)
        view.setYCoord(snapYCord)
        view.invalidate()
        tellListener()
    }

    private fun moveDirection():Boolean{ //true = X, false = Y
        var sumX = 0
        for(oTile in otherTiles){
            if(xCord == oTile.getXCoord())
                sumX++
        }
        return sumX < 3
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

        val error = 1.5

        if(mXMin >= thisXMax - error || mXMax <= thisXMin + error || mYMin >= thisYMax - error || mYMax <= thisYMin + error)
            return false

        return true
    }

    fun addOtherTile(tile: TileView){
        otherTiles.add(tile)
    }

    fun getPrevX():Float{
        return prevX
    }

    fun getPrevY():Float{
        return prevY
    }

    fun setPrevX(newX:Float){
        prevX = newX
    }

    fun setPrevY(newY:Float){
        prevY = newY
    }

    fun getXCoord():Int{
        return xCord
    }

    fun getYCoord():Int{
        return yCord
    }

    fun setXCoord(newX:Int){
        xCord = newX
    }

    fun setYCoord(newY:Int){
        yCord = newY
    }

    fun isCorrectSpot():Boolean{
        if(!(xCord == (tile.getNumber() - 1) % 4))
            return false
        if(!(yCord == (tile.getNumber() - 1) / 4))
            return false
        return true
    }

    fun addListener(l: TileViewListener){
        activity = l
    }

    fun tellListener(){
        activity.checkWinCondition()
    }

    fun getTile():Tile{
        return tile
    }
}