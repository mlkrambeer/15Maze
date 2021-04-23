package edu.coe.krambeer.a15maze

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.constraintlayout.widget.ConstraintLayout

class MainActivity : AppCompatActivity() {
    private var width = 0
    private var height = 0
    lateinit var container: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val displayMetrics = DisplayMetrics()
        width = displayMetrics.widthPixels
        height = displayMetrics.heightPixels
        val tileSize = 200f

        val testTile = Tile(30f, 30f, tileSize, 5)
        val tileView = TileView(this, testTile)
        testTile.setBounds(tileView.right - tileSize.toInt(), tileView.bottom - tileSize.toInt())

        container = findViewById(R.id.container)
        container.addView(tileView)

    }

}