package edu.coe.krambeer.a15maze

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.size

class MainActivity : AppCompatActivity() {
    private var width = 0
    private var height = 0
    lateinit var container: ConstraintLayout
    lateinit var newGameButton: Button

    private var allTiles: ArrayList<TileView> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        newGameButton = findViewById(R.id.newGame)
        newGameButton.setOnClickListener{ newGame() }
    }

    private fun newGame(){
        container = findViewById(R.id.container)
        container.removeAllViews()
        allTiles = ArrayList()
        width = container.size
        height = container.size

        val tileSize = 270f

        for(i in 0..14){
            val testTile = Tile(0f + (i%4) * tileSize, 0f + (i/4) * tileSize, tileSize, i + 1)
            val tileView = TileView(this, testTile)

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
}