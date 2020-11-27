package org.korge.sample.kuyo

/*
import com.soywiz.korge.bitmapfont.*
import com.soywiz.korge.scene.*
import com.soywiz.korge.view.*
import kotlin.coroutines.*

class MainMenuScene : Scene() {
    val queue = JobQueue()

    override suspend fun Container.sceneInit() {
        val font = resourcesRoot["font1.fnt"].readBitmapFont()
        val endless = Text("ENDLESS", 96.0, font = font).apply {
            position(448, 64 + 128 * 0)
            //anchor(0.5, 0.5)
            //elasticButton(queue)
        }
        val localCoOp = Text(font, "LOCAL CO-OP", 96).apply {
            position(448, 64 + 128 * 1)
            //anchor(0.5, 0.5)
            //elasticButton(queue)
        }
        val credits = Text(font, "CREDITS", 96).apply {
            position(448, 64 + 128 * 2)
            //anchor(0.5, 0.5)
            //elasticButton(queue)
        }
        this.root += endless
        this.root += localCoOp
        this.root += credits

        launch(coroutineContext) {
            val result = optionSelector(
                root, listOf(
                    endless to "endless",
                    localCoOp to "localcoop",
                    credits to "credits"
                )
            )
            println(result)
            changeSceneTo(KuyoScene())
        }
    }
}

suspend fun optionSelector(events: Container, options: List<Pair<View, String>>) = suspendCoroutine<String> { c ->
    val queue: JobQueue = JobQueue()
    var index = 0

    fun activate(aindex: Int) {
        //queue.cancel().queue {
        queue.queue {

            parallel {
                for ((index, option) in options.withIndex()) {
                    val view = option.first
                    sequence {
                        //println("aaa: $index, $aindex")
                        if (index == aindex) {
                            view.tween(view::scale[1.2], time = 0.1, easing = Easing.ELASTIC_EASE_OUT)
                        } else {
                            view.tween(view::scale[1.0], time = 0.1, easing = Easing.ELASTIC_EASE_OUT)
                        }
                    }
                }
            }
        }
    }

    fun move(delta: Int) {
        val oldIndex = index
        val newIndex = (index + delta) umod options.size
        //println("$oldIndex -> $newIndex")
        //deactivate(options[oldIndex].first)
        index = newIndex
        activate(newIndex)
    }

    events.apply {
        val view = View()
        addChild(view)
        view.keys {
            down(Key.UP) { move(-1) }
            down(Key.DOWN) { move(+1) }
            down(Key.LEFT) { move(-1) }
            down(Key.RIGHT) { move(+1) }
            down(Key.ENTER) {
                removeChild(view)
                c.resume(options[index].second)
            }
        }
    }
    activate(0)
}

fun View.elasticButton(queue: JobQueue) {
    mouse {
        over { queue.discard { tween(::scale[2.0], time = 0.3, easing = Easing.ELASTIC_EASE_OUT) } }
        out { queue.discard { tween(::scale[1.0], time = 0.3, easing = Easing.ELASTIC_EASE_OUT) } }
    }
}
*/
