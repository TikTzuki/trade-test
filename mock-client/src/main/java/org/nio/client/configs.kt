package org.nio.client

import com.tik.grpc.bank.service.ReactorBankAccountServiceGrpc.ReactorBankAccountServiceStub
import net.devh.boot.grpc.client.inject.GrpcClient
import net.devh.boot.grpc.client.inject.GrpcClientBean
import org.springframework.context.annotation.Configuration


@Configuration
@GrpcClientBean(
    clazz = ReactorBankAccountServiceStub::class,
    beanName = "bank-account-service",
    client = GrpcClient("bank-account-service")
)
class Config