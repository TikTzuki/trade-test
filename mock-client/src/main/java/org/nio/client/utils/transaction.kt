package org.nio.client.utils

fun genReferenceId(userId: String): String {
    return "ref-${userId}-${System.currentTimeMillis()}"
}