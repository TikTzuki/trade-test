package org.nio.wallet.endpoint

import com.nio.wallet.grpc.WalletServiceOuterClass.AccountData
import com.nio.wallet.grpc.WalletServiceOuterClass.CreateAccountRequest
import org.nio.wallet.account.Account
import java.math.BigDecimal
import java.util.UUID.randomUUID

fun Account.toGrpc(): AccountData {
    return AccountData.newBuilder()
        .setId(this.id.toString())
        .build()
}

fun CreateAccountRequest.of(): Account {
    return Account(
        id = randomUUID().toString(),
        balance = BigDecimal.ZERO
    )
}