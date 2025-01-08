package org.nio.wallet.endpoint

import com.nio.wallet.grpc.WalletServiceOuterClass.AccountData
import com.nio.wallet.grpc.WalletServiceOuterClass.CreateAccountRequest
import org.nio.wallet.account.BankAccount
import java.math.BigDecimal
import java.util.UUID.randomUUID

fun BankAccount.toGrpc(): AccountData {
    return AccountData.newBuilder()
        .setId(this.id.toString())
        .build()
}

fun CreateAccountRequest.of(): BankAccount {
    return BankAccount(
        randomUUID(),
        balance = BigDecimal.ZERO
    )
}