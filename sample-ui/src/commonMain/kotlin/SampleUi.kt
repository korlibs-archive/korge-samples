import com.soywiz.kmem.*
import com.soywiz.korge.*
import com.soywiz.korge.input.*
import com.soywiz.korge.view.*
import com.soywiz.korgw.*
import com.soywiz.korim.color.*
import com.soywiz.korio.async.*
import com.soywiz.korma.geom.*
import kotlin.math.*
import kotlin.properties.*

suspend fun main() = Korge(quality = GameWindow.Quality.PERFORMANCE, title = "UI") {
	/*
	uiButton(256.0, 32.0) {
		position(64, 64)
		onClick {
			println("CLICKED!")
		}
		draggable()
	}
	*/
	addChild(UIScrollBar(256.0, 32.0, 0.0, 16.0, 64.0).position(64, 64).apply {
		onChange {
			println(it.ratio)
		}
	})
	addChild(UIScrollBar(32.0, 256.0, 0.0, 16.0, 64.0).position(64, 128).apply {
		onChange {
			println(it.ratio)
		}
	})
}

open class UIScrollBar(
	width: Double,
	height: Double,
	current: Double,
	pageSize: Double,
	totalSize: Double,
	buttonSize: Double = 32.0,
	direction: Direction = if (width > height) Direction.Horizontal else Direction.Vertical,
	var stepSize: Double = pageSize / 10.0
) : UIView() {
	val onChange = Signal<UIScrollBar>()
	enum class Direction { Vertical, Horizontal }
	var current by Delegates.observable(current) { _, _, _ -> updatedPos() }
	var pageSize by Delegates.observable(pageSize) { _, _, _ -> updatedPos() }
	var totalSize by Delegates.observable(totalSize) { _, _, _ -> updatedPos() }
	var direction by Delegates.observable(direction) { _, _, _ -> reshape() }
	val isHorizontal get() = direction == Direction.Horizontal
	val isVertical get() = direction == Direction.Vertical
	override var ratio: Double
		set(value) = run { current = value.clamp01() * (totalSize - pageSize) }
		get() = (current / (totalSize - pageSize)).clamp(0.0, 1.0)
	override var width: Double by Delegates.observable(width) { _, _, _ -> reshape() }
	override var height: Double by Delegates.observable(height) { _, _, _ -> reshape() }
	var buttonSize by Delegates.observable(buttonSize) { _, _, _ -> reshape() }
	val buttonWidth get() = if (isHorizontal) buttonSize else width
	val buttonHeight get() = if (isHorizontal) height else buttonSize
	val clientWidth get() = if (isHorizontal) width - buttonWidth * 2 else width
	val clientHeight get() = if (isHorizontal) height else height - buttonHeight * 2

	protected val background = solidRect(100, 100, Colors.DARKGREY)
	protected val lessButton = uiButton(16, 16, "-")
	protected val moreButton = uiButton(16, 16, "+")
	protected val caretButton = uiButton(16, 16, "")

	protected val views get() = stage?.views

	init {
		reshape()

		var slx: Double = 0.0
		var sly: Double = 0.0
		var iratio: Double = 0.0
		var sratio: Double = 0.0
		val tempP = Point()

		lessButton.onDown {
			deltaCurrent(-stepSize)
			reshape()
		}
		moreButton.onDown {
			deltaCurrent(+stepSize)
			reshape()
		}
		background.onClick {
			val pos = if (isHorizontal) caretButton.localMouseX(views!!) else caretButton.localMouseY(views!!)
			deltaCurrent(pageSize * pos.sign)
		}
		caretButton.onMouseDrag {
			val lmouse = background.localMouseXY(views, tempP)
			val lx = lmouse.x
			val ly = lmouse.y
			val cratio = if (isHorizontal) lmouse.x / background.width else lmouse.y / background.height
			if (it.start) {
				slx = lx
				sly = ly
				iratio = ratio
				sratio = cratio
			}
			val dratio = cratio - sratio
			ratio = iratio + dratio
			reshape()
		}
	}

	private fun deltaCurrent(value: Double) {
		current = (current + value).clamp(0.0, totalSize)
	}

	private fun reshape() {
		if (isHorizontal) {
			background.position(buttonWidth, 0).size(clientWidth, clientHeight)
			lessButton.position(0, 0).size(buttonWidth, buttonHeight)
			moreButton.position(width - buttonWidth, 0).size(buttonWidth, buttonHeight)
			val caretWidth = clientWidth * (pageSize / totalSize)
			caretButton.position(buttonWidth + (clientWidth - caretWidth) * ratio, 0).size(caretWidth, buttonHeight)
		} else {
			background.position(0, buttonHeight).size(clientWidth, clientHeight)
			lessButton.position(0, 0).size(buttonWidth, buttonHeight)
			moreButton.position(0, height - buttonHeight).size(buttonWidth, buttonHeight)
			val caretHeight = clientHeight * (pageSize / totalSize)
			caretButton.position(0, buttonHeight + (clientHeight - caretHeight) * ratio).size(buttonWidth, caretHeight)
		}
	}

	private fun updatedPos() {
		reshape()
		onChange(this)
	}
}

// @TODO: Move to Korge
inline fun <T : View> T.size(width: Number, height: Number): T = this.apply {
	this.width = width.toDouble()
	this.height = height.toDouble()
}

// @TODO: Move to Korge
inline fun <T : View> T.localMouseXY(views: Views, target: Point = Point()): Point = target.setTo(localMouseX(views), localMouseY(views))
