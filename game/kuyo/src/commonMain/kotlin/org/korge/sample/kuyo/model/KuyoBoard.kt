package org.korge.sample.kuyo.model

import com.soywiz.kds.*
import com.soywiz.korma.geom.*
import org.korge.sample.kuyo.model.KuyoBoard.Companion.EMPTY

data class KuyoItem(val pos: PointInt, val color: Int)

interface KuyoShape {
    val items: List<KuyoItem>
    fun rotatedLeft(): KuyoShape = rotatedRight().rotatedRight().rotatedRight()
    fun rotatedRight(): KuyoShape
}

data class KuyoShape2(val k1: KuyoItem, val k2: KuyoItem) : KuyoShape {
    constructor(c1: Int, c2: Int) : this(KuyoItem(PointInt(0, 0), c1), KuyoItem(PointInt(0, -1), c2))

    override val items = listOf(k1, k2)
    override fun rotatedRight(): KuyoShape2 {
        return when (k2.pos) {
            PointInt(0, 1) -> KuyoShape2(k1, k2.copy(pos = PointInt(-1, 0)))
            PointInt(-1, 0) -> KuyoShape2(k1, k2.copy(pos = PointInt(0, -1)))
            PointInt(0, -1) -> KuyoShape2(k1, k2.copy(pos = PointInt(+1, 0)))
            PointInt(+1, 0) -> KuyoShape2(k1, k2.copy(pos = PointInt(0, +1)))
            else -> TODO("${k2.pos}")
        }
    }
}

data class KuyoDrop(val pos: PointInt, val shape: KuyoShape) {
    val transformedItems get() = shape.items.map { it.copy(pos = pos + it.pos) }
    fun displaced(delta: PointInt): KuyoDrop = copy(pos = pos + delta)
    fun rotated(direction: Int): KuyoDrop = if (direction < 0) rotatedLeft() else rotatedRight()
    fun rotatedLeft(): KuyoDrop = copy(shape = shape.rotatedLeft())
    fun rotatedRight(): KuyoDrop = copy(shape = shape.rotatedRight())
}

data class KuyoBoard(val board: Array2<Int> = Array2(6, 12) { 0 }) {
    companion object {
        const val EMPTY = 0
        const val BORDER = -1
    }

    val width get() = board.width
    val height get() = board.height
    operator fun get(x: Int, y: Int): Int {
        if ((x !in 0 until width) || (y !in 0 until height)) return BORDER
        return board.tryGet(x, y) ?: EMPTY
    }
    operator fun get(pos: PointInt): Int = get(pos.x, pos.y)
    operator fun set(pos: PointInt, value: Int) = run { board.trySet(pos.x, pos.y, value) }
    fun clone() = KuyoBoard(board.clone())
}

data class KuyoStep<TKuyoTransform : KuyoTransform>(
	val src: KuyoBoard,
	val dst: KuyoBoard,
	val transforms: List<TKuyoTransform>
)

data class ColoredPoint(val pos: PointInt, val color: Int)

interface KuyoTransform {
    data class Move(val src: PointInt, val dst: PointInt, val color: Int) : KuyoTransform {
        val csrc = ColoredPoint(src, color)
        val cdst = ColoredPoint(dst, color)
    }

    data class Swap(val src: ColoredPoint, val dst: ColoredPoint) : KuyoTransform
    data class Explode(val items: List<PointInt>) : KuyoTransform
    data class Place(val item: KuyoItem) : KuyoTransform
}

fun KuyoBoard.swap(p1: PointInt, p2: PointInt): KuyoStep<KuyoTransform.Swap> {
    return kuyoStep { dst, transforms ->
        val c1 = dst[p1]
        val c2 = dst[p2]
        dst[p1] = c2
        dst[p2] = c1
        transforms += KuyoTransform.Swap(ColoredPoint(p1, c1), ColoredPoint(p2, c2))
    }
}

fun <T : KuyoTransform> KuyoBoard.kuyoStep(callback: (dst: KuyoBoard, transforms: ArrayList<T>) -> Unit): KuyoStep<T> {
    val src = this
    val dst = this.clone()
    val transforms = arrayListOf<T>()
    callback(dst, transforms)
    return KuyoStep(src, dst, transforms)
}

fun KuyoBoard.gravity(): KuyoStep<KuyoTransform.Move> {
    val src = this
    val dst = this.clone()
    val transforms = arrayListOf<KuyoTransform.Move>()
    for (x in 0 until width) {
        for (y in height - 1 downTo 0) {
            val posSrc = PointInt(x, y)
            val color = dst[posSrc]
            // We have a chip here
            if (color != EMPTY) {
                // Try moving down as much as possible
                for (y2 in 1 until height + 1 - y) {
                    val posDst = PointInt(x, y + y2)
                    val posDstPrev = PointInt(x, y + y2 - 1)
                    val c = dst[posDst]
                    if (c != EMPTY) {
                        if (posSrc != posDstPrev) {
                            dst[posSrc] = EMPTY
                            dst[posDstPrev] = color
                            transforms += KuyoTransform.Move(posSrc, posDstPrev, color)
                        }
                        break
                    }
                }
            }
        }
    }
    return KuyoStep(src, dst, transforms)
}

fun KuyoBoard.explode(): KuyoStep<KuyoTransform.Explode> {
    val src = this
    val dst = this.clone()
    val transforms = arrayListOf<KuyoTransform.Explode>()
    val explored = hashSetOf<PointInt>()

    fun explore(p: PointInt): List<PointInt> {
        if (p in explored) return listOf()
        explored += p
        val color = dst[p]

        val deltas = listOf(PointInt(-1, 0), PointInt(+1, 0), PointInt(0, -1), PointInt(0, +1))
        val nexts = deltas.map { p + it }

        return listOf(p) + nexts.filter { dst[it] == color }.flatMap { explore(it) }
    }

    for (x in 0 until width) {
        for (y in 0 until height) {
            val p = PointInt(x, y)
            if (p !in explored && dst[p] != EMPTY) {
                val items = explore(p)
                if (items.size >= 4) {
                    transforms += KuyoTransform.Explode(items)
                }
            }
        }
    }

    for (pos in transforms.flatMap { it.items }) {
        dst[pos] = EMPTY
    }

    return KuyoStep(src, dst, transforms)
}

fun KuyoBoard._canPlace(drop: KuyoDrop, placing: Boolean): Boolean {
    for (item in drop.shape.items) {
        val puyoPos = drop.pos + item.pos
        if (placing && puyoPos.y < 0) return false
        if (this[puyoPos] != EMPTY) return false
    }
    return true
}

fun KuyoBoard.canMove(drop: KuyoDrop): Boolean = _canPlace(drop, false)
fun KuyoBoard.canPlace(drop: KuyoDrop): Boolean = _canPlace(drop, true)

fun KuyoBoard.place(drop: KuyoDrop): KuyoStep<KuyoTransform.Place> {
    val src = this
    val dst = this.clone()
    val transforms = arrayListOf<KuyoTransform.Place>()
    for (item in drop.transformedItems) {
        dst[item.pos] = item.color
        transforms += KuyoTransform.Place(item)
    }
    return KuyoStep(src, dst, transforms)
}

fun KuyoDrop.tryMove(delta: PointInt, board: KuyoBoard): KuyoDrop? {
    val newDrop = this.displaced(delta)
    return if (board.canMove(newDrop)) newDrop else null
}

fun KuyoDrop.tryRotate(direction: Int, board: KuyoBoard): KuyoDrop? {
    val deltas = listOf(PointInt(0, 0), PointInt(-1, 0), PointInt(+1, 0), PointInt(0, -1))
    for (delta in deltas) {
        val newDrop = this.displaced(delta).rotated(direction)
        if (board.canMove(newDrop)) return newDrop
    }
    return null
}

fun KuyoDrop.moveOrHold(delta: PointInt, board: KuyoBoard): KuyoDrop = tryMove(delta, board) ?: this
fun KuyoDrop.rotateOrHold(direction: Int, board: KuyoBoard): KuyoDrop = tryRotate(direction, board) ?: this

/////////////// UTILS

fun boardString(vararg lines: String): String = board(*lines).toBoardString()

fun board(vararg lines: String): KuyoBoard {
    fun Char.toId(): Int = when (this) {
        '1' -> 1
        '2' -> 2
        '3' -> 3
        '4' -> 4
        '5' -> 5
        '0', '.' -> 0
        else -> 0
    }

    val board = KuyoBoard(Array2(lines.first().length, lines.size) { 0 })
    for (y in 0 until board.height) {
        for (x in 0 until board.width) {
            board.board[x, y] = lines[y][x].toId()
        }
    }
    return board
}

fun KuyoBoard.toBoardString(): String {
    val out = arrayListOf<String>()
    for (y in 0 until height) {
        var line = ""
        for (x in 0 until width) {
            val v = this[PointInt(x, y)]
            line += when (v) {
                0 -> '.'
                else -> '0' + v
            }
        }
        out += line
    }
    return out.joinToString("\n")
}
