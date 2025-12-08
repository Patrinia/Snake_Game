package com.example.myapplication

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import java.util.Random


data class Coordinate(val x: Int, val y: Int)
enum class Direction { UP, DOWN, LEFT, RIGHT }

// EatablesType: 뱀이 획득할 수 있는 모든 타입 (일반 과자 또는 배틀 적)
enum class EatablesType {
    NORMAL_SNACK, // 일반 과자 (빨간색)
    ENEMY_TYPE_A, // 배틀 적 A
    ENEMY_TYPE_B, // 배틀 적 B
    ENEMY_TYPE_C  // 배틀 적 C
}

// SnakeView: 뱀 게임의 로직 및 화면 그리기를 담당하는 커스텀 뷰
class SnakeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 변수
    var isPlaying = false

    internal var currentDirection = Direction.RIGHT
    private var nextDirection = Direction.RIGHT

    private var eatablesType: EatablesType = EatablesType.NORMAL_SNACK

    // 황금 과자(적) 등장 확률 (30% 확률)
    private val GOLD_FOOD_CHANCE = 30

    // 이벤트를 외부로 전달하기 위한 인터페이스 정의
    interface GameListener {
        fun onGameOver(score: Int)
        fun onEnterBattle(enemyType: EatablesType)
    }
    var gameListener: GameListener? = null

    private var snake: MutableList<Coordinate> = mutableListOf(
        Coordinate(10, 10), Coordinate(9, 10), Coordinate(8, 10)
    )
    private var food: Coordinate? = null
    private var columnCount = 20
    private var rowCount = 20

    // --- 타이머 및 그리기 도구 ---
    private val handler = Handler(Looper.getMainLooper())
    private val normalFrameRate: Long = 300
    private val fastFrameRate: Long = 100
    private var currentFrameRate: Long = normalFrameRate
    private val random = Random()
    private val snakePaint = Paint().apply { color = android.graphics.Color.BLUE }
    private val foodPaint = Paint().apply { color = android.graphics.Color.RED }

    // 뱀 이동 루프를 반복 실행하는 Runnable
    private val gameRunnable: Runnable = object : Runnable {
        override fun run() {
            moveSnake()
            handler.postDelayed(this, currentFrameRate)
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
        if (direction != nextDirection) {
            nextDirection = direction
        }
    }

    // 게임 루프를 멈추고 상태를 중지
    fun stopGame() {
        handler.removeCallbacks(gameRunnable)
        isPlaying = false
    }

    // 게임 상태 초기화 및 루프 재시작
    fun resetGame() {
        stopGame()

        snake = mutableListOf(Coordinate(10, 10), Coordinate(9, 10), Coordinate(8, 10))
        currentDirection = Direction.RIGHT
        food = null
        postInvalidate()
        startGame()
    }

    // 게임 루프를 시작
    fun startGame() {
        if (isPlaying) return
        if (food == null) generateFood()
        isPlaying = true
        handler.postDelayed(gameRunnable, currentFrameRate)
    }

    // 현재 속도를 설정하고 게임 루프의 타이밍을 재설정
    fun setSpeed(isFast: Boolean) {
        // 새 속도 결정
        currentFrameRate = if (isFast) fastFrameRate else normalFrameRate

        // 기존 루프 중지
        handler.removeCallbacks(gameRunnable)

        // 새 속도로 루프 재시작
        if (isPlaying) {
            handler.postDelayed(gameRunnable, currentFrameRate)
        }
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

            // EatablesType에 따라 색깔 설정
            foodPaint.color = when (eatablesType) {
                EatablesType.NORMAL_SNACK -> android.graphics.Color.RED      // 일반 과자 (빨간색)
                EatablesType.ENEMY_TYPE_A -> android.graphics.Color.MAGENTA // 적 A (마젠타)
                EatablesType.ENEMY_TYPE_B -> android.graphics.Color.YELLOW   // 적 B (노란색)
                EatablesType.ENEMY_TYPE_C -> android.graphics.Color.CYAN      // 적 C (청록색)
            }

            // 먹이 그리기
            canvas.drawRect(it.x * cellSize, it.y * cellSize, (it.x + 1) * cellSize, (it.y + 1) * cellSize, foodPaint)
        }
    }

    // --- 핵심 로직 함수 ---

    // 뱀을 한 칸 이동시키고, 먹이 섭취 및 충돌 확인
    fun moveSnake() {
        if (!isPlaying) return

        currentDirection = nextDirection

        val head = snake.first(); var newX = head.x; var newY = head.y
        when (currentDirection) { Direction.UP -> newY--; Direction.DOWN -> newY++; Direction.LEFT -> newX--; Direction.RIGHT -> newX++ }
        val newHead = Coordinate(newX, newY)

        if (checkCollision(newHead)) {
            gameListener?.onGameOver(snake.size - 3); return
        }

        snake.add(0, newHead) // 길이 증가

        if (food != null && newHead == food) {

            // 일반 과자가 아닐 경우 (즉, 적 타입일 경우) 배틀 이벤트 발생
            if (eatablesType != EatablesType.NORMAL_SNACK) {
                gameListener?.onEnterBattle(eatablesType) // 타입 정보를 전달

                food = null
                eatablesType = EatablesType.NORMAL_SNACK // 기본 타입으로 리셋

                return // 전투 진입 후 뱀의 이동 및 길이 조정 로직은 중단
            } else {
                // 일반 과자 섭취
                generateFood()
            }
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
        var isEnemy: Boolean = false

        // 황금 과자(적) 확률 계산
        if (random.nextInt(100) < GOLD_FOOD_CHANCE) {
            isEnemy = true
        }

        do {
            // 0부터 (칸 수 - 1) 사이에서 무작위 x, y 좌표 생성
            val randX = random.nextInt(columnCount)
            val randY = random.nextInt(rowCount)
            newFood = Coordinate(randX, randY)

            // 모서리 4칸 제외 조건
            val isNearEdge = (newFood.x < 4 || newFood.x >= columnCount - 4) ||
                    (newFood.y < 4 || newFood.y >= rowCount - 4)

            // 겹침 방지 + 적일 경우 모서리 제외
        } while (snake.contains(newFood) || (isEnemy && isNearEdge))

        food = newFood

        // 먹이 타입 설정
        if (isEnemy) {
            // 3가지 적 타입 중 랜덤으로 하나 결정
            val enemyTypes = listOf(EatablesType.ENEMY_TYPE_A, EatablesType.ENEMY_TYPE_B, EatablesType.ENEMY_TYPE_C)
            eatablesType = enemyTypes[random.nextInt(enemyTypes.size)]
        } else {
            eatablesType = EatablesType.NORMAL_SNACK
        }
    }
}