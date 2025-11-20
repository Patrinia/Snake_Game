package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivitySnakeGameBinding
import android.view.View
import android.util.Log
import android.widget.Toast
import com.example.myapplication.Direction
import com.example.myapplication.SnakeView.GameListener

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

        // '다시 하기' 버튼 클릭 시 재시작 함수 호출
        binding.btnRestart.setOnClickListener {
            restartGame()
        }
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

    // --- Game Over/재시작 로직 ---

    // GameListener 구현: SnakeView에서 충돌 발생 시 호출됨
    override fun onGameOver(score: Int) {
        snakeView.stopGame() // 뱀 이동 루프 중지

        Log.d("SnakeGame", "Game Over! 최종 점수: $score")
        Toast.makeText(this, "Game Over! Score: $score", Toast.LENGTH_LONG).show()

        binding.btnRestart.visibility = View.VISIBLE // '다시 하기' 버튼 표시
    }

    // 게임을 초기화하고 다시 시작
    private fun restartGame() {
        binding.btnRestart.visibility = View.GONE // 버튼 숨기기
        snakeView.resetGame() // SnakeView 상태 초기화 및 루프 재시작 요청
    }
}