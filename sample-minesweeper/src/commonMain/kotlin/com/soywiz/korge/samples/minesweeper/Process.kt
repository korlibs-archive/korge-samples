package com.soywiz.korge.samples.minesweeper

import com.soywiz.kds.*
import com.soywiz.klock.*
import com.soywiz.kmem.*
import com.soywiz.korau.sound.*
import com.soywiz.korev.*
import com.soywiz.korev.KeysEvents
import com.soywiz.korge.component.*
import com.soywiz.korge.input.*
import com.soywiz.korge.time.*
import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.*
import com.soywiz.korim.format.*
import com.soywiz.korio.async.*
import com.soywiz.korio.file.std.*
import kotlinx.coroutines.*
import kotlin.reflect.*

abstract class Process(parent: Container) : Container() {
	init {
		parent.addChild(this)
	}

	override val stage: Stage get() = super.stage!!
	val views: Views get() = stage.views
	val type: KClass<out View> get() = this::class
	var fps: Double = 60.0

	val key get() = stage.views.key

	val Mouse get() = views.mouseV
	val Screen get() = views.screenV
	val audio get() = views.audioV

	var angle: Double
		get() = rotationDegrees
		set(value) = run { rotationDegrees = value }

	suspend fun frame() {
		//delayFrame()
		delay((1.0 / fps).seconds)
	}

	fun action(action: KSuspendFunction0<Unit>) {
		throw ChangeActionException(action)
	}

	abstract suspend fun main()

	private lateinit var job: Job

	fun destroy() {
		removeFromParent()
	}

	open fun onDestroy() {
	}

	class ChangeActionException(val action: KSuspendFunction0<Unit>) : Exception()

	init {
		job = views.launchAsap {
			var action = ::main
			while (true) {
				try {
					action()
					break
				} catch (e: ChangeActionException) {
					action = e.action
				}
			}
		}

		addComponent(object : StageComponent {
			override val view: View = this@Process

			override fun added(views: Views) {
				//println("added: $views")
			}

			override fun removed(views: Views) {
				//println("removed: $views")
				job.cancel()
				onDestroy()
			}
		})
	}

	fun collision(type: KClass<out View>): Boolean {
		return false
	}
}

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
class extraPropertyFixed<T : Any?>(val name: String? = null, val default: () -> T) {
	inline operator fun getValue(thisRef: Extra, property: KProperty<*>): T {
		if (thisRef.extra == null) thisRef.extra = LinkedHashMap()
		return (thisRef.extra!!.getOrPut(name ?: property.name) { default() } as T)
	}

	inline operator fun setValue(thisRef: Extra, property: KProperty<*>, value: T): Unit = run {
		if (thisRef.extra == null) thisRef.extra = LinkedHashMap()
		thisRef.extra!![name ?: property.name] = value as Any?
	}
}

private val Views.componentsInStagePrev by extraPropertyFixed { linkedSetOf<StageComponent>() }
private val Views.componentsInStageCur by extraPropertyFixed { linkedSetOf<StageComponent>() }
private val Views.componentsInStage by extraPropertyFixed { linkedSetOf<StageComponent>() }
private val Views.tempComponents2 by extraPropertyFixed { arrayListOf<Component>() }

fun Views.registerStageComponent() {
	onBeforeRender {
		componentsInStagePrev.clear()
		componentsInStagePrev += componentsInStageCur
		componentsInStageCur.clear()
		stage.forEachComponent<StageComponent>(tempComponents2) {
			componentsInStageCur += it
			//println("DEMO: $it -- $componentsInStage")
			if (it !in componentsInStage) {
				componentsInStage += it
				it.added(views)
			}
		}
		for (it in componentsInStagePrev) {
			if (it !in componentsInStageCur) {
				it.removed(views)
			}
		}
	}
}

/**
 * Component with [added] and [removed] methods that are executed
 * once the view is going to be displayed, and when the view has been removed
 */
interface StageComponent : Component {
	fun added(views: Views)
	fun removed(views: Views)
}

class Key2(val views: Views) {
	operator fun get(key: Key): Boolean = views.keysPressed[key] == true
}


class MouseV(val views: Views) {
	val left: Boolean get() = pressing[0]
	val right: Boolean get() = pressing[1] || pressing[2]
	val x: Int get() = (views.stage.localMouseX(views)).toInt()
	val y: Int get() = (views.stage.localMouseY(views)).toInt()
	val pressing = BooleanArray(8)
	val pressed = BooleanArray(8)
	val released = BooleanArray(8)
	val _pressed = BooleanArray(8)
	val _released = BooleanArray(8)
}

class ScreenV(val views: Views) {
	val width: Double get() = views.virtualWidth.toDouble()
	val height: Double get() = views.virtualHeight.toDouble()
}

class AudioV(val views: Views) {
	fun play(sound: NativeSound, repeat: Int = 0) {
	}
}

val Views.keysPressed by extraPropertyFixed { LinkedHashMap<Key, Boolean>() }
val Views.key by Extra.PropertyThis<Views, Key2> { Key2(this) }

val Views.mouseV by Extra.PropertyThis<Views, MouseV> { MouseV(this) }
val Views.screenV by Extra.PropertyThis<Views, ScreenV> { ScreenV(this) }
val Views.audioV by Extra.PropertyThis<Views, AudioV> { AudioV(this) }

fun Views.registerProcessSystem() {
	registerStageComponent()

	stage.addEventListener<MouseEvent> { e ->
		when (e.type) {
			MouseEvent.Type.MOVE -> Unit
			MouseEvent.Type.DRAG -> Unit
			MouseEvent.Type.UP -> {
				mouseV.pressing[e.button.id] = false
				mouseV._released[e.button.id] = true
			}
			MouseEvent.Type.DOWN -> {
				mouseV.pressing[e.button.id] = true
				mouseV._pressed[e.button.id] = true
			}
			MouseEvent.Type.CLICK -> Unit
			MouseEvent.Type.ENTER -> Unit
			MouseEvent.Type.EXIT -> Unit
			MouseEvent.Type.SCROLL -> Unit
		}
	}
	// @TODO: Use onAfterRender
	onBeforeRender {
		arraycopy(mouseV._released, 0, mouseV.released, 0, 8)
		arraycopy(mouseV._pressed, 0, mouseV.pressed, 0, 8)

		mouseV._released.fill(false)
		mouseV._pressed.fill(false)
	}
	stage.addEventListener<KeyEvent> { e ->
		keysPressed[e.key] = e.type == KeyEvent.Type.DOWN
	}
}

suspend fun readImage(path: String) = resourcesVfs[path].readBitmapSlice()
suspend fun readSound(path: String) = resourcesVfs[path].readNativeSoundOptimized()

// @TODO: Move to KorIM
fun <T : Bitmap> BitmapSlice<T>.split(width: Int, height: Int): List<BmpSlice> {
	val self = this
	val nheight = self.height / height
	val nwidth = self.width / width
	return arrayListOf<BmpSlice>().apply {
		for (y in 0 until nheight) {
			for (x in 0 until nwidth) {
				add(self.sliceWithSize(x * width, y * height, width, height))
			}
		}
	}
}
