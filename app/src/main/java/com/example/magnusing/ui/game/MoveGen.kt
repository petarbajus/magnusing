package com.example.magnusing.ui.game

private fun rowOf(index: Int) = index / 8
private fun colOf(index: Int) = index % 8
private fun inBounds(r: Int, c: Int) = r in 0..7 && c in 0..7
private fun idx(r: Int, c: Int) = r * 8 + c

fun targetsForPiece(
    from: Int,
    piece: Piece,
    board: List<Piece?>
): Set<Int> {
    return when (piece.type) {
        PieceType.Knight -> knightTargets(from, piece.color, board)
        PieceType.Bishop -> slidingTargets(from, piece.color, board, directions = listOf(
            -1 to -1, -1 to 1, 1 to -1, 1 to 1
        ))
        PieceType.Rook -> slidingTargets(from, piece.color, board, directions = listOf(
            -1 to 0, 1 to 0, 0 to -1, 0 to 1
        ))
        PieceType.Queen -> slidingTargets(from, piece.color, board, directions = listOf(
            -1 to -1, -1 to 1, 1 to -1, 1 to 1,
            -1 to 0, 1 to 0, 0 to -1, 0 to 1
        ))
        PieceType.King -> kingTargets(from, piece.color, board)
        PieceType.Pawn -> pawnTargets(from, piece.color, board)
    }
}

private fun knightTargets(from: Int, side: PieceColor, board: List<Piece?>): Set<Int> {
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
        if (!inBounds(nr, nc)) continue

        val to = idx(nr, nc)
        val dest = board[to]
        if (dest == null || dest.color != side) {
            targets.add(to)
        }
    }
    return targets
}

private fun kingTargets(from: Int, side: PieceColor, board: List<Piece?>): Set<Int> {
    val r = rowOf(from)
    val c = colOf(from)

    val deltas = listOf(
        -1 to -1, -1 to 0, -1 to 1,
        0 to -1,          0 to 1,
        1 to -1,  1 to 0,  1 to 1
    )

    val targets = mutableSetOf<Int>()
    for ((dr, dc) in deltas) {
        val nr = r + dr
        val nc = c + dc
        if (!inBounds(nr, nc)) continue

        val to = idx(nr, nc)
        val dest = board[to]
        if (dest == null || dest.color != side) {
            targets.add(to)
        }
    }
    return targets
}

private fun slidingTargets(
    from: Int,
    side: PieceColor,
    board: List<Piece?>,
    directions: List<Pair<Int, Int>>
): Set<Int> {
    val r0 = rowOf(from)
    val c0 = colOf(from)

    val targets = mutableSetOf<Int>()

    for ((dr, dc) in directions) {
        var r = r0 + dr
        var c = c0 + dc

        while (inBounds(r, c)) {
            val to = idx(r, c)
            val dest = board[to]

            if (dest == null) {
                targets.add(to)
            } else {
                // Blocked: can capture opponent, cannot capture own
                if (dest.color != side) targets.add(to)
                break
            }

            r += dr
            c += dc
        }
    }

    return targets
}

private fun pawnTargets(from: Int, side: PieceColor, board: List<Piece?>): Set<Int> {
    val r = rowOf(from)
    val c = colOf(from)

    // Row 0 is top (black home rank). White moves "up" (toward decreasing row).
    val dir = if (side == PieceColor.White) -1 else 1
    val startRow = if (side == PieceColor.White) 6 else 1

    val targets = mutableSetOf<Int>()

    // Forward 1 if empty
    val oneR = r + dir
    if (inBounds(oneR, c)) {
        val oneIdx = idx(oneR, c)
        if (board[oneIdx] == null) {
            targets.add(oneIdx)

            // Forward 2 from start row if both empty
            val twoR = r + 2 * dir
            if (r == startRow && inBounds(twoR, c)) {
                val twoIdx = idx(twoR, c)
                if (board[twoIdx] == null) {
                    targets.add(twoIdx)
                }
            }
        }
    }

    // Captures diagonally
    for (dc in listOf(-1, 1)) {
        val capR = r + dir
        val capC = c + dc
        if (!inBounds(capR, capC)) continue
        val capIdx = idx(capR, capC)
        val dest = board[capIdx]
        if (dest != null && dest.color != side) {
            targets.add(capIdx)
        }
    }

    return targets
}
