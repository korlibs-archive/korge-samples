package com.soywiz.korge.experimental.s3d.model

import com.soywiz.kds.*
import com.soywiz.korge.experimental.s3d.model.internal.*
import com.soywiz.korio.file.*
import com.soywiz.korio.util.*

// https://en.wikipedia.org/wiki/Wavefront_.obj_file

suspend fun VfsFile.readOBJScene(): WavefrontScene = OBJ.read(this.readString())

class OBJ {
	companion object {
		fun read(str: String): WavefrontScene = OBJ().read(str)
	}

	private val vData = doubleArrayListOf()
	private val vtData = doubleArrayListOf()
	private val vnData = doubleArrayListOf()

	fun read(str: String) = str.reader().readObj()

	fun StrReader.readObj(): WavefrontScene {
		vData.clear()
		vtData.clear()
		vnData.clear()

		val vertexData = FloatArrayList()
		val indices = IntArrayList()

		var vertexCount = 0
		var currentObjectName: String? = null
		var materialName: String? = null

		val objects = linkedHashMapOf<String, WavefrontMesh>()

		fun flushObj() {
			if (currentObjectName != null) {
				objects[currentObjectName!!] = WavefrontMesh(vertexData.toFloatArray(), indices.toIntArray())
			}

			vertexData.clear()
			indices.clear()
		}

		while (!eof) {
			skipSpaces2()
			when {
				tryExpect2("v ") -> {
					val x = skipSpaces2().tryReadNumber()
					val y = skipSpaces2().tryReadNumber()
					val z = skipSpaces2().tryReadNumber()
					val w = skipSpaces2().tryReadNumber(1.0)
					vData.apply { add(x); add(y); add(z); add(w) }
					skipUntilIncluded('\n')
					//println("v: $x, $y, $z, $w")
				}
				tryExpect2("vt ") -> {
					val u = skipSpaces2().tryReadNumber()
					val v = skipSpaces2().tryReadNumber()
					val w = skipSpaces2().tryReadNumber()
					vtData.apply { add(u); add(v); add(w) }
					skipUntilIncluded('\n')
					//println("vt: $u, $v, $w")
				}
				tryExpect2("vn ") -> {
					val x = skipSpaces2().tryReadNumber()
					val y = skipSpaces2().tryReadNumber()
					val z = skipSpaces2().tryReadNumber()
					vnData.apply { add(x); add(y); add(z) }
					skipUntilIncluded('\n')
					//println("vn: $x, $y, $z")
				}
				tryExpect2("f ") -> {
					//println("faces:")
					while (peekChar() != '\n') {
						skipSpaces()
						val vi = tryReadInt(-1)
						val vti = if (tryExpect2("/")) tryReadInt(-1) else -1
						val vni = if (tryExpect2("/")) tryReadInt(-1) else -1
						//for (n in 0 until 4)
						//println("  - $vi, $vti, $vni")
					}
					expect('\n')
				}
				tryExpect2("s ") -> {
					val shading = readUntilIncluded('\n')?.trim() ?: ""
					//println("s: $shading")
				}
				tryExpect2("o ") -> {
					val name = readUntilIncluded('\n')?.trim() ?: ""
					flushObj()
					currentObjectName = name
					//println("o: $name")
				}
				tryExpect2("#") -> {
					skipUntilIncluded('\n')
				}
				tryExpect2("mtllib ") -> {
					val file = readUntilIncluded('\n')?.trim() ?: ""
					//println("mtllib: $file")
				}
				tryExpect2("usemtl ") -> {
					val material = readUntilIncluded('\n')?.trim() ?: ""
					//println("usemtl: $material")
					materialName = material
				}
				else -> {
					val line = readUntilIncluded('\n')
					error("Invalid or unsupported command at line '$line'")
				}
			}
		}
		flushObj()

		return WavefrontScene(objects)
	}
}
