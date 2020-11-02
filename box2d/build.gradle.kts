import com.soywiz.korge.gradle.*

apply<KorgeGradlePlugin>()

korge {
	id = "com.soywiz.sample1"
	name = "Sample1"
	description = "A sample using Korge and the gradle plugin"
	orientation = com.soywiz.korge.gradle.Orientation.LANDSCAPE
	jvmMainClassName = "Sample1Kt"

	//admob("ca-app-pub-xxxxxxxx~yyyyyy")

	bundle("https://github.com/korlibs/korge-bundles.git::korge-box2d::4ac7fcee689e1b541849cedd1e017016128624b9##d15f5b28abbc2f58ba375d7204a6db2e90342978d02e675cd29249d0cade8f9f")
}
