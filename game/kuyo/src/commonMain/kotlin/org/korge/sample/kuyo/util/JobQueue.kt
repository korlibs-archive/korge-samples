package org.korge.sample.kuyo.util

import com.soywiz.korio.async.*
import com.soywiz.korio.concurrent.lock.*
import kotlinx.coroutines.*
import kotlin.coroutines.*

class JobQueue(val context: CoroutineContext = EmptyCoroutineContext) {
	private val lock = Lock()
	private val tasks = arrayListOf<suspend () -> Unit>()
	var running = false; private set
	private var currentJob: Job? = null
	val size: Int get() = tasks.size + (if (running) 1 else 0)

	private suspend fun run() {
		running = true
		try {

			while (true) {
				val task = lock { if (tasks.isNotEmpty()) tasks.removeAt(0) else null } ?: break
				val job = launch(context) { task() }
				currentJob = job
				job.join()
				currentJob = null
			}
		} catch (e: Throwable) {
			println(e)
		} finally {
			currentJob = null
			running = false
		}
	}

	/**
	 * Discards all the queued but non running tasks
	 */
	fun discard(): JobQueue {
		lock { tasks.clear() }
		return this
	}

	/**
	 * Discards all the queued tasks and cancels the running one, sending a complete signal.
	 * If complete=true, a tween for example will be set directly to the end step
	 * If complete=false, a tween for example will stop to the current step
	 */
	fun cancel(complete: Boolean = false): JobQueue {
		currentJob?.cancel(CancelException(complete))
		return this
	}

	fun cancelComplete() = cancel(true)

	fun queue(callback: suspend () -> Unit) {
		lock { tasks += callback }
		if (!running) launch(context) { run() }
	}

	fun discard(callback: suspend () -> Unit) {
		discard()
		queue(callback)
	}

	operator fun invoke(callback: suspend () -> Unit) = queue(callback)
}

open class CancelException(val complete: Boolean = false) : kotlinx.coroutines.CancellationException(null)
