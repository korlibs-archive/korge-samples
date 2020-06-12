import com.soywiz.korge.Korge
import com.soywiz.korge.scene.Module
import com.soywiz.korge.scene.Scene
import com.soywiz.korinject.AsyncInjector
import com.soywiz.korma.geom.SizeInt
import kotlin.reflect.KClass

suspend fun main() = Korge(Korge.Config(module = MyModule))

object MyModule : Module() {
	// define the opening scene
	override val mainScene = MenuScene::class

	// define the game configs
	override val title: String = "My Test Game"
	override val icon: String = "icon.png"
	override val size: SizeInt = SizeInt(800, 600)

	// add the scenes to the module
	override suspend fun AsyncInjector.configure() {
		mapPrototype { MenuScene() }
		mapPrototype { PlayScene() }
	}
}
