package com.soywiz.korge.experimental.s3d.model

import com.soywiz.kds.*
import com.soywiz.kds.iterators.*
import com.soywiz.korag.*
import com.soywiz.korag.shader.*
import com.soywiz.korge.experimental.s3d.*
import com.soywiz.korge.experimental.s3d.model.internal.*
import com.soywiz.korio.file.*
import com.soywiz.korio.serialization.xml.*
import com.soywiz.korio.util.*
import com.soywiz.korma.geom.*
import kotlin.math.*

suspend fun VfsFile.readColladaLibrary(): Library3D = ColladaParser.parse(readXml())

object ColladaParser {
	data class SourceParam(val name: String, val data: FloatArrayList)
	data class Source(val id: String, val params: FastStringMap<SourceParam>)
	data class Input(val semantic: String, val offset: Int, val source: Source, val indices: IntArrayList)
	data class Geometry(val id: String, val name: String, val inputs: FastStringMap<Input> = FastStringMap())

	fun parse(xml: Xml): Library3D = Library3D().apply {
		parseCameras(xml)
		parseLights(xml)
		parseImages(xml)
		parseEffects(xml)
		parseMaterials(xml)
		parseGeometries(xml)
		parseAnimations(xml)
		parseControllers(xml)
		parseVisualScenes(xml)
		parseScene(xml)
	}

	fun Library3D.parseScene(xml: Xml) {
		val scene = xml["scene"]
	}

	fun Library3D.parseControllers(xml: Xml) {
		for (controller in xml["library_controllers"]["controller"]) {
		}
	}

	fun Library3D.parseMaterials(xml: Xml) {
		for (material in xml["library_materials"]["material"]) {
		}
	}

	fun Library3D.parseEffects(xml: Xml) {
		for (animation in xml["library_effects"]["effect"]) {
		}
	}

	fun Library3D.parseImages(xml: Xml) {
		for (animation in xml["library_images"]["image"]) {
		}
	}

	fun Library3D.parseAnimations(xml: Xml) {
		for (animation in xml["library_animations"]["animation"]) {
		}
	}

	fun Library3D.parseLights(xml: Xml) {
		for (light in xml["library_lights"]["light"]) {
			val id = light.getString("id")
			val name = light.getString("name")
			log { "Light id=$id, name=$name" }
		}
	}

	fun Library3D.parseCameras(xml: Xml) {
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
						log { "Unsupported camera technique ${v.nameLC}" }
					}
				}
			}

			cameraDefs[id] = persp ?: Library3D.CameraDef()
			log { "Camera id=$id, name=$name, persp=$persp" }
		}
	}

	fun Library3D.parseGeometries(xml: Xml) {
		val floatArrays = FastStringMap<FloatArray>()
		val sources = FastStringMap<Source>()
		val vertices = FastStringMap<Any>()
		val geometries = arrayListOf<Geometry>()

		for (geometry in xml["library_geometries"]["geometry"]) {
			val id = geometry.getString("id") ?: "unknown"
			val name = geometry.getString("name") ?: "unknown"
			val geom = Geometry(id, name)
			geometries += geom
			log { "Geometry id=$id, name=$name" }
			for (mesh in geometry["mesh"]) {
				for (source in mesh["source"]) {
					val sourceId = source.getString("id") ?: source.getString("name") ?: "unknown"
					val float_array = source["float_array"].firstOrNull()
					if (float_array != null) {
						val floats = float_array.text.reader().readFloats(FloatArrayList(float_array.getInt("count")
							?: 7))
						val id = float_array.getString("id") ?: float_array.getString("name") ?: "unknown"
						floatArrays[id] = floats.toFloatArray()
						//println("$id: " + FloatArrayList().also { it.add(floats) })
					}
					val rparams = FastStringMap<SourceParam>()
					val accessor = source["technique_common"]["accessor"].firstOrNull()
					if (accessor != null) {
						val source = accessor.getString("source")?.trim('#') ?: "unknown"
						val offset = accessor.getInt("offset") ?: 0
						val count = accessor.getInt("count") ?: 0
						val stride = accessor.getInt("stride") ?: 0
						log { "ACCESSOR: $source, $offset, $count, $stride" }
						for ((index, param) in accessor["param"].withIndex()) {
							val paramName = param.getString("name") ?: "unknown"
							val paramType = param.getString("type") ?: "unknown"
							val paramData = FloatArrayList()
							val paramOffset = offset + index
							log { "  - PARAM: $param : paramName=$paramName, paramType=$paramType" }
							val sourceData = floatArrays[source] ?: continue
							log { "    - PARAM_DATA" }
							for (n in 0 until count) {
								paramData.add(sourceData[paramOffset + n * stride])
							}
							rparams[paramName] = SourceParam(paramName, paramData)
							//println("paramData[$paramName]: ${paramData}")
						}
					}
					sources[sourceId] = Source(sourceId, rparams)
				}

				for (vertices in mesh["vertices"]) {
					val verticesId = vertices.getString("id") ?: vertices.getString("name") ?: "unknown"
					log { "vertices: $vertices" }
					for (input in vertices["input"]) {
						val semantic = input.getString("semantic") ?: "UNKNOWN"
						val source = input.getString("source")?.trim('#') ?: "unknown"
						val rsource = sources[source]
						if (rsource != null) {
							sources[verticesId] = rsource
						}
					}
				}

				log { "SOURCES.KEYS: " + sources.keys }
				log { "SOURCES: ${sources.keys.map { it to sources[it] }.toMap()}" }

				for (triangles in mesh["triangles"]) {
					val trianglesCount = triangles.getInt("count") ?: 0
					log { "triangles: $triangles" }
					var stride = 1
					val inputs = arrayListOf<Input>()
					for (input in triangles["input"]) {
						val offset = input.getInt("offset") ?: 0
						stride = max(stride, offset + 1)

						val semantic = input.getString("semantic") ?: "unknown"
						val source = input.getString("source")?.trim('#') ?: "unknown"
						val rsource = sources[source] ?: continue
						inputs += Input(semantic, offset, rsource, intArrayListOf())
						log { "INPUT: semantic=$semantic, source=$source, offset=$offset, source=$rsource" }
					}
					val pdata = (triangles["p"].firstOrNull()?.text ?: "").reader().readInts()
					//println("P: " + pdata.toList())
					for (input in inputs) {
						log { "INPUT: semantic=${input.semantic}, trianglesCount=$trianglesCount, stride=$stride, offset=${input.offset}" }
						for (n in 0 until trianglesCount * 3) {
							input.indices.add(pdata[input.offset + n * stride])
						}
						log { "  - ${input.indices}" }
					}
					for (input in inputs) {
						geom.inputs[input.semantic] = input
					}
				}
			}
		}

		for (geom in geometries) {
			val px = FloatArrayList()
			val py = FloatArrayList()
			val pz = FloatArrayList()

			val nx = FloatArrayList()
			val ny = FloatArrayList()
			val nz = FloatArrayList()

			val u0 = FloatArrayList()
			val v0 = FloatArrayList()

			val VERTEX = geom.inputs["VERTEX"]
			if (VERTEX != null) {
				for (pname in listOf("X", "Y", "Z")) {
					val p = VERTEX.source.params[pname]
					val array = when (pname) {
						"X" -> px
						"Y" -> py
						"Z" -> pz
						else -> TODO()
					}
					if (p != null) {
						//println(VERTEX.indices)
						VERTEX.indices.fastForEach { index ->
							array.add(p.data[index])
						}
					}
				}
			}
			val NORMAL = geom.inputs["NORMAL"]
			if (NORMAL != null) {
				for (pname in listOf("X", "Y", "Z")) {
					val p = NORMAL.source.params[pname]
					val array = when (pname) {
						"X" -> nx
						"Y" -> ny
						"Z" -> nz
						else -> TODO()
					}
					if (p != null) {
						NORMAL.indices.fastForEach { index -> array.add(p.data[index]) }
					}
				}
			}

			// @TODO: We should use separate components
			val combinedData = floatArrayListOf()
			for (n in 0 until px.size) {
				combinedData.add(px[n])
				combinedData.add(py[n])
				combinedData.add(pz[n])
				if (nx.size >= px.size) {
					combinedData.add(nx[n])
					combinedData.add(ny[n])
					combinedData.add(nz[n])
				} else {
					combinedData.add(0f)
					combinedData.add(0f)
					combinedData.add(0f)
				}
			}

			//println(combinedData.toString())

			geometryDefs[geom.id] = Library3D.RawGeometryDef(Mesh3D(combinedData.toFloatArray(), VertexLayout(View3D.a_pos, View3D.a_norm), null, AG.DrawType.TRIANGLES))
			log { "px: $px" }
			log { "py: $py" }
			log { "pz: $pz" }
			log { "nx: $nx" }
			log { "ny: $ny" }
			log { "nz: $nz" }
		}
	}

	fun Library3D.parseVisualScenes(xml: Xml) {
		for (vscene in xml["library_visual_scenes"]["visual_scene"]) {
			val id = vscene.getString("id")
			val name = vscene.getString("name")
			log { "VisualScene id=$id, name=$name" }
			for (node in vscene["node"]) {
				val id = node.getString("id")
				val name = node.getString("name")
				var transform = Matrix3D()
				node.allNodeChildren.fastForEach { v ->
					when (v.nameLC) {
						"matrix" -> {
							val sid = v.getString("sid")
							val matrix = v.text.reader().readMatrix()
							when (sid) {
								"transform" -> {
									transform = matrix
								}
								else -> {
									log { "  Unhandled matrix sid=$sid" }
								}
							}
						}
						else -> {
							log { "  Unhandled ${v.nameLC}" }
						}
					}
				}
				log { "  Node id=$id, name=$name, transform=$transform" }
			}
		}
	}

	inline fun log(str: () -> String) {
		// DO NOTHING
	}

	@Deprecated("", ReplaceWith("log { str }", "com.soywiz.korge.experimental.s3d.model.ColladaParser.log"))
	inline fun log(str: String) = log { str }
}
