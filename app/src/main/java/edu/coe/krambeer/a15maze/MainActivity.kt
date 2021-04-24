package edu.coe.krambeer.a15maze

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.size
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private var width = 0
    private var height = 0
    lateinit var container: ConstraintLayout
    lateinit var newGameButton: Button
    lateinit var fixButton: Button
    private val randomOrder: MutableList<Int> = arrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16).toMutableList()

    private var allTiles: ArrayList<TileView> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        newGameButton = findViewById(R.id.newGame)
        newGameButton.setOnClickListener{ newGame() }

        fixButton = findViewById(R.id.fix)
        fixButton.setOnClickListener { fixTiles() }
    }

    private fun newGame(){
        randomOrder.shuffle()
        container = findViewById(R.id.container)
        container.removeAllViews()
        allTiles = ArrayList()
        width = container.size
        height = container.size

        val tileSize = 250f     //270f is closest fit
        val spacing = 20f

        for(i in 0..15){
            if(randomOrder[i] == 16)
                continue
            val testTile = Tile((i%4) * (tileSize + spacing), (i/4) * (tileSize + spacing), tileSize, randomOrder[i])
            val tileView = TileView(this, testTile, (i%4), i/4)

            testTile.setBounds(container.right, container.bottom)

            container.addView(tileView)

            allTiles.add(tileView)
        }

        for(tile1 in allTiles){
            for(tile2 in allTiles){
                if(tile1 == tile2)
                    continue
                tile1.addOtherTile(tile2)
            }
        }
    }

    private fun fixTiles(){
        for(tile in allTiles){
            tile.snapToNearestSquare(tile)
        }
    }
}