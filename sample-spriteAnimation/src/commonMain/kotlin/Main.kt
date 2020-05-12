import com.soywiz.klock.*
import com.soywiz.korev.*
import com.soywiz.korge.*
import com.soywiz.korge.input.*
import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.*
import com.soywiz.korim.format.*
import com.soywiz.korio.file.std.*

suspend fun main() = Korge(width = 512, height = 512) {
	val spriteMap = resourcesVfs["character.png"].readBitmap()
	val spriteView = Image(Bitmaps.transparent).apply {
		position(250, 250)
		scale(3)
	}

	val spriteAnimationLeft = SpriteAnimation(
		spriteView = spriteView,
		spriteMap = spriteMap,
		spriteWidth = 16,
		spriteHeight = 32,
		marginTop = 96,
		marginLeft = 1,
		columns = 4,
		lines = 1
	)

	val spriteAnimationRight = SpriteAnimation(
		spriteView = spriteView,
		spriteMap = spriteMap,
		spriteWidth = 16,
		spriteHeight = 32,
		marginTop = 32,
		marginLeft = 1,
		columns = 4,
		lines = 1
	)

	val spriteAnimationUp = SpriteAnimation(
		spriteView = spriteView,
		spriteMap = spriteMap,
		spriteWidth = 16,
		spriteHeight = 32,
		marginTop = 64,
		marginLeft = 1,
		columns = 4,
		lines = 1
	)

	val spriteAnimationDown = SpriteAnimation(
		spriteView = spriteView,
		spriteMap = spriteMap,
		spriteWidth = 16,
		spriteHeight = 32,
		marginTop = 0,
		marginLeft = 1,
		columns = 4,
		lines = 1
	)

	addChild(spriteView)


	keys {
		onKeyDown {
			when (it.key) {
				Key.LEFT -> {
					spriteAnimationLeft.nextSprite(); spriteView.x -= 2
				}
				Key.RIGHT -> {
					spriteAnimationRight.nextSprite(); spriteView.x += 2
				}
				Key.DOWN -> {
					spriteAnimationDown.nextSprite(); spriteView.y += 2
				}
				Key.UP -> {
					spriteAnimationUp.nextSprite(); spriteView.y -= 2
				}
				Key.L -> {
					spriteAnimationDown.playLooped()
				}
				Key.T -> {
					spriteAnimationDown.play(5)
				}
				Key.D -> {
					spriteAnimationDown.playForDuration(10.seconds)
				}
				Key.S -> {
					spriteAnimationDown.stop()
				}

				else -> {
				}
			}
		}
	}
}
