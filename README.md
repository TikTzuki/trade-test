Cassandra CQL

```dockerfile
docker run -d --name=prometheus -p 9090:9090 -v .\prometheus\prometheus.yaml:/etc/prometheus/prometheus.yml prom/prometheus

rate(grpc_server_call_duration_seconds_count{grpc_method="com.nio.wallet.grpc.WalletService/transfer"}[1m])
```

```sql
select * from system.peers;
truncate <table_name>;
```