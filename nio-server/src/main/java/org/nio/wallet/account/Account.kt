package org.nio.wallet.account

import org.springframework.data.annotation.Version
import org.springframework.data.cassandra.core.cql.PrimaryKeyType.PARTITIONED
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import java.math.BigDecimal

const val ACCOUNT_TABLE = "account"

@Table(ACCOUNT_TABLE)
data class Account(
    @PrimaryKeyColumn(type = PARTITIONED)
    var id: String?,
    var balance: BigDecimal,
    @Version
    var version: Long
)