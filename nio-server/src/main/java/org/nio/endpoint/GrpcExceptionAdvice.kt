package org.nio.endpoint

import com.nio.wallet.grpc.WalletServiceOuterClass.ErrorCode
import com.nio.wallet.grpc.WalletServiceOuterClass.ErrorDetail
import io.grpc.Metadata
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.protobuf.ProtoUtils
import mu.KLogging
import net.devh.boot.grpc.server.advice.GrpcAdvice
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler
import org.nio.transaction.InsertTransactionFail
import org.nio.transaction.InsufficientBalance

@GrpcAdvice
class GrpcExceptionAdvice {
    @GrpcExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(ex: RuntimeException): StatusException {
        return Status.INTERNAL
            .withDescription(ex.localizedMessage)
            .withCause(ex)
            .asException()
    }

    @GrpcExceptionHandler(InsufficientBalance::class)
    fun handle(ex: InsufficientBalance): StatusException {
        val detail = ErrorDetail.newBuilder()
            .setCode(ErrorCode.INSUFFICIENT_BALANCE)
            .apply { ex.message?.let { msg -> setMessage(msg) } }
            .build()
        val metadata = Metadata().apply {
            put(ProtoUtils.keyForProto(ErrorDetail.getDefaultInstance()), detail)
        }
        return Status.INVALID_ARGUMENT.asException(metadata)
    }

    @GrpcExceptionHandler(InsertTransactionFail::class)
    fun handle(ex: InsertTransactionFail): StatusException {
        val detail = ErrorDetail.newBuilder()
            .setCode(ErrorCode.INSERT_TRANSACTION_FAILED)
            .apply { ex.message?.let { msg -> setMessage(msg) } }
            .build()
        val metadata = Metadata().apply {
            put(ProtoUtils.keyForProto(ErrorDetail.getDefaultInstance()), detail)
        }
        return Status.INVALID_ARGUMENT.asException(metadata)
    }

    companion object : KLogging()
}