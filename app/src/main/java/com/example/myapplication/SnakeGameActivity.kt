package com.example.myapplication

import android.annotation.SuppressLint
<<<<<<< Updated upstream
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.SnakeView.GameListener
import com.example.myapplication.databinding.ActivitySnakeGameBinding
=======
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivitySnakeGameBinding
import android.util.Log
import android.widget.Toast
import com.example.myapplication.SnakeView.GameListener
import android.view.MotionEvent
>>>>>>> Stashed changes

// SnakeGameActivity: 사용자 입력 및 게임 오버 이벤트를 처리하는 Activity
class SnakeGameActivity : AppCompatActivity(), GameListener { // GameListener 인터페이스 구현

    // --- 변수 선언 ---
    private lateinit var binding: ActivitySnakeGameBinding // View Binding 객체 (UI 요소 접근용)
    private lateinit var snakeView: SnakeView // XML에서 연결된 뱀 게임 뷰 인스턴스

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySnakeGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        snakeView = binding.snakeGameArea
        snakeView.gameListener = this // Activity를 GameListener로 등록

        setupDirectionButtons() // 방향키 버튼 이벤트 설정
        setupActionButton() // 가속 버튼 이벤트 리스너 연결
<<<<<<< Updated upstream
=======

        /*// '다시 하기' 버튼 클릭 시 재시작 함수 호출
        binding.btnRestart.setOnClickListener {
            restartGame()
        }*/
>>>>>>> Stashed changes
    }

    // --- 이벤트 처리 ---

    // 방향키 버튼 클릭 시 뱀의 방향을 설정
    private fun setupDirectionButtons() {
        // UP 버튼: 현재 방향이 DOWN이 아닐 때만 UP으로 변경 (역주행 방지)
        binding.btnUp.setOnClickListener {
            if (snakeView.currentDirection != Direction.DOWN) snakeView.setDirection(Direction.UP)
        }
        // DOWN 버튼
        binding.btnDown.setOnClickListener {
            if (snakeView.currentDirection != Direction.UP) snakeView.setDirection(Direction.DOWN)
        }
        // LEFT 버튼
        binding.btnLeft.setOnClickListener {
            if (snakeView.currentDirection != Direction.RIGHT) snakeView.setDirection(Direction.LEFT)
        }
        // RIGHT 버튼
        binding.btnRight.setOnClickListener {
            if (snakeView.currentDirection != Direction.LEFT) snakeView.setDirection(Direction.RIGHT)
        }
    }

    // 가속 버튼 터치 이벤트 처리
    @SuppressLint("ClickableViewAccessibility")
    private fun setupActionButton() {
<<<<<<< Updated upstream
        // 🚨 타입 추론 오류 해결: 람다 파라미터 v와 event에 타입을 명시
        binding.btnAction.setOnTouchListener { v: View, event: MotionEvent ->
=======
        binding.btnAction.setOnTouchListener { v, event ->
>>>>>>> Stashed changes
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // 버튼을 누르는 순간: 가속 모드 시작
                    snakeView.setSpeed(true)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // 버튼에서 손을 떼는 순간: 기본 속도로 복귀
                    snakeView.setSpeed(false)
<<<<<<< Updated upstream

                    // 경고 해결: performClick() 호출을 통해 클릭 이벤트를 명시적으로 발생
=======
>>>>>>> Stashed changes
                    v.performClick()
                    true
                }
                else -> false
            }
        }
<<<<<<< Updated upstream

        // 경고 해결: setOnTouchListener 사용 시 접근성 경고를 막기 위해 빈 OnClickListener 추가
        binding.btnAction.setOnClickListener { /* Empty */ }
=======
>>>>>>> Stashed changes
    }

    // --- Game Over/재시작 로직 ---

    // SnakeView에서 충돌 발생 시 호출됨
    override fun onGameOver(score: Int) {
        snakeView.stopGame() // 뱀 이동 루프 중지

        // 게임 오버 점수 및 재시작 버튼이 포함된 AlertDialog 띄우기
        AlertDialog.Builder(this)
            .setTitle("GAME OVER")
            .setMessage("최종 점수: $score\n\n다시 플레이 하시겠습니까?")
            .setPositiveButton("다시 하기") { dialog, which ->
                restartGame() // '다시 하기' 버튼 클릭 시 게임 재시작 함수 호출
            }
            .setNegativeButton("종료") { dialog, which ->
                finish() // Activity 종료 (타이틀 화면으로 돌아감)
            }
            .setCancelable(false) // 다이얼로그 외부 터치 방지
            .show()

        Log.d("SnakeGame", "Game Over! 최종 점수: $score")
    }

    // SnakeView에서 황금 과자(적) 섭취 시 호출됨
    override fun onEnterBattle(enemyType: EatablesType) {
        snakeView.stopGame() // 뱀 이동 루프 중지

        // 적 타입 이름을 사용자 친화적인 문자열로 변환
        val enemyName = when (enemyType) {
            EatablesType.ENEMY_TYPE_A -> "보스 몬스터 아이콘"
            EatablesType.ENEMY_TYPE_B -> "중급 몬스터 아이콘"
            EatablesType.ENEMY_TYPE_C -> "일반 몬스터 아이콘"
            EatablesType.NORMAL_SNACK -> "일반 과자" // 여기에 도달하지 않아야 함
        }

        // 임시 메시지 창 (AlertDialog) 띄우기
        AlertDialog.Builder(this)
            .setTitle("전투 진입")
            .setMessage("$enemyName 을(를) 획득했습니다! 전투 화면으로 진입합니다.")
            .setPositiveButton("전투 끝") { dialog, which ->
                // TODO: BattleActivity에서 복귀 시점에 이 로직을 실행
                Toast.makeText(this, "뱀 게임 복귀 및 재개", Toast.LENGTH_SHORT).show()
                resumeGame() // 멈췄던 게임 재개

            }
            .setCancelable(false) // 전투 진입은 취소할 수 없음
            .show()
    }

    // 뱀 게임을 멈췄던 상태 그대로 다시 시작
    private fun resumeGame() {
        // 멈췄던 게임을 재개하기 전에, 혹시 모를 잔여 루프를 완전히 중지
        snakeView.stopGame()

        // 뱀의 현재 상태에서 게임을 다시 시작
        snakeView.startGame()
    }

    // 게임을 초기화하고 다시 시작
    private fun restartGame() {
        snakeView.resetGame() // SnakeView 상태 초기화 및 루프 재시작 요청
    }
}