package org.nio.wallet.transaction

import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import java.math.BigDecimal
import java.time.Instant

enum class TransactionType {
    DEPOSIT,
    WITHDRAWAL
}

@Table("transaction")
data class Transaction(
    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
    val refId: String,

    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
    val timeStamp: Instant,

    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
    val accountId: String,

    @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED)
    val shard: Int,

    @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED)
    val type: TransactionType,

    val amount: BigDecimal,

    )