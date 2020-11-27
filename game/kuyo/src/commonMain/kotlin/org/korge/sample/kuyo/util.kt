package org.korge.sample.kuyo

/*
import com.soywiz.korge.view.*
import com.soywiz.korio.error.*
import kotlin.coroutines.*

class ParallelJob {
    @PublishedApi
    internal val jobs = arrayListOf<suspend () -> Unit>()

    fun sequence(job: suspend () -> Unit) {
        jobs += job
    }
}

suspend inline fun parallel(callback: ParallelJob.() -> Unit) {
    val pj = ParallelJob()
    callback(pj)
    parallel(*pj.jobs.toTypedArray())
}

fun JobQueue.discard(callback: suspend () -> Unit) = discard().queue(callback)

fun View.timer(time: Double, callback: (TimerComponent) -> Unit): TimerComponent {
    val component = TimerComponent(this, time, callback)
    addComponent(component)
    return component
}

suspend fun View.delay(time: Double) = suspendCoroutine<Unit> { c ->
    timer(time) {
        it.setTime()
        c.resume(Unit)
    }
}

suspend fun JobQueue.await() = suspendCoroutine<Unit> { c ->
    queue { c.resume(Unit) }
}
*/
