package org.nio.config

import io.grpc.ServerBuilder
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.Executor
import java.util.concurrent.Executors


@Configuration
class GrpcConfig {
    @Bean
    fun keepAliveServerConfigurer(): GrpcServerConfigurer {
        return GrpcServerConfigurer { serverBuilder: ServerBuilder<*>? ->
            if (serverBuilder is NettyServerBuilder) {
                serverBuilder.executor(grpcExecutor())
            }
        }
    }

    fun grpcExecutor(): Executor {
//        return VirtualThreadTaskExecutor()
//        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), Thread.ofVirtual().factory())
        return Executors.newVirtualThreadPerTaskExecutor()
//        return Executors.newWorkStealingPool()
    }
}