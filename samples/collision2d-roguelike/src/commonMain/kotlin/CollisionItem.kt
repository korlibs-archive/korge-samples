import Config.TILE_SIZE
import com.soywiz.kds.IntArray2

data class CollisionItem(
	val x: Double,
	val y: Double,
	val boundsTop: Double,
	val boundsBottom: Double,
	val boundsLeft: Double,
	val boundsRight: Double
)

fun IntArray2.getCollisionItems(
	startX: Double,
	startY: Double,
): Set<CollisionItem> {

	val collisionItems = mutableSetOf<CollisionItem>()

	data.indices.filter { data[it] != 0 }.map { Pair(it % width, it / width) }
		.onEach {
			collisionItems.add(
				CollisionItem(
					x = (it.first * TILE_SIZE) + startX,
					y = (it.second * TILE_SIZE) + startY,
					boundsTop = (it.second * TILE_SIZE) + startY,
					boundsBottom = (it.second * TILE_SIZE + TILE_SIZE) + startY,
					boundsLeft = (it.first * TILE_SIZE) + startX,
					boundsRight = (it.first * TILE_SIZE + TILE_SIZE) + startX
				)
			)
		}

	return collisionItems
}
