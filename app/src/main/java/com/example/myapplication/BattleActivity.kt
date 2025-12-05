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

    // ★ 전투 변수
    private var playerHP = 20
    private var enemyHP = 20

    private var playerAtk = 0
    private var enemyAtk = 0
    private var playerDef = 0
    private var enemyDef = 0

    // ★ 주사위 잔여 횟수
    private var playerDiceRemain = 1
    private var enemyDiceRemain = 1

    // ★ 턴 정보
    private var isPlayerTurn = true

    // ★ 스팸 방지 Lock
//    private var isDiceRolling = false


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

        // 결과 텍스트 ("선공" / "후공" 및 전투 로그)
        whoFirst = findViewById(R.id.whofirst)

        // 초기 상태
        playerChoose.setImageDrawable(null)
        enemyChoose.setImageDrawable(null)
        whoFirst.text = ""

        btnRSPZone.visibility = View.VISIBLE
        diceZone.visibility = View.GONE

        btnScissor.setOnClickListener { playRSP(0) }
        btnRock.setOnClickListener { playRSP(1) }
        btnPaper.setOnClickListener { playRSP(2) }

        // 플레이어 턴 버튼 리스너 (사용자 조작)
        btnRollDice.setOnClickListener { playerTurn() }
    }

    // 0:가위, 1:바위, 2:보
    private fun playRSP(playerChoice: Int) {
        if (readyToBattle) return

        val enemyChoice = Random.nextInt(3)

        // 플레이어 이미지
        when (playerChoice) {
            0 -> playerChoose.setImageResource(R.drawable.scissors100px)
            1 -> playerChoose.setImageResource(R.drawable.rock100px)
            2 -> playerChoose.setImageResource(R.drawable.paper100px)
        }

        // 적 이미지
        when (enemyChoice) {
            0 -> enemyChoose.setImageResource(R.drawable.scissors100px)
            1 -> enemyChoose.setImageResource(R.drawable.rock100px)
            2 -> enemyChoose.setImageResource(R.drawable.paper100px)
        }

        // 승패 판정
        val result = (3 + playerChoice - enemyChoice) % 3

        when (result) {
            0 -> { // 비김
                whoFirst.text = "비김! 다시!"
                Handler(Looper.getMainLooper()).postDelayed({
                    whoFirst.text = ""
                    playerChoose.setImageDrawable(null)
                    enemyChoose.setImageDrawable(null)
                }, 1200)
            }

            1 -> { // 플레이어 선공
                whoFirst.text = "선공"
                readyToBattle = true
                isPlayerTurn = true

                Handler(Looper.getMainLooper()).postDelayed({
                    // 3초 후 이미지 제거하고 주사위 UI로 전환
                    playerChoose.setImageDrawable(null)
                    enemyChoose.setImageDrawable(null)
                    whoFirst.text = ""

                    showDiceUI()
                    // 플레이어가 선공이므로 버튼 활성화
                    playerDiceRemain = 1
                    btnRollDice.isEnabled = true

                }, 3000)
            }

            2 -> { // 플레이어 후공
                whoFirst.text = "후공"
                readyToBattle = true
                isPlayerTurn = false

                Handler(Looper.getMainLooper()).postDelayed({
                    // 3초 후 이미지 제거하고 주사위 UI로 전환
                    playerChoose.setImageDrawable(null)
                    enemyChoose.setImageDrawable(null)
                    whoFirst.text = ""

                    showDiceUI()
                    // 적이 먼저 굴려야 하므로 버튼 비활성
                    btnRollDice.isEnabled = false
                    enemyDiceRemain = 1

                    // 적 자동 실행 (약간의 지연)
                    Handler(Looper.getMainLooper()).postDelayed({
                        enemyTurn()
                    }, 700)

                }, 3000)
            }
        }
    }

    // RSP → Dice UI 전환
    private fun showDiceUI() {
        btnRSPZone.visibility = View.GONE
        diceZone.visibility = View.VISIBLE
    }

    // ============================================================
    // ⭐ 플레이어가 버튼으로 주사위 굴림
    // ============================================================
    private fun playerTurn() {
        if (!isPlayerTurn) return                 // 내 턴 아니면 무시
//        if (isDiceRolling) return                 // 연타 방지
        if (playerDiceRemain <= 0) return         // 남은 횟수 없으면 무시

//        isDiceRolling = true
        btnRollDice.isEnabled = false

        Handler(Looper.getMainLooper()).postDelayed({
            // 주사위 결과 처리는 공통 함수(rollDice_Common)로 수행
            rollDice()
        }, 500)
    }

    // ⭐ 적의 자동 턴

    private fun enemyTurn() {
        // 남은 굴림 횟수 없으면 턴 종료
        if (enemyDiceRemain <= 0) {
            endEnemyTurn()
            return
        }

        // 적이 굴리는 동안 플레이어는 버튼 금지
        isPlayerTurn = false
        btnRollDice.isEnabled = false
//        isDiceRolling = true

        Handler(Looper.getMainLooper()).postDelayed({
            // 공통 처리 호출 (isPlayerTurn이 false이므로 적 방식으로 처리됨)
            rollDice()
        }, 500)
    }

    // ============================================================
    // ⭐ 공통 주사위 로직: 결과 생성 → 이미지 반영 → 대미지 적용 → 상태체크
    // ============================================================
    private fun rollDice() {
        val diceNumber = Random.nextInt(1, 7)

        // 주사위 리소스(화면에 보여줄 이미지)
        val diceRes = when (diceNumber) {
            1 -> R.drawable.dice1
            2 -> R.drawable.dice2
            3 -> R.drawable.dice3
            4 -> R.drawable.dice4
            5 -> R.drawable.dice5
            else -> R.drawable.dice6
        }

        // 누가 굴렸는지에 따라 이미지 반영 위치와 대미지 계산/로그가 달라짐
        if (isPlayerTurn) {
            // 플레이어가 굴림 -> playerChoose에 주사위 이미지 반영
            playerChoose.setImageResource(diceRes)

            val damage = (playerAtk + diceNumber - enemyDef).coerceAtLeast(0)
            enemyHP -= damage
            if (enemyHP < 0) enemyHP = 0

            // 로그
            whoFirst.text = "플레이어가 적에게 $damage 대미지를 줬습니다. (주사위 $diceNumber)"

            // 남은 횟수 차감
            playerDiceRemain--
        } else {
            // 적이 굴림 -> enemyChoose에 주사위 이미지 반영
            enemyChoose.setImageResource(diceRes)

            val damage = (enemyAtk + diceNumber - playerDef).coerceAtLeast(0)
            playerHP -= damage
            if (playerHP < 0) playerHP = 0

            // 로그
            whoFirst.text = "적이 플레이어에게 $damage 대미지를 줬습니다. (주사위 $diceNumber)"

            // 남은 횟수 차감
            enemyDiceRemain--
        }

//        // 애니메이션/락 해제
//        isDiceRolling = false

        // 전투 상태 체크 (HP, 남은 주사위 등)
        checkBattleState()
    }

    // ============================================================
    // ⭐ 전투 체력/턴 종료 체크
    // ============================================================
    private fun checkBattleState() {
        // 승패 체크
        if (playerHP <= 0) {
            whoFirst.text = "플레이어 패배!"
            btnRollDice.isEnabled = false
            return
        }
        if (enemyHP <= 0) {
            whoFirst.text = "적 패배!"
            btnRollDice.isEnabled = false
            return
        }

        // 턴별 남은 주사위 체크: 남은 횟수 없으면 턴 종료 흐름 시작
        if (isPlayerTurn) {
            if (playerDiceRemain <= 0) {
                Handler(Looper.getMainLooper()).postDelayed({
                    endPlayerTurn()
                }, 1200)
            } else {
                // 아직 굴릴 횟수 남음 -> 버튼 다시 활성화
                btnRollDice.isEnabled = true
            }
        } else {
            if (enemyDiceRemain <= 0) {
                Handler(Looper.getMainLooper()).postDelayed({
                    endEnemyTurn()
                }, 1200)
            } else {
                // 적이 추가 굴림을 할 수 있다면 자동으로 진행
                Handler(Looper.getMainLooper()).postDelayed({
                    enemyTurn()
                }, 800)
            }
        }
    }


    // 플레이어 턴 종료 → 5초 후 적 턴
    private fun endPlayerTurn() {
        isPlayerTurn = false
        btnRollDice.isEnabled = false

        // 턴이 바뀔 때 적의 굴림 횟수 초기화 (보상/스킬로 변경 가능)
        enemyDiceRemain = 1

        Handler(Looper.getMainLooper()).postDelayed({
            enemyTurn()
        }, 5000)
    }

    // 적 턴 종료 → 5초 후 플레이어 턴
    private fun endEnemyTurn() {
        isPlayerTurn = true

        // 플레이어의 굴림 횟수 초기화
        playerDiceRemain = 1

        Handler(Looper.getMainLooper()).postDelayed({
            // 플레이어가 다시 굴릴 수 있도록 버튼 활성화
            btnRollDice.isEnabled = true
        }, 5000)
    }

}
