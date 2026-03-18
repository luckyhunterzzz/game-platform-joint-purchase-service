CREATE TABLE participation_applications (
    id                UUID PRIMARY KEY,
    version           INTEGER NOT NULL DEFAULT 0,
    offer_id          UUID                     NOT NULL,
    applicant_user_id UUID                     NOT NULL,
    status            VARCHAR(50)              NOT NULL,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_participation_applications_offer
        FOREIGN KEY (offer_id) REFERENCES joint_purchase_offers (id)
);

CREATE UNIQUE INDEX ux_participation_applications_offer_user
    ON participation_applications (offer_id, applicant_user_id);

COMMENT ON TABLE participation_applications IS 'Stores user applications to join a specific joint purchase offer';
COMMENT ON COLUMN participation_applications.id IS 'Unique identifier for the application';
COMMENT ON COLUMN participation_applications.version IS 'Optimistic locking version for data consistency';
COMMENT ON COLUMN participation_applications.offer_id IS 'Reference to the joint purchase offer';
COMMENT ON COLUMN participation_applications.applicant_user_id IS 'ID of the user who wants to join the purchase';
COMMENT ON COLUMN participation_applications.status IS 'Current state of the application (e.g., SUBMITTED, REJECTED)';
COMMENT ON COLUMN participation_applications.created_at IS 'Timestamp when the application was submitted';
COMMENT ON COLUMN participation_applications.updated_at IS 'Timestamp of the last status change';