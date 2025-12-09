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
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random
import android.content.Intent
import com.example.myapplication.SnakeGameActivity.Companion.EXTRA_PLAYER_HP
import com.example.myapplication.SnakeGameActivity.Companion.EXTRA_NEW_PLAYER_HP
import com.example.myapplication.SnakeGameActivity.Companion.EXTRA_EXTRA_ATK
import com.example.myapplication.SnakeGameActivity.Companion.EXTRA_EXTRA_DEF
import com.example.myapplication.SnakeGameActivity.Companion.EXTRA_EXTRA_DICE
import com.example.myapplication.SnakeGameActivity.Companion.EXTRA_FINAL_ATK
import com.example.myapplication.SnakeGameActivity.Companion.EXTRA_FINAL_DEF
import com.example.myapplication.SnakeGameActivity.Companion.EXTRA_FINAL_DICE

class BattleActivity : AppCompatActivity() {

    private lateinit var btnScissor: ImageButton
    private lateinit var btnRock: ImageButton
    private lateinit var btnPaper: ImageButton

    private lateinit var playerChoose: ImageView
    private lateinit var enemyChoose: ImageView
    private lateinit var whoFirst: TextView

    private lateinit var btnRSPZone: LinearLayout
    private lateinit var diceZone: LinearLayout
    private lateinit var btnRollDice: ImageButton

    private lateinit var statusWindow: LinearLayout

    private lateinit var txtStatusHP: TextView
    private lateinit var txtStatusAtk: TextView
    private lateinit var txtStatusDef: TextView

    private lateinit var txtStatusDiceCnt: TextView

    private lateinit var btnStatus: ImageButton

    private lateinit var rewardZone: LinearLayout
    private lateinit var reward1: ImageButton
    private lateinit var reward2: ImageButton
    private lateinit var reward3: ImageButton

    //HP 바 & 텍스트
    private lateinit var playerHpBar: ProgressBar
    private lateinit var enemyHpBar: ProgressBar
    private lateinit var playerHpText: TextView
    private lateinit var enemyHpText: TextView

    private var isStatusOpen = false
    private var readyToBattle = false

    // 전투 변수
    private var playerMaxHP = 20
    private var playerHP = playerMaxHP


    private val enemyMaxHP = 6
    private var enemyHP = enemyMaxHP

    private var playerAtk = 0
    private var enemyAtk = 0
    private var playerDef = 0
    private var enemyDef = 0
    private var playerAdditionalDice = 0
    // 주사위 잔여 횟수

    // HP
    // 뱀 게임에서 받아온 초기 HP (뱀의 길이)
    private var initialSnakeHP = 0
    // 적 초기 HP (승리 시 회복량 계산에 사용)
    private var enemyInitialHP = 0

    // 주사위 잔여 횟수
    private var playerDiceRemain = 1
    private var enemyDiceRemain = 1

    // 턴 정보
    private var isPlayerTurn = true

    private val rewardList = listOf(
        Reward("체력 +2", R.drawable.reward_hp, type = "hp", value = 2),
        Reward("공격력 +1", R.drawable.reward_atk, type = "atk", value = 1),
        Reward("방어력 +1", R.drawable.reward_def, type = "def", value = 1),
        Reward("추가 주사위 +1", R.drawable.reward_dice, type = "dice", value = 1)
    )

    data class Reward(
        val name: String,
        val img: Int,
        val type: String,
        val value: Int
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_battle)

        btnRSPZone = findViewById(R.id.btnRSP_zone)

        btnScissor = findViewById(R.id.btnScissor)
        btnRock = findViewById(R.id.btnRock)
        btnPaper = findViewById(R.id.btnPaper)

        diceZone = findViewById(R.id.dicezone)
        btnRollDice = findViewById(R.id.btnRollDice)

        playerChoose = findViewById(R.id.playerchoose)
        enemyChoose = findViewById(R.id.enemychoose)

        whoFirst = findViewById(R.id.whofirst)

        // 스테이터스창
        statusWindow = findViewById(R.id.statusWindow)
        txtStatusHP = findViewById(R.id.txtStatusPlayerHP)
        txtStatusAtk = findViewById(R.id.txtStatusPlayerAtk)
        txtStatusDef = findViewById(R.id.txtStatusPlayerDef)
        txtStatusDiceCnt = findViewById(R.id.txtStatusPlayerDiceCnt)

        btnStatus = findViewById(R.id.playerStatus)

        //// SnakeGameActivity에서 전달된 HP를 받음
        initialSnakeHP = intent.getIntExtra(SnakeGameActivity.EXTRA_PLAYER_HP, playerMaxHP)
        // SnakeGameActivity에서 전달된 스탯을 받음
        val intentExtraAtk = intent.getIntExtra(EXTRA_EXTRA_ATK, 0)
        val intentExtraDef = intent.getIntExtra(EXTRA_EXTRA_DEF, 0)
        val intentExtraDice = intent.getIntExtra(EXTRA_EXTRA_DICE, 0)
        playerHP = initialSnakeHP // 뱀의 길이를 플레이어의 초기 HP로 설정
        // 기존 초기 스탯(0)에 전달받은 누적 스탯을 더합니다.
        playerAtk += intentExtraAtk
        playerDef += intentExtraDef
        playerDiceRemain += intentExtraDice


        // 적 초기 HP 저장
        enemyInitialHP = enemyMaxHP

        // 초기 상태
        playerChoose.setImageDrawable(null)
        enemyChoose.setImageDrawable(null)
        whoFirst.text = ""

        btnRSPZone.visibility = View.VISIBLE
        diceZone.visibility = View.GONE

        rewardZone = findViewById(R.id.rewardZone)
        reward1 = findViewById(R.id.reward1)
        reward2 = findViewById(R.id.reward2)
        reward3 = findViewById(R.id.reward3)

        // HP Bar & Text
        playerHpBar = findViewById(R.id.playerHpBar)
        enemyHpBar = findViewById(R.id.enemyHpBar)
        playerHpText = findViewById(R.id.playerHPText)
        enemyHpText = findViewById(R.id.enemyHPText)

        // 초기 세팅
        playerHpBar.max = initialSnakeHP // 뱀의 HP를 최대치로
        playerMaxHP = initialSnakeHP     // 최대 HP 변수도 뱀 HP로 갱신
        enemyHpBar.max = enemyInitialHP

        updateHpUI()
        updateStatusUI()

        btnScissor.setOnClickListener { playRSP(0) }
        btnRock.setOnClickListener { playRSP(1) }
        btnPaper.setOnClickListener { playRSP(2) }

        // 플레이어 턴 버튼 리스너 (사용자 조작)
        btnRollDice.setOnClickListener { playerTurn() }

        // 상태창 버튼 리스너
        btnStatus.setOnClickListener { toggleStatusWindow() }
    }

    private fun updateStatusUI() {
        txtStatusHP.text = "현재 HP: $playerHP"
        txtStatusAtk.text = "추가 공격력: +$playerAtk"
        txtStatusDef.text = "방어력: +$playerDef"
        txtStatusDiceCnt.text = "주사위 개수: +$playerAdditionalDice"
    }

    private fun toggleStatusWindow() {
        if (isStatusOpen) {
            statusWindow.visibility = View.GONE
        } else {
            statusWindow.visibility = View.VISIBLE
        }
        isStatusOpen = !isStatusOpen
    }

    // HP Bar + 텍스트 즉시 갱신
    private fun updateHpUI() {
        playerHpBar.progress = playerHP
        enemyHpBar.progress = enemyHP

        playerHpText.text = "${playerHP} / ${playerMaxHP}"
        enemyHpText.text = "${enemyHP} / ${enemyMaxHP}"
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

    private fun playerTurn() {
        if (!isPlayerTurn) return                 // 내 턴 아니면 무시
        if (playerDiceRemain <= 0) return         // 남은 횟수 없으면 무시

        btnRollDice.isEnabled = false

        Handler(Looper.getMainLooper()).postDelayed({
            rollDice()
        }, 500)
    }

    //적의 자동 턴

    private fun enemyTurn() {
        // 남은 굴림 횟수 없으면 턴 종료
        if (enemyDiceRemain <= 0) {
            endEnemyTurn()
            return
        }

        // 적이 굴리는 동안 플레이어는 버튼 금지
        isPlayerTurn = false
        btnRollDice.isEnabled = false

        Handler(Looper.getMainLooper()).postDelayed({
            rollDice()
        }, 500)
    }

    private fun rollDice() {
        val diceNumber = Random.nextInt(1, 7)

        val diceRes = when (diceNumber) {
            1 -> R.drawable.dice1
            2 -> R.drawable.dice2
            3 -> R.drawable.dice3
            4 -> R.drawable.dice4
            5 -> R.drawable.dice5
            else -> R.drawable.dice6
        }

        if (isPlayerTurn) {
            playerChoose.setImageResource(diceRes)

            val damage = (playerAtk + diceNumber - enemyDef).coerceAtLeast(0)
            enemyHP -= damage
            if (enemyHP < 0) enemyHP = 0

            // 로그
            whoFirst.text = "$damage 대미지 공격!"

            // 남은 횟수 차감
            playerDiceRemain--
        } else {
            // 적이 굴림 -> enemyChoose에 주사위 이미지 반영
            enemyChoose.setImageResource(diceRes)

            val damage = (enemyAtk + diceNumber - playerDef).coerceAtLeast(0)
            playerHP -= damage
            if (playerHP < 0) playerHP = 0

            // 로그
            whoFirst.text = "$damage 대미지 피격!"

            // 남은 횟수 차감
            enemyDiceRemain--
        }

        updateHpUI()
        updateStatusUI()
        checkBattleState()
    }

    private fun checkBattleState() {
        // 승패 체크
        if (playerHP <= 0) {
            whoFirst.text = "패배!"
            btnRollDice.isEnabled = false

            // 패배 시 즉시 SnakeGameActivity로 복귀 (길이 0을 전달하여 게임 오버 처리)
            Handler(Looper.getMainLooper()).postDelayed({
                setResult(RESULT_CANCELED) // 패배 코드를 CANCELED로 간주
                finish()
            }, 2000)

            return
        }
        if (enemyHP <= 0) {
            whoFirst.text = "승리!"
            btnRollDice.isEnabled = false

            Handler(Looper.getMainLooper()).postDelayed({
                showRewardSelection()
            }, 3000)

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


    // 플레이어 턴 종료 → 3초 후 적 턴
    private fun endPlayerTurn() {
        isPlayerTurn = false
        btnRollDice.isEnabled = false

        // 턴이 바뀔 때 적의 굴림 횟수 초기화 (보상/스킬로 변경 가능)
        enemyDiceRemain = 1

        Handler(Looper.getMainLooper()).postDelayed({
            enemyTurn()
        }, 3000)
    }

    // 적 턴 종료 → 3초 후 플레이어 턴
    private fun endEnemyTurn() {
        isPlayerTurn = true

        // 플레이어의 굴림 횟수 초기화
        playerDiceRemain = 1 + playerAdditionalDice

        Handler(Looper.getMainLooper()).postDelayed({
            // 플레이어가 다시 굴릴 수 있도록 버튼 활성화
            btnRollDice.isEnabled = true
        }, 3000)
    }

    private fun showRewardSelection() {
        // reward 3개 랜덤 추출
        val picked = rewardList.shuffled().take(3)

        // 이미지 설정
        reward1.setImageResource(picked[0].img)
        reward2.setImageResource(picked[1].img)
        reward3.setImageResource(picked[2].img)

        rewardZone.visibility = View.VISIBLE

        // 클릭 리스너
        reward1.setOnClickListener { applyReward(picked[0]) }
        reward2.setOnClickListener { applyReward(picked[1]) }
        reward3.setOnClickListener { applyReward(picked[2]) }
    }

    private fun applyReward(reward: Reward) {
        when (reward.type) {
            "hp" -> playerHP += reward.value
            "atk" -> playerAtk += reward.value
            "def" -> playerDef += reward.value
            "dice" -> playerAdditionalDice += reward.value
        }

        updateHpUI()
        updateStatusUI()

        // 보상창 숨기기
        rewardZone.visibility = View.GONE

        // 뱀게임으로 복귀
        // 전투에서 승리할 시 캐릭터는 상대방의 초기 hp 만큼 hp를 회복
        // 뱀 게임으로 복귀할 최종 HP 계산
        val finalHpAfterHeal = playerHP + enemyInitialHP

        // 결과를 SnakeGameActivity에 반환
        val resultIntent = Intent().apply {
            // HP 반환
            putExtra(SnakeGameActivity.EXTRA_NEW_PLAYER_HP, finalHpAfterHeal)

            // 최종 스탯 값들을 Intent에 담아 반환
            putExtra(EXTRA_FINAL_ATK, playerAtk)
            putExtra(EXTRA_FINAL_DEF, playerDef)
            putExtra(EXTRA_FINAL_DICE, playerDiceRemain)
        }
        setResult(RESULT_OK, resultIntent)
        finish() // Activity 종료 및 복귀
    }

}
