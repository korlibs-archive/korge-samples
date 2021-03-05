import com.soywiz.korge.*
import com.soywiz.korim.color.*
import spaceredux.*

suspend fun main() = Korge(width = 512, height = 512, bgcolor = Colors["#2b2b2b"]) {
    //spaceShooterRedux.putBackground(SpaceShooterReduxBackground.Type.BLUE, speedX = 2.0, speedY = 1.0)
    spaceShooterRedux.putDemoCode()
}
