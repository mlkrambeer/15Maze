package edu.coe.krambeer.a15maze

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.size
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), TileViewListener {
    private var width = 0
    private var height = 0
    lateinit var container: ConstraintLayout
    lateinit var newGameButton: Button
    lateinit var winText: TextView
    private var randomOrder: Array<Int> = arrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)
    private var blankLocation = 15

    private var allTiles: ArrayList<TileView> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        newGameButton = findViewById(R.id.newGame)
        newGameButton.setOnClickListener{ newGame() }

        winText = findViewById(R.id.winText)
    }

    private fun newGame(){
        getValidRandomOrder()
        winText.text = ""
        container = findViewById(R.id.container)
        container.removeAllViews()
        allTiles = ArrayList()
        width = container.size
        height = container.size

        val tileSize = 250f
        val spacing = 20f
        val offset = 7f

        for(i in 0..15){
            if(randomOrder[i] == 16)
                continue
            val testTile = Tile((i%4) * (tileSize + spacing) + offset, (i/4) * (tileSize + spacing) + offset, tileSize, randomOrder[i])
            val tileView = TileView(this, testTile, (i%4), i/4)

            testTile.setBounds(container.right, container.bottom)

            container.addView(tileView)

            allTiles.add(tileView)

            tileView.addListener(this)
        }

        for(tile1 in allTiles){
            for(tile2 in allTiles){
                if(tile1 == tile2)
                    continue
                tile1.addOtherTile(tile2)
            }
        }
    }

    //tiles used to clip into each other and get stuck; fixed for now, but I'm keeping this code around
    private fun fixTiles(){
        val newAllTiles: ArrayList<TileView> = ArrayList()
        for(tile in allTiles){
            val x = tile.getXCoord()
            val y = tile.getYCoord()
            val tileObj = tile.getTile()
            val copy = TileView(this, tileObj, x, y)
            copy.addListener(this)
            container.removeView(tile)
            container.addView(copy)

            newAllTiles.add(copy)
        }

        allTiles = newAllTiles

        for(tile1 in allTiles){
            for(tile2 in allTiles){
                if(tile1 == tile2)
                    continue
                tile1.addOtherTile(tile2)
            }
        }
    }

    private fun getValidRandomOrder(){
        randomOrder = arrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)
        blankLocation = 15
        var validSwaps = 0
        while(validSwaps < 1000){
            val nextSwap = getNextSwap()
            swap(nextSwap, blankLocation)
            blankLocation = nextSwap
            validSwaps++
        }
    }

    private fun getNextSwap():Int{
        val possible: ArrayList<Int> = ArrayList()
        if(blankLocation > 3)
            possible.add(blankLocation - 4)
        if(blankLocation < 12)
            possible.add(blankLocation + 4)
        if(!(blankLocation % 4 == 0))
            possible.add(blankLocation - 1)
        if(!(blankLocation % 4 == 3))
            possible.add(blankLocation + 1)

        val num = possible.size
        return possible[(0 until num).random()]
    }

    private fun swap(index1:Int, index2:Int){
        val temp = randomOrder[index1]
        randomOrder[index1] = randomOrder[index2]
        randomOrder[index2] = temp
    }

    override fun checkWinCondition() {
        var win = true
        for(tile in allTiles){
            if(!tile.isCorrectSpot())
                win = false
        }
        if(win)
            winText.text = "You Win!"
    }


}