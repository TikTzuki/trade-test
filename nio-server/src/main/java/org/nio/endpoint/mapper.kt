package org.nio.endpoint

import com.nio.wallet.grpc.WalletServiceOuterClass.AccountData
import com.nio.wallet.grpc.WalletServiceOuterClass.CreateAccountRequest
import org.nio.account.Account
import java.util.UUID.randomUUID

fun Account.toGrpc(): AccountData {
    return AccountData.newBuilder()
        .setId(this.id.toString())
        .setBalance(this.balance.toString())
        .build()
}

fun CreateAccountRequest.of(): Account {
    return Account(
        id = randomUUID().toString(),
        balance = this.balance.toBigDecimal(),
        version = 1
    )
}