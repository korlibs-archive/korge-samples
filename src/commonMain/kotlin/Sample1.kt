import com.soywiz.korge.*
import com.soywiz.korge.box2d.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
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
	worldView {
		position(200, 200).scale(20)

		createBody {
			setPosition(0, -10)
		}.fixture {
			shape = BoxShape(100, 20)
			density = 0f
		}.setView(graphics {
			fill(Colors.RED) {
				drawRect(-50f, -10f, 100f, 20f)
				//anchor(0.5, 0.5)
			}
		})

		// Dynamic Body
		createBody {
			type = BodyType.DYNAMIC
			setPosition(0, 10)
		}.fixture {
			shape = BoxShape(2f, 2f)
			density = 0.5f
			friction = 0.2f
		}.setView(solidRect(2f, 2f, Colors.GREEN).anchor(.5, .5))

		createBody {
			type = BodyType.DYNAMIC
			setPosition(0.75, 15)
		}.fixture {
			shape = BoxShape(2f, 2f)
			density = 1f
			friction = 0.2f
		}.setView(graphics {
			fill(Colors.BLUE) {
				drawRect(-1f, -1f, 2f, 2f)
			}
		})
	}
}
