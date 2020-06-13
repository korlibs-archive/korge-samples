import com.soywiz.korge.gradle.*

apply<KorgeGradlePlugin>()

korge {
	id = "com.soywiz.samples.svg"
	name = "SvgSample"
	description = "A sample using Korge and the gradle plugin"
	orientation = com.soywiz.korge.gradle.Orientation.LANDSCAPE
	jvmMainClassName = "SvgSampleKt"
}
