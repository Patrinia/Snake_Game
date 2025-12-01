package com.example.myapplication

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.LinearInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class BattleActivity : AppCompatActivity() {

    private lateinit var btnScissor: ImageButton
    private lateinit var btnRock: ImageButton
    private lateinit var btnPaper: ImageButton

    private lateinit var playerChoose: ImageView
    private lateinit var enemyChoose: ImageView
    private lateinit var whoFirst: TextView

    private var playerWin = false
    private var enemyWin = false
    private var readyToBattle = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_battle)

        // 버튼
        btnScissor = findViewById(R.id.btnScissor)
        btnRock = findViewById(R.id.btnRock)
        btnPaper = findViewById(R.id.btnPaper)

        // 선택 이미지
        playerChoose = findViewById(R.id.playerchoose)
        enemyChoose = findViewById(R.id.enemychoose)

        // 결과 텍스트 ("선공" / "후공")
        whoFirst = findViewById(R.id.whofirst)

        // 최초에는 선택 표시 숨김
        playerChoose.setImageDrawable(null)
        enemyChoose.setImageDrawable(null)
        whoFirst.text = ""

        btnScissor.setOnClickListener { playRSP(0) }
        btnRock.setOnClickListener { playRSP(1) }
        btnPaper.setOnClickListener { playRSP(2) }
    }

    // 0:가위, 1:바위, 2:보
    private fun playRSP(playerChoice: Int) {
        if (readyToBattle) return

        val enemyChoice = Random.nextInt(3)

        // 플레이어 이미지 반영
        when (playerChoice) {
            0 -> playerChoose.setImageResource(R.drawable.scissors100px)
            1 -> playerChoose.setImageResource(R.drawable.rock100px)
            2 -> playerChoose.setImageResource(R.drawable.paper100px)
        }

        // AI 이미지 반영
        when (enemyChoice) {
            0 -> enemyChoose.setImageResource(R.drawable.scissors100px)
            1 -> enemyChoose.setImageResource(R.drawable.rock100px)
            2 -> enemyChoose.setImageResource(R.drawable.paper100px)
        }

        // 승패 판정
        val result = (3 + playerChoice - enemyChoice) % 3
        // 0: 비김, 1: 플레이어 승, 2: AI 승

        when (result) {
            0 -> { // 비김
                whoFirst.text = "비김! 다시!"
                Handler(Looper.getMainLooper()).postDelayed({
                    whoFirst.text = ""
                    playerChoose.setImageDrawable(null)
                    enemyChoose.setImageDrawable(null)
                }, 1200)
            }

            1 -> { // 플레이어 승
                whoFirst.text = "선공"
                readyToBattle = true
            }

            2 -> { // AI 승
                whoFirst.text = "후공"
                readyToBattle = true
            }
        }
    }
}
