package edu.coe.krambeer.a15maze

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.HandlerThread
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.size
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
    lateinit var moveNumView: TextView
    private var moveCounter = 0
    lateinit var bestMovesView: TextView
    private var bestMoves = -1

    lateinit var bestTimeView: TextView
    private var bestTime = (-1).toLong()
    lateinit var timerTextView: TextView
    private var startTime = System.currentTimeMillis()
    private var finishTime = System.currentTimeMillis()
    private var pauseTime = 0.toLong()
    private var resumeTime = 0.toLong()
    private var totalTimeDelay = 0.toLong()
    private val handler = android.os.Handler(Looper.getMainLooper())
    private val timerRunnable = object: Runnable{
        override fun run() {
            val time = System.currentTimeMillis() - startTime - totalTimeDelay
            timerTextView.text = "Time: ${formattedTime(time)}"
            handler.postDelayed(this, 500)
        }
    }
    private var wasRunning = false

    lateinit var counterSwitch: SwitchCompat
    lateinit var counterContainer: LinearLayout

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

        bestMovesView = findViewById(R.id.bestMoves)
        bestTimeView = findViewById(R.id.bestTime)
        bestMoves = prefs.getInt("BEST_MOVES", -1)
        bestTime = prefs.getLong("BEST_TIME", -1)
        if(bestMoves != -1)
            bestMovesView.text = "Best: $bestMoves"
        else
            bestMovesView.text = "Best: none"
        if(bestTime != -1L){
            bestTimeView.text = "Best: ${formattedTime(bestTime)}"
        }
        else
            bestTimeView.text = "Best: none"

        moveNumView = findViewById(R.id.moveNum)
        moveNumView.text = "Moves: $moveCounter"

        timerTextView = findViewById(R.id.time)

        counterSwitch = findViewById(R.id.toggleTimers)
        counterContainer = findViewById(R.id.counters)

        counterSwitch.setOnCheckedChangeListener(object: CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                if(isChecked)
                    counterContainer.visibility = View.VISIBLE
                else{
                    if(wasRunning){
                        handler.removeCallbacks(timerRunnable)
                        wasRunning = false
                    }
                    counterContainer.visibility = View.INVISIBLE
                    timerTextView.text = "Time: 0:00"
                }
            }

        })
    }

    override fun onStop() {
        super.onStop()
        val prefs = getPreferences(Context.MODE_PRIVATE).edit()
        prefs.putInt("LAST_IMAGE", picture)
        prefs.putInt("BEST_MOVES", bestMoves)
        prefs.putLong("BEST_TIME", bestTime)
        prefs.apply()
    }

    override fun onPause(){
        super.onPause()
        handler.removeCallbacks(timerRunnable)
        pauseTime = System.currentTimeMillis()
        if(!gameDone)
            container.visibility = View.INVISIBLE
    }

    override fun onResume(){
        super.onResume()
        resumeTime = System.currentTimeMillis()
        totalTimeDelay += resumeTime - pauseTime
        if(wasRunning)
            handler.post(timerRunnable)
        container.visibility = View.VISIBLE
    }

    private fun gameSelector(){
        val selection = radioButtons.checkedRadioButtonId

        gameDone = false
        getValidRandomOrder()
        winText.text = ""
        container.removeAllViews()

        moveCounter = 0
        moveNumView.text = "Moves: $moveCounter"

        startTime = System.currentTimeMillis()
        totalTimeDelay = 0.toLong()

        if(counterSwitch.isChecked){
            handler.post(timerRunnable)
            wasRunning = true
        }

        if(selection == R.id.numbers)
            newGame()
        else
            imageGame()
    }

    private fun imageGame(){
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

    override fun checkWinCondition(move:Int){
        if(gameDone)
            return

        moveCounter += move
        moveNumView.text = "Moves: $moveCounter"

        var win = true
        for(tile in allTiles){
            if(!tile.isCorrectSpot())
                win = false
        }
        if(win){
            finishTime = System.currentTimeMillis()
            gameDone = true
            winText.text = "You Win!"
            handler.removeCallbacks(timerRunnable)
            checkRecords()
            wasRunning = false
        }
    }

    override fun checkImageWinCondition(move:Int) {
        if(gameDone)
            return

        moveCounter += move
        moveNumView.text = "Moves: $moveCounter"

        var win = true
        for(tile in allImageTiles){
            if(!(tile.isCorrectSpot()))
                win = false
        }
        if(win){
            finishTime = System.currentTimeMillis()
            gameDone = true
            winText.text = "You Win!"

            handler.removeCallbacks(timerRunnable)
            checkRecords()
            wasRunning = false

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

    private fun checkRecords(){
        if(moveCounter < bestMoves || bestMoves == -1){
            bestMoves = moveCounter
            bestMovesView.text = "Best: $bestMoves"
        }

        if(wasRunning){
            val winTime = finishTime - startTime - totalTimeDelay
            if(winTime < bestTime || bestTime == (-1).toLong()){
                bestTime = winTime
                bestTimeView.text = "Best: ${formattedTime(bestTime)}"
            }
        }
    }

    private fun formattedTime(time:Long):String{
        var seconds = (time / 1000).toInt()
        val minutes = seconds / 60
        seconds %= 60
        return String.format("%d:%02d", minutes, seconds)
    }
}