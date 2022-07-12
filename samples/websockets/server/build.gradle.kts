import com.soywiz.korge.gradle.*

apply<KorgeGradlePlugin>()

korge {
	id = "com.soywiz.samples.websockets.server"
	targetJvm()

	dependencies {
		val ktorVersion = "1.6.0"
		dependencyMulti("io.ktor:ktor-server-core:$ktorVersion")
		dependencyMulti("io.ktor:ktor-server-netty:$ktorVersion")
		dependencyMulti("io.ktor:ktor-websockets:$ktorVersion")
		dependencyMulti("ch.qos.logback:logback-classic:1.2.3")
	}
}
