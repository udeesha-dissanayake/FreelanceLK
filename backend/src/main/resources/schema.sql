-- ================================================================
-- FreelanceLK Database Schema
-- Auto-generated from JPA entities
-- ================================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ================================================================
-- CORE USER TABLES
-- ================================================================

CREATE TABLE IF NOT EXISTS users (
    user_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(50)  NOT NULL,
    status          VARCHAR(50)  NOT NULL,
    created_at      TIMESTAMP    NOT NULL,
    updated_at      TIMESTAMP,
    last_login      TIMESTAMP,
    email_verified  BOOLEAN      NOT NULL DEFAULT FALSE,
    phone_verified  BOOLEAN      NOT NULL DEFAULT FALSE,
    two_factor_enabled BOOLEAN   NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS user_profiles (
    profile_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL UNIQUE REFERENCES users(user_id) ON DELETE CASCADE,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    display_name    VARCHAR(150),
    avatar_url      VARCHAR(500),
    bio             TEXT,
    location        VARCHAR(255),
    timezone        VARCHAR(50)  DEFAULT 'Asia/Colombo',
    language        VARCHAR(10)  DEFAULT 'en',
    date_of_birth   DATE,
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP
);

CREATE TABLE IF NOT EXISTS trust_scores (
    trust_score_id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                     UUID NOT NULL UNIQUE REFERENCES users(user_id) ON DELETE CASCADE,
    overall_score               NUMERIC(5,2) NOT NULL DEFAULT 0,
    review_score                NUMERIC(5,2) DEFAULT 0,
    verification_score          NUMERIC(5,2) DEFAULT 0,
    transaction_consistency_score NUMERIC(5,2) DEFAULT 0,
    completion_rate             NUMERIC(5,2) DEFAULT 0,
    response_time_score         NUMERIC(5,2) DEFAULT 0,
    total_reviews               INTEGER DEFAULT 0,
    total_completed_orders      INTEGER DEFAULT 0,
    total_cancelled_orders      INTEGER DEFAULT 0,
    last_calculated_at          TIMESTAMP,
    created_at                  TIMESTAMP,
    updated_at                  TIMESTAMP
);

CREATE TABLE IF NOT EXISTS wallets (
    wallet_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL UNIQUE REFERENCES users(user_id) ON DELETE CASCADE,
    balance         NUMERIC(15,2) NOT NULL DEFAULT 0,
    pending_balance NUMERIC(15,2) NOT NULL DEFAULT 0,
    currency        VARCHAR(3)    DEFAULT 'LKR',
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP
);

CREATE TABLE IF NOT EXISTS trust_score_weights (
    weight_id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    weight_name     VARCHAR(100) NOT NULL UNIQUE,
    weight_value    NUMERIC(5,2) NOT NULL,
    is_active       BOOLEAN DEFAULT TRUE,
    updated_at      TIMESTAMP
);

-- ================================================================
-- AUTHENTICATION TABLES
-- ================================================================

CREATE TABLE IF NOT EXISTS refresh_tokens (
    token_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    token_hash      VARCHAR(255) NOT NULL UNIQUE,
    expires_at      TIMESTAMP    NOT NULL,
    is_revoked      BOOLEAN      DEFAULT FALSE,
    revoked_at      TIMESTAMP,
    created_at      TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_sessions (
    session_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    ip_address      VARCHAR(50),
    user_agent      VARCHAR(255),
    login_at        TIMESTAMP,
    last_activity   TIMESTAMP,
    logout_at       TIMESTAMP
);

-- ================================================================
-- SKILL TABLES
-- ================================================================

CREATE TABLE IF NOT EXISTS skills (
    skill_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    skill_name      VARCHAR(100) NOT NULL UNIQUE,
    category        VARCHAR(100),
    created_at      TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_skills (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    skill_id            UUID NOT NULL REFERENCES skills(skill_id) ON DELETE CASCADE,
    proficiency_level   INTEGER DEFAULT 1,
    years_of_experience NUMERIC(4,1),
    created_at          TIMESTAMP
);

-- ================================================================
-- LISTING TABLES
-- ================================================================

CREATE TABLE IF NOT EXISTS listings (
    listing_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    listing_type    VARCHAR(50)  NOT NULL,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    status          VARCHAR(50),
    views_count     INTEGER DEFAULT 0,
    is_featured     BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP
);

CREATE TABLE IF NOT EXISTS listing_skills (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    listing_id      UUID NOT NULL REFERENCES listings(listing_id) ON DELETE CASCADE,
    skill_id        UUID NOT NULL REFERENCES skills(skill_id) ON DELETE CASCADE,
    is_required     BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP
);

CREATE TABLE IF NOT EXISTS listing_media (
    media_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    listing_id      UUID NOT NULL REFERENCES listings(listing_id) ON DELETE CASCADE,
    media_type      VARCHAR(20),
    media_url       VARCHAR(500) NOT NULL,
    display_order   INTEGER DEFAULT 0,
    is_primary      BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMP
);

CREATE TABLE IF NOT EXISTS saved_listings (
    saved_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    listing_id      UUID NOT NULL REFERENCES listings(listing_id) ON DELETE CASCADE,
    saved_at        TIMESTAMP
);

-- ================================================================
-- GIG TABLES
-- ================================================================

CREATE TABLE IF NOT EXISTS gigs (
    gig_id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    listing_id          UUID NOT NULL UNIQUE REFERENCES listings(listing_id) ON DELETE CASCADE,
    pricing_model       VARCHAR(50),
    base_price          NUMERIC(10,2),
    delivery_days       INTEGER,
    revisions_included  INTEGER,
    category            VARCHAR(100),
    subcategory         VARCHAR(100),
    requirements        TEXT,
    created_at          TIMESTAMP,
    updated_at          TIMESTAMP
);

CREATE TABLE IF NOT EXISTS gig_packages (
    package_id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    gig_id              UUID NOT NULL REFERENCES gigs(gig_id) ON DELETE CASCADE,
    package_name        VARCHAR(50)  NOT NULL,
    package_description TEXT,
    price               NUMERIC(10,2) NOT NULL,
    delivery_days       INTEGER NOT NULL,
    revisions           INTEGER
);

-- ================================================================
-- PART-TIME JOB TABLES
-- ================================================================

CREATE TABLE IF NOT EXISTS part_time_jobs (
    job_id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    listing_id          UUID NOT NULL UNIQUE REFERENCES listings(listing_id) ON DELETE CASCADE,
    employment_type     VARCHAR(50),
    hourly_rate         NUMERIC(10,2),
    monthly_salary      NUMERIC(10,2),
    hours_per_week      INTEGER,
    duration_months     INTEGER,
    start_date          DATE,
    end_date            DATE,
    location            VARCHAR(255),
    is_remote           BOOLEAN,
    requirements        TEXT,
    responsibilities    TEXT,
    created_at          TIMESTAMP,
    updated_at          TIMESTAMP
);

CREATE TABLE IF NOT EXISTS job_applications (
    application_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    listing_id          UUID NOT NULL REFERENCES listings(listing_id) ON DELETE CASCADE,
    freelancer_id       UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    cover_letter        TEXT,
    expected_rate       VARCHAR(100),
    status              VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    created_at          TIMESTAMP,
    updated_at          TIMESTAMP,
    UNIQUE (listing_id, freelancer_id)
);

-- ================================================================
-- ORDER TABLES
-- ================================================================

CREATE TABLE IF NOT EXISTS orders (
    order_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number    VARCHAR(50)   NOT NULL UNIQUE,
    listing_id      UUID NOT NULL REFERENCES listings(listing_id),
    buyer_id        UUID NOT NULL REFERENCES users(user_id),
    seller_id       UUID NOT NULL REFERENCES users(user_id),
    status          VARCHAR(50)   NOT NULL DEFAULT 'PENDING',
    amount          NUMERIC(10,2) NOT NULL,
    platform_fee    NUMERIC(10,2) DEFAULT 0,
    total_amount    NUMERIC(10,2) NOT NULL,
    payment_status  VARCHAR(50)   NOT NULL DEFAULT 'PENDING',
    delivery_date   TIMESTAMP,
    completed_at    TIMESTAMP,
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_deliverables (
    deliverable_id  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id        UUID NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    file_url        VARCHAR(500) NOT NULL,
    file_name       VARCHAR(255),
    file_size       BIGINT,
    description     TEXT,
    delivered_at    TIMESTAMP,
    approved_at     TIMESTAMP,
    created_at      TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_messages (
    message_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id        UUID NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
    sender_id       UUID NOT NULL REFERENCES users(user_id),
    message_text    TEXT NOT NULL,
    attachment_url  VARCHAR(500),
    is_read         BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMP
);

-- ================================================================
-- REVIEW TABLES
-- ================================================================

CREATE TABLE IF NOT EXISTS reviews (
    review_id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id                UUID UNIQUE REFERENCES orders(order_id),
    reviewer_id             UUID REFERENCES users(user_id),
    reviewee_id             UUID REFERENCES users(user_id),
    rating                  INTEGER,
    communication_rating    INTEGER,
    quality_rating          INTEGER,
    professionalism_rating  INTEGER,
    review_text             TEXT,
    is_public               BOOLEAN DEFAULT TRUE,
    created_at              TIMESTAMP,
    updated_at              TIMESTAMP
);

CREATE TABLE IF NOT EXISTS review_responses (
    response_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    review_id       UUID NOT NULL UNIQUE REFERENCES reviews(review_id) ON DELETE CASCADE,
    response_text   TEXT NOT NULL,
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP
);

-- ================================================================
-- TRANSACTION & FINANCIAL TABLES
-- ================================================================

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id        UUID REFERENCES orders(order_id),
    payer_id        UUID REFERENCES users(user_id),
    payee_id        UUID REFERENCES users(user_id),
    amount          NUMERIC(10,2),
    transaction_type VARCHAR(50),
    status          VARCHAR(50) DEFAULT 'PENDING',
    description     VARCHAR(500),
    created_at      TIMESTAMP,
    processed_at    TIMESTAMP
);

CREATE TABLE IF NOT EXISTS withdrawal_requests (
    withdrawal_id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID REFERENCES users(user_id),
    amount          NUMERIC(10,2),
    status          VARCHAR(50) DEFAULT 'PENDING',
    notes           TEXT,
    requested_at    TIMESTAMP,
    processed_at    TIMESTAMP,
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP
);

CREATE TABLE IF NOT EXISTS withdrawal_bank_details (
    withdrawal_id   UUID NOT NULL REFERENCES withdrawal_requests(withdrawal_id) ON DELETE CASCADE,
    detail_key      VARCHAR(100) NOT NULL,
    detail_value    VARCHAR(500),
    PRIMARY KEY (withdrawal_id, detail_key)
);

-- ================================================================
-- MESSAGING TABLES
-- ================================================================

CREATE TABLE IF NOT EXISTS conversations (
    conversation_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_one_id         UUID NOT NULL REFERENCES users(user_id),
    user_two_id         UUID NOT NULL REFERENCES users(user_id),
    last_message_at     TIMESTAMP,
    last_message_preview VARCHAR(500),
    created_at          TIMESTAMP
);

CREATE TABLE IF NOT EXISTS messages (
    message_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES conversations(conversation_id) ON DELETE CASCADE,
    sender_id       UUID NOT NULL REFERENCES users(user_id),
    content         TEXT NOT NULL,
    message_type    VARCHAR(50) DEFAULT 'TEXT',
    attachment_url  VARCHAR(500),
    is_read         BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMP
);

-- ================================================================
-- NOTIFICATION TABLES
-- ================================================================

CREATE TABLE IF NOT EXISTS notifications (
    notification_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    title               VARCHAR(255) NOT NULL,
    message             TEXT NOT NULL,
    notification_type   VARCHAR(50),
    related_entity_id   UUID,
    is_read             BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP
);

-- ================================================================
-- DISPUTE TABLES
-- ================================================================

CREATE TABLE IF NOT EXISTS disputes (
    dispute_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id        UUID NOT NULL UNIQUE REFERENCES orders(order_id),
    raised_by       UUID NOT NULL REFERENCES users(user_id),
    reason          VARCHAR(255) NOT NULL,
    description     TEXT,
    status          VARCHAR(50)  NOT NULL DEFAULT 'OPEN',
    resolution      TEXT,
    created_at      TIMESTAMP,
    resolved_at     TIMESTAMP
);

-- ================================================================
-- IDENTITY VERIFICATION TABLES
-- ================================================================

CREATE TABLE IF NOT EXISTS identity_verifications (
    verification_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID REFERENCES users(user_id) ON DELETE CASCADE,
    verification_type   VARCHAR(100),
    document_number     VARCHAR(100),
    document_url        VARCHAR(500),
    verification_status VARCHAR(50),
    submitted_at        TIMESTAMP,
    verified_at         TIMESTAMP,
    verified_by         VARCHAR(100),
    notes               TEXT,
    created_at          TIMESTAMP,
    updated_at          TIMESTAMP
);

-- ================================================================
-- INDEXES FOR PERFORMANCE
-- ================================================================

CREATE INDEX IF NOT EXISTS idx_listings_user_id     ON listings(user_id);
CREATE INDEX IF NOT EXISTS idx_listings_type        ON listings(listing_type);
CREATE INDEX IF NOT EXISTS idx_listings_status      ON listings(status);
CREATE INDEX IF NOT EXISTS idx_orders_buyer_id      ON orders(buyer_id);
CREATE INDEX IF NOT EXISTS idx_orders_seller_id     ON orders(seller_id);
CREATE INDEX IF NOT EXISTS idx_orders_status        ON orders(status);
CREATE INDEX IF NOT EXISTS idx_notifications_user   ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_messages_conv_id     ON messages(conversation_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user  ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_job_apps_listing     ON job_applications(listing_id);
CREATE INDEX IF NOT EXISTS idx_job_apps_freelancer  ON job_applications(freelancer_id);
