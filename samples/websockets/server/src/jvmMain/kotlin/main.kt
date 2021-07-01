import com.soywiz.korge.Korge
import com.soywiz.korge.input.onClick
import com.soywiz.korge.ui.uiButton
import com.soywiz.korge.ui.uiTextInput
import com.soywiz.korge.view.addUpdater
import com.soywiz.korge.view.position
import com.soywiz.korge.view.text
import com.soywiz.korim.color.Colors
import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.SendChannel
import kotlin.system.exitProcess

suspend fun main() {

	val port = 5000
	var lastReceivedMessageFromAClient = ""

	// Store the outgoing channels in order to enable broadcasting within the Korge context.
	val outgoingChannels: MutableList<SendChannel<Frame>> = mutableListOf()

	embeddedServer(Netty, port = port) {
		install(WebSockets)
		routing {
			webSocket("/") {
				outgoingChannels.add(outgoing)
				for (frame in incoming) {
					if (frame is Frame.Text) {
						lastReceivedMessageFromAClient = frame.readText()
					}
				}
				outgoingChannels.remove(outgoing)
			}
		}
	}.start(wait = false)

	Korge(width = 512, height = 150, bgcolor = Colors["#2b2b2b"], clipBorders = false, title = "Server") {
		text("Websockets server listening on port $port") {
			position(10, 10)
		}
		text("") {
			position(10, 40)
			addUpdater {
				text = "Number of connected clients: ${outgoingChannels.size}"
			}
		}
		text("") {
			position(10, 70)
			addUpdater {
				text = "Last received message: $lastReceivedMessageFromAClient"
			}
		}
		val textInput = uiTextInput("Greetings from server", 200.0) {
			position(300, 10)
		}
		uiButton(128.0, 32.0) {
			text = "broadcast"
			position(300, 40)
			onClick {
				outgoingChannels.forEach { it.send(Frame.Text(textInput.text)) }
			}
		}
		views.onClose {
			// Close the JVM in order to shut down the embedded Netty server
			exitProcess(0)
		}
	}
}
