-- Semillas deterministas SOLO para perfiles dev/nodocker (EX-R01: 1000.00 -> 5.00; 250.50 -> 1.25).
INSERT INTO ex_ping (id, idempotency_key, message, amount, currency, fee, received_at, version)
VALUES ('seed-ping-0001', 'seed-key-0001', 'Ping semilla uno', 1000.00, 'PEN', 5.00, TIMESTAMP '2025-01-15 10:00:00', 0);
INSERT INTO ex_ping (id, idempotency_key, message, amount, currency, fee, received_at, version)
VALUES ('seed-ping-0002', 'seed-key-0002', 'Ping semilla dos', 250.50, 'USD', 1.25, TIMESTAMP '2025-01-15 11:30:00', 0);
