package edu.coe.krambeer.a15maze

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.ScaleDrawable
import android.os.Build
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.res.ResourcesCompat.getDrawable
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.marginLeft
import kotlin.math.pow
import kotlin.math.sqrt

class ImageTileView(context: Context?, @DrawableRes picture: Int, private var xCord: Int, private var yCord: Int, private val number: Int, private val startLoc: Int): androidx.appcompat.widget.AppCompatImageView(context!!), View.OnTouchListener {
    private var displaySize = 270f

    private var touchOffsetX = 0f
    private var touchOffsetY = 0f

    private var prevX = 50f
    private var prevY = 50f

    private var directionSet = false
    private var moveXFlag = false
    private var colFlag = false

    private val otherTiles: ArrayList<ImageTileView> = ArrayList()

    private lateinit var activity: ImageTileViewListener

    init{
        setOnTouchListener(this)
        val image = getDrawable(resources, picture, resources.newTheme())!!
        val bitmap = (image as BitmapDrawable).bitmap
        val tileWidth = bitmap.width / 4
        val tileHeight = bitmap.height / 4
        val cropped = Bitmap.createBitmap(bitmap, ((number - 1) % 4) * tileWidth, ((number - 1) / 4) * tileHeight, tileWidth, tileHeight)
        setImageBitmap(cropped)

        prevX = (startLoc % 4) * displaySize
        prevY = (startLoc / 4) * displaySize

        invalidate()
    }

    override fun onTouch(v: View?, event: MotionEvent?):Boolean{
        val view = v as ImageTileView
        val touchX = event!!.rawX
        val touchY = event.rawY
        val action = event.action
        colFlag = false

        if(action == MotionEvent.ACTION_UP){
            directionSet = false
            snapToNearestSquare(view)
            return true
        }

        if(action == MotionEvent.ACTION_DOWN){
            touchOffsetX = touchX - view.x
            touchOffsetY = touchY - view.y
        }
        else if(action == MotionEvent.ACTION_MOVE){
            if(!directionSet){
                moveXFlag = !moveDirection()
                directionSet = true
            }

            if(moveXFlag){
                val newX = touchX - touchOffsetX
                view.x = boundedX(newX)
//                view.x = newX
                for(oTile in otherTiles){
                    if(oTile.isCollision(this))
                        colFlag = true
                }
                if(colFlag)
                    view.x = prevX
                else
                    prevX = newX
            }
            else{
                val newY = touchY - touchOffsetY
                view.y = boundedY(newY)
//                view.y = newY
                for(oTile in otherTiles){
                    if(oTile.isCollision(this))
                        colFlag = true
                }
                if(colFlag)
                    view.y = prevY
                else
                    prevY = newY
            }
        }

        view.invalidate()
        return true
    }

    fun snapToNearestSquare(view: ImageTileView){
        var minDist = Float.MAX_VALUE
        var snapX = 0f
        var snapY = 0f
        var snapXCord = 0
        var snapYCord = 0

        for(i in 0..15){
            val xSpot = (i%4) * view.getDisplaySize()
            val ySpot = (i/4) * view.getDisplaySize()
            val dist = sqrt((view.getCurrentX() - xSpot).toDouble().pow(2.0) + (view.getCurrentY() - ySpot).toDouble().pow(2.0))
            if(dist < minDist){
                minDist = dist.toFloat()
                snapX = xSpot
                snapY = ySpot
                snapXCord = (i%4)
                snapYCord = i/4
            }
        }

        view.setCurrentX(view.boundedX(snapX))
        view.setCurrentY(view.boundedY(snapY))
//        view.setCurrentX(snapX)
//        view.setCurrentY(snapY)

        view.setPrevX(snapX)
        view.setPrevY(snapY)
        view.setXCoord(snapXCord)
        view.setYCoord(snapYCord)

        view.invalidate()
        tellListener()
    }

    private fun moveDirection():Boolean{
        var sumX = 0
        for(oTile in otherTiles){
            if(xCord == oTile.getXCoord())
                sumX++
        }
        return sumX < 3
    }

    private fun isCollision(movingTile: ImageTileView):Boolean{
        val mXMin = movingTile.getCurrentX()
        val mXMax = mXMin + movingTile.getDisplaySize()
        val mYMin = movingTile.getCurrentY()
        val mYMax = mYMin + movingTile.getDisplaySize()

        val thisXMin = this.x
        val thisXMax = thisXMin + this.displaySize
        val thisYMin = this.y
        val thisYMax = thisYMin + this.displaySize

        val error = 1.5

        if(mXMin >= thisXMax - error || mXMax <= thisXMin + error || mYMin >= thisYMax - error || mYMax <= thisYMin + error)
            return false

        return true
    }

    fun isCorrectSpot():Boolean{
        if(!(xCord == (number - 1) % 4))
            return false
        if(!(yCord == (number - 1) / 4))
            return false
        return true
    }

    fun addOtherTile(tile: ImageTileView){
        otherTiles.add(tile)
    }

    fun addListener(l: ImageTileViewListener){
        activity = l
    }

    fun tellListener(){
        activity.checkImageWinCondition()
    }

    fun getDisplaySize():Float{
        return displaySize
    }

    fun getCurrentX():Float{
        return this.x
    }

    fun getCurrentY():Float{
        return this.y
    }

    fun setCurrentX(newX:Float){
        this.x = newX
    }

    fun setCurrentY(newY:Float){
        this.y = newY
    }

    fun boundedX(oldX:Float):Float{
        if(oldX < 0)
            return 0f
        if(oldX > displaySize * 3)
            return displaySize * 3
        return oldX
    }

    fun boundedY(oldY:Float):Float{
        if(oldY < 0)
            return 0f
        if(oldY > displaySize * 3)
            return displaySize * 3
        return oldY
    }

    fun getXCoord():Int{
        return xCord
    }

    fun getYCoord():Int{
        return yCord
    }

    fun setXCoord(newXCord:Int){
        xCord = newXCord
    }

    fun setYCoord(newYCord:Int){
        yCord = newYCord
    }

    fun getPrevX():Float{
        return prevX
    }

    fun getPrevY():Float{
        return prevY
    }

    fun setPrevX(oldX:Float){
        prevX = oldX
    }

    fun setPrevY(oldY:Float){
        prevY = oldY
    }
}