import com.soywiz.korge.gradle.KorgeGradlePlugin
import com.soywiz.korge.gradle.korge

apply<KorgeGradlePlugin>()

korge {
	id = "com.soywiz.sample1"
	name = "Box2D-Sample"
	description = "A sample using Korge and the gradle plugin"
	orientation = com.soywiz.korge.gradle.Orientation.LANDSCAPE

	//admob("ca-app-pub-xxxxxxxx~yyyyyy")

	bundle("https://github.com/korlibs/korge-bundles.git::korge-box2d::7439e5c7de7442f2cd33a1944846d44aea31af0a##9fd9d54abd8abc4736fd3439f0904141d9b6a26e9e2f1e1f8e2ed10c51f490fd")

	targetDefault()
}
