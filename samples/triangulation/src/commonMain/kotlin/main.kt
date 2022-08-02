import com.soywiz.korge.*
import com.soywiz.korge.input.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*
import com.soywiz.korim.vector.*
import com.soywiz.korim.vector.renderer.*
import com.soywiz.korma.geom.*
import com.soywiz.korma.geom.shape.*
import com.soywiz.korma.geom.vector.*
import com.soywiz.korma.triangle.triangulate.*
import com.soywiz.korma.triangle.poly2tri.*

suspend fun main() = Korge(width = 512, height = 512) {
	val stage = this
	text("Add Points by clicking with the mouse", 14.0).position(5.0, 5.0)
	graphics {
		val graphics = it
		graphics.renderer = GraphicsRenderer.CPU
		it.position(100, 100)

		val _points = arrayListOf<Point>()
		val pointLists = arrayListOf<List<Point>>()

		var additionalPoint: Point? = null

		fun repaint(finished: Boolean) {
			clear()
			/*
			val path = VectorPath {
				rect(0, 0, 100, 100)
				rect(25, 25, 50, 50)
			}
			 */

			val edges = _points + listOfNotNull(additionalPoint)

			for (point in edges) {
				fill(Colors.RED) {
					circle(point.x, point.y, 3.0)
				}
			}

			if (finished) {
				println("Points: $_points")
			}

			if (_points.size >= 3 || pointLists.isNotEmpty()) {
				stroke(Colors.GREEN, StrokeInfo(thickness = 1.0)) {
					val path = buildVectorPath {
						val pl: List<List<Point>> = pointLists + listOf(_points)
						for (points in pl) {
							var first = true
							for (p in points) {
								if (first) {
									first = false
									moveTo(p)
								} else {
									lineTo(p)
								}
							}
							close()
						}
					}

					for (triangle in path.triangulateSafe()) {
						val p0 = Point(triangle.p0)
						val p1 = Point(triangle.p1)
						val p2 = Point(triangle.p2)
						line(p0, p1)
						line(p1, p2)
						line(p2, p0)
					}
				}
			}

			for (n in 0 until edges.size - 1) {
				val e0 = Point(edges[n])
				val e1 = Point(edges[n + 1])
				val last = n == edges.size - 2
				stroke(if (last) Colors.RED else Colors.BLUE, StrokeInfo(thickness = 2.0)) {
					line(e0, e1)
				}
			}
		}

		stage.mouse {
			click {
				if (it.button.isRight) {
					pointLists.add(_points.toList())
					_points.clear()
				} else {
					_points.add(graphics.localMouseXY(views))
					repaint(finished = true)
				}
				//println("CLICK")
			}

			onMove {
				additionalPoint = graphics.localMouseXY(views)
				repaint(finished = false)
			}
		}

		repaint(finished = true)
	}
}
