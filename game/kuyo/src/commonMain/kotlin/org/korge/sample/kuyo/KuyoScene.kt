package org.korge.sample.kuyo

import com.soywiz.kds.*
import com.soywiz.klock.*
import com.soywiz.korev.*
import com.soywiz.korge.animate.*
import com.soywiz.korge.input.*
import com.soywiz.korge.input.keys
import com.soywiz.korge.input.mouse
import com.soywiz.korge.scene.*
import com.soywiz.korge.time.*
import com.soywiz.korge.tween.*
import com.soywiz.korge.view.*
import com.soywiz.korge.view.tween.*
import com.soywiz.korim.bitmap.*
import com.soywiz.korim.font.*
import com.soywiz.korim.format.*
import com.soywiz.korio.async.*
import com.soywiz.korio.lang.*
import com.soywiz.korma.geom.*
import com.soywiz.korma.geom.ds.*
import com.soywiz.korma.interpolation.*
import com.soywiz.korma.random.*
import kotlinx.coroutines.*
import org.korge.sample.kuyo.*
import org.korge.sample.kuyo.model.*
import org.korge.sample.kuyo.util.*
import kotlin.coroutines.*
import kotlin.random.*

class KuyoScene(val seed: Long = Random.nextLong()) : Scene() {
    lateinit var board: KuyoBoardView

    override suspend fun Container.sceneInit() {
        println("KuyoScene.init()")
        val rand1 = Random(seed)
        val rand2 = Random(seed)
        val font1 = resourcesRoot["kuyo/font1.fnt"].readBitmapFont()

        val tiles = KuyoTiles(resourcesRoot["kuyo/kuyos.png"].readBitmapSlice())
        val bg = resourcesRoot["kuyo/bg.png"].readBitmapSlice()
        root += Image(bg).apply {
            gamepad {
                connection {
                    println("connection: $it")
                }
            }
        }
        board = KuyoBoardView(0, KuyoBoard(), tiles, font1, rand = rand1, coroutineContext).position(64, 64).addTo(containerRoot)
        //KuyoBoardView(1, KuyoBoard(), tiles, font1, rand = rand2).also { it.position(640, 64); root += it }
    }
}

val DEFAULT_EASING = Easing.LINEAR

class KuyoDropView(val board: KuyoBoardView, private var model: KuyoDrop, val coroutineContext: CoroutineContext) : Container() {
    val dropModel get() = model
    val boardModel get() = board.model
    val views = (0 until 4).map { ViewKuyo(PointInt(0, 0), 0, board).also { addChild(it) } }
    val queue get() = board.queue

    fun rotateRight() {
        set(model.rotateOrHold(+1, board.model), time = 0.1.seconds)
    }

    fun rotateLeft() {
        set(model.rotateOrHold(-1, board.model), time = 0.1.seconds)
    }

    fun moveBy(delta: PointInt, easing: Easing = DEFAULT_EASING): Boolean {
        val newModel = model.tryMove(delta, board.model) ?: return false
        set(newModel, time = if (delta.x == 0) 0.05.seconds else 0.1.seconds, easing = easing)
        return true
    }

    fun setImmediate(newDrop: KuyoDrop) {
        model = newDrop
        for ((index, item) in newDrop.transformedItems.withIndex()) {
            val view = views[index]
            view.set(item.color)
            view.moveToImmediate(item.pos)
        }
        setHints()
    }

    fun setHints() {
        board.setHints(boardModel.place(model).dst.gravity().transforms.map { it.cdst })
    }

    fun set(newDrop: KuyoDrop, time: TimeSpan = 0.1.seconds, easing: Easing = DEFAULT_EASING) {
        queue.discard {
            model = newDrop

            setHints()

			launchImmediately(coroutineContext) {
                animateParallel {
                    for ((index, item) in newDrop.transformedItems.withIndex()) {
                        val view = views[index]
                        view.set(item.color)
                        sequence {
                            view.moveTo(this, item.pos, time = time, easing = easing)
                        }
                    }
                }
            }
        }
    }

    fun place() {
        queue.discard {
            //println("------")
            alpha = 0.0
            val placement = listOf(PointInt(0, 0), PointInt(0, 1), PointInt(0, 2), PointInt(0, 3)).firstNotNullOrNull {
                dropModel.tryMove(it, boardModel)?.let { if (boardModel.canPlace(it)) it else null }
            } ?: error("GAME OVER!")
            board.applyStep(boardModel.place(placement))
            var chains = 1
            do {
                board.applyStep(boardModel.gravity())
                val explosions = boardModel.explode()
                board.applyStep(explosions)
                if (explosions.transforms.isNotEmpty()) {
                    launchImmediately(coroutineContext) { board.chain(chains) }
                    chains++
                }
            } while (explosions.transforms.isNotEmpty())
            alpha = 1.0
            setImmediate(KuyoDrop(PointInt(2, 0), board.generateRandShape()))
            queue.discard()
        }
    }

    fun moveLeft() {
        moveBy(PointInt(-1, 0))
    }

    fun moveRight() {
        moveBy(PointInt(+1, 0))
    }

    fun moveDownOrPlace() {
        downTimer.close()
        if (!moveBy(PointInt(0, +1), easing = Easing.LINEAR)) {
            place()
        }
    }

    lateinit var downTimer: Closeable

    init {
        setImmediate(model)
        keys {
            down(Key.RIGHT) { moveRight() }
            down(Key.LEFT) { moveLeft() }
            down(Key.DOWN) { moveDownOrPlace() }
            down(Key.Z) { rotateLeft() }
            down(Key.X) { rotateRight() }
            down(Key.ENTER) { rotateRight() }
            //down(Key.UP) { moveBy(IPoint(0, -1)) }                // Only debug
            //down(Key.SPACE) { place() }                // Only debug
        }
        gamepad {
            stick(board.playerId, GameStick.LEFT) { x, y ->
                if (queue.size < 1) {
                    when {
                        x < -0.25 -> moveLeft()
                        x > +0.25 -> moveRight()
                    }
                }
                if (y < -0.25) moveDownOrPlace()
            }
            down(board.playerId, GameButton.LEFT) {
                moveLeft()
            }
            down(board.playerId, GameButton.RIGHT) {
                moveRight()
            }
            down(board.playerId, GameButton.BUTTON2) {
                rotateRight()
            }
            down(board.playerId, GameButton.BUTTON1) {
                rotateLeft()
            }
        }
        downTimer = timers.timeout(1.0.seconds) {
            moveDownOrPlace()
        }
    }
}

val NCOLORS = 4

class KuyoBoardView(
	val playerId: Int,
	var model: KuyoBoard,
	val tiles: KuyoTiles,
	val font: BitmapFont,
	val rand: Random = Random,
	val coroutineContext: CoroutineContext
) : Container() {
    val mwidth get() = model.width
    val mheight get() = model.height
    val queue = JobQueue()
    val hints = container()
	//val kuyos = Array2<ViewKuyo?>(model.width, model.height) { index ->
	//	//ViewKuyo(IPoint(x, y), 0, this@KuyoBoardView).apply { this@KuyoBoardView.addChild(this) }
	//	null as ViewKuyo?
	//}

	val kuyos = Array2<ViewKuyo?>(model.width, model.height, arrayOfNulls(model.width * model.height))

	fun generateRandShape() = KuyoShape2(rand[1..NCOLORS], rand[1..NCOLORS])

    val dropping = KuyoDropView(this, KuyoDrop(PointInt(2, 0), generateRandShape()), coroutineContext).apply {
        this@KuyoBoardView.addChild(this)
    }

    fun updateTexs() {
        for (y in 0 until mheight) {
            for (x in 0 until mwidth) {
                val p = PointInt(x, y)
                val kuyo = kuyos[p]
                if (kuyo != null) {
                    val c = model[p]
                    val left = model[PointInt(x - 1, y)] == c
                    val right = model[PointInt(x + 1, y)] == c
                    val up = model[PointInt(x, y - 1)] == c
                    val down = model[PointInt(x, y + 1)] == c
                    kuyo.bitmap = tiles.getTex(c, up, left, right, down)
                }
            }
        }
    }

    suspend fun chain(count: Int) {
        val text = Text("$count chains!", textSize = 32.0, font = font).also {
            this += it
            it.position(16, 96)
        }
        text.show(time = 0.3.seconds, easing = Easing.EASE_IN_OUT_QUAD)
		animateParallel {
            sequence { text.moveBy(0.0, -64.0, time = 0.3.seconds) }
            sequence { text.hide(time = 0.3.seconds) }
        }
        text.removeFromParent()
    }

    fun setHints(points: List<ColoredPoint>) {
        hints.removeChildren()
        for (p in points) {
            hints += Image(tiles.getHintTex(p.color)).also {
                it.x = p.pos.x * 32.0 + 8.0
                it.y = p.pos.y * 32.0 + 8.0
                it.alpha = 0.6
                it.scale = 0.75
            }
        }
    }

    suspend fun applyStep(step: KuyoStep<out KuyoTransform>) {
        model = step.dst
        //println(model.toBoardString())

		animateParallel {
            for (transform in step.transforms) {
                //println("TRANSFORM : $transform")
                when (transform) {
                    is KuyoTransform.Place -> {
                        val item = transform.item
                        //println("PLACED: $item")
                        sequence {
							kuyos[item.pos] =
								ViewKuyo(item.pos, item.color, this@KuyoBoardView).addTo(this@KuyoBoardView).also { alpha = 1.0 }
                        }
                    }
                    is KuyoTransform.Move -> {
                        val kuyo = kuyos[transform.src]
                        kuyos[transform.src] = null
                        kuyos[transform.dst] = kuyo
                        sequence {
							kuyo?.moveTo(this, transform.dst, time = 0.3.seconds, easing = Easing.EASE_IN_QUAD)
                        }
                    }
                    is KuyoTransform.Explode -> {
                        for (item in transform.items) {
                            val kuyo = kuyos[item] ?: continue
                            sequence {
                                wait(0.1.seconds)
								block { kuyo.bitmap = tiles.getDestroyTex(kuyo.color) }
								tween(kuyo::scale[1.5], time = 0.3.seconds, easing = Easing.EASE_IN_QUAD)
                                val destroyEasing = Easing.EASE_OUT_QUAD
                                parallel {
                                    sequence {
                                        tween(kuyo::scale[0.5], time = 0.1.seconds, easing = destroyEasing)
                                        tween(kuyo::scaleY[0.1], time = 0.1.seconds, easing = Easing.EASE_OUT_QUAD)
                                    }
                                    sequence {
                                        hide(time = 0.15.seconds, easing = destroyEasing)
                                        //kuyo.delay(time = 0.2)
                                    }
                                }
								block { kuyo.removeFromParent() }
                            }
                        }
                    }
                    else -> {
                        println("Unhandled transform : $transform")
                    }
                }
            }
        }
        updateTexs()
    }
}

class ViewKuyo(var ipos: PointInt, var color: Int, val board: KuyoBoardView) : BaseImage(board.tiles.empty) {
    val tiles get() = board.tiles

    init {
        anchor(0.5, 0.5)
        set(color)
        moveToImmediate(ipos)
        mouse {
            click {
                println("kuyo $ipos!")
            }
        }
    }

    fun getRPos(pos: PointInt) = (pos * PointInt(32, 32)) + PointInt(16, 16)

    fun moveTo(animator: Animator, npos: PointInt, time: TimeSpan = 0.1.seconds, easing: Easing = DEFAULT_EASING) {
        ipos = npos
        val screenPos = getRPos(npos)
		animator.apply {
			this@ViewKuyo.moveTo(screenPos.x.toDouble(), screenPos.y.toDouble(), time = time, easing = easing)
		}
    }

    fun moveToImmediate(npos: PointInt) {
        ipos = npos
        val screenPos = getRPos(ipos)
        x = screenPos.x.toDouble()
        y = screenPos.y.toDouble()
    }

    fun set(value: Int) {
        this.color = value
        bitmap = when (value) {
            0 -> tiles.empty
            in 0 until 5 -> tiles.colors[value]
            else -> tiles.default
        }
    }
}
