package org.korge.sample.kuyo

/*
import com.soywiz.kds.*
import com.soywiz.klock.*
import com.soywiz.korev.*
import com.soywiz.korge.input.*
import com.soywiz.korge.input.keys
import com.soywiz.korge.input.mouse
import com.soywiz.korge.scene.*
import com.soywiz.korge.time.*
import com.soywiz.korge.tween.*
import com.soywiz.korge.view.*
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
        board = KuyoBoardView(0, KuyoBoard(), tiles, font1, rand = rand1).position(64, 64).addTo(containerRoot)
        //KuyoBoardView(1, KuyoBoard(), tiles, font1, rand = rand2).also { it.position(640, 64); root += it }
    }
}

class KuyoTiles(val tex: BitmapSlice<Bitmap>) {
    val tiles = Array2(16, 16) { tex }

    //val tile = tex.sliceSize(0, 0, 32, 32)
    //val tile = arrayListOf<SceneTexture>()
    init {
        for (y in 0 until 16) {
            for (x in 0 until 16) {
                tiles[x, y] = tex.sliceWithSize(x * 32, y * 32, 32, 32)
            }
        }
    }

    fun getTex(
        color: Int,
        up: Boolean = false,
        left: Boolean = false,
        right: Boolean = false,
        down: Boolean = false
    ): BmpSlice {
        if (color <= 0) return tiles[5, 15]
        val combinable = true
        //val combinable = false
        var offset = 0
        if (combinable) {
            offset += if (down) 1 else 0
            offset += if (up) 2 else 0
            offset += if (right) 4 else 0
            offset += if (left) 8 else 0
        }
        return tiles[offset, color - 1]
    }

    fun getDestroyTex(color: Int): BmpSlice {
        return when (color) {
            1 -> tiles[0, 12]
            2 -> tiles[0, 13]
            3 -> tiles[2, 12]
            4 -> tiles[2, 13]
            5 -> tiles[4, 12]
            else -> tiles[5, 15]
        }
    }

    fun getHintTex(color: Int): BmpSlice {
        return tiles[5 + color - 1, 11]
    }

    val default = getTex(1)
    val colors = listOf(tiles[0, 0]) + (0 until 5).map { getTex(it + 1) }
    val empty = getTex(0)
}

val DEFAULT_EASING = Easing.LINEAR

class KuyoDropView(val board: KuyoBoardView, private var model: KuyoDrop, val coroutineScope: CoroutineScope) : Container() {
    val dropModel get() = model
    val boardModel get() = board.model
    val views = (0 until 4).map { ViewKuyo(PointInt(0, 0), 0, board).also { addChild(it) } }
    val queue get() = board.queue

    fun rotateRight() {
        set(model.rotateOrHold(+1, board.model), time = 0.1)
    }

    fun rotateLeft() {
        set(model.rotateOrHold(-1, board.model), time = 0.1)
    }

    fun moveBy(delta: PointInt, easing: Easing = DEFAULT_EASING): Boolean {
        val newModel = model.tryMove(delta, board.model) ?: return false
        set(newModel, time = if (delta.x == 0) 0.05 else 0.1, easing = easing)
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

    fun set(newDrop: KuyoDrop, time: Double = 0.1, easing: Easing = DEFAULT_EASING) {
        queue.discard {
            model = newDrop

            setHints()

            coroutineScope.launchImmediately {
                parallel {
                    for ((index, item) in newDrop.transformedItems.withIndex()) {
                        val view = views[index]
                        view.set(item.color)
                        sequence {
                            view.moveTo(item.pos, time = time, easing = easing)
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
        downTimer = timer(1.0.seconds) {
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
	val rand: Random = Random
) : Container() {
    val mwidth get() = model.width
    val mheight get() = model.height
    val queue = JobQueue()
    val hints = container()
    val kuyos = Array2<ViewKuyo?>(model.width, model.height) {
        //ViewKuyo(IPoint(x, y), 0, this@KuyoBoardView).apply { this@KuyoBoardView.addChild(this) }
        null
    }

    fun generateRandShape() = KuyoShape2(rand[1..NCOLORS], rand[1..NCOLORS])

    val dropping = KuyoDropView(this, KuyoDrop(PointInt(2, 0), generateRandShape())).apply {
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
        parallel {
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

        parallel {
            for (transform in step.transforms) {
                //println("TRANSFORM : $transform")
                when (transform) {
                    is KuyoTransform.Place -> {
                        val item = transform.item
                        //println("PLACED: $item")
                        sequence {
                            kuyos[item.pos] =
                                ViewKuyo(item.pos, item.color, this@KuyoBoardView).addTo(this@KuyoBoardView)
                                    .alpha(1.0)
                        }
                    }
                    is KuyoTransform.Move -> {
                        val kuyo = kuyos[transform.src]
                        kuyos[transform.src] = null
                        kuyos[transform.dst] = kuyo
                        sequence {
                            kuyo?.moveTo(transform.dst, time = 0.3, easing = Easing.EASE_IN_QUAD)
                        }
                    }
                    is KuyoTransform.Explode -> {
                        for (item in transform.items) {
                            val kuyo = kuyos[item] ?: continue
                            sequence {
                                kuyo.delay(0.1.seconds)
                                kuyo.bitmap = tiles.getDestroyTex(kuyo.color)
                                kuyo.tween(kuyo::scale[1.5], time = 0.3.seconds, easing = Easing.EASE_IN_QUAD)
                                val destroyEasing = Easing.EASE_OUT_QUAD
                                parallel {
                                    sequence {
                                        kuyo.tween(kuyo::scale[0.5], time = 0.1.seconds, easing = destroyEasing)
                                        kuyo.tween(kuyo::scaleY[0.1], time = 0.1.seconds, easing = Easing.EASE_OUT_QUAD)
                                    }
                                    sequence {
                                        kuyo.hide(time = 0.15.seconds, easing = destroyEasing)
                                        //kuyo.delay(time = 0.2)
                                    }
                                }
                                kuyo.removeFromParent()
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

class ViewKuyo(var ipos: PointInt, var color: Int, val board: KuyoBoardView) : Image(board.tiles.empty) {
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

    suspend fun moveTo(npos: PointInt, time: Double = 0.1, easing: Easing = DEFAULT_EASING) {
        ipos = npos
        val screenPos = getRPos(npos)
        moveTo(screenPos.x.toDouble(), screenPos.y.toDouble(), time = time.seconds, easing = easing)
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

class JobQueue(val context: CoroutineContext = EmptyCoroutineContext) {
    private val tasks = arrayListOf<suspend () -> Unit>()
    var running = false; private set
    private var currentJob: Job? = null
    val size: Int get() = tasks.size + (if (running) 1 else 0)

    private suspend fun run() {
        running = true
        try {

            while (true) {
                val task = synchronized(tasks) { if (tasks.isNotEmpty()) tasks.removeAt(0) else null } ?: break
                val job = launch { task() }
                currentJob = job
                job.join()
                currentJob = null
            }
        } catch (e: Throwable) {
            println(e)
        } finally {
            currentJob = null
            running = false
        }
    }

    /**
     * Discards all the queued but non running tasks
     */
    fun discard(): JobQueue {
        synchronized(tasks) { tasks.clear() }
        return this
    }

    /**
     * Discards all the queued tasks and cancels the running one, sending a complete signal.
     * If complete=true, a tween for example will be set directly to the end step
     * If complete=false, a tween for example will stop to the current step
     */
    fun cancel(complete: Boolean = false): JobQueue {
        currentJob?.cancel(CancelException(complete))
        return this
    }

    fun cancelComplete() = cancel(true)

    fun queue(callback: suspend () -> Unit) {
        synchronized(tasks) { tasks += callback }
        if (!running) launch { run() }
    }

    fun discard(callback: suspend () -> Unit) {
        discard()
        queue(callback)
    }

    operator fun invoke(callback: suspend () -> Unit) = queue(callback)
}

open class CancelException(val complete: Boolean = false) : RuntimeException()
*/
