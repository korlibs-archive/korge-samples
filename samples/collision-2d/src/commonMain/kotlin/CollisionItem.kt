import Config.TILE_SIZE

data class CollisionItem(
	val x: Double,
	val y: Double,
	val boundsTop: Double,
	val boundsBottom: Double,
	val boundsLeft: Double,
	val boundsRight: Double
)

fun IntArray.getCollisionMap(
	mapWidth: Int,
	mapHeight: Int
): Map<Int, IntArray> {
	val originalIntArray = this

	return buildMap {
		for (rowItem in 0 until mapHeight) {
			val columnItems = mutableListOf<Int>()
			for (columnItem in 0 until mapWidth) {
				columnItems.add(originalIntArray[rowItem * mapWidth + columnItem])
			}
			put(rowItem, columnItems.toIntArray())
		}
	}
}

fun Map<Int, IntArray>.getCollisionItems(
	startX: Double,
	startY: Double,
): Set<CollisionItem> {

	val collisionItems = mutableSetOf<CollisionItem>()

	apply {
		onEach { entry ->
			entry.value.onEachIndexed { index, tileId ->
				if (tileId != 0) {
					collisionItems.add(
						CollisionItem(
							x = (index * TILE_SIZE) + startX,
							y = (entry.key * TILE_SIZE) + startY,
							boundsTop = (entry.key * TILE_SIZE) + startY,
							boundsBottom = (entry.key * TILE_SIZE + TILE_SIZE) + startY,
							boundsLeft = (index * TILE_SIZE) + startX,
							boundsRight = (index * TILE_SIZE + TILE_SIZE) + startX
						)
					)
				}
			}
		}
	}

	return collisionItems
}
