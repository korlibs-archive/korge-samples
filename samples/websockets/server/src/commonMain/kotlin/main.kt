import com.soywiz.korge.Korge
import com.soywiz.korge.view.addUpdater
import com.soywiz.korge.view.text
import com.soywiz.korim.color.Colors
import com.soywiz.korio.net.http.defaultHttpFactory

suspend fun main() {

	val port = 5000
	var lastMessage = ""

	val httpServer = defaultHttpFactory.createServer()
	httpServer.websocketHandler { wsRequest -> wsRequest.onStringMessage {
		lastMessage = it
	} }
	httpServer.listen(port = port)

	Korge(width = 512, height = 200, bgcolor = Colors["#2b2b2b"], clipBorders = false, title = "Server") {
		text("Websockets server listening on port $port")
		text("") {
			y = 30.0
			addUpdater {
				text = "Last received message: $lastMessage"
			}
		}
	}
}
