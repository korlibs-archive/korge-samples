import com.soywiz.korge.*
import com.soywiz.korge.component.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import org.jbox2d.collision.shapes.*
import org.jbox2d.common.*
import org.jbox2d.dynamics.*

fun main() = Korge {
	//solidRect(300, 200, Colors.DARKCYAN)
	graphics {
		fill(Colors.DARKCYAN) {
			drawRect(-100, -100, 300, 200)
		}
		fill(Colors.AQUAMARINE) {
			drawCircle(0, 0, 100)
		}
		fill(Colors.AQUAMARINE) {
			drawCircle(100, 0, 100)
		}
		position(100, 100)
	}
	addChild(WorldView())
}

inline fun bodyDef(callback: BodyDef.() -> Unit): BodyDef = BodyDef().apply(callback)
inline fun World.createBody(callback: BodyDef.() -> Unit): Body = createBody(bodyDef(callback))
inline fun Body.fixture(callback: FixtureDef.() -> Unit): Body = this.also { createFixture(FixtureDef().apply(callback)) }
inline fun Body.setView(view: View): Body = this.also { userData = view }

class WorldView(val world: World = World(Vec2(0f, -10f))) : Container() {
	init {
		addUpdatable {
			world.step(it.toFloat() / 1000f, velocityIterations = 6, positionIterations = 2)
			updateViews()
		}

		val groundBody = world.createBody {
			position.set(0f, -10f)
		}.fixture {
			shape = PolygonShape().apply { setAsBox(50f, 10f) }
			density = 0f
		}.setView(graphics {
			fill(Colors.RED) {
				drawRect(-50f, -10f, 100f, 20f)
				//anchor(0.5, 0.5)
			}
		})

		// Dynamic Body
		val body = world.createBody {
			type = BodyType.DYNAMIC
			position.set(0f, 10f)
		}.fixture {
			shape = PolygonShape().apply { setAsBox(1f, 1f) }
			density = 1f
			friction = 0.2f
		}.setView(graphics {
			fill(Colors.BLUE) {
				drawRect(-1f, -1f, 2f, 2f)
			}
			//anchor(0.5, 0.5)
		})

		world.createBody {
			type = BodyType.DYNAMIC
			position.set(0.75f, 15f)
		}.fixture {
			shape = PolygonShape().apply { setAsBox(1f, 1f) }
			density = 1f
			friction = 0.2f
		}.setView(graphics {
			fill(Colors.BLUE) {
				drawRect(-1f, -1f, 2f, 2f)
			}
		})

		x = 200.0
		y = 200.0
		scale = 20.0
	}

	fun updateViews() {
		var node = world.bodyList
		while (node != null) {
			val userData = node.userData
			if (userData is View) {
				userData.x = node.position.x.toDouble()
				userData.y = -node.position.y.toDouble()
				userData.rotationRadians = -node.angle.toDouble()
			}
			node = node.m_next
		}
	}
}
