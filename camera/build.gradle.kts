import com.soywiz.korge.gradle.*

apply<KorgeGradlePlugin>()

korge {
	id = "com.soywiz.sample"
	name = "Camera sample"
	description = "A sample using Camera in KorGE"
	orientation = com.soywiz.korge.gradle.Orientation.LANDSCAPE
	jvmMainClassName = "SampleKt"
	targetDefault()
}
