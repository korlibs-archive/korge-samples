import com.soywiz.kds.*
import com.soywiz.kds.iterators.*
import com.soywiz.korag.*
import com.soywiz.korag.shader.*
import com.soywiz.korge.experimental.s3d.*
import com.soywiz.korge.render.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
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
		ctx3D.projMat.copyFrom(stage3D.camera.getMatrix(ctx.ag.backWidth.toDouble(), ctx.ag.backHeight.toDouble()))
		stage3D.render(ctx3D)
	}
}

class RenderContext3D() {
	lateinit var ag: AG
	val projMat: Matrix3D = Matrix3D()
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
	val viewMat = Matrix3D()
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

inline fun <T : View3D> T.position(x: Number, y: Number, z: Number): T = this.apply {
	viewMat.setToTranslation(x, y, z)
}

fun <T : View3D> T.addTo(container: Container3D) = this.apply {
	this.parent?.children?.remove(this)
	container.children += this
	this.parent = container
}

class Mesh3D(val data: FloatArray) {
	val modelMat = Matrix3D()
}

inline fun Container3D.cube(width: Number, height: Number = width, depth: Number = height, callback: Cube.() -> Unit = {}): Cube {
	return Cube(width.toDouble(), height.toDouble(), depth.toDouble()).apply(callback).addTo(this)
}

class Cube(var width: Double, var height: Double = width, var depth: Double = height) : View3D() {
	private val cubeSize = 1f

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

	override fun render(ctx: RenderContext3D) {
		val ag = ctx.ag

		ctx.dynamicVertexBufferPool.alloc { vertexBuffer ->
			vertexBuffer.upload(vertices)
			ag.draw(
				vertexBuffer,
				program = programColor3D,
				type = AG.DrawType.TRIANGLES,
				vertexLayout = vertexLayout,
				vertexCount = 6 * 6,
				uniforms = uniformValues.apply {
					this[u_ProjMat] = ctx.projMat
					this[u_ViewMat] = viewMat
					this[u_ModMat] = modelMat
				},
				renderState = rs
			)
		}
	}
}
