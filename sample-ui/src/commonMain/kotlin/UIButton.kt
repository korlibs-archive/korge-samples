import com.soywiz.korge.html.*
import com.soywiz.korge.input.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import com.soywiz.korma.geom.*
import kotlin.properties.*

inline fun Container.uiButton(
	width: Number = 128,
	height: Number = 64,
	label: String = "Button",
	skin: UISkin = DefaultUISkin,
	block: UIButton.() -> Unit = {}
): UIButton = UIButton(width.toDouble(), height.toDouble(), label, skin).also { addChild(it) }.apply(block)

open class UIButton(
	width: Double = 128.0,
	height: Double = 64.0,
	label: String = "Button",
	skin: UISkin = DefaultUISkin
) : UIView() {
	var skin: UISkin by Delegates.observable(skin) { _, _, _ -> updateState() }
	var label by Delegates.observable(label) { _, _, _ -> updateState() }
	private val rect = ninePatch(skin.normal, width, height, 16.0 / 64.0, 16.0 / 64.0, (64.0 - 16.0) / 64.0, (64.0 - 16.0) / 64.0) {}
	private val textShadow = text(label).also { it.position(1, 1) }
	private val text = text(label)
	private var bover by Delegates.observable(false) { _, _, _ -> updateState() }
	private var bpressing by Delegates.observable(false) { _, _, _ -> updateState() }
	init {
		this.width = width
		this.height = height
		mouse {
			onOver {
				bover = true
			}
			onOut {
				bover = false
			}
			onDown {
				bpressing = true
			}
			onUpAnywhere {
				bpressing = false
			}
		}
		updateState()
	}

	private fun updateState() {
		when {
			bpressing -> {
				rect.tex = skin.down
			}
			bover -> {
				rect.tex = skin.hover
			}
			else -> {
				rect.tex = skin.normal
			}
		}
		text.format = Html.Format(align = Html.Alignment.MIDDLE_CENTER)
		text.setTextBounds(Rectangle(0, 0, width, height))
		text.setText(label)
		textShadow.format = Html.Format(align = Html.Alignment.MIDDLE_CENTER, color = Colors.BLACK.withA(64))
		textShadow.setTextBounds(Rectangle(0, 0, width, height))
		textShadow.setText(label)
	}

	override fun updatedSize() {
		super.updatedSize()
		rect.width = width
		rect.height = height
		updateState()
	}
}
