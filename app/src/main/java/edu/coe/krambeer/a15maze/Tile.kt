package edu.coe.krambeer.a15maze

class Tile(xInit: Float, yInit: Float, sizeInit: Float, numberInit: Int) {
    private var x = xInit //bottom left corner
    private var y = yInit //bottom left corner
    private var minX = 0
    private var maxX = 0
    private var minY = 0
    private var maxY = 0
    private var size = sizeInit
    private var number = numberInit

    fun getX():Float{
        return x
    }

    fun getY():Float{
        return y
    }

    fun getSize():Float{
        return size
    }

    fun getNumber():Int{
        return number
    }

    fun setX(newX:Float){
        x = newX
    }

    fun setY(newY:Float){
        y = newY
    }

    fun setBounds(xBound:Int, yBound:Int){
        maxX = xBound
        maxY = yBound
    }

    fun boundedX(xVal:Float):Float{
        if(xVal < minX)
            return 0f
        else if(xVal > maxX - size)
            return maxX - size
        else
            return xVal
    }

    fun boundedY(yVal:Float):Float{
        if(yVal < minY)
            return 0f
        else if(yVal > maxY - size)
            return maxY - size
        else
            return yVal
    }
}