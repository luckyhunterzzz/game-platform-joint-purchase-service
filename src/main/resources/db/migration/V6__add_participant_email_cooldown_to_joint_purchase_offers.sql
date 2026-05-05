ALTER TABLE joint_purchase_offers
    ADD COLUMN participants_email_send_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN last_participants_email_sent_at TIMESTAMP WITH TIME ZONE;
