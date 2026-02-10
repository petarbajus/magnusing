import kotlin.math.pow
import kotlin.math.roundToInt

data class EloUpdate(val newElo: Int, val delta: Int)

fun eloUpdate(
    playerElo: Int,
    opponentElo: Int,
    score: Double,     // win=1.0, draw=0.5, loss=0.0
    kFactor: Int = 32
): EloUpdate {
    val expected = 1.0 / (1.0 + 10.0.pow((opponentElo - playerElo) / 400.0))
    val delta = (kFactor * (score - expected)).roundToInt()
    return EloUpdate(playerElo + delta, delta)
}
