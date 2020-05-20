//import com.soywiz.korge.admob.*
import com.soywiz.kds.*
import com.soywiz.korge.*
import com.soywiz.korge.box2d.*
import com.soywiz.korge.view.*
import com.soywiz.korgw.*
import com.soywiz.korim.color.*
import org.jbox2d.common.*
import org.jbox2d.dynamics.*

suspend fun main() = Korge(quality = GameWindow.Quality.PERFORMANCE, title = "My Awesome Box2D Game!") {
	solidRect(20, 20, Colors.RED).position(100, 100).centered.registerBody(dynamic = true)
	solidRect(20, 20, Colors.RED).position(107, 75).centered.registerBody(dynamic = true)
	solidRect(400, 100, Colors.WHITE).position(100, 300).centered.registerBody(dynamic = false)
}

///////////////////////////////
// Core
///////////////////////////////

private val BOX2D_WORLD_KEY = "box2dWorld"

fun createWorld(rootView: View, gravityX: Double = 0.0, gravityY: Double = 100.0, velocityIterations: Int = 6, positionIterations: Int = 2): World {
	return World(Vec2(gravityX.toFloat(), gravityY.toFloat())).also { world ->
		rootView.addUpdater {
			world.step(it.seconds.toFloat(), velocityIterations, positionIterations)
			world.forEachBody { node ->
				val px = node.position.x.toDouble()
				val py = node.position.y.toDouble()
				val view = node[WorldView.ViewKey]
				if (view != null) {
					view.x = px
					view.y = py
					view.rotationRadians = node.angle.toDouble()
				}
				//println(node.position)
			}
		}
	}
}

fun View.getOrCreateWorldRef(): WorldRef {
	val root = this.root
	val world = root.getExtraTyped<World>(BOX2D_WORLD_KEY)
	if (world == null) {
		val newWorld = createWorld(root)
		root.setExtra(BOX2D_WORLD_KEY, newWorld)
		return newWorld
	} else {
		return world
	}
}

fun <T : View> T.registerBody(density: Number = 0.5f, friction: Number = 0.2f, dynamic: Boolean = true, world: WorldRef = this.getOrCreateWorldRef()): T = this.apply {
	val body = world.createBody {
		type = if (dynamic) BodyType.DYNAMIC else BodyType.STATIC
		setPosition(x, y)
	}.fixture {
		this.shape = BoxShape(width, height)
		this.density = density.toFloat()
		this.friction = friction.toFloat()
	}
	body[WorldView.ViewKey] = this
	body.view = this
}
