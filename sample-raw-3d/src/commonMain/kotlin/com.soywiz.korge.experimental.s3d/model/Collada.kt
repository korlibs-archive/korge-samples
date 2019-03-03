package com.soywiz.korge.experimental.s3d.model

import com.soywiz.kds.*
import com.soywiz.kds.iterators.*
import com.soywiz.korge.experimental.s3d.model.internal.*
import com.soywiz.korio.file.*
import com.soywiz.korio.serialization.xml.*
import com.soywiz.korma.geom.*

suspend fun VfsFile.readColladaLibrary(): Library3D = ColladaParser.parse(readXml())

object ColladaParser {
	fun parse(str: String) = parse(Xml(str))

	fun parseFloats(str: String, capacity: Int = 7): FloatArray {
		val floats = FloatArrayList(capacity)
		val reader = com.soywiz.korio.util.StrReader(str)
		while (!reader.eof) {
			val pos0 = reader.pos
			val float = reader.skipSpaces2().tryReadNumber().toFloat()
			val pos1 = reader.pos
			if (pos1 == pos0) error("Invalid number at $pos0 in '$str'")
			floats.add(float)
			//println("float: $float, ${reader.pos}/${reader.length}")
		}
		return floats.toFloatArray()
	}

	fun parseMatrix(str: String): Matrix3D {
		val f = parseFloats(str, 16)
		if (f.size == 16) {
			return Matrix3D().setRows(
				f[0], f[1], f[2], f[3],
				f[4], f[5], f[6], f[7],
				f[8], f[9], f[10], f[11],
				f[12], f[13], f[14], f[15]
			)
		} else {
			error("Invalid matrix size ${f.size} : str='$str'")
		}
	}

	fun parse(xml: Xml): Library3D {
		val library = Library3D()

		for (camera in xml["library_cameras"]["camera"]) {
			val id = camera.getString("id") ?: "Unknown"
			val name = camera.getString("name") ?: "Unknown"
			var persp: Library3D.PerspectiveCameraDef? = null
			for (v in camera["optics"]["technique_common"].allChildren) {
				when (v.nameLC) {
					"_text_" -> Unit
					"perspective" -> {
						val xfov = v["xfov"].firstOrNull()?.text?.toDoubleOrNull() ?: 45.0
						val znear = v["znear"].firstOrNull()?.text?.toDoubleOrNull() ?: 0.01
						val zfar = v["zfar"].firstOrNull()?.text?.toDoubleOrNull() ?: 100.0
						persp = Library3D.PerspectiveCameraDef(xfov.degrees, znear, zfar)
					}
					else -> {
						println("Unsupported camera technique ${v.nameLC}")
					}
				}
			}

			library.cameraDefs[id] = persp ?: Library3D.CameraDef()
			println("Camera id=$id, name=$name, persp=$persp")
		}
		for (light in xml["library_lights"]["light"]) {
			val id = light.getString("id")
			val name = light.getString("name")
			println("Light id=$id, name=$name")
		}
		for (geometry in xml["library_geometries"]["geometry"]) {
			val id = geometry.getString("id")
			val name = geometry.getString("name")
			println("Geometry id=$id, name=$name")
			for (mesh in geometry["mesh"]) {
				for (source in mesh["source"]) {
					val float_array = source["float_array"].firstOrNull()
					if (float_array != null) {
						val floats = parseFloats(float_array.text, float_array.getInt("count") ?: 7)
						val id = float_array.getString("id")
						println("$id: " + FloatArrayList().also { it.add(floats) })
					}
				}
				for (triangles in mesh["triangles"]) {

				}
			}
		}
		for (vscene in xml["library_visual_scenes"]["visual_scene"]) {
			val id = vscene.getString("id")
			val name = vscene.getString("name")
			println("VisualScene id=$id, name=$name")
			for (node in vscene["node"]) {
				val id = node.getString("id")
				val name = node.getString("name")
				var transform = Matrix3D()
				node.allNodeChildren.fastForEach { v ->
					when (v.nameLC) {
						"matrix" -> {
							val sid = v.getString("sid")
							val matrix = parseMatrix(v.text)
							when (sid) {
								"transform" -> {
									transform = matrix
								}
								else -> {
									println("  Unhandled matrix sid=$sid")
								}
							}
						}
						else -> {
							println("  Unhandled ${v.nameLC}")
						}
					}
				}
				println("  Node id=$id, name=$name, transform=$transform")
			}
		}

		return library
	}
}
