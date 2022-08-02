import Config.TILE_SIZE
import com.soywiz.klock.timesPerSecond
import com.soywiz.korev.Key
import com.soywiz.korge.Korge
import com.soywiz.korge.tiled.TiledMapView
import com.soywiz.korge.tiled.tiledMapView
import com.soywiz.korge.view.*
import com.soywiz.korge.view.camera.CameraContainer
import com.soywiz.korge.view.camera.cameraContainer
import com.soywiz.korge.view.tiles.*
import com.soywiz.korim.color.Colors
import com.soywiz.korim.tiles.tiled.*
import com.soywiz.korio.file.std.resourcesVfs

suspend fun main() = Korge(width = 512, height = 512) {

	val collisionGroup = mutableSetOf<CollisionItem>()
	var mainCameraContainer: CameraContainer? = null
	var tiledMapView: TiledMapView? = null
	var player: SolidRect? = null

	stage.apply {
		fixedSizeContainer(scaledWidth, scaledHeight, clip = true) {
			position(0, 0)

			mainCameraContainer = cameraContainer(scaledWidth, scaledHeight) {
				tiledMapView = tiledMapView(resourcesVfs["tilemap.tmx"].readTiledMap(), smoothing = false) {
					player = solidRect(1 * TILE_SIZE, 1 * TILE_SIZE, color = Colors.DARKRED) {
						addChild(this)
						position(1 * TILE_SIZE, 1 * TILE_SIZE)
					}
				}
			}
		}
	}

	addFixedUpdater(stage.gameWindow.fps.timesPerSecond, false, 1) {

		collisionGroup.clear()
		// you can use this to check whether your map contains all the required layers
		tiledMapView["collision"].first
			.also { layer ->
				if (layer is DummyView) {
					views.gameWindow.close()
					throw Exception("Check your collision layer lookup string.")
				} else collisionGroup.addAll(
					(layer as BaseTileMap).intMap.getCollisionItems(
						layer.globalX,
						layer.globalY
					)
				)
			}

		player?.apply {
			when {
				keys.justPressed(Key.UP) -> if (collisionGroup.none { item ->
						PlayerUtils.handleInputKeyUp(
							this,
							item
						)
					}) {
					y -= TILE_SIZE
				}
				keys.justPressed(Key.DOWN) -> if (collisionGroup.none { item ->
						PlayerUtils.handleInputKeyDown(
							this,
							item
						)
					}) {
					y += TILE_SIZE
				}
				keys.justPressed(Key.LEFT) -> if (collisionGroup.none { item ->
						PlayerUtils.handleInputKeyLeft(
							this,
							item
						)
					}) {
					x -= TILE_SIZE
				}
				keys.justPressed(Key.RIGHT) -> if (collisionGroup.none { item ->
						PlayerUtils.handleInputKeyRight(
							this,
							item
						)
					}) {
					x += TILE_SIZE
				}
			}
			mainCameraContainer?.follow(this)
		}

		if (stage.input.keys.justPressed(Key.ESCAPE)) {
			gameWindow.close()
		}
	}
}

object PlayerUtils {

	fun handleInputKeyUp(player: SolidRect, item: CollisionItem) = (
		item.boundsBottom >= player.globalBounds.top
			&& item.boundsTop <= player.globalBounds.y
			&& item.x <= player.globalBounds.x
			&& item.boundsRight > player.globalBounds.x
		)

	fun handleInputKeyDown(player: SolidRect, item: CollisionItem) = (
		item.boundsTop <= player.globalBounds.bottom
			&& item.y >= player.globalBounds.y
			&& item.x <= player.globalBounds.x
			&& item.boundsRight > player.globalBounds.x)

	fun handleInputKeyLeft(player: SolidRect, item: CollisionItem) = (
		item.boundsRight >= player.globalBounds.x
			&& item.boundsLeft <= player.globalBounds.x
			&& item.y <= player.globalBounds.y
			&& item.boundsBottom > player.globalBounds.y)

	fun handleInputKeyRight(player: SolidRect, item: CollisionItem) = (
		item.boundsLeft <= player.globalBounds.right
			&& item.x >= player.globalBounds.x
			&& item.y <= player.globalBounds.y
			&& item.boundsBottom > player.globalBounds.y)
}

object Config {

	const val TILE_SIZE = 32
}
