import com.soywiz.kds.*
import com.soywiz.korge.component.*
import com.soywiz.korge.input.*
import com.soywiz.korge.render.*
import com.soywiz.korge.view.*
import kotlin.properties.*

open class UIView : Container() {
	override var width: Double by Delegates.observable(96.0) { prop, old, new -> updatedSize() }
	override var height: Double by Delegates.observable(32.0) { prop, old, new -> updatedSize() }

	protected open fun updatedSize() {
	}

	override fun renderInternal(ctx: RenderContext) {
		registerUISupportOnce()
		super.renderInternal(ctx)
	}

	private var registered = false
	private fun registerUISupportOnce() {
		if (registered) return
		val stage = stage ?: return
		registered = true
		if (stage.getExtra("uiSupport") == true) return
		stage.setExtra("uiSupport", true)
		stage.keys {
			onKeyDown {

			}
		}
		stage?.getOrCreateComponent { stage ->
			object : UpdateComponentWithViews {
				override val view: View = stage
				override fun update(views: Views, ms: Double) {
				}
			}
		}
	}

}
