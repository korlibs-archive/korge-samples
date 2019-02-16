import com.soywiz.korge.*
import com.soywiz.korge.particle.*
import com.soywiz.korge.view.*
import com.soywiz.korio.file.std.*

suspend fun main() = Korge {
	val emitter = resourcesVfs["particle/particle.pex"].readParticle()
	particleEmitter(emitter).position(width * 0.5, height * 0.5)
}
