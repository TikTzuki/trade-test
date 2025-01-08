package org.nio.wallet.account

import org.springframework.data.cassandra.core.cql.PrimaryKeyType.PARTITIONED
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import java.math.BigDecimal
import java.util.*


@Table("bankaccount")
data class BankAccount(
    @PrimaryKeyColumn(type = PARTITIONED)
    val id: UUID,
    var balance: BigDecimal
) {
    fun depositBalance(amount: BigDecimal): BankAccount {
        if (amount < BigDecimal.ZERO)
            throw InvalidAmountException(String.format("invalid amount %s for bank account: %s", amount, id))
        balance = balance.add(amount)
        return this
    }

    fun withdrawBalance(amount: BigDecimal): BankAccount {
        val currentBalance = balance.subtract(amount)
        if (amount < BigDecimal.ZERO || currentBalance < BigDecimal.ZERO)
            throw InvalidAmountException(String.format("invalid amount %s for bank account: %s", amount, id))
        balance = balance.subtract(amount)
        return this
    }
}