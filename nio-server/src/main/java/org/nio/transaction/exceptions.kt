package org.nio.transaction

class VersionConflict(val refId: String) : RuntimeException()

class InsertTransactionFail(val refId: String) : RuntimeException()

class InsufficientBalance(val refId: String) : RuntimeException()