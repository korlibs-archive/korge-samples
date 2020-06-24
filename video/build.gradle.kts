import com.soywiz.korge.gradle.*

apply<KorgeGradlePlugin>()

korge {
	id = "com.soywiz.samplevideo"
	name = "SampleVideo"
	orientation = com.soywiz.korge.gradle.Orientation.LANDSCAPE
	jvmMainClassName = "MainKt"
}

dependencies {
	add("commonMainApi", "com.soywiz.korlibs.korvi:korvi:0.2.3")
	add("commonMainApi", "com.soywiz.korlibs.korgw:korgw:1.12.21") // @TODO: This line shouldn't be required later
}
