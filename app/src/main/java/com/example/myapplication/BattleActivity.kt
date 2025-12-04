package com.example.myapplication

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
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

    private lateinit var btnRSPZone: LinearLayout
    private lateinit var diceZone: LinearLayout
    private lateinit var diceImage: ImageView
    private lateinit var btnRollDice: ImageButton

    private var readyToBattle = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_battle)

        //가위바위보 영역
        btnRSPZone = findViewById(R.id.btnRSP_zone)

        //가위바위보 버튼
        btnScissor = findViewById(R.id.btnScissor)
        btnRock = findViewById(R.id.btnRock)
        btnPaper = findViewById(R.id.btnPaper)

        // 주사위 영역
        diceZone = findViewById(R.id.dicezone)
        diceImage = findViewById(R.id.diceImage)
        btnRollDice = findViewById(R.id.btnRollDice)

        // 선택 이미지
        playerChoose = findViewById(R.id.playerchoose)
        enemyChoose = findViewById(R.id.enemychoose)

        // 결과 텍스트 ("선공" / "후공")
        whoFirst = findViewById(R.id.whofirst)

        // 최초에는 선택 표시 숨김
        playerChoose.setImageDrawable(null)
        enemyChoose.setImageDrawable(null)
        whoFirst.text = ""
        diceZone.visibility = View.GONE

        btnScissor.setOnClickListener { playRSP(0) }
        btnRock.setOnClickListener { playRSP(1) }
        btnPaper.setOnClickListener { playRSP(2) }

        btnRollDice.setOnClickListener { rollDice() }
    }

    // 0:가위, 1:바위, 2:보
    private fun playRSP(playerChoice: Int) {
        if (readyToBattle) return

        val enemyChoice = Random.nextInt(3)

        // 플레이어가 선택한 가위바위보 이미지
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
                showDiceUI()
            }

            2 -> { // AI 승
                whoFirst.text = "후공"
                readyToBattle = true
                showDiceUI()
            }
        }
    }

    //RSP → Dice UI 교체
    private fun showDiceUI() {
        btnRSPZone.visibility = View.GONE   // 가위바위보 버튼 숨김
        diceZone.visibility = View.VISIBLE  // 주사위 UI 표시
    }

    // ⭐ 주사위 굴리기
    private fun rollDice() {
        // 굴리는 애니메이션
        val animator = ObjectAnimator.ofFloat(diceImage, "rotation", 0f, 360f)
        animator.duration = 500
        animator.interpolator = LinearInterpolator()
        animator.start()

        Handler(Looper.getMainLooper()).postDelayed({
            val diceNumber = Random.nextInt(1, 7)

            val diceRes = when (diceNumber) {
                1 -> R.drawable.dice1
                2 -> R.drawable.dice2
                3 -> R.drawable.dice3
                4 -> R.drawable.dice4
                5 -> R.drawable.dice5
                else -> R.drawable.dice6
            }

            diceImage.setImageResource(diceRes)

            // 🎯 여기서 diceNumber에 따라 공격력/피해량/턴 적용 등 원하는 기능을 추가하면 됨

        }, 500)
    }

}
