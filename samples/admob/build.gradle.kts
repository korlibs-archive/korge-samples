import com.soywiz.korge.gradle.*

apply<KorgeGradlePlugin>()

repositories {
	mavenLocal()
	mavenCentral()
	google()
	maven("https://jitpack.io")
}

korge {
	id = "com.soywiz.samples.admob"
	admob("ca-app-pub-3940256099942544~3347511713")
	targetDefault()
}

tasks.named<Copy>("jvmProcessResources") {
	duplicatesStrategy = DuplicatesStrategy.WARN
}
