package com.soywiz.korge.experimental.s3d

import com.soywiz.kds.*
import com.soywiz.kds.iterators.*
import com.soywiz.kmem.*
import com.soywiz.korag.*
import com.soywiz.korag.shader.*
import com.soywiz.korge.experimental.s3d.model.internal.*
import com.soywiz.korge.render.*
import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.*
import com.soywiz.korim.color.*
import com.soywiz.korma.geom.*

inline fun Container.scene3D(views: Views3D = Views3D(), callback: Stage3D.() -> Unit = {}): Stage3DView =
	Stage3DView(Stage3D(views).apply(callback)).addTo(this)

class Views3D {
}

fun Container3D.light(callback: Light3D.() -> Unit = {}) = Light3D().apply(callback).addTo(this)

open class Light3D(
	var color: RGBA = Colors.WHITE,
	var constantAttenuation: Double = 1.0,
	var linearAttenuation: Double = 0.0,
	var quadraticAttenuation: Double = 0.00111109
) : View3D() {
	internal val colorVec = Vector3D()
	internal val attenuationVec = Vector3D()

	fun setTo(
		color: RGBA = Colors.WHITE,
		constantAttenuation: Double = 1.0,
		linearAttenuation: Double = 0.0,
		quadraticAttenuation: Double = 0.00111109
	) = this.apply {
		this.color = color
		this.constantAttenuation = constantAttenuation
		this.linearAttenuation = linearAttenuation
		this.quadraticAttenuation = quadraticAttenuation
	}

	override fun render(ctx: RenderContext3D) {
	}
}

class Stage3D(val views: Views3D) : Container3D() {
	lateinit var view: Stage3DView
	//var ambientColor: RGBA = Colors.WHITE
	var ambientColor: RGBA = Colors.BLACK // No ambient light
	var ambientPower: Double = 0.3
	var camera: Camera3D = Camera3D.Perspective().apply {
		positionLookingAt(0, 1, -10, 0, 0, 0)
	}
}

class Stage3DView(val stage3D: Stage3D) : View() {
	init {
		stage3D.view = this
	}

	private val ctx3D = RenderContext3D()
	override fun renderInternal(ctx: RenderContext) {
		ctx.flush()
		ctx.ag.clear(depth = 1f, clearColor = false)
		//ctx.ag.clear(color = Colors.RED)
		ctx3D.ag = ctx.ag
		ctx3D.rctx = ctx
		ctx3D.projMat.copyFrom(stage3D.camera.getProjMatrix(ctx.ag.backWidth.toDouble(), ctx.ag.backHeight.toDouble()))
		ctx3D.cameraMat.copyFrom(stage3D.camera.transform.matrix)
		ctx3D.ambientColor.setToColorPremultiplied(stage3D.ambientColor).scale(stage3D.ambientPower)
		ctx3D.cameraMatInv.invert(stage3D.camera.transform.matrix)
		ctx3D.projCameraMat.multiply(ctx3D.projMat, ctx3D.cameraMatInv)
		ctx3D.lights.clear()
		stage3D.foreachDescendant {
			if (it is Light3D) {
				if (it.active) ctx3D.lights.add(it)
			}
		}
		stage3D.render(ctx3D)
	}
}



fun View3D?.foreachDescendant(handler: (View3D) -> Unit) {
	if (this != null) {
		handler(this)
		if (this is Container3D) {
			this.children.fastForEach { child ->
				child.foreachDescendant(handler)
			}
		}
	}
}

class RenderContext3D() {
	lateinit var ag: AG
	lateinit var rctx: RenderContext
	val textureUnit = AG.TextureUnit()
	val bindMat4 = Matrix3D()
	val bones = Array(128) { Matrix3D() }
	val tmepMat = Matrix3D()
	val projMat: Matrix3D = Matrix3D()
	val lights = arrayListOf<Light3D>()
	val projCameraMat: Matrix3D = Matrix3D()
	val cameraMat: Matrix3D = Matrix3D()
	val cameraMatInv: Matrix3D = Matrix3D()
	val dynamicVertexBufferPool = Pool { ag.createVertexBuffer() }
	val ambientColor: Vector3D = Vector3D()
}

abstract class View3D {
	var active = true
	var id: String? = null
	var name: String? = null
	val transform = Transform3D()

	var localX: Double
		set(localX) = run { transform.setTranslation(localX, localY, localZ, localW) }
		get() = transform.translation.x.toDouble()

	var localY: Double
		set(localY) = run { transform.setTranslation(localX, localY, localZ, localW) }
		get() = transform.translation.y.toDouble()

	var localZ: Double
		set(localZ) = run { transform.setTranslation(localX, localY, localZ, localW) }
		get() = transform.translation.z.toDouble()

	var localW: Double
		set(localW) = run { transform.setTranslation(localX, localY, localZ, localW) }
		get() = transform.translation.w.toDouble()

	var parent: Container3D? = null
	val modelMat = Matrix3D()
	//val position = Vector3D()

	abstract fun render(ctx: RenderContext3D)
}

class Skeleton3D(val skin: Skin3D, val headJoint: Joint3D) : View3D() {
	val allJoints = headJoint.descendantsAndThis
	val jointsByName = allJoints.associateBy { it.name }
	val matrices = Array(allJoints.size) { Matrix3D() }

	//init {
	//	println(jointsByName)
	//	println(jointsByName.values.map { it.name })
	//	println(jointsByName.values.map { it.name })
	//}

	override fun render(ctx: RenderContext3D) {
	}
}

open class Joint3D constructor(val skin: Skin3D, val bone: Bone3D, val jointParent: Joint3D? = null, initialMatrix: Matrix3D) : Container3D() {
//open class Joint3D constructor(val jointParent: Joint3D? = null, initialMatrix: Matrix3D) : View3D() {
	val index = bone.index
	init {
		this.transform.setMatrix(initialMatrix)
		this.name = bone.name
	}
	val poseMatrix = this.transform.globalMatrix.clone()
	val poseMatrixInv = poseMatrix.clone().invert()
	//val poseMatrixInv = bone.invBindMatrix * skin.bindShapeMatrix

	val childJoints = arrayListOf<Joint3D>()
	val descendants: List<Joint3D> get() = childJoints.flatMap { it.descendantsAndThis }
	val descendantsAndThis: List<Joint3D> get() = listOf(this) + descendants

	init {
		Unit
	}

	override fun render(ctx: RenderContext3D) {
	}

	override fun toString(): String = "Joint3D($index, $name)"
}

open class Container3D : View3D() {
	val children = arrayListOf<View3D>()

	fun removeChild(child: View3D) {
		children.remove(child)
	}

	fun addChild(child: View3D) {
		child.removeFromParent()
		children += child
		child.parent = this
	}

	operator fun plusAssign(child: View3D) = addChild(child)

	override fun render(ctx: RenderContext3D) {
		children.fastForEach {
			it.render(ctx)
		}
	}
}

fun View3D.removeFromParent() {
	parent?.removeChild(this)
	parent = null
}

inline fun <reified T : View3D> View3D?.findByType() = sequence<T> {
	for (it in descendants()) {
		if (it is T) yield(it)
	}
}

inline fun <reified T : View3D> View3D?.findByTypeWithName(name: String) = sequence<T> {
	for (it in descendants()) {
		if (it is T && it.name == name) yield(it)
	}
}

fun View3D?.descendants(): Sequence<View3D> = sequence<View3D> {
	val view = this@descendants ?: return@sequence
	yield(view)
	if (view is Container3D) {
		view.children.fastForEach {
			yieldAll(it.descendants())
		}
	}
}

operator fun View3D?.get(name: String): View3D? {
	if (this?.id == name) return this
	if (this?.name == name) return this
	if (this is Container3D) {
		this.children.fastForEach {
			val result = it[name]
			if (result != null) return result
		}
	}
	return null
}

fun <T : View3D> T.name(name: String) = this.apply { this.name = name }

inline fun <T : View3D> T.position(x: Number, y: Number, z: Number, w: Number = 1f): T = this.apply {
	transform.setTranslation(x, y, z, w)
}

inline fun <T : View3D> T.rotation(x: Angle = 0.degrees, y: Angle = 0.degrees, z: Angle = 0.degrees): T = this.apply {
	transform.setRotation(x, y, z)
}

inline fun <T : View3D> T.scale(x: Number = 1, y: Number = 1, z: Number = 1, w: Number = 1): T = this.apply {
	transform.setScale(x, y, z, w)
}

inline fun <T : View3D> T.lookAt(x: Number, y: Number, z: Number): T = this.apply {
	transform.lookAt(x, y, z)
}

inline fun <T : View3D> T.positionLookingAt(px: Number, py: Number, pz: Number, tx: Number, ty: Number, tz: Number): T = this.apply {
	transform.setTranslationAndLookAt(px, py, pz, tx, ty, tz)
}

fun <T : View3D> T.addTo(container: Container3D) = this.apply {
	container.addChild(this)
}

data class Bone3D constructor(
	val index: Int,
	val name: String,
	val invBindMatrix: Matrix3D
) {
}

data class Skin3D(val bindShapeMatrix: Matrix3D, val bones: List<Bone3D>) {
	//val matrices = Array(bones.size) { Matrix3D() }
}

open class MaterialLight(val kind: String)
data class MaterialLightColor(val color: RGBA) : MaterialLight("color") {
	val colorVec = Vector3D().setToColor(color)
}
data class MaterialLightTexture(val bitmap: Bitmap?) : MaterialLight("texture") {
	val textureUnit = AG.TextureUnit()
}

data class Material3D(
	val emission: MaterialLight,
	val ambient: MaterialLight,
	val diffuse: MaterialLight,
	val specular: MaterialLight,
	val shiness: Float,
	val indexOfRefraction: Float
) {
	val kind: String = "${emission.kind}_${ambient.kind}_${diffuse.kind}_${specular.kind}"
}

class Mesh3D constructor(
	val data: FloatArray,
	val layout: VertexLayout,
	val program: Program?,
	val drawType: AG.DrawType,
	val hasTexture: Boolean = false,
	val maxWeights: Int = 0
) {
	var skin: Skin3D? = null

	val fbuffer by lazy {
		FBuffer.alloc(data.size * 4).apply {
			setAlignedArrayFloat32(0, this@Mesh3D.data, 0, this@Mesh3D.data.size)
		}
		//FBuffer.wrap(MemBufferAlloc(data.size * 4)).apply {
		//	arraycopy(this@Mesh3D.data, 0, this@apply.mem, 0, this@Mesh3D.data.size) // Bug in kmem-js?
		//}
	}

	var material: Material3D? = null

	//val modelMat = Matrix3D()
	val vertexSizeInBytes = layout.totalSize
	val vertexSizeInFloats = vertexSizeInBytes / 4
	val vertexCount = data.size / vertexSizeInFloats

	init {
		println("vertexCount: $vertexCount, vertexSizeInFloats: $vertexSizeInFloats, data.size: ${data.size}")
	}
}

inline fun Container3D.box(width: Number = 1, height: Number = width, depth: Number = height, callback: Cube.() -> Unit = {}): Cube {
	return Cube(width.toDouble(), height.toDouble(), depth.toDouble()).apply(callback).addTo(this)
}

class Cube(var width: Double, var height: Double, var depth: Double) : ViewWithMesh3D(Cube.mesh) {
	override fun prepareExtraModelMatrix(mat: Matrix3D) {
		mat.identity().scale(width, height, depth)
	}

	companion object {
		private val cubeSize = .5f

		private val vertices = floatArrayOf(
			-cubeSize, -cubeSize, -cubeSize, 1f, 0f, 0f,  //p1
			-cubeSize, -cubeSize, +cubeSize, 1f, 0f, 0f,  //p2
			-cubeSize, +cubeSize, +cubeSize, 1f, 0f, 0f,  //p3
			-cubeSize, -cubeSize, -cubeSize, 1f, 0f, 0f,  //p1
			-cubeSize, +cubeSize, +cubeSize, 1f, 0f, 0f,  //p3
			-cubeSize, +cubeSize, -cubeSize, 1f, 0f, 0f,  //p4

			+cubeSize, +cubeSize, -cubeSize, 0f, 1f, 0f,  //p5
			-cubeSize, -cubeSize, -cubeSize, 0f, 1f, 0f,  //p1
			-cubeSize, +cubeSize, -cubeSize, 0f, 1f, 0f,  //p4
			+cubeSize, +cubeSize, -cubeSize, 0f, 1f, 0f,  //p5
			+cubeSize, -cubeSize, -cubeSize, 0f, 1f, 0f,  //p7
			-cubeSize, -cubeSize, -cubeSize, 0f, 1f, 0f,  //p1

			+cubeSize, -cubeSize, +cubeSize, 0f, 0f, 1f,  //p6
			-cubeSize, -cubeSize, -cubeSize, 0f, 0f, 1f,  //p1
			+cubeSize, -cubeSize, -cubeSize, 0f, 0f, 1f,  //p7
			+cubeSize, -cubeSize, +cubeSize, 0f, 0f, 1f,  //p6
			-cubeSize, -cubeSize, +cubeSize, 0f, 0f, 1f,  //p2
			-cubeSize, -cubeSize, -cubeSize, 0f, 0f, 1f,  //p1

			+cubeSize, +cubeSize, +cubeSize, 0f, 1f, 1f,  //p8
			+cubeSize, +cubeSize, -cubeSize, 0f, 1f, 1f,  //p5
			-cubeSize, +cubeSize, -cubeSize, 0f, 1f, 1f,  //p4
			+cubeSize, +cubeSize, +cubeSize, 0f, 1f, 1f,  //p8
			-cubeSize, +cubeSize, -cubeSize, 0f, 1f, 1f,  //p4
			-cubeSize, +cubeSize, +cubeSize, 0f, 1f, 1f,  //p3

			+cubeSize, +cubeSize, +cubeSize, 1f, 1f, 0f,  //p8
			-cubeSize, +cubeSize, +cubeSize, 1f, 1f, 0f,  //p3
			+cubeSize, -cubeSize, +cubeSize, 1f, 1f, 0f,  //p6
			-cubeSize, +cubeSize, +cubeSize, 1f, 1f, 0f,  //p3
			-cubeSize, -cubeSize, +cubeSize, 1f, 1f, 0f,  //p2
			+cubeSize, -cubeSize, +cubeSize, 1f, 1f, 0f,  //p6

			+cubeSize, +cubeSize, +cubeSize, 1f, 0f, 1f,  //p8
			+cubeSize, -cubeSize, -cubeSize, 1f, 0f, 1f,  //p7
			+cubeSize, +cubeSize, -cubeSize, 1f, 0f, 1f,  //p5
			+cubeSize, -cubeSize, -cubeSize, 1f, 0f, 1f,  //p7
			+cubeSize, +cubeSize, +cubeSize, 1f, 0f, 1f,  //p8
			+cubeSize, -cubeSize, +cubeSize, 1f, 0f, 1f   //p6
		)

		val mesh = Mesh3D(vertices, Shaders3D.layoutPosCol, Shaders3D.programColor3D, AG.DrawType.TRIANGLES, hasTexture = false)
	}
}

inline fun Container3D.mesh(mesh: Mesh3D, callback: ViewWithMesh3D.() -> Unit = {}): ViewWithMesh3D {
	return ViewWithMesh3D(mesh).apply(callback).addTo(this)
}

open class ViewWithMesh3D(
	var mesh: Mesh3D,
	var skeleton: Skeleton3D? = null
) : View3D() {

	private val uniformValues = AG.UniformValues()
	private val rs = AG.RenderState(depthFunc = AG.CompareMode.LESS_EQUAL)
	//private val rs = AG.RenderState(depthFunc = AG.CompareMode.ALWAYS)

	private val tempMat1 = Matrix3D()
	private val tempMat2 = Matrix3D()
	private val tempMat3 = Matrix3D()

	protected open fun prepareExtraModelMatrix(mat: Matrix3D) {
		mat.identity()
	}

	fun AG.UniformValues.setMaterialLight(ctx: RenderContext3D, uniform: Shaders3D.MaterialLightUniform, actual: MaterialLight) {
		when (actual) {
			is MaterialLightColor -> {
				this[uniform.u_color] = actual.colorVec
			}
			is MaterialLightTexture -> {
				actual.textureUnit.texture = actual.bitmap?.let { ctx.rctx.agBitmapTextureManager.getTextureBase(it).base }
				actual.textureUnit.linear = true
				this[uniform.u_texUnit] = actual.textureUnit
			}
		}
	}

	override fun render(ctx: RenderContext3D) {
		val ag = ctx.ag

		ctx.dynamicVertexBufferPool.alloc { vertexBuffer ->
			//vertexBuffer.upload(mesh.data)
			vertexBuffer.upload(mesh.fbuffer)

			//tempMat2.invert()
			//tempMat3.multiply(ctx.cameraMatInv, this.localTransform.matrix)
			//tempMat3.multiply(ctx.cameraMatInv, Matrix3D().invert(this.localTransform.matrix))
			//tempMat3.multiply(this.localTransform.matrix, ctx.cameraMat)

			Shaders3D.apply {
				val meshMaterial = mesh.material
				ag.draw(
					vertexBuffer,
					type = mesh.drawType,
					program = mesh.program ?: getProgram3D(ctx.lights.size.clamp(0, 4), mesh.maxWeights, meshMaterial, mesh.hasTexture),
					vertexLayout = mesh.layout,
					vertexCount = mesh.vertexCount,
					blending = AG.Blending.NONE,
					//vertexCount = 6 * 6,
					uniforms = uniformValues.apply {
						this[u_ProjMat] = ctx.projCameraMat
						this[u_ViewMat] = transform.globalMatrix
						this[u_ModMat] = tempMat2.multiply(tempMat1.apply { prepareExtraModelMatrix(this) }, modelMat)
						//this[u_NormMat] = tempMat3.multiply(tempMat2, localTransform.matrix).invert().transpose()
						this[u_NormMat] = tempMat3.multiply(tempMat2, transform.globalMatrix).invert()

						this[u_Shiness] = meshMaterial?.shiness ?: 0.5f
						this[u_IndexOfRefraction] = meshMaterial?.indexOfRefraction ?: 1f

						if (meshMaterial != null) {
							setMaterialLight(ctx, ambient, meshMaterial.ambient)
							setMaterialLight(ctx, diffuse, meshMaterial.diffuse)
							setMaterialLight(ctx, emission, meshMaterial.emission)
							setMaterialLight(ctx, specular, meshMaterial.specular)
						}

						val skeleton = this@ViewWithMesh3D.skeleton
						this[u_BindShapeMatrix] = ctx.bindMat4.identity()
						if (skeleton != null) {
							//this[u_BindShapeMatrix] = ctx.bindMat4.copyFrom(skeleton.skin.bindShapeMatrix)
							//skeleton.allJoints[1].transform.rotate(10.degrees, 0.degrees, 0.degrees)
							skeleton.allJoints.fastForEach {
								//skeleton.matrices[it.index].copyFrom(it.inverseBindTransform)
								skeleton.matrices[it.index].multiply(it.poseMatrixInv, it.transform.globalMatrix)
								//if (it.name == "Upper_Leg.L") println("${it.name}: ${skeleton.matrices[it.index]}")
							}
							this[u_BoneMats] = skeleton.matrices
						} else {

						}

						this[u_AmbientColor] = ctx.ambientColor

						ctx.lights.fastForEachWithIndex { index, light: Light3D ->
							val lightColor = light.color
							this[lights[index].u_sourcePos] = light.transform.translation
							this[lights[index].u_color] = light.colorVec.setTo(lightColor.rf, lightColor.gf, lightColor.bf, 1f)
							this[lights[index].u_attenuation] = light.attenuationVec.setTo(light.constantAttenuation, light.linearAttenuation, light.quadraticAttenuation)
						}
					},
					renderState = rs
				)
			}
		}
	}
}
