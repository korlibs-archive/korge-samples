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
	val circle = circle(radius = 32.0)

	circle.addUpdater {
		circle.x++
		circle.y++
		circle.radius++
	}
}
