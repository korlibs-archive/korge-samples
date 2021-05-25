import com.soywiz.korge.Korge
import com.soywiz.korge.input.onClick
import com.soywiz.korge.service.process.NativeProcess
import com.soywiz.korge.ui.*
import com.soywiz.korge.view.position
import com.soywiz.korgw.GameWindow
import com.soywiz.korim.bitmap.asNinePatchSimpleRatio
import com.soywiz.korim.bitmap.sliceWithSize
import com.soywiz.korim.color.ColorTransform
import com.soywiz.korim.color.transform
import com.soywiz.korim.font.readBitmapFont
import com.soywiz.korim.format.readNativeImage
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korio.util.AsyncOnce

suspend fun main2() = Korge(quality = GameWindow.Quality.PERFORMANCE, title = "UI", width = 800, height = 400) {
	val nativeProcess = NativeProcess(views)

	uiSkin = OtherUISkin().also {
		it.textFont = resourcesVfs["uifont.fnt"].readBitmapFont()
	}

	uiButton(256.0, 32.0) {
		text = "Disabled Button"
		position(128, 128)
		onClick {
			println("CLICKED!")
		}
	}
}

private val otherColorTransform = ColorTransform(0.7, 0.9, 1.0)
private val OTHER_UI_SKIN_IMG by lazy {
	DEFAULT_UI_SKIN_IMG.withColorTransform(otherColorTransform)
}

private val OtherUISkinOnce = AsyncOnce<UISkin>()

suspend fun OtherUISkin(): UISkin = OtherUISkinOnce {
	//val ui = resourcesVfs["korge-ui.png"].readNativeImage().toBMP32().withColorTransform(otherColorTransform)
	val ui = resourcesVfs["korge-ui.png"].readNativeImage()

	UISkin {
		buttonNormal = ui.sliceWithSize(0, 0, 64, 64).asNinePatchSimpleRatio(0.25, 0.25, 0.75, 0.75)
		buttonOver = ui.sliceWithSize(64, 0, 64, 64).asNinePatchSimpleRatio(0.25, 0.25, 0.75, 0.75)
		buttonDown = ui.sliceWithSize(127, 0, 64, 64).asNinePatchSimpleRatio(0.25, 0.25, 0.75, 0.75)
		buttonBackColor = buttonBackColor.transform(otherColorTransform)
	}
}
