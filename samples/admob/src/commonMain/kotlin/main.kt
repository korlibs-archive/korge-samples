import com.soywiz.klock.*
import com.soywiz.korge.*
//import com.soywiz.korge.component.length.bindLength
import com.soywiz.korge.resources.*
import com.soywiz.korge.tween.*
import com.soywiz.korge.view.*
import com.soywiz.korgw.*
import com.soywiz.korim.color.*
import com.soywiz.korim.format.*
import com.soywiz.korio.file.std.*
import com.soywiz.korio.resources.*
import com.soywiz.korma.geom.*
import com.soywiz.korma.interpolation.*
import com.soywiz.korge.admob.*

// @TODO: We could autogenerate this via gradle
val ResourcesContainer.korge_png by resourceBitmap("korge.png")

suspend fun main() {
    //GLOBAL_CHECK_GL = true
    Korge(width = 512, height = 512, bgcolor = Colors["#2b2b2b"], clipBorders = false) {
        gameWindow.icon = korge_png.get().bmp.toBMP32().scaled(32, 32)

		val admob = AdmobCreate(this.views, testing=true)

		//var bannerConfig = Admob.Config(id="ca-app-pub-3940256099942544/6300978111")

		//admob.bannerPrepare(bannerConfig)
		//admob.bannerShow()

		//admob.interstitialWaitAndShow(Admob.Config(id="ca-app-pub-3940256099942544/1033173712"))

		val minDegrees = (-16).degrees
		val maxDegrees = (+16).degrees

		val image = image(korge_png) {
			//val image = image(resourcesVfs["korge.png"].readbitmapslice) {
			rotation = maxDegrees
			anchor(.5, .5)
			scale(.8)
			position(256, 256)
		}

        while (true) {
            image.tween(image::rotation[minDegrees], time = 1.seconds, easing = Easing.EASE_IN_OUT)
            image.tween(image::rotation[maxDegrees], time = 1.seconds, easing = Easing.EASE_IN_OUT)
        }
    }
}
