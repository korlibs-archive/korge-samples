import com.soywiz.kmem.*
import com.soywiz.korev.*
import com.soywiz.korge.input.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import com.soywiz.korio.async.*
import com.soywiz.korio.lang.*
import com.soywiz.korma.geom.vector.*
import com.soywiz.korma.geom.*
import kotlin.math.*

fun Container.addTouchGamepad(width: Double = 320.0, height: Double = 224.0, radius: Double = height / 8, onStick: (x: Double, y: Double) -> Unit = { x, y -> }, onButton: (button: Int, pressed: Boolean) -> Unit = { button, pressed -> }) {
    val view = this
    lateinit var ball: View
    container {
        position(+radius * 1.1, height - radius * 1.1)
        graphics { fill(Colors.BLACK) { circle(0, 0, radius) } }.alpha(0.2)
        ball = graphics { fill(Colors.WHITE) { circle(0, 0, radius * 0.7) } }.alpha(0.2)
    }

    val button = graphics { position(width - radius * 1.1, height - radius * 1.1).fill(Colors.WHITE) { circle(0, 0, radius * 0.7) } }.alpha(0.2)

    var dragging = false
    var startX = 0.0
    var startY = 0.0
    val directions = LinkedHashSet<Gestures.Direction>()
    val lastDirections = LinkedHashSet<Gestures.Direction>()

    val keyEvent = KeyEvent()

    fun dispatchKey(key: Key, pressed: Boolean) {
        val views = stage?.views ?: return
        launchAsap(views.coroutineContext) {
            try {
                views.dispatch(keyEvent.apply {
                    this.key = key
                    this.type = if (pressed) KeyEvent.Type.DOWN else KeyEvent.Type.UP
                    //println(this)
                })
            } catch (e: Throwable) {
                e.printStackTrace() // @TODO: Check
            }
        }
    }

    button
            .onDown {
                button.alpha = 0.3
				onButton(0, true)
            }
            .onUpAnywhere {
                //button.alpha = 1.0
                button.alpha = 0.2
				onButton(0, false)
            }

    view.addEventListener<MouseEvent> {
        val px = view.globalMatrixInv.transformX(it.x, it.y)
        val py = view.globalMatrixInv.transformY(it.x, it.y)

        directions.clear()

        val stage = view.stage ?: return@addEventListener

        when (it.type) {
            MouseEvent.Type.DOWN -> {

                if (px >= width / 2) return@addEventListener
                startX = px
                startY = py
                ball.alpha = 0.3
                dragging = true
            }
            MouseEvent.Type.DRAG -> {
                if (dragging) {
                    val deltaX = px - startX
                    val deltaY = py - startY
                    val length = hypot(deltaX, deltaY)
                    val maxLength = radius * 0.3
                    val lengthClamped = length.clamp(0.0, maxLength)
                    val angle = Angle.between(startX, startY, px, py)
                    ball.position(cos(angle) * lengthClamped, sin(angle) * lengthClamped)

                    if (lengthClamped >= maxLength * 0.5) {
                        val quadrant = ((angle + 90.degrees + 45.degrees).normalized.degrees) / 90.0

                        //println(quadrant)

                        if (quadrant >= 3.9 || quadrant <= 1.1) directions.add(Gestures.Direction.Up)
                        if (quadrant in 0.9..2.1) directions.add(Gestures.Direction.Right)
                        if (quadrant in 1.9..3.1) directions.add(Gestures.Direction.Down)
                        if (quadrant in 2.9..4.1) directions.add(Gestures.Direction.Left)
                    }

					val lengthNormalized = lengthClamped / maxLength
					onStick(cos(angle) * lengthNormalized, sin(angle) * lengthNormalized)
				}
            }
            MouseEvent.Type.UP -> {
                ball.position(0, 0)
                ball.alpha = 0.2
                dragging = false
				onStick(0.0, 0.0)
            }
        }

        //// Release old keys
        //for (dir in lastDirections) if (dir !in directions) dispatchKeyForDirection(dir, false)
        //// Press new keys
        //for (dir in directions) if (dir !in lastDirections) dispatchKeyForDirection(dir, true)

        lastDirections.clear()
        lastDirections.addAll(directions)
    }

    //view.addEventListener<ReshapeEvent> {
    //    println("reshaped ${view.width}x${view.height}")
    //}
}

