import com.soywiz.klock.*
import com.soywiz.korge.*
import com.soywiz.korge.tween.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import com.soywiz.korio.async.*
import com.soywiz.korma.interpolation.*
import kotlinx.coroutines.*
import kotlin.coroutines.*
import kotlin.math.*
import kotlin.reflect.*

suspend fun main() = Korge(width = 512, height = 512, virtualWidth = 512, virtualHeight = 512) {
	val rect1 = solidRect(100, 100, Colors.RED)
	val rect2 = solidRect(100, 100, Colors.BLUE)

	animate {
		sequence(defaultTime = 1.seconds, defaultSpeed = 256.0) {
			wait(1.seconds)
			parallel {
				//rect1.moveTo(0, 150)
				rect1.moveToWithSpeed(512 - 100, 0)
				rect2.moveToWithSpeed(0, 512 - 100 - 100)
				//rect1.moveTo(0, height - 100)
			}
			parallel {
				//rect1.moveTo(0, 150)
				rect1.moveTo(512 - 100, 512 - 100)
				rect2.moveTo(512 - 100, 512 - 100)
				//rect1.moveTo(0, height - 100)
			}
			parallel(defaultTime = 1.seconds) {
				rect1.hide()
				rect2.hide()
			}
		}
	}
}

interface BaseAnimatorNode {
	suspend fun execute()
}

enum class AnimatorNodeKind {
	Parallel, Sequence
}

open class AnimatorNode(
	val root: View,
	val defaultTime: TimeSpan = 0.5.seconds,
	val defaultSpeed: Double = 10.0,
	val defaultEasing: Easing = Easing.EASE_IN_OUT_QUAD,
	val kind: AnimatorNodeKind = AnimatorNodeKind.Sequence
) : BaseAnimatorNode {
	@PublishedApi
	internal val nodes = arrayListOf<BaseAnimatorNode>()

	override suspend fun execute() {
		when (kind) {
			AnimatorNodeKind.Sequence -> {
				for (node in nodes) node.execute()
			}
			AnimatorNodeKind.Parallel -> {
				nodes.map { launchImmediately(coroutineContext) { it.execute() } }.joinAll()
			}
		}
	}

	inline fun parallel(
		defaultTime: TimeSpan = this.defaultTime,
		defaultSpeed: Double = this.defaultSpeed,
		defaultEasing: Easing = this.defaultEasing,
		callback: AnimatorNode.() -> Unit
	) {
		nodes.add(AnimatorNode(root, defaultTime, defaultSpeed, defaultEasing, AnimatorNodeKind.Parallel).apply(callback))
	}

	inline fun sequence(
		defaultTime: TimeSpan = this.defaultTime,
		defaultSpeed: Double = this.defaultSpeed,
		defaultEasing: Easing = this.defaultEasing,
		callback: AnimatorNode.() -> Unit
	) {
		nodes.add(AnimatorNode(root, defaultTime, defaultSpeed, defaultEasing, AnimatorNodeKind.Sequence).apply(callback))
	}

	class TweenNode(val view: View, val time: TimeSpan, val easing: Easing, vararg val vfs: () -> V2<*>) : BaseAnimatorNode {
		override suspend fun execute() {
			view.tween(*vfs.map { it() }.toTypedArray(), time = time, easing = easing)
		}
	}

	inline fun View.moveTo(x: Number, y: Number, time: TimeSpan = defaultTime, easing: Easing = defaultEasing) {
		nodes.add(TweenNode(this, time, easing, { this::x[x] }, { this::y[y] }))
	}

	inline fun View.moveToWithSpeed(x: Number, y: Number, speed: Number = defaultSpeed, easing: Easing = defaultEasing) {
		val distance = hypot(this.x - x.toDouble(), this.y - y.toDouble())
		return moveTo(x, y, (distance / speed.toDouble()).seconds, easing)
	}
	inline fun View.alpha(alpha: Number, time: TimeSpan = defaultTime, easing: Easing = defaultEasing) {
		nodes.add(TweenNode(this, time, easing, { this::alpha[alpha] }))
	}
	inline fun View.show(time: TimeSpan = defaultTime, easing: Easing = defaultEasing) = alpha(1, time, easing)
	inline fun View.hide(time: TimeSpan = defaultTime, easing: Easing = defaultEasing) = alpha(0, time, easing)
	/*
	inline fun <T : View> T.tween(vararg props: Pair<KMutableProperty0<Double>, Double>, time: TimeSpan = defaultTime, easing: Easing = defaultEasing) {
		TODO()
	}
	 */

	inline fun wait(time: TimeSpan) {
		nodes.add(TweenNode(root, time, Easing.LINEAR))
	}
}

class Animator(view: View) : AnimatorNode(view) {
}

suspend fun View.animate(block: Animator.() -> Unit = {}): Animator = Animator(this).apply(block).also { it.execute() }
