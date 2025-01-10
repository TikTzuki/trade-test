package org.nio.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.web.server.GracefulShutdownCallback;
import org.springframework.boot.web.server.GracefulShutdownResult;
import reactor.netty.DisposableServer;

import java.time.Duration;
import java.util.function.Supplier;

public class GracefulShutdown {
    private static final Log logger = LogFactory.getLog(GracefulShutdown.class);
    private final Supplier<DisposableServer> disposableServer;
    private volatile Thread shutdownThread;
    private volatile boolean shuttingDown;

    public GracefulShutdown(Supplier<DisposableServer> disposableServer) {
        this.disposableServer = disposableServer;
    }

    public void shutDownGracefully(GracefulShutdownCallback callback) {
        DisposableServer server = this.disposableServer.get();
        if (server != null) {
            logger.info("Commencing graceful shutdown. Waiting for active requests to complete");
            this.shutdownThread = new Thread(() -> this.doShutdown(callback, server), "netty-shutdown");
            this.shutdownThread.start();
        }
    }

    private void doShutdown(GracefulShutdownCallback callback, DisposableServer server) {
        this.shuttingDown = true;

        try {
            server.disposeNow(Duration.ofNanos(Long.MAX_VALUE));
            logger.info("Graceful shutdown complete");
            callback.shutdownComplete(GracefulShutdownResult.IDLE);
        } catch (Exception var7) {
            logger.info("Graceful shutdown aborted with one or more requests still active");
            callback.shutdownComplete(GracefulShutdownResult.REQUESTS_ACTIVE);
        } finally {
            this.shutdownThread = null;
            this.shuttingDown = false;
        }

    }

    void abort() {
        Thread shutdownThread = this.shutdownThread;
        if (shutdownThread != null) {
            while(!this.shuttingDown) {
                this.sleep(50L);
            }

            shutdownThread.interrupt();
        }

    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException var4) {
            Thread.currentThread().interrupt();
        }

    }
}
