import com.soywiz.korge.Korge
import com.soywiz.korge.box2d.registerBodyWithFixture
import com.soywiz.korge.input.onClick
import com.soywiz.korge.view.position
import com.soywiz.korge.view.rotation
import com.soywiz.korge.view.solidRect
import com.soywiz.korgw.GameWindow
import com.soywiz.korim.color.Colors
import com.soywiz.korma.geom.Angle
import com.soywiz.korma.geom.degrees
import org.jbox2d.dynamics.BodyType
import kotlin.random.Random

/**
 * Interactive sample for the integrated [Box-2D](http://www.jbox2d.org) physic lib.
 *
 * Click on any place to spawn a new box!
 */
suspend fun main() = Korge(
	width = 800, height = 800,
	quality = GameWindow.Quality.PERFORMANCE, title = "My Awesome Box2D Game!"
) {

	solidRect(50, 50, Colors.RED).position(400, 50).rotation(30.degrees)
		.registerBodyWithFixture(type = BodyType.DYNAMIC, density = 2, friction = 0.01)
	solidRect(50, 50, Colors.RED).position(300, 100).registerBodyWithFixture(type = BodyType.DYNAMIC)
	solidRect(50, 50, Colors.RED).position(450, 100).rotation(15.degrees)
		.registerBodyWithFixture(type = BodyType.DYNAMIC)
	solidRect(600, 100, Colors.WHITE).position(100, 600).registerBodyWithFixture(
		type = BodyType.STATIC,
		friction = 0.2
	)

	onClick {
		val pos = it.currentPosLocal
		solidRect(50, 50, Colors.RED).position(pos.x, pos.y).rotation(randomAngle())
			.registerBodyWithFixture(type = BodyType.DYNAMIC)
	}

}

fun randomAngle(): Angle = Random.nextInt(0, 90).degrees
