import com.soywiz.klock.milliseconds
import com.soywiz.korge.Korge
import com.soywiz.korge.render.BatchBuilder2D
import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korim.bitmap.effect.BitmapEffect
import com.soywiz.korim.font.DefaultTtfFont
import com.soywiz.korim.font.toBitmapFont
import com.soywiz.korim.format.readBitmap
import com.soywiz.korim.text.TextAlignment.Companion.TOP_LEFT
import com.soywiz.korio.file.std.resourcesVfs

const val numberOfSprites = 10_000

const val spriteHeight = 32
const val spriteWidth = 16
const val height = 1200
const val width = 1600

suspend fun main() = Korge(width = width, height = height, batchMaxQuads = BatchBuilder2D.MAX_BATCH_QUADS) {

	val spriteMap = resourcesVfs["character.png"].readBitmap()
	val animations = animations(spriteMap)

	val sprites = Array(numberOfSprites) {
		sprite(animations[it % animations.size]).xy(randomX(), randomY()).scale(2.0)
	}

	sprites.forEachIndexed { index, sprite ->
		sprite.playAnimationLooped(animations[index % animations.size])
	}

	val font = DefaultTtfFont.toBitmapFont(
		fontSize = 96.0,
		effect = BitmapEffect(dropShadowX = 2, dropShadowY = 2, dropShadowRadius = 1)
	)
	text("Player: $numberOfSprites", font = font, textSize = 96.0, alignment = TOP_LEFT).position(16.0, 16.0)

	addUpdater {
		val scale = if (it == 0.0.milliseconds) 0.0 else (it / 16.666666.milliseconds)

		sprites.forEachIndexed { index, sprite ->
			sprite.walkDirection(index % animations.size, scale)
		}
	}
}

fun randomX() = (spriteWidth..(width - spriteWidth)).random()
fun randomY() = (spriteHeight..(height - spriteHeight)).random()

fun animations(spriteMap: Bitmap) = arrayOf(
	spriteAnimation(spriteMap, 96), // left
	spriteAnimation(spriteMap, 32), // right
	spriteAnimation(spriteMap, 64), // up
	spriteAnimation(spriteMap, 0) // down
)

fun spriteAnimation(
	spriteMap: Bitmap,
	marginTop: Int = 0,
) = SpriteAnimation(spriteMap, spriteWidth, spriteHeight, marginTop, 1, 4, 1)

fun Sprite.walkDirection(indexOfAnimation: Int, scale: Double = 1.0) {
	val delta = 2 * scale
	when (indexOfAnimation) {
		0 -> x -= delta
		1 -> x += delta
		2 -> y -= delta
		3 -> y += delta
	}
}
