import com.soywiz.kds.*
import com.soywiz.kds.iterators.*
import com.soywiz.korag.*
import com.soywiz.korag.shader.*
import com.soywiz.korge.experimental.s3d.*
import com.soywiz.korge.render.*
import com.soywiz.korge.view.*
import com.soywiz.korma.geom.*

inline fun Container.scene3D(views: Views3D = Views3D(), callback: Stage3D.() -> Unit = {}): Stage3DView =
	Stage3DView(Stage3D(views).apply(callback)).addTo(this)

class Views3D {
}

class Stage3D(val views: Views3D) : Container3D() {
	lateinit var view: Stage3DView
	var camera = Camera3D.Perspective()
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
		ctx3D.projMat.copyFrom(stage3D.camera.getProjMatrix(ctx.ag.backWidth.toDouble(), ctx.ag.backHeight.toDouble()))
		ctx3D.cameraMat.copyFrom(stage3D.camera.transform.matrix)
		ctx3D.cameraMatInv.invert(stage3D.camera.transform.matrix)
		ctx3D.projCameraMat.multiply(ctx3D.projMat, ctx3D.cameraMatInv)
		stage3D.render(ctx3D)
	}
}

class RenderContext3D() {
	lateinit var ag: AG
	val tmepMat = Matrix3D()
	val projMat: Matrix3D = Matrix3D()
	val projCameraMat: Matrix3D = Matrix3D()
	val cameraMat: Matrix3D = Matrix3D()
	val cameraMatInv: Matrix3D = Matrix3D()
	val dynamicVertexBufferPool = Pool { ag.createVertexBuffer() }
}

abstract class View3D {
	companion object {
		val u_ProjMat = Uniform("u_ProjMat", VarType.Mat4)
		val u_ViewMat = Uniform("u_ViewMat", VarType.Mat4)
		val u_ModMat = Uniform("u_ModMat", VarType.Mat4)
		val point = Attribute("point", VarType.Float3, normalized = false)
		val a_col = Attribute("a_Col", VarType.Float3, normalized = true)
		val v_col = Varying("v_Col", VarType.Float3)
		val programColor3D = Program(
			vertex = VertexShader {
				SET(v_col, a_col)
				SET(out, u_ProjMat * u_ViewMat * u_ModMat * vec4(point, 1f.lit))
			},
			fragment = FragmentShader {
				SET(out, vec4(v_col, 1f.lit))
			},
			name = "programColor3D"
		)
		val vertexLayout = VertexLayout(point, a_col)

		private val FLOATS_PER_VERTEX = vertexLayout.totalSize / Int.SIZE_BYTES /*Float.SIZE_BYTES is not defined*/
	}

	var parent: Container3D? = null
	val localTransform = Transform3D()
	val modelMat = Matrix3D()
	//val position = Vector3D()

	abstract fun render(ctx: RenderContext3D)
}

open class Container3D : View3D() {
	val children = arrayListOf<View3D>()

	override fun render(ctx: RenderContext3D) {
		children.fastForEach {
			it.render(ctx)
		}
	}
}

inline fun <T : View3D> T.position(x: Number, y: Number, z: Number, w: Number = 1f): T = this.apply {
	localTransform.setTranslation(x, y, z, w)
}

inline fun <T : View3D> T.rotation(x: Angle, y: Angle, z: Angle): T = this.apply {
	localTransform.setRotation(x, y, z)
}

inline fun <T : View3D> T.scale(x: Number = 1, y: Number = 1, z: Number = 1, w: Number = 1): T = this.apply {
	localTransform.setScale(x, y, z, w)
}

fun <T : View3D> T.addTo(container: Container3D) = this.apply {
	this.parent?.children?.remove(this)
	container.children += this
	this.parent = container
}

class Mesh3D(val data: FloatArray) {
	val modelMat = Matrix3D()
}

inline fun Container3D.box(width: Number, height: Number = width, depth: Number = height, callback: Box.() -> Unit = {}): Box {
	return Box(width.toDouble(), height.toDouble(), depth.toDouble()).apply(callback).addTo(this)
}

class Box(var width: Double, var height: Double = width, var depth: Double = height) : View3D() {
	private val cubeSize = .5f

	private val vertices = floatArrayOf(
		-cubeSize, -cubeSize, -cubeSize,  1f, 0f, 0f,  //p1
		-cubeSize, -cubeSize, +cubeSize,  1f, 0f, 0f,  //p2
		-cubeSize, +cubeSize, +cubeSize,  1f, 0f, 0f,  //p3
		-cubeSize, -cubeSize, -cubeSize,  1f, 0f, 0f,  //p1
		-cubeSize, +cubeSize, +cubeSize,  1f, 0f, 0f,  //p3
		-cubeSize, +cubeSize, -cubeSize,  1f, 0f, 0f,  //p4

		+cubeSize, +cubeSize, -cubeSize,  0f, 1f, 0f,  //p5
		-cubeSize, -cubeSize, -cubeSize,  0f, 1f, 0f,  //p1
		-cubeSize, +cubeSize, -cubeSize,  0f, 1f, 0f,  //p4
		+cubeSize, +cubeSize, -cubeSize,  0f, 1f, 0f,  //p5
		+cubeSize, -cubeSize, -cubeSize,  0f, 1f, 0f,  //p7
		-cubeSize, -cubeSize, -cubeSize,  0f, 1f, 0f,  //p1

		+cubeSize, -cubeSize, +cubeSize,  0f, 0f, 1f,  //p6
		-cubeSize, -cubeSize, -cubeSize,  0f, 0f, 1f,  //p1
		+cubeSize, -cubeSize, -cubeSize,  0f, 0f, 1f,  //p7
		+cubeSize, -cubeSize, +cubeSize,  0f, 0f, 1f,  //p6
		-cubeSize, -cubeSize, +cubeSize,  0f, 0f, 1f,  //p2
		-cubeSize, -cubeSize, -cubeSize,  0f, 0f, 1f,  //p1

		+cubeSize, +cubeSize, +cubeSize,  0f, 1f, 1f,  //p8
		+cubeSize, +cubeSize, -cubeSize,  0f, 1f, 1f,  //p5
		-cubeSize, +cubeSize, -cubeSize,  0f, 1f, 1f,  //p4
		+cubeSize, +cubeSize, +cubeSize,  0f, 1f, 1f,  //p8
		-cubeSize, +cubeSize, -cubeSize,  0f, 1f, 1f,  //p4
		-cubeSize, +cubeSize, +cubeSize,  0f, 1f, 1f,  //p3

		+cubeSize, +cubeSize, +cubeSize,  1f, 1f, 0f,  //p8
		-cubeSize, +cubeSize, +cubeSize,  1f, 1f, 0f,  //p3
		+cubeSize, -cubeSize, +cubeSize,  1f, 1f, 0f,  //p6
		-cubeSize, +cubeSize, +cubeSize,  1f, 1f, 0f,  //p3
		-cubeSize, -cubeSize, +cubeSize,  1f, 1f, 0f,  //p2
		+cubeSize, -cubeSize, +cubeSize,  1f, 1f, 0f,  //p6

		+cubeSize, +cubeSize, +cubeSize,  1f, 0f, 1f,  //p8
		+cubeSize, -cubeSize, -cubeSize,  1f, 0f, 1f,  //p7
		+cubeSize, +cubeSize, -cubeSize,  1f, 0f, 1f,  //p5
		+cubeSize, -cubeSize, -cubeSize,  1f, 0f, 1f,  //p7
		+cubeSize, +cubeSize, +cubeSize,  1f, 0f, 1f,  //p8
		+cubeSize, -cubeSize, +cubeSize,  1f, 0f, 1f   //p6
	)

	private val uniformValues = AG.UniformValues()
	private val rs = AG.RenderState(depthFunc = AG.CompareMode.LESS_EQUAL)
	//private val rs = AG.RenderState(depthFunc = AG.CompareMode.ALWAYS)

	private val tempMat1 = Matrix3D()
	private val tempMat2 = Matrix3D()

	private val tempMat3 = Matrix3D()

	override fun render(ctx: RenderContext3D) {
		val ag = ctx.ag

		ctx.dynamicVertexBufferPool.alloc { vertexBuffer ->
			vertexBuffer.upload(vertices)
			tempMat1.setToScale(width, height, depth)
			tempMat2.multiply(modelMat, tempMat1)
			//tempMat3.multiply(ctx.cameraMatInv, this.localTransform.matrix)
			//tempMat3.multiply(ctx.cameraMatInv, Matrix3D().invert(this.localTransform.matrix))
			//tempMat3.multiply(this.localTransform.matrix, ctx.cameraMat)

			ag.draw(
				vertexBuffer,
				program = programColor3D,
				type = AG.DrawType.TRIANGLES,
				vertexLayout = vertexLayout,
				vertexCount = 6 * 6,
				uniforms = uniformValues.apply {
					this[u_ProjMat] = ctx.projCameraMat
					this[u_ViewMat] = localTransform.matrix
					this[u_ModMat] = tempMat2
				},
				renderState = rs
			)
		}
	}
}
