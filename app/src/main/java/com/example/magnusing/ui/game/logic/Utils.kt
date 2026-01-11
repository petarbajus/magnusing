package com.example.magnusing.ui.game.logic

fun rowOf(i: Int) = i / 8
fun colOf(i: Int) = i % 8
fun idx(r: Int, c: Int) = r * 8 + c
fun inBounds(r: Int, c: Int) = r in 0..7 && c in 0..7