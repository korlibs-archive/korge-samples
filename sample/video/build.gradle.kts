import com.soywiz.korge.gradle.*

apply<KorgeGradlePlugin>()

korge {
	id = "com.soywiz.samplevideo"
	name = "SampleVideo"
	orientation = com.soywiz.korge.gradle.Orientation.LANDSCAPE
	jvmMainClassName = "MainKt"

	targetDefault()
}

repositories {
	maven { url = uri("http://dl.bintray.com/korlibs/korlibs/") }
}

dependencies {
	add("commonMainApi", "com.soywiz.korlibs.korvi:korvi:2.0.5")
}
