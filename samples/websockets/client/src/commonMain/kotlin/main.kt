import com.soywiz.korio.net.ws.WebSocketClient

suspend fun main() {
	println("creating client")
	val wsClient = WebSocketClient(url = "ws://localhost:5000")
	println("sending messages")
	wsClient.send("hello1")
	wsClient.send("hello2")
	wsClient.send("hello3")
	println("messages sent")
}
