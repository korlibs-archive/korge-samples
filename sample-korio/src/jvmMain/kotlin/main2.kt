import com.soywiz.korio.dynamic.*
import java.net.*

object Main2 {
	@JvmStatic
	fun main(args: Array<String>) {
		val cl = ClassLoader.getSystemClassLoader()
		val data = KDynamic { cl["ucp"]["path"] } as List<URL>
		println(data)
	}
}
