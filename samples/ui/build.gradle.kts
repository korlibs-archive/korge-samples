import com.soywiz.korge.gradle.KorgeGradlePlugin
import com.soywiz.korge.gradle.Orientation.LANDSCAPE
import com.soywiz.korge.gradle.korge

apply<KorgeGradlePlugin>()

korge {
	id = "com.soywiz.sampleui"
	name = "SampleUi"
	description = "A sample using Korge and the gradle plugin"
	orientation = LANDSCAPE

	targetDefault()
}
