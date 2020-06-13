import com.soywiz.korio.*
import com.soywiz.korio.file.std.*

suspend fun main() = Korio {
	println(resourcesVfs["hello.txt"].readString())
}
