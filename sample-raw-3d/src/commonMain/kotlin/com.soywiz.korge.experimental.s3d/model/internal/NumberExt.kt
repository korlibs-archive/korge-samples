package com.soywiz.korge.experimental.s3d.model.internal

import com.soywiz.kmem.*

fun Int.nextMultipleOf(v: Int) = this.nextAlignedTo(v)
fun Long.nextMultipleOf(v: Long) = this.nextAlignedTo(v)

fun Int.prevMultipleOf(v: Int) = this.prevAlignedTo(v)
fun Long.prevMultipleOf(v: Long) = this.prevAlignedTo(v)

