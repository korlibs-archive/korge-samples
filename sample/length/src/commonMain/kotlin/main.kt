import com.soywiz.korge.*
import com.soywiz.korge.component.length.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import com.soywiz.korma.geom.*

suspend fun main() {
    Korge(width = 512, height = 512, bgcolor = Colors["#2b2b2b"], clipBorders = false, scaleAnchor = Anchor.TOP_LEFT) {
	//Korge(width = 512, height = 512, bgcolor = Colors["#2b2b2b"], clipBorders = false, scaleAnchor = Anchor.TOP_LEFT, scaleMode = ScaleMode.NO_SCALE) {
	//Korge(width = 512, height = 512, bgcolor = Colors["#2b2b2b"]) {
		solidRect(512, 512, Colors.DARKOLIVEGREEN).lengths {
			//width = 100.vw
			//height = 100.vh
		}
        solidRect(0, 0, Colors.RED).centered.lengths {
        	x = 50.vw
			y = 50.vh
			//y = 50.vh - (3.cm / 2)
			width = 50.vw
			height = 3.cm
		}
    }
}
