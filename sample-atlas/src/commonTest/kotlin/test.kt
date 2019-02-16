import com.soywiz.korge.tests.*
import com.soywiz.korge.view.*
import com.soywiz.korma.geom.*
import kotlin.test.*

class AtlasTest : ViewsForTesting() {
	@Test
	fun test() = viewsTest {
		atlasMain()
		assertEquals(3, stage.children.size)
		assertEquals(Size(68, 204), (stage.children.first() as Image).texture.bmp.size)
	}
}
