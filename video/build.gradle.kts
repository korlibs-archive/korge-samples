import com.soywiz.korge.gradle.*

apply<KorgeGradlePlugin>()

korge {
	id = "com.soywiz.samplevideo"
	name = "SampleVideo"
	orientation = com.soywiz.korge.gradle.Orientation.LANDSCAPE
	jvmMainClassName = "MainKt"
}

dependencies {
	add("commonMainApi", "com.soywiz.korlibs.korvi:korvi:2.0.0-rc2")
}
