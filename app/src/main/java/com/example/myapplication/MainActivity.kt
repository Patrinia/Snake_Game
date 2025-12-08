package com.example.myapplication

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.content.Intent
import com.example.myapplication.BattleActivity
import com.example.myapplication.SnakeGameActivity
import android.widget.Button

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val startButton = findViewById<Button>(R.id.button) // XML의 '시작' 버튼 ID 확인 바람
        val endButton = findViewById<Button>(R.id.button3)  // XML의 '종료' 버튼 ID 확인 바람

        // '시작' 버튼 리스너: SnakeGameActivity로 이동
        startButton.setOnClickListener {
            val intent = Intent(this, SnakeGameActivity::class.java)
            startActivity(intent)
            // finish()는 선택 사항이지만, 메인 화면을 살려두는 것이 일반적임
        }

        // '종료' 버튼 리스너: 앱을 닫음
        endButton.setOnClickListener {
            finishAffinity()
        }

    }
}