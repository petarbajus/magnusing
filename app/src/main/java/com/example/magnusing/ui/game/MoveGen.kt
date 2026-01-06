package com.example.magnusing.ui.game

private fun rowOf(index: Int) = index / 8
private fun colOf(index: Int) = index % 8

fun knightTargets(
    from: Int,
    board: List<Piece?>,
    sideToMove: PieceColor
): Set<Int> {
    val r = rowOf(from)
    val c = colOf(from)

    val deltas = listOf(
        -2 to -1, -2 to  1,
        -1 to -2, -1 to  2,
        1 to -2,  1 to  2,
        2 to -1,  2 to  1
    )

    val targets = mutableSetOf<Int>()

    for ((dr, dc) in deltas) {
        val nr = r + dr
        val nc = c + dc
        if (nr !in 0..7 || nc !in 0..7) continue

        val to = nr * 8 + nc
        val destPiece = board[to]

        // Allow move if empty OR occupied by opponent
        if (destPiece == null || destPiece.color != sideToMove) {
            targets.add(to)
        }
    }

    return targets
}
