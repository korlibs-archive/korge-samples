package com.soywiz.korge.samples.minesweeper

import com.soywiz.klock.*
import com.soywiz.korau.sound.*
import com.soywiz.korge.html.*
import com.soywiz.korge.render.*
import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.*
import com.soywiz.korio.lang.*
import com.soywiz.korma.random.*
import kotlin.random.*

// Proceso que se encarga del tablero
class Board(
	parent: Container,
	val imageset: BmpSlice,
	val imagenes: List<BmpSlice>,
	val click: NativeSound,
	val boom: NativeSound,
	// Se establecen el ancho, el alto y la cantidad de minas
	// Características del tablero: ancho, alto, cantidad de minas
	var bwidth: Int,
	var bheight: Int,
	var minas: Int
) : Process(parent) {
	// Matriz con el tablero
	var board: Array<IntArray> = arrayOf()
	// Matriz de máscara (indica que partes del tablero están destapadas)
	var mask: Array<BooleanArray> = arrayOf()
	// Matriz de marcado (indica que partes del tablero están marcadas como "posible mina") (click derecho)
	var mark: Array<BooleanArray> = arrayOf()

	// Variables utilizadas para el contador
	var stime: DateTime = DateTime.EPOCH
	var tstop: DateTime = DateTime.EPOCH
	var timeText: Text

	var lastx: Int = 0
	var lasty: Int = 0

	// Constructor del proceso en el cual se le pasan el ancho, el alto y la cantidad de minas
	init {
		// Se crea el texto del contador
		//timeText = new Text("", 50, 50, Text.Align.center, Text.Align.middle, Color.white, new Font("Arial", 40));
		//timeText = Text("", 50, 50, Text.Align.center, Text.Align.middle, Color.white, Font.fromResource("font.ttf", 40));
		val FONT_HEIGHT = 32.0
		timeText = text("", textSize = FONT_HEIGHT).xy((bwidth * imageset.height) / 2, -FONT_HEIGHT).apply {
			format = Html.Format(align = Html.Alignment.CENTER, size = FONT_HEIGHT.toInt())
			//format = Html.Format(align = Html.Alignment.CENTER, face = defaultUIFont)
		}
		// Se pinta el contador como hijo del tablero
		//timeText.group.z = this;
		// Y se actualiza su texto
		updateTimeText()

		// Se centra el tablero en la pantalla
		x = Screen.width / 2 - (bwidth * imageset.height) / 2
		y = Screen.height / 2 - (bheight * imageset.height - 10 - FONT_HEIGHT) / 2

		// Se establecen algunas características del texto, posición, borde y sombra
		//timeText.shadow = 5;
		//timeText.border = 1;

		// Se reinicia el tablero
		clear()
	}

	// Destructor, aquí se quita el texto cuando se borra el tablero
	override fun onDestroy() {
		timeText.removeFromParent()
	}

	// Devuelve el tiempo actual (en milisegundos)
	val time: DateTime get() = DateTime.now()

	// Resetea el contador
	fun resetTimer() {
		stime = time
		tstop = DateTime.EPOCH
	}

	// Para el contador
	fun stopTimer() {
		tstop = time
	}

	// Devuelve el tiempo ha pasado en segundos desde que se inició el contador
	val elapsed: Int
		get() = run {
			var ctime = time
			if (tstop != DateTime.EPOCH) ctime = tstop
			return (ctime - stime).seconds.toInt()
		}

	// Actualiza el texto del contador con el formato %02d:%02d  MM:SS
	fun updateTimeText() {
		timeText.text = "%02d:%02d".format(elapsed / 60, elapsed % 60)
	}

	// Función que se encarga de borrar el tablero y crear uno nuevo
	fun clear() {
		// Ahora que vamos a borrar un nuevo tablero (y que vamos a crear una nueva partida)
		// reiniciamos el contador
		resetTimer()

		// Creamos las matrices con el tablero, la máscara de visión y la máscara de marcado (de posibles minas)
		board = Array(bheight) { IntArray(bwidth) }
		mask = Array(bheight) { BooleanArray(bwidth) }
		mark = Array(bheight) { BooleanArray(bwidth) }

		// Comprobamos que no se intenten colocar mas minas que posiciones hay, evitando así un bucle infinito
		// en realidad solo se colocan como mucho una cantidad de minas igual a las posiciones del tablero - 1
		// para que pueda haber partida (si no se ganaría directamente)
		if (minas > bwidth * bheight - 1) minas = bwidth * bheight - 1

		// Ahora procederemos a colocar las minas en el tablero
		for (n in 0 until minas) {
			// Declaramos px, py que utilizaremos para almacenar las posiciones temporales de la mina
			var px: Int = 0
			var py: Int = 0
			do {
				// Obtenemos una posible posición de la mina
				px = Random[0, bwidth - 1]
				py = Random[0, bheight - 1]
				// Comprobamos si en esa posición hay una mina y estaremos buscando posiciones hasta
				// que en esa posición no haya mina
			} while (board[py][px] == 10)

			// Ahora que sabemos que en esa posición no hay mina, colocamos una
			board[py][px] = 10
		}

		// Ahora que hemos colocado las minas, vamos a colocar los números alrededor de ellas
		// Esta es una parte interesante del buscaminas, aquí se colcan los numeros alrededor de las minas

		// Nos recorremos el tablero entero
		for (y in 0 until bheight) {
			for (x in 0 until bwidth) {
				// Comprobamos que en esa posición no haya mina, si hay mina, "pasamos", hacemos un continue y seguimos a la siguiente posición
				// sin ejecutar lo que viene después
				if (board[y][x] == 10) continue

				// Ahora vamos a contar las minas que hay alrededor de esta posición (ya que en esta posición no hay mina y es posible que tengamos
				// que poner un número si tiene alguna mina contigua)
				var count = 0
				// Recorremos con x1 € [-1,1], y1 € [-1, 1]
				for (y1 in -1..+1) {
					for (x1 in -1..+1) {
						// Ahora x + x1 y y + y1 tomaran posiciones de la matriz contiguas a la posición actual
						// empezando por x - 1, y - 1 para acabar en x + 1, y + 1
						// Comprobamos que la posición esté dentro de la matriz, ya que por ejemplo en la posición 0
						// la posición 0 - 1, 0 - 1, sería la -1, -1, que no está dentro de la matriz y si no está dentro
						// de los límites de la matriz, pasamos
						if (!in_bounds(x + x1, y + y1)) continue
						// Si en esta posición contigua hay una mina entonces incrementamos el contador
						if (board[y + y1][x + x1] == 10) count++
					}
				}

				// Introducimos en el tablero la nueva imagen (puesto que la imagen con 0 posiciones es la 1 y las siguientes
				// son 1, 2, 3, 4, 5, 6, 7, 8) ponemos la imagen correspondiente a count + 1
				board[y][x] = count + 1
			}
		}

		// Ahora ya tenemos el tablero preparado
	}

	// Indica si una posición está dentro de la matriz
	fun in_bounds(px: Int, py: Int): Boolean {
		// Si la posición es negativa o si la posición está mas a la derecha del ancho del tablero, devuelve false (no está dentro)
		if (px < 0 || px >= bwidth) return false
		// Si ocurre lo mismo con la posición y, también devolvemos false
		if (py < 0 || py >= bheight) return false
		// Si no hemos devuelto ya false, quiere decir que la posición si que está dentro del tablero, así que devolvemos true
		return true
	}

	var fillpos = 0

	// Rellena una posición (recursivamente; la forma mas clara y sencilla)
	suspend fun fill(px: Int, py: Int) {
		if (!in_bounds(px, py)) return
		if (mask[py][px] || mark[py][px]) return
		mask[py][px] = true

		if (fillpos % 7 == 0) audio.play(click)
		frame()
		fillpos++

		if (board[py][px] != 1) return
		fill(px - 1, py)
		fill(px + 1, py)
		fill(px, py - 1)
		fill(px, py + 1)
		fill(px - 1, py - 1)
		fill(px + 1, py + 1)
		fill(px + 1, py - 1)
		fill(px - 1, py + 1)
	}

	suspend fun show_board_lose() {
		// Subfunción de show_board_lose que se encarga de
		// desenmascarar una posición despues de comprobar
		// si es correcta
		fun unmask(x: Int, y: Int): Boolean {
			if (!in_bounds(x, y)) return false
			mask[y][x] = true
			return true
		}

		// Propagación con forma de diamante
		var dist = 0
		while (true) {
			var drawing = false

			for (n in 0..dist) {
				if (unmask(lastx - n + dist, lasty - n)) drawing = true
				if (unmask(lastx + n - dist, lasty - n)) drawing = true
				if (unmask(lastx - n + dist, lasty + n)) drawing = true
				if (unmask(lastx + n - dist, lasty + n)) drawing = true
			}

			if (!drawing) break

			dist++
			frame()
			//if (dist >= max(width * 2, height * 2)) break;
		}
	}

	suspend fun show_board_win() {
		for (y in 0 until bheight) {
			for (x in 0 until bwidth) {
				if (board[y][x] == 10) {
					mask[y][x] = false
					mark[y][x] = true
					frame()
				} else {
					mask[y][x] = true
				}
			}
		}
	}

	suspend fun check(px: Int, py: Int): Boolean {
		if (!in_bounds(px, py)) return false

		// Guardamos la última posición en la que hicimos click
		lastx = px; lasty = py

		// Estamos ante una mina
		if (board[py][px] == 10) return true

		// Estamos ante una casilla vacía
		if (board[py][px] == 1) {
			fps = 140.0
			fillpos = 0
			fill(px, py)
			fps = 60.0
			return false
		}

		if (!mask[py][px]) {
			mask[py][px] = true
			audio.play(click)
		}

		return false
	}

	// Comprueba si el tablero está en un estado en el cual podemos dar por ganada la partida
	fun check_win(): Boolean {
		var count = 0
		for (y in 0 until bheight) {
			for (x in 0 until bwidth) {
				if (mask[y][x]) count++
			}
		}

		return (count == bwidth * bheight - minas)
	}

	// La acción principal redirecciona a la acción de juego
	override suspend fun main() = action(::play)

	// La acción principal de juego que se encarga de gestionar los clicks de ratón
	suspend fun play() {
		while (true) {
			//println("Mouse.x: ${Mouse.x}, x=$x")
			if (Mouse.x >= x && Mouse.x < x + bwidth * imageset.height) {
				if (Mouse.y >= y && Mouse.y < y + bheight * imageset.height) {
					val px = ((Mouse.x - x) / imageset.height).toInt()
					val py = ((Mouse.y - y) / imageset.height).toInt()

					if (Mouse.released[0]) {
						if (!mark[py][px]) {
							if (check(px, py)) {
								action(::lose)
							} else if (check_win()) {
								action(::win)
							}
						}
					} else if (Mouse.released[1] || Mouse.released[2]) {
						mark[py][px] = !mark[py][px]
					}
				}
			}

			frame()
		}
	}

	// Acción del tablero que ocurre cuando el jugador ha perdido
	suspend fun lose() {
		audio.play(boom, 0)
		stopTimer()
		show_board_lose()

		while (true) {
			if (Mouse.left || Mouse.right) {
				clear()
				for (n in 0 until 10) frame()
				action(::play)
			}
			frame()
		}
	}

	// Acción del tablero que ocurre cuando el jugador ha ganado
	suspend fun win() {
		stopTimer()
		show_board_win()

		while (true) {
			if (Mouse.left || Mouse.right) {
				clear()
				for (n in 0 until 10) frame()
				action(::play)
			}
			frame()
		}
	}

	val images = Array(bheight) { py ->
		Array(bwidth) { px ->
			image(Bitmaps.transparent).xy(px * imageset.height, py * imageset.height).scale(0.9)
		}
	}

	override fun renderInternal(ctx: RenderContext) {
		for (py in 0 until bheight) {
			for (px in 0 until bwidth) {
				val image = if (!mask[py][px]) {
					imagenes[if (mark[py][px]) 11 else 0]
				} else {
					imagenes[board[py][px]]
				}

				images[py][px].texture = image
			}
		}

		super.renderInternal(ctx)
	}
}
