package com.example.magnusing.ui.game.engine

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class StockfishEngine(private val context: Context) {

    private var process: Process? = null
    private var input: BufferedWriter? = null
    private var output: BufferedReader? = null

    private fun enginePathInNativeLibDir(): String {
        // You packaged the Stockfish *executable* as:
        // app/src/main/jniLibs/arm64-v8a/libstockfish.so
        // so it will be extracted here at install time:
        val libDir = context.applicationInfo.nativeLibraryDir
        return "$libDir/libstockfish.so"
    }

    suspend fun start() = withContext(Dispatchers.IO) {
        if (process != null) return@withContext

        val enginePath = enginePathInNativeLibDir()

        // Usually already executable in nativeLibraryDir, but harmless to ensure.
        try {
            ProcessBuilder("sh", "-c", "chmod 755 '$enginePath'").start().waitFor()
        } catch (_: Exception) {
            // ignore
        }

        process = ProcessBuilder(enginePath)
            .redirectErrorStream(true)
            .start()

        input = BufferedWriter(OutputStreamWriter(process!!.outputStream))
        output = BufferedReader(InputStreamReader(process!!.inputStream))

        send("uci")
        waitFor("uciok")

        send("isready")
        waitFor("readyok")
    }

    suspend fun stop() = withContext(Dispatchers.IO) {
        try { send("quit") } catch (_: Exception) {}
        process?.destroy()
        process = null
        input = null
        output = null
    }

    suspend fun bestMoveFromFen(
        fen: String,
        moveTimeMs: Int = 400
    ): String = withContext(Dispatchers.IO) {

        send("ucinewgame")
        send("isready")
        waitFor("readyok")

        send("position fen $fen")
        send("go movetime $moveTimeMs")

        while (true) {
            val line = output!!.readLine() ?: break
            if (line.startsWith("bestmove ")) {
                return@withContext line
                    .removePrefix("bestmove ")
                    .trim()
                    .split(" ")
                    .first()
            }
        }
        error("Stockfish did not return bestmove")
    }

    private fun send(cmd: String) {
        input!!.apply {
            write(cmd)
            write("\n")
            flush()
        }
    }

    private fun waitFor(token: String) {
        while (true) {
            val line = output!!.readLine() ?: error("Stockfish output ended")
            if (line.trim() == token) return
        }
    }
}
