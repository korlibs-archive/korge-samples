import com.soywiz.klock.*
import com.soywiz.korge.*
import com.soywiz.korge.view.*
import com.soywiz.korgw.*
import com.soywiz.korim.color.*
import com.soywiz.korio.async.*
import com.soywiz.korma.geom.vector.*

suspend fun main() = Korge(quality = GameWindow.Quality.PERFORMANCE, title = "My Awesome Box2D Game!") {
	setupCircle()
}

fun Stage.setupCircle() {
	val circle = Circle(radius = 32.0)
	addChild(circle)
	launch {
		while (true) {
			circle.x++
			circle.y++
			circle.radius++
			delay(16.milliseconds)
		}
	}
}
