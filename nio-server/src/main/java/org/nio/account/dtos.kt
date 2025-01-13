package org.nio.account

import java.math.BigDecimal

@JvmRecord
data class AccountBalance(
    val balance: BigDecimal,
    val version: Long
)