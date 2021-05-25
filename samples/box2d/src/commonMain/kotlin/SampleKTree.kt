import com.soywiz.korge.Korge
import com.soywiz.korge.box2d.registerBox2dSupportOnce
import com.soywiz.korge.view.ktree.readKTree
import com.soywiz.korgw.GameWindow
import com.soywiz.korio.file.std.resourcesVfs

/**
 * Not used in gradle run! Check `main.kt`.
 *
 * Uses a `ktree` asset which is editable in the [KorGE Visual Editor](https://korlibs.soywiz.com/korge/editor/).
 */
suspend fun main() =
	Korge(width = 920, height = 720, quality = GameWindow.Quality.PERFORMANCE, title = "My Awesome Box2D Game!") {
		registerBox2dSupportOnce()
		addChild(resourcesVfs["restitution.ktree"].readKTree(views))
	}
