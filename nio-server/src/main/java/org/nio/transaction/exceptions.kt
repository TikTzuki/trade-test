package org.nio.transaction

class InsertTransactionFail(val refId: String) : RuntimeException()

class InsufficientBalance(val refId: String) : RuntimeException()