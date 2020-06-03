import com.soywiz.korev.Key
import com.soywiz.korge.input.keys
import com.soywiz.korge.input.onClick
import com.soywiz.korge.scene.Scene
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.text
import com.soywiz.korge.ui.*
import com.soywiz.korge.view.position
import com.soywiz.korim.color.Colors

class MenuScene() : Scene() {
    suspend override fun Container.sceneInit() {
        // set a background color
        views.clearColor = Colors.BLACK

        // Add a text to show the name of the game
        var gameNameText = text("Super Pong Bros II") {
            position(views.actualVirtualWidth/2 - 128, views.actualVirtualHeight/2 - 128)
        }

        var playButton = textButton(256.0, 32.0) {
            text = "Play"
            position(views.actualVirtualWidth/2 - 128, views.actualVirtualHeight/2 - 64)
            onClick {
                  sceneContainer.changeToAsync<PlayScene>()
            }
        }
        var exitButton = textButton(256.0, 32.0) {
            text = "Exit"
            position(views.actualVirtualWidth/2 - 128, views.actualVirtualHeight/2)
            onClick {
                  views.gameWindow.close()
            }
        }
    }

}
