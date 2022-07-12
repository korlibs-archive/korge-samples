import com.soywiz.korge.Korge
import com.soywiz.korge.input.onClick
import com.soywiz.korge.ui.uiButton
import com.soywiz.korge.ui.uiTextInput
import com.soywiz.korge.view.addUpdater
import com.soywiz.korge.view.position
import com.soywiz.korge.view.text
import com.soywiz.korim.color.Colors
import com.soywiz.korio.net.ws.WebSocketClient

class WebSocket {

	var lastMessageFromServer: String = ""
		private set

	private var webSocketClient: WebSocketClient? = null

	fun isConnected() = webSocketClient != null
	fun status() = if (isConnected()) "connected" else "disconnected"

	suspend fun connect() {
		webSocketClient = WebSocketClient("ws://localhost:5000/")
		webSocketClient?.let { addWebSocketClientHandlers(it) }
	}

	suspend fun sendMessage(message: String) = webSocketClient?.send(message)

	fun close() {
		webSocketClient?.close()
		webSocketClient = null
	}

	private fun addWebSocketClientHandlers(newWsClient: WebSocketClient) {
		newWsClient.onStringMessage.add {
			lastMessageFromServer = it
		}
		newWsClient.onClose {
			webSocketClient = null
		}
	}
}

suspend fun main() {

	Korge(width = 512, height = 150, bgcolor = Colors["#2b2b2b"], clipBorders = false, title = "Client") {
		val webSocket = WebSocket()
		text("") {
			position(10, 10)
			addUpdater {
				text = "Websockets client is currently ${webSocket.status()}"
			}
		}
		text("") {
			position(10, 40)
			addUpdater {
				text = "Last message from server: ${webSocket.lastMessageFromServer}"
			}
		}
		val textInput = uiTextInput("Hello from client!", 400.0) {
			position(10, 70)
			addUpdater {
				visible = webSocket.isConnected()
			}
		}
		uiButton(128.0, 32.0) {
			text = "connect"
			position(370, 100)
			onClick {
				webSocket.connect()
			}
			addUpdater {
				visible = !webSocket.isConnected()
			}
		}
		uiButton(128.0, 32.0) {
			text = "disconnect"
			position(370, 100)
			onClick {
				webSocket.close()
			}
			addUpdater {
				visible = webSocket.isConnected()
			}
		}
		uiButton(128.0, 32.0) {
			text = "send"
			position(10, 100)
			onClick {
				webSocket.sendMessage(textInput.text)
			}
			addUpdater {
				visible = webSocket.isConnected()
			}
		}
	}
}
