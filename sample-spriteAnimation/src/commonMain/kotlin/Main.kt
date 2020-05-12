import com.soywiz.klock.*
import com.soywiz.korev.*
import com.soywiz.korge.*
import com.soywiz.korge.input.*
import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.*
import com.soywiz.korim.format.*
import com.soywiz.korio.file.std.*

suspend fun main() = Korge(width = 512, height = 512) {
	val spriteMap = resourcesVfs["gfx/character/character.png"].readBitmap()
	val spriteView = Image(Bitmaps.transparent).apply {
		position(250,250)
		scale(3)
	}

	val spriteAnimationLeft = SpriteAnimation(
		id = "left",
		spriteMap = spriteMap,
		spriteWidth = 16,
		spriteHeight = 32,
		marginTop = 96,
		marginLeft = 1,
		columns = 4,
		rows = 1
	)

	val spriteAnimationRight = SpriteAnimation(
		id = "right",
		spriteMap = spriteMap,
		spriteWidth = 16,
		spriteHeight = 32,
		marginTop = 32,
		marginLeft = 1,
		columns = 4,
		rows = 1
	)

	val spriteAnimationUp = SpriteAnimation(
		id = "up",
		spriteMap = spriteMap,
		spriteWidth = 16,
		spriteHeight = 32,
		marginTop = 64,
		marginLeft = 1,
		columns = 4,
		rows = 1
	)

	val spriteAnimationDown = SpriteAnimation(
		id = "down",
		spriteMap = spriteMap,
		spriteWidth = 16,
		spriteHeight = 32,
		marginTop = 0,
		marginLeft = 1,
		columns = 4,
		rows = 1
	)

	val charachterSprite = Sprite(spriteAnimationLeft, spriteAnimationRight, spriteAnimationUp, spriteAnimationDown).apply {
		scale(3)
		xy(100,100)
	}

	addChild(charachterSprite)


	keys {
		onKeyDown{
			when (it.key){
				Key.LEFT -> {charachterSprite.playAnimation(id = "left"); charachterSprite.x-=2 }
				Key.RIGHT ->{ charachterSprite.playAnimation(id ="right"); charachterSprite.x+=2}
				Key.DOWN -> {charachterSprite.playAnimation(id ="down"); charachterSprite.y+=2}
				Key.UP -> {charachterSprite.playAnimation(id ="up"); charachterSprite.y-=2}
				Key.L -> {charachterSprite.playAnimationLooped(id ="up"); charachterSprite.y-=2}
				Key.T -> {charachterSprite.playAnimation(times = 10, id ="up"); charachterSprite.y-=2}
				Key.D -> {charachterSprite.playAnimationForDuration(1.seconds, id ="up"); charachterSprite.y-=2}
				Key.S -> {charachterSprite.stopAnimation()}


				else -> {}
			}
		}
	}
}
