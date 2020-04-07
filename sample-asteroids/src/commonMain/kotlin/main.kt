import com.soywiz.korev.*
import com.soywiz.korge.*
import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.*
import com.soywiz.korim.color.*
import com.soywiz.korim.vector.*
import com.soywiz.korma.geom.*
import com.soywiz.korma.geom.vector.*

val WIDTH = 640
val HEIGHT = 480

suspend fun main() = Korge(width = WIDTH, height = HEIGHT, virtualWidth = WIDTH, virtualHeight = HEIGHT) {
	val assets = Assets()

	solidRect(WIDTH, HEIGHT, Colors["#222"])
	val ship = image(assets.shipBitmap).center().position(320, 240)

	val pressing = BooleanArray(Key.MAX)
	fun pressing(key: Key) = pressing[key.ordinal]
	keys {
		down { pressing[key.ordinal] = true }
		up { pressing[key.ordinal] = false }
	}

	var bulletReload = 0
	addUpdater {
		if (pressing(Key.LEFT)) ship.rotation -= 3.degrees
		if (pressing(Key.RIGHT)) ship.rotation += 3.degrees
		if (pressing(Key.UP)) ship.advance(2.0)
		if (pressing(Key.DOWN)) ship.advance(-1.5)

		if (bulletReload > 0) bulletReload--

		if (bulletReload <= 0 && pressing(Key.SPACE)) {
			bulletReload = 6
			val bullet = image(assets.bulletBitmap)
				.center()
				.position(ship.x, ship.y)
				.rotation(ship.rotation)
				.advance(assets.shipSize * 0.75)

			fun bulletFrame() {
				bullet.advance(+3.0)
				if (bullet.x < 10 || bullet.y < 10 || bullet.x > WIDTH + 10 || bullet.y > HEIGHT + 10) {
					bullet.removeFromParent()
				}
			}

			//launch {
			//	while (true) {
			//		bulletFrame()
			//		bullet.delayFrame()
			//	}
			//}
			bullet.addUpdater { bulletFrame()  }
		}
	}

	//image(shipBitmap)
}

suspend fun waitFrame() {

}

fun View.advance(amount: Double, rot: Angle = (-90).degrees) = this.apply {
	x += (this.rotation + rot).cosine * amount
	y += (this.rotation + rot).sine * amount
}

inline fun View.advance(amount: Number, rot: Angle = (-90).degrees) = advance(amount.toDouble(), rot)

class Assets {
	val shipSize = 24
	val shipBitmap = NativeImage(shipSize, shipSize).context2d {
		lineWidth = shipSize * 0.05
		lineCap = Context2d.LineCap.ROUND
		stroke(Colors.WHITE) {
			moveTo(shipSize * 0.5, 0)
			lineTo(shipSize, shipSize)
			lineTo(shipSize * 0.5, shipSize * 0.8)
			lineTo(0, shipSize)
			close()
		}
	}
	val bulletBitmap = NativeImage(3, (shipSize * 0.3).toInt()).context2d {
		lineWidth = 1.0
		lineCap = Context2d.LineCap.ROUND
		stroke(Colors.WHITE) {
			moveTo(width / 2, 0)
			lineToV(height)
		}
	}
}
