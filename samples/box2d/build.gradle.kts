import com.soywiz.korge.gradle.*

apply<KorgeGradlePlugin>()

korge {
	id = "com.soywiz.sample1"
	name = "Sample1"
	description = "A sample using Korge and the gradle plugin"
	orientation = com.soywiz.korge.gradle.Orientation.LANDSCAPE
	jvmMainClassName = "Sample1Kt"

	//admob("ca-app-pub-xxxxxxxx~yyyyyy")

	bundle("https://github.com/korlibs/korge-bundles.git::korge-box2d::73daf015ca725cc2717fa74213bc870e770ee2cd##fb2e67184e3374a53ba3ab43c28bbe78eeca070a76c3df4f565df169bbede60b")

	targetDefault()
}
