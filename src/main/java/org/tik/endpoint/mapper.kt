package org.tik.endpoint

import com.tik.grpc.bank.service.Bank.BankAccountData
import com.tik.grpc.bank.service.Bank.CreateBankAccountRequest
import org.tik.bank.BankAccount
import java.math.BigDecimal
import java.util.UUID.randomUUID

fun BankAccount.toGrpc(): BankAccountData {
    return BankAccountData.newBuilder()
        .setId(this.id.toString())
        .build()
}

fun CreateBankAccountRequest.of(): BankAccount {
    return BankAccount(
        randomUUID(),
        balance = BigDecimal.ZERO
    )
}