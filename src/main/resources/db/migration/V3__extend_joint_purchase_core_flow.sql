ALTER TABLE joint_purchase_offers
    ALTER COLUMN alliance_name DROP NOT NULL;

ALTER TABLE joint_purchase_offers
    RENAME COLUMN purchase_window_start TO planned_start_at;

ALTER TABLE joint_purchase_offers
    RENAME COLUMN purchase_window_end TO planned_end_at;

ALTER TABLE joint_purchase_offers
    ADD COLUMN reserve_participants INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN current_reserve_participants INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN auto_approve_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN screenshot_bucket VARCHAR(255),
    ADD COLUMN screenshot_object_key VARCHAR(1024);

UPDATE joint_purchase_offers
SET status = CASE status
    WHEN 'OPEN' THEN 'OPEN_FOR_APPLICATIONS'
    WHEN 'FULL' THEN 'MAIN_GROUP_FILLED'
    WHEN 'CLOSED' THEN 'COMPLETED'
    WHEN 'DRAFT' THEN 'OPEN_FOR_APPLICATIONS'
    ELSE status
END;

ALTER TABLE participation_applications
    ADD COLUMN assigned_participation_type VARCHAR(20),
    ADD COLUMN reviewed_by_user_id UUID,
    ADD COLUMN reviewed_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN screenshot_bucket VARCHAR(255),
    ADD COLUMN screenshot_object_key VARCHAR(1024);

UPDATE participation_applications
SET status = CASE status
    WHEN 'SUBMITTED' THEN 'PENDING_ORGANIZER_REVIEW'
    WHEN 'AUTO_APPROVED' THEN 'APPROVED_MAIN'
    WHEN 'MANUAL_REVIEW' THEN 'PENDING_ORGANIZER_REVIEW'
    ELSE status
END;

CREATE TABLE joint_purchase_participants (
    id                 UUID PRIMARY KEY,
    version            INTEGER                  NOT NULL DEFAULT 0,
    offer_id           UUID                     NOT NULL,
    application_id     UUID                     NOT NULL,
    user_id            UUID                     NOT NULL,
    participation_type VARCHAR(20)              NOT NULL,
    status             VARCHAR(20)              NOT NULL,
    joined_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at         TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_joint_purchase_participants_offer
        FOREIGN KEY (offer_id) REFERENCES joint_purchase_offers (id),
    CONSTRAINT fk_joint_purchase_participants_application
        FOREIGN KEY (application_id) REFERENCES participation_applications (id)
);

CREATE UNIQUE INDEX ux_joint_purchase_participants_active_offer_user
    ON joint_purchase_participants (offer_id, user_id)
    WHERE status = 'ACTIVE';

CREATE UNIQUE INDEX ux_joint_purchase_participants_active_main_user
    ON joint_purchase_participants (user_id)
    WHERE status = 'ACTIVE' AND participation_type = 'MAIN';

CREATE TABLE joint_purchase_participant_feedback (
    id                  UUID PRIMARY KEY,
    version             INTEGER                  NOT NULL DEFAULT 0,
    offer_id            UUID                     NOT NULL,
    application_id      UUID                     NOT NULL,
    participant_user_id UUID                     NOT NULL,
    author_user_id      UUID                     NOT NULL,
    result              VARCHAR(20)              NOT NULL,
    description         VARCHAR(3000),
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_joint_purchase_feedback_offer
        FOREIGN KEY (offer_id) REFERENCES joint_purchase_offers (id),
    CONSTRAINT fk_joint_purchase_feedback_application
        FOREIGN KEY (application_id) REFERENCES participation_applications (id),
    CONSTRAINT ux_joint_purchase_feedback_offer_application UNIQUE (offer_id, application_id)
);

COMMENT ON COLUMN joint_purchase_offers.planned_start_at IS 'Planned time when the joint purchase should start';
COMMENT ON COLUMN joint_purchase_offers.planned_end_at IS 'Planned time when the joint purchase should end';
COMMENT ON COLUMN joint_purchase_offers.screenshot_bucket IS 'MinIO bucket for the offer screenshot';
COMMENT ON COLUMN joint_purchase_offers.screenshot_object_key IS 'MinIO object key for the offer screenshot';
COMMENT ON COLUMN participation_applications.screenshot_bucket IS 'MinIO bucket for the application screenshot';
COMMENT ON COLUMN participation_applications.screenshot_object_key IS 'MinIO object key for the application screenshot';
COMMENT ON TABLE joint_purchase_participant_feedback IS 'Organizer feedback for completed MAIN participants';
