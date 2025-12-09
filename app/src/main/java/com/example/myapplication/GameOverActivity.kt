// GameOverActivity.kt (새로 만들 파일)
package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.Button
import android.widget.TextView

class GameOverActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gameover)

        // UI 요소 바인딩 (XML ID에 맞춰 수정해야 함)
        val restartButton = findViewById<Button>(R.id.button) // '다시 하기' 버튼
        val endButton = findViewById<Button>(R.id.button3)    // '종료' 버튼
        val recordTextView = findViewById<TextView>(R.id.textView2) // '기록' TextView

        // 🐍 SnakeGameActivity에서 전달된 점수를 받아와 TextView에 표시함
        val finalScore = intent.getIntExtra("FINAL_SCORE", 0)
        recordTextView.text = finalScore.toString()

        // --- 버튼 리스너 설정 ---

        // '다시 하기' 버튼: MainActivity로 돌아가 새 게임을 시작함
        restartButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            // MainActivity로 돌아가 스택을 비우고 새 게임을 준비함
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        // '종료' 버튼: 앱을 완전히 닫음
        endButton.setOnClickListener {
            finishAffinity() // 모든 Activity를 종료하고 앱을 닫음
        }
    }
}