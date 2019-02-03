import com.soywiz.korge.gradle.*

apply(plugin = "korge")

dependencies {
	add("commonMainApi", "com.soywiz:korge-swf:$korgeVersion")
}

korge {
	id = "com.soywiz.samples.tictactoe"
	//icon = buildDir["src/commonMain/resources/icon.png"]
}
