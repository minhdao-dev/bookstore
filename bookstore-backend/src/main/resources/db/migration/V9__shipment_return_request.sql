ALTER TABLE shipment
    ADD COLUMN delivered_at        TIMESTAMP,
    ADD COLUMN return_requested_at TIMESTAMP;