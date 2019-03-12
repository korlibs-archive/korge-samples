package com.soywiz.korge.experimental.s3d.model

import com.soywiz.kds.*
import com.soywiz.korge.experimental.s3d.*
import com.soywiz.korim.bitmap.*
import com.soywiz.korim.color.*
import com.soywiz.korim.format.*
import com.soywiz.korio.file.std.*
import com.soywiz.korma.geom.*

data class Library3D(
	val cameraDefs: FastStringMap<CameraDef> = FastStringMap(),
	val lightDefs: FastStringMap<LightDef> = FastStringMap(),
	val materialDefs: FastStringMap<MaterialDef> = FastStringMap(),
	val effectDefs: FastStringMap<EffectDef> = FastStringMap(),
	val imageDefs: FastStringMap<ImageDef> = FastStringMap(),
	val geometryDefs: FastStringMap<GeometryDef> = FastStringMap(),
	val skins: FastStringMap<ColladaParser.Skin> = FastStringMap()
) {

	suspend fun loadTextures() {
		imageDefs.fastValueForEach { image ->
			image.texure = resourcesVfs[image.initFrom].readBitmap()
		}
	}

	fun instantiateMaterials() {
		geometryDefs.fastValueForEach { geom ->
			geom.mesh.material = geom.material?.instantiate()
		}
	}

	val library = this

	val mainScene = Scene3D().apply {
		id = "MainScene"
		name = "MainScene"
	}
	var scenes = FastStringMap<Scene3D>()

	open class Instance3D {
		val transform = Matrix3D()
		var def: Def? = null
		val children = arrayListOf<Instance3D>()
		var id: String = ""
		var name: String = ""
		var type: String = ""
	}

	open class Scene3D : Instance3D() {
	}

	open class Def

	open class Pong

	open class ImageDef(val id: String, val name: String, val initFrom: String, var texure: Bitmap? = null) : Def() {

	}

	open class ObjectDef : Def()

	open class MaterialDef(val id: String, val name: String, val effects: List<EffectDef>) : Def()

	open class LightDef : ObjectDef()

	open class CameraDef : ObjectDef()
	data class PerspectiveCameraDef(val xfov: Angle, val zmin: Double, val zmax: Double) : CameraDef()

	interface LightKindDef {
		val sid: String
	}
	open class LightTexDef(override val sid: String, val texture: EffectParamSampler2D?, val lightKind: String) : LightKindDef
	open class LightColorDef(override val sid: String, val color: RGBA, val lightKind: String) : LightKindDef

	data class EffectParamSurface(val surfaceType: String, val initFrom: Library3D.ImageDef?)
	data class EffectParamSampler2D(val surface: EffectParamSurface?)


	open class EffectDef() : Def()

	data class StandardEffectDef(
		val id: String,
		val name: String,
		val emission: LightKindDef?,
		val ambient: LightKindDef?,
		val diffuse: LightKindDef?,
		val specular: LightKindDef?,
		val shiness: Float?,
		val index_of_refraction: Float?
	) : EffectDef()

	data class GeometryDef(
		val mesh: Mesh3D,
		val skin: SkinDef? = null,
		val material: MaterialDef? = null
	) : ObjectDef()

	data class BoneDef(val name: String, val pose: Matrix3D) : Def() {
		fun toBone() = Bone3D(name, pose.clone())
	}

	data class SkinDef(
		val bindShapeMatrix: Matrix3D,
		val bones: List<BoneDef>
	) : Def()


	class PointLightDef(
		val color: RGBA,
		val constantAttenuation: Double,
		val linearAttenuation: Double,
		val quadraticAttenuation: Double
	) : LightDef()
}

fun Library3D.Instance3D.instantiate(): View3D {
	val def = this.def
	val view: View3D = when (def) {
		null -> {
			Container3D().also { container ->
				for (child in children) {
					container.addChild(child.instantiate())
				}
			}
		}
		is Library3D.GeometryDef -> {
			ViewWithMesh3D(def.mesh)
		}
		is Library3D.PerspectiveCameraDef -> {
			Camera3D.Perspective(def.xfov, def.zmin, def.zmax)
		}
		is Library3D.PointLightDef -> {
			Light3D(def.color, def.constantAttenuation, def.linearAttenuation, def.quadraticAttenuation)
		}
		else -> TODO("def=$def")
	}
	view.id = this.id
	view.name = this.name
	//view.localTransform.setMatrix(this.transform.clone().transpose())
	view.localTransform.setMatrix(this.transform)
	if (def is Library3D.PointLightDef) {
		println(view.localTransform.matrix)
		println(view.localTransform.translation)
		println(view.localTransform.rotation)
		println(view.localTransform.scale)
		println("def: $def")
	}
	return view
}

fun Library3D.LightKindDef.instantiate(): MaterialLight {
	return when (this) {
		is Library3D.LightTexDef -> MaterialLightTexture(this.texture?.surface?.initFrom?.texure)
		is Library3D.LightColorDef -> MaterialLightColor(this.color)
		else -> error("Unsupported $this")
	}
}

fun Library3D.MaterialDef.instantiate(): Material3D {
	val effect = this.effects.firstOrNull() as? Library3D.StandardEffectDef?
	return Material3D(
		emission = effect?.emission?.instantiate() ?: MaterialLightColor(Colors.BLACK),
		ambient = effect?.ambient?.instantiate() ?: MaterialLightColor(Colors.BLACK),
		diffuse = effect?.diffuse?.instantiate() ?: MaterialLightColor(Colors.BLACK),
		specular = effect?.specular?.instantiate() ?: MaterialLightColor(Colors.BLACK),
		shiness = effect?.shiness ?: 0.5f,
		indexOfRefraction = effect?.index_of_refraction ?: 1f
	)
}
