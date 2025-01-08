package org.nio.wallet.transaction

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

@Table("transaction")
data class Transaction(
    @PrimaryKeyColumn(type = PARTITIONED)
    val id: String,

    @PrimaryKeyColumn(type = PARTITIONED)
    val accountId: String,

    @PrimaryKeyColumn(type = PARTITIONED)
    val ticketId: String,

    @PrimaryKeyColumn(type = PARTITIONED)
    val shard: Int,

    @PrimaryKeyColumn(type = CLUSTERED, ordering = DESCENDING)
    val timeStamp: Instant,

    val type: TransactionType,

    val refId: String,

    val amount: BigDecimal,

    val balanceAfterWrite: BigDecimal

) {
    companion object {
        fun generateShard(id: String, accountId: String, ticketId: String): Int {
            val hash = abs(id.hashCode() + accountId.hashCode() + ticketId.hashCode())
            return (hash % 9) + 1
        }
    }
}