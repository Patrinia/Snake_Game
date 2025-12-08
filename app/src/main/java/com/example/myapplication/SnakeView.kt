package com.example.myapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.myapplication.R
import android.util.AttributeSet
import android.view.View
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import java.util.Random

// --- 데이터 및 열거형 클래스 ---

// 뱀의 각 칸 좌표를 저장하는 데이터 클래스
data class Coordinate(val x: Int, val y: Int)
// 뱀의 이동 방향 정의
enum class Direction { UP, DOWN, LEFT, RIGHT }

// 뱀이 획득할 수 있는 모든 타입 (일반 과자 또는 배틀 적)
enum class EatablesType {
    NORMAL_SNACK, // 일반 과자 (빨간색, 획득 시 길이 증가)
    ENEMY_TYPE_A, // 배틀 적 A (획득 시 배틀 진입)
    ENEMY_TYPE_B, // 배틀 적 B
    ENEMY_TYPE_C  // 배틀 적 C
}

// 이벤트를 외부(Activity)로 전달하기 위한 인터페이스 정의
interface GameListener {
    fun onGameOver(score: Int) // 게임 오버 발생 시 호출
    fun onEnterBattle(enemyType: EatablesType) // 적(황금 과자) 획득 시 배틀 진입을 요청하며 적 타입을 전달
}


// SnakeView: 뱀 게임의 로직 및 화면 그리기를 담당하는 커스텀 뷰
class SnakeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // --- 변수 및 Bitmap ---
    private lateinit var enemyBitmapA: Bitmap // 적 이미지 A
    private lateinit var enemyBitmapB: Bitmap // 적 이미지 B
    private lateinit var enemyBitmapC: Bitmap // 적 이미지 C

    var isPlaying = false // 게임 진행 상태

    internal var currentDirection = Direction.RIGHT // 현재 뱀의 이동 방향
    private var nextDirection = Direction.RIGHT // 다음 뱀의 이동 방향 (버튼 입력에 따라 저장됨)

    private var eatablesType: EatablesType = EatablesType.NORMAL_SNACK // 현재 맵에 생성된 먹이의 타입

    private val GOLD_FOOD_CHANCE = 30 // 황금 과자(적) 등장 확률 (30%)

    var gameListener: GameListener? = null // 외부로 이벤트를 전달할 리스너

    // 뱀의 몸통 좌표 리스트 (첫 번째 요소가 머리)
    private var snake: MutableList<Coordinate> = mutableListOf(
        Coordinate(10, 10), Coordinate(9, 10), Coordinate(8, 10)
    )
    private var food: Coordinate? = null // 먹이의 좌표
    private var columnCount = 20 // 맵의 가로 칸 수
    private var rowCount = 20 // 맵의 세로 칸 수

    // --- 타이머 및 그리기 도구 ---
    private val handler = Handler(Looper.getMainLooper()) // UI 업데이트를 위한 핸들러
    private val normalFrameRate: Long = 300 // 기본 프레임 속도 (느림, 300ms)
    private val fastFrameRate: Long = 100 // 가속 프레임 속도 (빠름, 100ms)
    private var currentFrameRate: Long = normalFrameRate // 현재 적용 중인 프레임 속도
    private val random = Random() // 랜덤 값 생성을 위한 객체
    private val snakePaint = Paint().apply { color = android.graphics.Color.BLUE } // 뱀을 그릴 때 사용할 페인트 (파란색)
    private val foodPaint = Paint().apply { color = android.graphics.Color.RED } // 먹이를 그릴 때 사용할 페인트 (빨간색)

    // 뱀 이동 루프를 반복 실행하는 Runnable
    private val gameRunnable: Runnable = object : Runnable {
        override fun run() {
            moveSnake() // 뱀 이동
            // 지정된 속도(currentFrameRate)에 따라 반복 실행 예약
            handler.postDelayed(this, currentFrameRate)
        }
    }

    // --- 뷰 라이프사이클 함수 ---

    // 뷰가 윈도우에 부착될 때 호출됨
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startGame() // 게임 시작
    }

    // 뷰의 크기가 변경되거나 설정될 때 호출됨
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // 뷰 크기가 결정된 후 Bitmap 로드 및 크기 조정을 실행
        loadAndScaleBitmaps()
    }

    // 화면에 뱀과 먹이를 그립니다 (주기적으로 호출됨)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 한 칸의 크기 계산
        val cellSize = width / columnCount.toFloat()

        // 뱀 그리기
        for (coord in snake) {
            canvas.drawRect(
                coord.x * cellSize,
                coord.y * cellSize,
                (coord.x + 1) * cellSize,
                (coord.y + 1) * cellSize,
                snakePaint
            )
        }

        // 먹이 그리기
        food?.let {
            // 커진 이미지를 중앙에 배치하기 위한 오프셋 (셀 크기의 절반)
            val offset = cellSize / 4f

            val left = it.x * cellSize - offset
            val top = it.y * cellSize - offset

            if (eatablesType == EatablesType.NORMAL_SNACK) {
                // 일반 과자일 경우 기존처럼 색깔 칠하기 (1x1 크기)
                foodPaint.color = android.graphics.Color.RED
                canvas.drawRect(it.x * cellSize, it.y * cellSize, (it.x + 1) * cellSize, (it.y + 1) * cellSize, foodPaint)
            } else {
                // 적 타입일 경우 이미지(Bitmap)로 그리기
                val bitmapToDraw = when (eatablesType) {
                    EatablesType.ENEMY_TYPE_A -> enemyBitmapA
                    EatablesType.ENEMY_TYPE_B -> enemyBitmapB
                    EatablesType.ENEMY_TYPE_C -> enemyBitmapC
                    else -> enemyBitmapA // 안전을 위한 기본값
                }

                // 오프셋이 적용된 좌표에 커진 이미지 그리기
                canvas.drawBitmap(bitmapToDraw, left, top, null)
            }
        }
    }

    // --- 핵심 로직 함수 ---

    // 뱀의 방향을 설정 (Activity에서 호출)
    fun setDirection(direction: Direction) {
        if (direction != nextDirection) {
            nextDirection = direction
        }
    }

    // 게임 루프를 시작
    fun startGame() {
        if (isPlaying) return
        if (food == null) generateFood() // 먹이가 없으면 생성
        isPlaying = true
        handler.postDelayed(gameRunnable, currentFrameRate)
    }

    // 게임 루프를 멈추고 상태를 중지
    fun stopGame() {
        handler.removeCallbacks(gameRunnable) // 반복 실행 중단
        isPlaying = false
    }

    // 게임 상태 초기화 및 루프 재시작
    fun resetGame() {
        stopGame()
        // 뱀 초기 위치와 길이 설정
        snake = mutableListOf(Coordinate(10, 10), Coordinate(9, 10), Coordinate(8, 10))
        currentDirection = Direction.RIGHT
        nextDirection = Direction.RIGHT
        food = null
        postInvalidate() // 화면 갱신
        startGame()
    }

    // 현재 속도를 설정하고 게임 루프의 타이밍을 재설정 (가속 버튼에 사용)
    fun setSpeed(isFast: Boolean) {
        currentFrameRate = if (isFast) fastFrameRate else normalFrameRate

        handler.removeCallbacks(gameRunnable) // 기존 루프 제거

        if (isPlaying) {
            handler.postDelayed(gameRunnable, currentFrameRate) // 새 속도로 루프 재시작
        }
    }

    // 뱀을 한 칸 이동시키고, 먹이 섭취 및 충돌 확인
    fun moveSnake() {
        if (!isPlaying) return

        currentDirection = nextDirection // 방향 업데이트

        val head = snake.first()
        var newX = head.x
        var newY = head.y

        // 새 머리 위치 계산
        when (currentDirection) {
            Direction.UP -> newY--
            Direction.DOWN -> newY++
            Direction.LEFT -> newX--
            Direction.RIGHT -> newX++
        }
        val newHead = Coordinate(newX, newY)

        // 충돌 확인 (벽 또는 자기 몸통)
        if (checkCollision(newHead)) {
            gameListener?.onGameOver(snake.size - 3) // 게임 오버 리스너 호출
            return
        }

        snake.add(0, newHead) // 새 머리를 리스트 맨 앞에 추가 (길이 증가)

        // 먹이 섭취 확인
        if (food != null && newHead == food) {
            // 일반 과자가 아닐 경우 (적)
            if (eatablesType != EatablesType.NORMAL_SNACK) {
                gameListener?.onEnterBattle(eatablesType) // 배틀 이벤트 발생
                food = null
                eatablesType = EatablesType.NORMAL_SNACK // 다음 먹이는 일반 과자로 초기화
                return // 전투 진입 후 길이 조정 로직은 중단
            } else {
                // 일반 과자 섭취 (길이 유지, 꼬리 제거 안 함)
                generateFood() // 새 먹이 생성
            }
        } else {
            snake.removeAt(snake.size - 1) // 먹지 못했으므로 꼬리 제거 (길이 유지)
        }
        postInvalidate() // 화면 갱신 요청
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

            // 모서리 4칸 제외 조건 (적이 모서리에 나오지 않도록 함)
            val isNearEdge = (newFood.x < 4 || newFood.x >= columnCount - 4) ||
                    (newFood.y < 4 || newFood.y >= rowCount - 4)

            // 겹침 방지 + 적일 경우 모서리 제외
        } while (snake.contains(newFood) || (isEnemy && isNearEdge))

        food = newFood

        // 먹이 타입 설정
        if (isEnemy) {
            // 3가지 적 타입 중 랜덤으로 하나 결정
            val enemyTypes = listOf(
                EatablesType.ENEMY_TYPE_A,
                EatablesType.ENEMY_TYPE_B,
                EatablesType.ENEMY_TYPE_C
            )
            eatablesType = enemyTypes[random.nextInt(enemyTypes.size)]
        } else {
            eatablesType = EatablesType.NORMAL_SNACK // 일반 과자
        }
    }

    // 충돌 확인 (벽 또는 자기 몸통)
    private fun checkCollision(newHead: Coordinate): Boolean {
        // 벽 충돌 확인
        val hitWall =
            newHead.x < 0 || newHead.x >= columnCount || newHead.y < 0 || newHead.y >= rowCount
        // 자기 몸통 충돌 확인 (머리 제외)
        val hitSelf = snake.subList(1, snake.size).contains(newHead)
        return hitWall || hitSelf
    }

    // Bitmap 로드 및 크기 조정
    private fun loadAndScaleBitmaps() {
        // 뱀 칸 크기(cellSizePx)를 계산
        val cellSizePx = width / columnCount

        // 아이콘을 1.5배 크기로 만들기 위해 목표 크기를 계산
        val scaleFactor = 1.5f
        val targetSizePx = (cellSizePx * scaleFactor).toInt()

        // 이미지를 리소스로부터 로드
        enemyBitmapA = BitmapFactory.decodeResource(resources, R.drawable.enemy_a)
        enemyBitmapB = BitmapFactory.decodeResource(resources, R.drawable.enemy_b)
        enemyBitmapC = BitmapFactory.decodeResource(resources, R.drawable.enemy_c)

        // 계산된 목표 크기에 맞게 Bitmap 크기 조정
        enemyBitmapA = Bitmap.createScaledBitmap(enemyBitmapA, targetSizePx, targetSizePx, false)
        enemyBitmapB = Bitmap.createScaledBitmap(enemyBitmapB, targetSizePx, targetSizePx, false)
        enemyBitmapC = Bitmap.createScaledBitmap(enemyBitmapC, targetSizePx, targetSizePx, false)
    }

    // 전투 결과에 따라 뱀의 길이를 설정하고 0 이하면 게임 오버를 호출
    fun setSnakeLength(newLength: Int) {
        if (newLength <= 0) {
            gameListener?.onGameOver(snake.size - 3) // HP가 0 이하면 게임 오버
            return
        }

        // 뱀의 길이를 조절
        while (snake.size < newLength) {
            // 길이가 늘어나면 가장 뒤 꼬리 좌표를 복제하여 추가
            val lastCoord = snake.last()
            snake.add(lastCoord)
        }
        while (snake.size > newLength) {
            // 길이가 줄어들면 꼬리 제거
            snake.removeAt(snake.size - 1)
        }

        postInvalidate() // UI 갱신 요청
    }

    // 현재 뱀의 길이를 반환
    fun getSnakeLength(): Int {
        return snake.size
    }
}