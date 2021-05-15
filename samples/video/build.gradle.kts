import com.soywiz.korge.gradle.*

apply<KorgeGradlePlugin>()

korge {
	id = "com.soywiz.samplevideo"
	name = "SampleVideo"
	orientation = com.soywiz.korge.gradle.Orientation.LANDSCAPE
	jvmMainClassName = "MainKt"

	targetDefault()
}

dependencies {
	add("commonMainApi", "com.soywiz.korlibs.korvi:korvi:2.0.7")
	//add("commonMainApi", "com.soywiz.korlibs.korvi:korvi:2.1.1")
}
