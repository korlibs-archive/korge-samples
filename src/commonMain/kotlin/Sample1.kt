import com.soywiz.korge.*
import com.soywiz.korge.box2d.*
import com.soywiz.korge.scene.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import com.soywiz.korma.geom.vector.*
import com.soywiz.korui.light.*
import org.jbox2d.collision.shapes.*
import org.jbox2d.dynamics.*

fun main() = Korge(quality = LightQuality.PERFORMANCE) {
	//sceneContainer(views)
	//solidRect(300, 200, Colors.DARKCYAN)
	graphics {
		fill(Colors.DARKCYAN) {
			rect(-100, -100, 300, 200)
		}
		fill(Colors.AQUAMARINE) {
			circle(0, 0, 100)
		}
		fill(Colors.AQUAMARINE) {
			circle(100, 0, 100)
		}
		position(100, 100)
	}
	worldView {
		position(400, 400).scale(20)

		createBody {
			setPosition(0, -10)
		}.fixture {
			shape = BoxShape(100, 20)
			density = 0f
		}.setViewWithContainer(solidRect(100, 20, Colors.RED).position(-50, -10))

		// Dynamic Body
		createBody {
			type = BodyType.DYNAMIC
			setPosition(0, 7)
		}.fixture {
			shape = BoxShape(2f, 2f)
			density = 0.5f
			friction = 0.2f
		}.setView(solidRect(2f, 2f, Colors.GREEN).anchor(.5, .5))

		createBody {
			type = BodyType.DYNAMIC
			setPosition(0.75, 13)
		}.fixture {
			shape = BoxShape(2f, 2f)
			density = 1f
			friction = 0.2f
		}.setView(graphics {
			fill(Colors.BLUE) {
				rect(-1f, -1f, 2f, 2f)
			}
		})

		createBody {
			type = BodyType.DYNAMIC
			setPosition(0.5, 15)
		}.fixture {
			shape = CircleShape().apply { m_radius = 2f }
			density = 22f
			friction = 3f
		}.setView(graphics {
			fill(Colors.BLUE) {
				circle(0, 0, 200)
			}
			fill(Colors.DARKCYAN) {
				circle(100, 100, 20)
			}
			scale(1f / 100f)
		})
	}
}
