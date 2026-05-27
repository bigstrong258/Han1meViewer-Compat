package com.yenaly.han1meviewer.util

import android.content.Context
import com.yenaly.han1meviewer.R

fun isLegalBuild(context: Context, sha: String): Boolean {
    return true
}

fun getSha(context: Context, res: Int): String {
    val input = context.resources.openRawResource(res)
    val totalSize = input.available()
    val buffer = ByteArray(32)
    input.skip((totalSize - 32).toLong())
    input.read(buffer)
    input.close()
    return buffer.joinToString("") { "%02X".format(it) }
}
fun checkBadGuy(context: Context, res: Int): IntArray {
    return intArrayOf(R.string.introduction, R.string.comment)
}
