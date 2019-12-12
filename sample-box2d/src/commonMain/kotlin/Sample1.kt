import com.soywiz.korge.*
import com.soywiz.korge.box2d.*
import com.soywiz.korge.view.*
import com.soywiz.korgw.*
import com.soywiz.korim.color.*
import com.soywiz.korim.format.*
import com.soywiz.korio.file.std.*
import org.jbox2d.callbacks.*
import org.jbox2d.collision.*
import org.jbox2d.dynamics.*
import org.jbox2d.dynamics.contacts.*

suspend fun main() = Korge(quality = GameWindow.Quality.PERFORMANCE, title = "My Awesome Box2D Game!") {
	worldView {
		position(400, 400).scale(20)
		createBody {
			setPosition(0, -10)
		}.fixture {
			shape = BoxShape(100, 20)
			density = 0f
		}.setView(solidRect(100, 20, Colors.RED).position(-50, -10))

		val ball = createBody {
			type = BodyType.DYNAMIC
			setPosition(0, 0)
		}.fixture {
			shape = BoxShape(4f, 4f)
			density = 985f
			friction = 0f
			userData = "ball"
		}.setView(container {
			// [...]
		})

		val enemy = createBody {
			type = BodyType.DYNAMIC
			setPosition(5, 0)
		}.fixture {
			shape = BoxShape(10, 20)
		}.setView(container {
			image(resourcesVfs["korge-ui.png"].readBitmap())
				.size(4f, 4f)
				.position(-2f, -2f)
		})
	}.apply {
		world.setContactListener(contactListener)
	}
}

val contactListener = object : ContactListener {
	override fun beginContact(contact: Contact) {
		println("beginContact")
	}

	override fun endContact(contact: Contact) {
		println("endContact")
	}

	override fun postSolve(contact: Contact, impulse: ContactImpulse) {
		println("postSolve")
	}

	override fun preSolve(contact: Contact, oldManifold: Manifold) {
		println("preSolve")
	}
}
