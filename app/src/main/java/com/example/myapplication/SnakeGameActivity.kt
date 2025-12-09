package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivitySnakeGameBinding
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.app.AlertDialog
import android.content.Intent
import com.example.myapplication.Direction
import com.example.myapplication.EatablesType
import com.example.myapplication.GameListener


// SnakeGameActivity: 사용자 입력 및 게임 오버 이벤트를 처리하는 Activity
class SnakeGameActivity : AppCompatActivity(), GameListener {

    // --- 상수 정의: 다른 Activity와의 통신에 사용되는 키 ---
    companion object {
        // BattleActivity 호출을 위한 요청 코드
        const val BATTLE_REQUEST_CODE = 100

        // 뱀의 현재 길이(HP) 전달 키
        const val EXTRA_PLAYER_HP = "com.example.myapplication.EXTRA_PLAYER_HP"
        // 배틀 후 변경된 HP 복귀 키
        const val EXTRA_NEW_PLAYER_HP = "com.example.myapplication.EXTRA_NEW_PLAYER_HP"

        // 뱀 게임 -> 배틀 (누적 스탯 전달)
        const val EXTRA_EXTRA_ATK = "com.example.myapplication.EXTRA_EXTRA_ATK"
        const val EXTRA_EXTRA_DEF = "com.example.myapplication.EXTRA_EXTRA_DEF"
        const val EXTRA_EXTRA_DICE = "com.example.myapplication.EXTRA_EXTRA_DICE"

        // 배틀 -> 뱀 게임 (최종 누적 스탯 복귀)
        const val EXTRA_FINAL_ATK = "com.example.myapplication.EXTRA_FINAL_ATK_FINAL"
        const val EXTRA_FINAL_DEF = "com.example.myapplication.EXTRA_FINAL_DEF_FINAL"
        const val EXTRA_FINAL_DICE = "com.example.myapplication.EXTRA_FINAL_DICE_FINAL"
    }

    // --- 변수 선언 ---
    private lateinit var binding: ActivitySnakeGameBinding
    private lateinit var snakeView: SnakeView

    // --- 플레이어 스탯 저장 (누적 보상) ---
    private var playerExtraAtk: Int = 0    // 보상으로 얻은 추가 공격력
    private var playerExtraDef: Int = 0    // 보상으로 얻은 방어력
    private var playerExtraDice: Int = 0   // 보상으로 얻은 추가 주사위 개수

    // --- Activity 라이프사이클 함수 ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding 초기화
        binding = ActivitySnakeGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        snakeView = binding.snakeGameArea
        snakeView.gameListener = this // SnakeView의 리스너를 이 Activity로 설정

        setupDirectionButtons() // 방향키 설정
        setupActionButton()     // 가속 버튼 설정
    }

    // --- 이벤트 처리 ---

    // 방향키 버튼 클릭 시 뱀의 방향을 설정
    private fun setupDirectionButtons() {
        // UP 버튼: 현재 DOWN이 아니면 UP으로 방향 설정
        binding.btnUp.setOnClickListener {
            if (snakeView.currentDirection != Direction.DOWN) snakeView.setDirection(Direction.UP)
        }
        // DOWN 버튼: 현재 UP이 아니면 DOWN으로 방향 설정
        binding.btnDown.setOnClickListener {
            if (snakeView.currentDirection != Direction.UP) snakeView.setDirection(Direction.DOWN)
        }
        // LEFT 버튼: 현재 RIGHT가 아니면 LEFT로 방향 설정
        binding.btnLeft.setOnClickListener {
            if (snakeView.currentDirection != Direction.RIGHT) snakeView.setDirection(Direction.LEFT)
        }
        // RIGHT 버튼: 현재 LEFT가 아니면 RIGHT로 방향 설정
        binding.btnRight.setOnClickListener {
            if (snakeView.currentDirection != Direction.LEFT) snakeView.setDirection(Direction.RIGHT)
        }
    }

    // 가속 버튼 터치 이벤트 처리 (ACTION_DOWN 시 가속, ACTION_UP 시 기본 속도)
    @SuppressLint("ClickableViewAccessibility")
    private fun setupActionButton() {
        binding.btnAction.setOnTouchListener { v: View, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // 버튼을 누르는 순간: 가속 모드 시작
                    snakeView.setSpeed(true)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // 버튼에서 손을 떼는 순간: 기본 속도로 복귀
                    snakeView.setSpeed(false)
                    v.performClick() // 접근성 경고 해결
                    true
                }
                else -> false
            }
        }
        // onTouchListener 사용 시 접근성 경고를 막기 위해 빈 OnClickListener 추가
        binding.btnAction.setOnClickListener { /* Empty */ }
    }

    // SnakeView에서 충돌 발생 시 호출됨 (GameListener 구현)
    override fun onGameOver(score: Int) {
        snakeView.stopGame() // 게임 루프 중지

        // 최종 점수를 뱀의 현재 길이로 설정
        val finalLengthScore = snakeView.getSnakeLength()

        // GameOverActivity로 이동
        val intent = Intent(this, GameOverActivity::class.java).apply {
            // 뱀의 길이를 FINAL_SCORE로 전달
            putExtra("FINAL_SCORE", finalLengthScore)
        }
        startActivity(intent)

        finish()

        Log.d("SnakeGame", "Game Over! 최종 점수: $finalLengthScore")
    }

    // SnakeView에서 적(황금 과자) 섭취 시 호출됨 (GameListener 구현)
    override fun onEnterBattle(enemyType: EatablesType) {
        snakeView.stopGame() // 뱀 이동 루프 중지

        // 뱀의 현재 길이(HP)를 가져옴
        val currentSnakeLength = snakeView.getSnakeLength()

        // BattleActivity로 이동을 위한 Intent 생성
        val intent = Intent(this, BattleActivity::class.java).apply {
            // 뱀의 길이를 HP로 간주하여 BattleActivity에 전달
            putExtra(EXTRA_PLAYER_HP, currentSnakeLength)

            // 적 타입 정보를 Intent에 전달 (Enum 이름)
            putExtra("ENEMY_TYPE_KEY", enemyType.name) // name 속성을 사용하여 문자열로 변환

            // 누적된 스탯 값들을 Intent에 추가로 전달
            putExtra(EXTRA_EXTRA_ATK, playerExtraAtk)
            putExtra(EXTRA_EXTRA_DEF, playerExtraDef)
            putExtra(EXTRA_EXTRA_DICE, playerExtraDice)
        }

        // Activity 실행 및 결과 요청 (onActivityResult가 호출됨)
        startActivityForResult(intent, BATTLE_REQUEST_CODE)
    }

    // BattleActivity 종료 후 결과를 받을 때 호출됨
    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // BATTLE_REQUEST_CODE에 대한 응답인지 확인
        if (requestCode == BATTLE_REQUEST_CODE) {

            if (resultCode == RESULT_OK) {
                // 전투 승리 시
                // 새로운 HP(뱀 길이)를 받아서 업데이트
                val finalHp = data?.getIntExtra(EXTRA_NEW_PLAYER_HP, snakeView.getSnakeLength()) ?: snakeView.getSnakeLength()
                snakeView.setSnakeLength(finalHp)

                // BattleActivity가 보낸 최종 스탯 값들을 받아서 저장
                playerExtraAtk = data?.getIntExtra(EXTRA_FINAL_ATK, playerExtraAtk) ?: playerExtraAtk
                playerExtraDef = data?.getIntExtra(EXTRA_FINAL_DEF, playerExtraDef) ?: playerExtraDef
                playerExtraDice = data?.getIntExtra(EXTRA_FINAL_DICE, playerExtraDice) ?: playerExtraDice

                Log.d("Battle", "전투 승리! 뱀 길이 $finalHp 로 업데이트. 누적 스탯: ATK:$playerExtraAtk, DEF:$playerExtraDef, DICE:$playerExtraDice")

                // 승리했을 때만 게임을 재개
                resumeGame()

            } else if (resultCode == RESULT_CANCELED) {
                // 전투 패배 시
                Log.d("Battle", "전투 패배! 게임 오버 처리.")
                // 뱀 길이를 0으로 설정하여 내부적으로 onGameOver를 호출하게 함
                snakeView.setSnakeLength(0)
            }
        }
    }

    // 뱀 게임을 멈췄던 상태 그대로 다시 시작
    private fun resumeGame() {
        snakeView.stopGame()
        snakeView.startGame()
    }

    // 게임을 초기화하고 다시 시작
    private fun restartGame() {
        // 게임 재시작 시 누적 스탯을 0으로 초기화
        playerExtraAtk = 0
        playerExtraDef = 0
        playerExtraDice = 0

        snakeView.resetGame()
    }
}