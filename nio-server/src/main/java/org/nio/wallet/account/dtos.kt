package org.nio.wallet.account

import java.math.BigDecimal

@JvmRecord
data class AccountBalance(
    val balance: BigDecimal,
    val version: Long
)