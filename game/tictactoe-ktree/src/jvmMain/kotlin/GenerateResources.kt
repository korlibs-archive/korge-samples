import com.soywiz.korim.bitmap.*
import com.soywiz.korim.color.*
import com.soywiz.korim.format.*
import com.soywiz.korio.file.std.*
import com.soywiz.korma.geom.vector.*
import kotlinx.coroutines.*

object GenerateResources {
	@JvmStatic
	fun main(args: Array<String>) {
		runBlocking {
			Bitmap32(64, 64).context2d {
				stroke(Colors.RED, lineWidth = 6.0) {
					line(0 + 4, 0 + 4, 64 - 4, 64 - 4)
					line(64 - 4, 0 + 4, 0 + 4, 64 - 4)
				}
			}.writeTo(localCurrentDirVfs["src/commonMain/resources/cross.png"], PNG)

			Bitmap32(64, 64).context2d {
				stroke(Colors.BLUE, lineWidth = 6.0) {
					circle(32, 32, 32 - 4)
				}
			}.writeTo(localCurrentDirVfs["src/commonMain/resources/circle.png"], PNG)
		}
	}
}