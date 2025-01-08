package org.nio.client

import com.nio.wallet.grpc.ReactorWalletServiceGrpc.ReactorWalletServiceStub
import net.devh.boot.grpc.client.inject.GrpcClient
import net.devh.boot.grpc.client.inject.GrpcClientBean
import org.springframework.context.annotation.Configuration


@Configuration
@GrpcClientBean(
    clazz = ReactorWalletServiceStub::class,
    beanName = "bank-account-service",
    client = GrpcClient("bank-account-service")
)
class Config