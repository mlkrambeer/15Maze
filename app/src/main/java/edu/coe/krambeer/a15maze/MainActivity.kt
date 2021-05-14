package edu.coe.krambeer.a15maze

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.widget.*
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.size
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), TileViewListener, ImageTileViewListener {
    private var width = 0
    private var height = 0
    lateinit var container: FrameLayout
    lateinit var newGameButton: Button
    lateinit var winText: TextView
    lateinit var radioButtons: RadioGroup
    lateinit var randomSwitch: SwitchCompat
    private var randomOrder: Array<Int> = arrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)
    private var blankLocation = 15

    private var allTiles: ArrayList<TileView> = ArrayList()
    private var allImageTiles: ArrayList<ImageTileView> = ArrayList()

    private var gameDone = false

    private var enableRandomPicture = true
    private var picture: Int = R.drawable.gingkotree //default image

    //my array
    private val allPictures: Array<Int> = arrayOf(R.drawable.animals,    R.drawable.babby,           R.drawable.beatificvision,
                                                  R.drawable.chef,       R.drawable.cliffsofdover,   R.drawable.criticalmoments,
                                                  R.drawable.dogecoin,
                                                  R.drawable.doggarlic,  R.drawable.ducks,           R.drawable.gingkotree,
                                                  R.drawable.glacier,    R.drawable.illusion,        R.drawable.mandelbrot,
                                                  R.drawable.mugiwara,   R.drawable.pamukkale,       R.drawable.pantanal,
                                                  R.drawable.pizza,      R.drawable.pucci,           R.drawable.shiptonscave,
                                                  R.drawable.supermoon,  R.drawable.sushi,           R.drawable.tree)

    //mom's array
//    private val allPictures: Array<Int> = arrayOf(R.drawable.tomhanks,  R.drawable.cliffsofdover, R.drawable.criticalmoments,
//                                                  R.drawable.pamukkale, R.drawable.pantanal,      R.drawable.gingkotree)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        container = findViewById(R.id.container)

        newGameButton = findViewById(R.id.newGame)
        newGameButton.setOnClickListener{ gameSelector() }

        winText = findViewById(R.id.winText)

        radioButtons = findViewById(R.id.tileType)

        randomSwitch = findViewById(R.id.toggleRandom)

        val prefs = getPreferences(Context.MODE_PRIVATE)
        picture = prefs.getInt("LAST_IMAGE", picture)


    }

    override fun onStop() {
        super.onStop()
        val prefs = getPreferences(Context.MODE_PRIVATE).edit()
        prefs.putInt("LAST_IMAGE", picture)
        prefs.apply()
    }

    private fun gameSelector(){
        val selection = radioButtons.checkedRadioButtonId
        if(selection == R.id.numbers)
            newGame()
        else
            imageGame()
    }

    private fun imageGame(){
        gameDone = false
        getValidRandomOrder()
        winText.text = ""
        container.removeAllViews()
        allImageTiles = ArrayList()

        val tileSize = 270

        enableRandomPicture = randomSwitch.isChecked
        if(enableRandomPicture)
            picture = allPictures[(allPictures.indices).random()]

        for(i in 0..15){
            if(randomOrder[i] == 16)
                continue

            val tileView = ImageTileView(this, picture, i%4, i/4, randomOrder[i], i)
            tileView.addListener(this)
            val params = FrameLayout.LayoutParams(tileSize, tileSize)
            params.leftMargin = (i%4) * tileSize
            params.topMargin = (i/4) * tileSize
            container.addView(tileView, params)

            allImageTiles.add(tileView)
        }

        for(tile1 in allImageTiles){
            for(tile2 in allImageTiles){
                if(tile1 == tile2)
                    continue
                tile1.addOtherTile(tile2)
            }
        }
    }

    private fun newGame(){
        getValidRandomOrder()
        winText.text = ""
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
    private fun fixTiles(){  //delete and re-create all tiles
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
        while(validSwaps < 999){  //if you do an odd number of swaps, it guarantees the puzzle won't start solved
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

    override fun checkWinCondition(){
        var win = true
        for(tile in allTiles){
            if(!tile.isCorrectSpot())
                win = false
        }
        if(win)
            winText.text = "You Win!"
    }

    override fun checkImageWinCondition() {
        if(gameDone)
            return
        var win = true
        for(tile in allImageTiles){
            if(!(tile.isCorrectSpot()))
                win = false
        }
        if(win){
            gameDone = true
            winText.text = "You Win!"

            val tileView = ImageTileView(this, picture, 3, 3, 16, 15)
            tileView.addListener(this)
            val params = FrameLayout.LayoutParams(270, 270)
            params.leftMargin = 3 * 270
            params.topMargin = 3 * 270
            container.addView(tileView, params)

            allImageTiles.add(tileView)

            for(tile1 in allImageTiles){
                for(tile2 in allImageTiles){
                    if(tile1 == tile2)
                        continue
                    tile1.addOtherTile(tile2)
                }
            }
        }
    }
}