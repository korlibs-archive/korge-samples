package mmo.minigames.kuyo

import com.soywiz.korma.geom.*
import org.korge.sample.kuyo.*
import kotlin.test.*

class KuyoModelTest {
    @Test
    fun gravity1() {
        boardShouldBe(
            board(
                ".3.5",
                "1...",
                "...4",
                ".2.."
            ),
            { it.gravity().dst },
            board(
                "....",
                "....",
                ".3.5",
                "12.4"
            )
        )
    }

    @Test
    fun gravity2() {
        boardShouldBe(
            board(
                "1111",
                "....",
                "....",
                "...."
            ),
            { it.gravity().dst },
            board(
                "....",
                "....",
                "....",
                "1111"
            )
        )
    }

    @Test
    fun gravity3() {
        transformsShouldBe(
            board(
                "1111",
                "....",
                "....",
                "...."
            ),
            { it.gravity().transforms },
            "[Move(src=(0, 0), dst=(0, 3), color=1), Move(src=(1, 0), dst=(1, 3), color=1), Move(src=(2, 0), dst=(2, 3), color=1), Move(src=(3, 0), dst=(3, 3), color=1)]"
        )
    }

    @Test
    fun explode1() {
        boardShouldBe(
            board(
                "1111",
                "2222",
                "3331",
                ".111"
            ),
            { it.explode().dst },
            board(
                "....",
                "....",
                "333.",
                "...."
            )
        )
    }

    @Test
    fun explode2() {
        transformsShouldBe(
            board(
                "1111",
                "1...",
                "...1",
                ".111"
            ),
            { it.explode().transforms },
            "[Explode(items=[(0, 0), (1, 0), (2, 0), (3, 0), (0, 1)]), Explode(items=[(1, 3), (2, 3), (3, 3), (3, 2)])]"
        )
    }

    @Test
    fun place1() {
        val drop = KuyoDrop(PointInt(0, 1), KuyoShape2(1, 2))

        boardShouldBe(
            board(
                "....",
                "....",
                "....",
                "...."
            ),
            { it.place(drop).dst },
            board(
                "2...",
                "1...",
                "....",
                "...."
            )
        )
    }

    @Test
    fun place2() {
        val drop = KuyoDrop(PointInt(0, 0), KuyoShape2(1, 2))

        boardShouldBe(
            board(
                "....",
                "....",
                "....",
                "...."
            ),
            { it.place(drop.rotatedRight()).dst },
            board(
                "12..",
                "....",
                "....",
                "...."
            )
        )
    }

    @Test
    fun place3() {
        val drop = KuyoDrop(PointInt(0, 0), KuyoShape2(1, 2))

        boardShouldBe(
            board(
                "....",
                "....",
                "....",
                "...."
            ),
            { it.place(drop.moveOrHold(PointInt(0, +1), it)).dst },
            board(
                "2...",
                "1...",
                "....",
                "...."
            )
        )
    }

    @Test
    fun place4() {
        boardShouldBe(
            board(
                "....",
                "....",
                "....",
                "...."
            ),
            { it.place(KuyoDrop(PointInt(1, 1), KuyoShape2(1, 2))).dst },
            board(
                ".2..",
                ".1..",
                "....",
                "...."
            )
        )
    }

    @Test
    fun swap2() {
        boardShouldBe(
            board(
                "....",
                "....",
                "....",
                "1234"
            ),
            { it.swap(PointInt(1, 3), PointInt(2, 3)).dst },
            board(
                "....",
                "....",
                "....",
                "1324"
            )
        )
        transformsShouldBe(
            board(
                "....",
                "....",
                "....",
                "1234"
            ),
            { it.swap(PointInt(1, 3), PointInt(2, 3)).transforms },
            "[Swap(src=ColoredPoint(pos=(1, 3), color=2), dst=ColoredPoint(pos=(2, 3), color=3))]"
        )
    }

    //@Test
    //fun placeGravity1() {
    //    val drop = KuyoDrop(IPoint(1, 0), KuyoShape2(1, 2))

    //    val board = board(
    //        "....",
    //        "....",
    //        "....",
    //        "...."
    //    )

    //    assertEquals(
    //        "...",
    //        //board.place(drop).dst.gravity().transforms.toString()
    //        board.place(drop).dst.gravity().dst.toBoardString()
    //    )
    //}
}

fun boardShouldBe(src: KuyoBoard, transform: (KuyoBoard) -> KuyoBoard, dst: KuyoBoard) {
    assertEquals(dst.toBoardString(), transform(src).toBoardString())
}

fun transformsShouldBe(src: KuyoBoard, transform: (KuyoBoard) -> List<KuyoTransform>, dst: String) {
    assertEquals(dst, transform(src).toString().simplifyVectorToString())
}

private fun String.simplifyVectorToString() = this.replace(Regex("IVector2Int\\(x=(\\d+), y=(\\d+)\\)"), "($1, $2)")
