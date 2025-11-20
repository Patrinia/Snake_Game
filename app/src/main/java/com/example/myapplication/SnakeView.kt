package com.example.myapplication

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import java.util.Random


data class Coordinate(val x: Int, val y: Int) // 뱀의 몸통 및 먹이 위치 저장 (좌표)
enum class Direction { UP, DOWN, LEFT, RIGHT } // 뱀의 이동 방향

// SnakeView: 뱀 게임의 로직 및 화면 그리기를 담당하는 커스텀 뷰
class SnakeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // --- 게임 상태 및 통신 변수 ---
    var isPlaying = false // 현재 게임 진행 상태
    internal var currentDirection = Direction.RIGHT // 현재 뱀의 이동 방향 (Activity에서 접근)

    // 게임 오버 이벤트를 Activity로 전달하기 위한 인터페이스 정의
    interface GameListener {
        fun onGameOver(score: Int)
    }
    // Activity에서 설정할 리스너 인스턴스
    var gameListener: GameListener? = null

    private var snake: MutableList<Coordinate> = mutableListOf( // 뱀의 초기 위치
        Coordinate(10, 10), Coordinate(9, 10), Coordinate(8, 10)
    )
    private var food: Coordinate? = null
    private var columnCount = 20 // 가로 칸 수 (그리드)
    private var rowCount = 20    // 세로 칸 수 (그리드)

    // --- 타이머 및 그리기 도구 ---
    private val handler = Handler(Looper.getMainLooper())
    private val frameRate: Long = 300 // 뱀 속도 (300ms마다 이동)
    private val random = Random()
    private val snakePaint = Paint().apply { color = android.graphics.Color.BLUE }
    private val foodPaint = Paint().apply { color = android.graphics.Color.RED }

    // 뱀 이동 루프를 반복 실행하는 Runnable
    private val gameRunnable: Runnable = object : Runnable {
        override fun run() {
            moveSnake()
            handler.postDelayed(this, frameRate)
        }
    }

    // --- 뷰 라이프사이클 및 게임 제어 함수 ---

    // 뷰가 화면에 붙을 때 게임 시작
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startGame()
    }

    // 뱀의 방향을 설정 (Activity에서 호출)
    fun setDirection(direction: Direction) {
        currentDirection = direction
    }

    // 게임 루프를 멈추고 상태를 중지
    fun stopGame() {
        handler.removeCallbacks(gameRunnable)
        isPlaying = false
    }

    // 게임 상태 초기화 및 루프 재시작
    fun resetGame() {
        snake = mutableListOf(Coordinate(10, 10), Coordinate(9, 10), Coordinate(8, 10))
        currentDirection = Direction.RIGHT
        food = null
        postInvalidate()
        startGame()
    }

    // 게임 루프를 시작
    private fun startGame() {
        if (food == null) generateFood()
        isPlaying = true
        handler.postDelayed(gameRunnable, frameRate)
    }

    // --- 그리기 함수 ---

    // 화면에 뱀과 먹이를 그립니다 (주기적으로 호출됨)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cellSize = width / columnCount.toFloat()

        // 뱀 그리기
        for (coord in snake) {
            canvas.drawRect(coord.x * cellSize, coord.y * cellSize, (coord.x + 1) * cellSize, (coord.y + 1) * cellSize, snakePaint)
        }
        // 먹이 그리기
        food?.let {
            canvas.drawRect(it.x * cellSize, it.y * cellSize, (it.x + 1) * cellSize, (it.y + 1) * cellSize, foodPaint)
        }
    }

    // --- 핵심 로직 함수 ---

    // 뱀을 한 칸 이동시키고, 먹이 섭취 및 충돌 확인
    fun moveSnake() {
        val head = snake.first(); var newX = head.x; var newY = head.y
        when (currentDirection) { Direction.UP -> newY--; Direction.DOWN -> newY++; Direction.LEFT -> newX--; Direction.RIGHT -> newX++ }
        val newHead = Coordinate(newX, newY)

        if (checkCollision(newHead)) {
            gameListener?.onGameOver(snake.size - 3); return
        }

        snake.add(0, newHead) // 길이 증가

        if (food != null && newHead == food) {
            generateFood()
        } else {
            snake.removeAt(snake.size - 1) // 길이 유지
        }
        postInvalidate()
    }

    // 충돌 확인 (벽 또는 자기 몸통)
    private fun checkCollision(newHead: Coordinate): Boolean {
        val hitWall = newHead.x < 0 || newHead.x >= columnCount || newHead.y < 0 || newHead.y >= rowCount
        val hitSelf = snake.subList(1, snake.size).contains(newHead)
        return hitWall || hitSelf
    }

    // 뱀과 겹치지 않는 무작위 위치에 먹이 생성
    private fun generateFood() {
        var newFood: Coordinate
        do {
            newFood = Coordinate(random.nextInt(columnCount), random.nextInt(rowCount))
        } while (snake.contains(newFood))
        food = newFood
    }
}