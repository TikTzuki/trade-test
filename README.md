Cassandra CQL

```bash
k -n database exec -ti cluster1-dc1-default-sts-1 -c cassandra -- sh -c "cqlsh -u 'cluster1-superuser' -p 't72v7ioLWEfo7fPbiWfjO1YZ2NWoxxAjLuQaqNdNA8wSbFKicPirjA'"
```

```dockerfile
docker run -d --name=prometheus -p 9090:9090 -v .\prometheus\prometheus.yaml:/etc/prometheus/prometheus.yml prom/prometheus

rate(grpc_server_call_duration_seconds_count{grpc_method="com.nio.wallet.grpc.WalletService/transfer"}[1m])
```

```sql
select * from system.peers;
truncate <table_name>;
```

// CPU tuning
```bash
16 cores | 160K 2s | 80K 1.2s | 5K | Need 140 cores
```

source_elapsed is the time in microseconds

# SQS

A queue may still store a message after it has been consumed due to several reasons related to reliability and fault tolerance:

1. Acknowledgment Mechanism
   Manual Acknowledgment: The message stays in the queue until the consumer explicitly confirms successful processing.
   Automatic Acknowledgment: If enabled, the message is removed after being delivered, but in some systems, it remains until processing is confirmed.
2. Message Visibility Timeout (Temporary Lock)
   In systems like Amazon SQS, when a consumer picks up a message, it becomes invisible for a period (visibility timeout).
   If the consumer fails to process it within that time, the message becomes visible again for reprocessing.
3. Dead-Letter Queue (DLQ)
   If a message fails to be processed after several attempts, it may be moved to a DLQ for debugging or further analysis.
   Until then, it can remain in the main queue.
4. Durability and Persistence
   For durability, messages are sometimes stored persistently (disk/database) until successful processing is confirmed.
   This prevents data loss in case of system failures.
5. Batch Processing
   Some consumers process messages in batches and only acknowledge after the entire batch is processed.
   Until acknowledgment, messages stay in the queue.
6. Consumer Failures
   If a consumer crashes before acknowledgment, the message remains in the queue to be retried by another consumer.