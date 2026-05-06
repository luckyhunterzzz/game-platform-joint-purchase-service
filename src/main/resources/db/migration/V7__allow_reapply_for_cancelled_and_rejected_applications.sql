DROP INDEX IF EXISTS ux_participation_applications_offer_user;

CREATE UNIQUE INDEX ux_participation_applications_offer_user_active
    ON participation_applications (offer_id, applicant_user_id)
    WHERE status IN (
        'PENDING_TRUST_CHECK',
        'PENDING_ORGANIZER_REVIEW',
        'APPROVED_MAIN',
        'APPROVED_RESERVE'
    );
