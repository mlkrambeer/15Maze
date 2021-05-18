package edu.coe.krambeer.a15maze

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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

class ImageTileView(context: Context?, @DrawableRes picture: Int?, selectedBitmap: Bitmap?, private var xCord: Int, private var yCord: Int, private val number: Int, startLoc: Int): androidx.appcompat.widget.AppCompatImageView(context!!), View.OnTouchListener, MoveSwitchListener {
    private var displaySize = 270f //270f works

    private var touchOffsetX = 0f
    private var touchOffsetY = 0f

    private var prevX = 50f
    private var prevY = 50f

    private var directionSet = false
    private var moveXFlag = false
    private var colFlag = false

    private val otherTiles: ArrayList<ImageTileView> = ArrayList()

    private lateinit var activity: ImageTileViewListener

    private var fastMove = true //flag for which movement scheme to use

    init{
        setOnTouchListener(this)
        val metrics = context!!.resources.displayMetrics
        displaySize = metrics.widthPixels.toFloat() / 4

        if(picture != null)
            presetImageInit(picture, startLoc)
        else
            selectedImageInit(selectedBitmap!!, startLoc)
    }

    private fun presetImageInit(@DrawableRes picture: Int, startLoc: Int){
        val image = getDrawable(resources, picture, resources.newTheme())!!
        val bitmap = image.toBitmap()

        val imageWidth = bitmap.width / 4
        val imageHeight = bitmap.height / 4
        val tileSize: Int
        if(imageWidth < imageHeight)  //check which of the two is smaller, and use that for both dimensions; this will crop longer dimension
            tileSize = imageWidth
        else
            tileSize = imageHeight
        val cropped = Bitmap.createBitmap(bitmap, ((number - 1) % 4) * tileSize, ((number - 1) / 4) * tileSize, tileSize, tileSize)
        setImageBitmap(cropped)

        prevX = (startLoc % 4) * displaySize
        prevY = (startLoc / 4) * displaySize

        invalidate()
    }

    private fun selectedImageInit(selectedBitmap: Bitmap, startLoc: Int){
        val imageWidth = selectedBitmap.width / 4
        val imageHeight = selectedBitmap.height / 4
        val tileSize: Int
        if(imageWidth < imageHeight)
            tileSize = imageWidth
        else
            tileSize = imageHeight
        val cropped = Bitmap.createBitmap(selectedBitmap, ((number - 1) % 4) * tileSize, ((number - 1) / 4) * tileSize, tileSize, tileSize)
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
            if(fastMove)
                snapToNextSquare(view)
            else
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

    override fun setFastMove(value:Boolean){
        fastMove = value
    }

    fun snapToNextSquare(view: ImageTileView){
        val currentLocation = view.getXCoord() + 4 * view.getYCoord()
        val neighbors = getNeighbors(currentLocation)
        var isOpen = true
        var destination = currentLocation

        for(neighbor in neighbors){
            for(otherTile in view.otherTiles){
                if(neighbor == (otherTile.getXCoord() + 4 * otherTile.getYCoord())){
                    isOpen = false
                    break
                }
            }
            if(isOpen){
                destination = neighbor
                break
            }
            else
                isOpen = true
        }

        val move: Int
        if(destination == currentLocation)
            move = 0
        else
            move = 1

        val snapX = (destination % 4) * view.getDisplaySize()
        val snapY = (destination / 4) * view.getDisplaySize()

        view.setCurrentX(view.boundedX(snapX))
        view.setCurrentY(view.boundedY(snapY))
        view.setPrevX(snapX)
        view.setPrevY(snapY)
        view.setXCoord(destination % 4)
        view.setYCoord(destination / 4)

        view.invalidate()

        tellListener(move)
    }

    private fun getNeighbors(loc:Int):ArrayList<Int>{
        val neighbors = ArrayList<Int>()
        if(loc > 3)
            neighbors.add(loc - 4)
        if(loc < 12)
            neighbors.add(loc + 4)
        if((loc % 4) != 3)
            neighbors.add(loc + 1)
        if((loc % 4) != 0)
            neighbors.add(loc - 1)
        return neighbors
    }

    //more error prone movement
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

        val move: Int  //only increment move counter if moved to new position
        if(snapXCord == view.getXCoord() && snapYCord == view.getYCoord())
            move = 0
        else
            move = 1

        view.setCurrentX(view.boundedX(snapX))
        view.setCurrentY(view.boundedY(snapY))
        view.setPrevX(snapX)
        view.setPrevY(snapY)
        view.setXCoord(snapXCord)
        view.setYCoord(snapYCord)

        view.invalidate()

        tellListener(move)
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

    fun tellListener(move:Int){
        activity.checkImageWinCondition(move)
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