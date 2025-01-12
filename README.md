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
