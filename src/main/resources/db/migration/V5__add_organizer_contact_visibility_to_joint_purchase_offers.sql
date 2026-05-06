ALTER TABLE joint_purchase_offers
    ADD COLUMN show_organizer_contacts BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN show_organizer_game_nickname BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN show_organizer_telegram BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN show_organizer_vk BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN show_organizer_discord BOOLEAN NOT NULL DEFAULT FALSE;
