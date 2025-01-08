package org.nio.wallet.account

import org.springframework.data.cassandra.core.cql.PrimaryKeyType.PARTITIONED
import org.springframework.data.cassandra.core.mapping.CassandraType
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import java.math.BigDecimal

const val ACCOUNT_TABLE = "account"

@Table(ACCOUNT_TABLE)
data class Account(
    @PrimaryKeyColumn(type = PARTITIONED)
    @CassandraType(type = CassandraType.Name.TEXT)
    val id: String,
    var balance: BigDecimal
) {
    fun depositBalance(amount: BigDecimal): Account {
        if (amount < BigDecimal.ZERO)
            throw InvalidAmountException(String.format("invalid amount %s for bank account: %s", amount, id))
        balance = balance.add(amount)
        return this
    }

    fun withdrawBalance(amount: BigDecimal): Account {
        val currentBalance = balance.subtract(amount)
        if (amount < BigDecimal.ZERO || currentBalance < BigDecimal.ZERO)
            throw InvalidAmountException(String.format("invalid amount %s for bank account: %s", amount, id))
        balance = balance.subtract(amount)
        return this
    }
}