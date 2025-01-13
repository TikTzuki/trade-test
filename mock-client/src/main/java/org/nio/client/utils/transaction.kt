package org.nio.client.utils

import org.apache.commons.lang3.RandomStringUtils

fun genReferenceId(userId: String): String {
    return "ref-${userId}-${RandomStringUtils.insecure().nextAlphanumeric(10)}-${System.currentTimeMillis()}"
}