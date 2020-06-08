import com.soywiz.klock.*
import com.soywiz.korev.*
import com.soywiz.korge.*
import com.soywiz.korge.input.*
import com.soywiz.korge.view.*

suspend fun main() = Korge {
	var line = 0
	fun textLine(text: String) = text(text).position(0, line++ * 16).apply { this.filtering = false }
	fun nowUnix() = DateTime.now().unixMillisLong

	textLine("Events :")
	val keysEvText = textLine("KeysEv")
	val keysDownText = textLine("Keys:Down")
	val keysUpText = textLine("Keys:Up")
	val mouseEvText = textLine("Mouse1")
	val mouseMoveText = textLine("MouseMove")
	val mouseDownText = textLine("MouseDown")
	val mouseUpText = textLine("MouseUp")
	val mouseClickText = textLine("MouseClick")
	val resizeText = textLine("Resize")
	val gamepadConnectedText = textLine("GamepadConnectedEv")
	val gamepadButtonText = textLine("GamepadButtonEv")
	val gamepadStickText = textLine("GamepadStickEv")
	val gamepadUpdateText = textLine("GamepadUpdateEv")
	val gamepadUpdate2Text = textLine("GamepadUpdate2Ev")

	//stage.addEventListener<KeyEvent> { keysEvText.text = "${nowUnix()}:$it" }
	//stage.addEventListener<MouseEvent> { mouseEvText.text = "${nowUnix()}:$it" }
	//stage.addEventListener<ReshapeEvent> { resizeText.text = "${nowUnix()}:$it" }
	//stage.addEventListener<GamePadConnectionEvent> { gamepadConnectedText.text = "${nowUnix()}:$it" }
	//stage.addEventListener<GamePadUpdateEvent> {
	//	gamepadUpdateText.text = "${nowUnix()}:$it"
	//	gamepadUpdate2Text.text = "" + it.gamepads.lastOrNull { it.connected }?.rawButtonsPressed
	//}

	gamepad {
		button.invoke { gamepadButtonText.text = "$it" }
		stick.invoke { gamepadStickText.text = "$it" }
	}

	keys {
		onKeyDown { keysDownText.text = "Key:Down:${nowUnix()}:${it.key}" }
		onKeyUp { keysUpText.text = "Key:Up:${nowUnix()}:${it.key}" }
	}

	mouse {
		onMove { mouseMoveText.text = "Mouse:Move:${nowUnix()}:$it" }
		onDown { mouseDownText.text = "Mouse:Down:${nowUnix()}:$it" }
		onUp { mouseUpText.text = "Mouse:Up:${nowUnix()}:$it" }
		onClick { mouseClickText.text = "Mouse:Click:${nowUnix()}:$it" }
	}
}
