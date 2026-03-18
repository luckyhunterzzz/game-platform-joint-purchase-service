CREATE TABLE joint_purchase_offers (
    id                    UUID PRIMARY KEY,
    version               INTEGER NOT NULL DEFAULT 0,
    organizer_user_id     UUID                     NOT NULL,
    title                 VARCHAR(200)             NOT NULL,
    description           TEXT,
    alliance_name         VARCHAR(150)             NOT NULL,
    required_participants INTEGER                  NOT NULL,
    current_participants  INTEGER                  NOT NULL DEFAULT 0,
    status                VARCHAR(50)              NOT NULL,
    purchase_window_start TIMESTAMP WITH TIME ZONE NOT NULL,
    purchase_window_end   TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at            TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at            TIMESTAMP WITH TIME ZONE NOT NULL
);

COMMENT ON TABLE joint_purchase_offers IS 'Stores information about group purchase events created by organizers';
COMMENT ON COLUMN joint_purchase_offers.id IS 'Unique identifier for the offer';
COMMENT ON COLUMN joint_purchase_offers.version IS 'Optimistic locking version to prevent data conflicts';
COMMENT ON COLUMN joint_purchase_offers.organizer_user_id IS 'ID of the user who created and manages the offer';
COMMENT ON COLUMN joint_purchase_offers.title IS 'Display name of the purchase event';
COMMENT ON COLUMN joint_purchase_offers.description IS 'Detailed information about the items or terms of purchase';
COMMENT ON COLUMN joint_purchase_offers.alliance_name IS 'Name of the alliance participating in this purchase';
COMMENT ON COLUMN joint_purchase_offers.required_participants IS 'Target number of participants needed for the deal';
COMMENT ON COLUMN joint_purchase_offers.current_participants IS 'Current number of accepted participants in the offer';
COMMENT ON COLUMN joint_purchase_offers.status IS 'Current state of the offer (e.g., OPEN, CLOSED, CANCELLED)';
COMMENT ON COLUMN joint_purchase_offers.purchase_window_start IS 'Time when members can start submitting applications';
COMMENT ON COLUMN joint_purchase_offers.purchase_window_end IS 'Deadline for participating in this purchase';
COMMENT ON COLUMN joint_purchase_offers.created_at IS 'Timestamp when the record was first created';
COMMENT ON COLUMN joint_purchase_offers.updated_at IS 'Timestamp of the last update to this record';