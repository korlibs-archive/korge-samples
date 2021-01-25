import com.soywiz.korge.*
import com.soywiz.korge.component.length.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import com.soywiz.korma.geom.*
import com.soywiz.korui.layout.*
import kotlin.math.*

suspend fun main() {
    Korge(width = 512, height = 512, bgcolor = Colors["#2b2b2b"], clipBorders = false, scaleAnchor = Anchor.TOP_LEFT) {
	//Korge(width = 512, height = 512, bgcolor = Colors["#2b2b2b"], clipBorders = false, scaleAnchor = Anchor.TOP_LEFT, scaleMode = ScaleMode.NO_SCALE) {
	//Korge(width = 512, height = 512, bgcolor = Colors["#2b2b2b"]) {
		solidRect(512, 512, Colors.DARKOLIVEGREEN).lengths {
			//width = 100.vw
			//height = 100.vh
		}
		solidRect(512, 512, Colors.DARKRED).lengths {
			x = 1.cm
			y = 1.cm
			width = max(100.vw - (1.cm * 2), 0.5.cm)
			height = max(100.vh - (1.cm * 2), 0.5.cm)
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

fun LengthExtensions.max(a: Length, b: Length): Length = object : Length.Fixed() {
	override fun calc(ctx: LengthContext): Int = max(a.calc(ctx), b.calc(ctx))
}

fun LengthExtensions.min(a: Length, b: Length): Length = object : Length.Fixed() {
	override fun calc(ctx: LengthContext): Int = min(a.calc(ctx), b.calc(ctx))
}
