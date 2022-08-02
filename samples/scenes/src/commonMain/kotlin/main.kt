import com.soywiz.klock.*
import com.soywiz.korge.Korge
import com.soywiz.korge.input.*
import com.soywiz.korge.scene.*
import com.soywiz.korge.view.*
import com.soywiz.korge.view.filter.*
import com.soywiz.korim.color.Colors
import com.soywiz.korinject.AsyncInjector
import kotlin.reflect.KClass

suspend fun main() = Korge(Korge.Config(module = MyModule))

object MyModule : Module() {
	override val mainScene: KClass<out Scene> = MyScene1::class

	override suspend fun AsyncInjector.configure() {
		mapInstance(MyDependency("HELLO WORLD"))
		mapPrototype { MyScene1(get()) }
		mapPrototype { MyScene2(get()) }
	}
}

class MyDependency(val value: String)

const val MARGIN = 10

class MyScene1(private val myDependency: MyDependency) : Scene() {
    suspend override fun SContainer.sceneInit() {
        val mainText = text("MyScene1: ${myDependency.value}", 32.0) {
            smoothing = false
            position(MARGIN, MARGIN)
        }
        text("Click any square to switch to MyScene2") {
            alignTopToBottomOf(mainText, 10)
            positionX(MARGIN)
        }

        solidRect(100, 100, Colors.RED) {
            position(200, 200)
            alpha = 0.7
            onOver { alpha = 1.0 }
            onOut { alpha = 0.7 }
            onClick {
                sceneContainer.changeTo<MyScene2>()
            }
        }
        solidRect(100, 100, Colors.BLUE) {
            position(250, 250)
            alpha = 0.7
            onOver { alpha = 1.0 }
            onOut { alpha = 0.7 }
            onClick {
                sceneContainer.changeTo<MyScene2>(
					//transition = MaskTransition(TransitionFilter.Transition.SWEEP),
					//time = 1.seconds
                )
            }
        }
    }
}

class MyScene2(private val myDependency: MyDependency) : Scene() {
	suspend override fun SContainer.sceneInit() {
        text("MyScene2: ${myDependency.value}", 32.0) {
            smoothing = false
            position(MARGIN, 10)
        }

        val blueSquare = solidRect(100, 100, Colors.BLUE) {
            position(200, 200)
            onClick {
                sceneContainer.changeTo<MyScene1>(
					MyDependency("From MyScene2"),
					//transition = MaskTransition(TransitionFilter.Transition.CIRCULAR),
					//time = 1.seconds
				)
            }
        }

        text("Click the square to switch to MyScene1") {
            alignTopToBottomOf(blueSquare, 10)
            centerXOn(blueSquare)
        }
    }
}
