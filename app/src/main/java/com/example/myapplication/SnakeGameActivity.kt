package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivitySnakeGameBinding
import android.util.Log
import android.widget.Toast
import com.example.myapplication.SnakeView.GameListener
import android.view.MotionEvent
import android.view.View
import android.app.AlertDialog

// SnakeGameActivity: 사용자 입력 및 게임 오버 이벤트를 처리하는 Activity
class SnakeGameActivity : AppCompatActivity(), GameListener {

    // --- 변수 선언 ---
    private lateinit var binding: ActivitySnakeGameBinding
    private lateinit var snakeView: SnakeView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySnakeGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        snakeView = binding.snakeGameArea
        snakeView.gameListener = this

        setupDirectionButtons()
        setupActionButton()
    }

    // --- 이벤트 처리 ---

    // 방향키 버튼 클릭 시 뱀의 방향을 설정
    private fun setupDirectionButtons() {
        binding.btnUp.setOnClickListener {
            if (snakeView.currentDirection != Direction.DOWN) snakeView.setDirection(Direction.UP)
        }
        binding.btnDown.setOnClickListener {
            if (snakeView.currentDirection != Direction.UP) snakeView.setDirection(Direction.DOWN)
        }
        binding.btnLeft.setOnClickListener {
            if (snakeView.currentDirection != Direction.RIGHT) snakeView.setDirection(Direction.LEFT)
        }
        binding.btnRight.setOnClickListener {
            if (snakeView.currentDirection != Direction.LEFT) snakeView.setDirection(Direction.RIGHT)
        }
    }

    // 가속 버튼 터치 이벤트 처리
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

                    // 경고 해결: performClick() 호출
                    v.performClick()
                    true
                }
                else -> false
            }
        }

        // 경고 해결: onTouchListener 사용 시 접근성 경고를 막기 위해 빈 OnClickListener 추가
        binding.btnAction.setOnClickListener { /* Empty */ }
    }

    // --- Game Over/재시작 로직 ---

    // SnakeView에서 충돌 발생 시 호출됨
    override fun onGameOver(score: Int) {
        snakeView.stopGame()

        // 게임 오버 AlertDialog
        AlertDialog.Builder(this)
            .setTitle("GAME OVER")
            .setMessage("최종 점수: $score\n\n다시 플레이 하시겠습니까?")
            .setPositiveButton("다시 하기") { dialog, which ->
                restartGame()
            }
            .setNegativeButton("종료") { dialog, which ->
                finish()
            }
            .setCancelable(false)
            .show()

        Log.d("SnakeGame", "Game Over! 최종 점수: $score")
    }

    // SnakeView에서 황금 과자(적) 섭취 시 호출됨
    override fun onEnterBattle(enemyType: EatablesType) {
        snakeView.stopGame()

        // 적 타입 이름을 사용자 친화적인 문자열로 변환
        val enemyName = when (enemyType) {
            EatablesType.ENEMY_TYPE_A -> "보스 몬스터 아이콘"
            EatablesType.ENEMY_TYPE_B -> "중급 몬스터 아이콘"
            EatablesType.ENEMY_TYPE_C -> "일반 몬스터 아이콘"
            EatablesType.NORMAL_SNACK -> "일반 과자"
        }

        // 전투 진입 AlertDialog
        AlertDialog.Builder(this)
            .setTitle("전투 진입")
            .setMessage("$enemyName 을(를) 획득했습니다! 전투 화면으로 진입합니다.")
            .setPositiveButton("전투 끝") { dialog, which ->
                Toast.makeText(this, "뱀 게임 복귀 및 재개", Toast.LENGTH_SHORT).show()
                resumeGame()

            }
            .setCancelable(false)
            .show()
    }

    // 뱀 게임을 멈췄던 상태 그대로 다시 시작
    private fun resumeGame() {
        snakeView.stopGame()
        snakeView.startGame()
    }

    // 게임을 초기화하고 다시 시작
    private fun restartGame() {
        snakeView.resetGame()
    }
}