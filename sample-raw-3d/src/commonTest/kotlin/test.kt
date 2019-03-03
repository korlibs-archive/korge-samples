import com.soywiz.korge.experimental.s3d.model.*
import com.soywiz.korio.async.*
import com.soywiz.korio.file.std.*
import kotlin.test.*

class Library3DTest {
	@Test
	fun test() = suspendTest {
		val library = resourcesVfs["scene.dae"].readColladaLibrary()
		println(library)
	}
}
