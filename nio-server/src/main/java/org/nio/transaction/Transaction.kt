package org.nio.transaction

import org.springframework.data.cassandra.core.cql.Ordering.DESCENDING
import org.springframework.data.cassandra.core.cql.PrimaryKeyType.CLUSTERED
import org.springframework.data.cassandra.core.cql.PrimaryKeyType.PARTITIONED
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import java.math.BigDecimal
import java.time.Instant
import kotlin.math.abs

enum class TransactionType {
    DEPOSIT,
    WITHDRAW
}

enum class TransactionAction {
    BET,
    WIN,
}

@Table("transaction_by_account")
data class TransactionByAccount(
    @PrimaryKeyColumn(type = PARTITIONED)
    val accountId: String,

    val transactionId: String,

    @PrimaryKeyColumn(type = CLUSTERED, ordering = DESCENDING)
    val timeStamp: Instant,
)

@Table("transaction")
data class Transaction(
    @PrimaryKeyColumn(type = PARTITIONED)
    val id: String,

    @PrimaryKeyColumn(type = CLUSTERED, ordering = DESCENDING)
    val timeStamp: Instant,

    val accountId: String,

    val ticketId: String,

    val type: TransactionType,

    val action: TransactionAction,

    val refId: String,

    val amount: BigDecimal,

    val balanceAfterWrite: BigDecimal,

    var version: Long
) {
    companion object {
        fun generateShard(id: String, accountId: String, ticketId: String): Int {
            val hash = abs(id.hashCode() + accountId.hashCode() + ticketId.hashCode())
            return (hash % 9) + 1
        }
    }

}