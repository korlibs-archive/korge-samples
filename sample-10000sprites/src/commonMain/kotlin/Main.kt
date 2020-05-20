import com.soywiz.klock.*
import com.soywiz.korev.*
import com.soywiz.korge.*
import com.soywiz.korge.input.*
import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.*
import com.soywiz.korim.format.*
import com.soywiz.korio.file.std.*

suspend fun main() = Korge(width = 1600, height = 1200) {

	val numberOfGreen = 5000
	val numberOfRed = numberOfGreen

	val redSpriteMap = resourcesVfs["character.png"].readBitmap()
	val greenSpriteMap = resourcesVfs["character2.png"].readBitmap()

	val greenAnimations = animations(greenSpriteMap)
	val redAnimations = animations(redSpriteMap)

	val greenSprites = Array<Sprite>(numberOfGreen) {
		Sprite(greenAnimations[it % greenAnimations.size]).xy((10..1590).random(), (10..1190).random()).scale(2.0)
	}

	val redSprites = Array<Sprite>(numberOfRed) {
		Sprite(redAnimations[it % redAnimations.size]).xy((10..1590).random(), (10..1190).random()).scale(2.0)
	}

	redSprites.forEach {
		addChild(it)
	}

	greenSprites.forEach {
		addChild(it)
	}

	addUpdater {

		greenSprites.forEachIndexed { index, sprite ->
			sprite.playAnimationLooped(greenAnimations[index % greenAnimations.size]).apply { walkdirection(sprite, index % greenAnimations.size) }
		}

		redSprites.forEachIndexed { index, sprite ->
			sprite.playAnimationLooped(redAnimations[index % redAnimations.size]).apply { walkdirection(sprite, index % redAnimations.size) }
		}

	}
}

fun animations(spriteMap: Bitmap)  = arrayOf(
	SpriteAnimation(spriteMap,16,32,96,1,4,1), // left
	SpriteAnimation(spriteMap,16,32,32,1,4,1), // right
	SpriteAnimation(spriteMap,16,32,64,1,4,1), // up
	SpriteAnimation(spriteMap,16,32,0,1,4,1)) // down

fun walkdirection(sprite : Sprite, indexOfAnimation : Int) {
	sprite.apply {
		val delta = 2
		when (indexOfAnimation) {
			0 -> x-=delta
			1 -> x+=delta
			2 -> y-=delta
			3 -> y+=delta
		}
	}
}
