package org.tik.bank

import com.tik.grpc.bank.service.Bank.BankAccountData
import com.tik.grpc.bank.service.Bank.CreateBankAccountRequest
import java.util.UUID.randomUUID

fun BankAccount.toGrpc(): BankAccountData {
    return BankAccountData.newBuilder()
        .setId(this.id.toString())
        .build()
}

fun CreateBankAccountRequest.of(): BankAccount {
    return BankAccount(randomUUID())
}