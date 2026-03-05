-- ============================================================
-- Database Setup Script for History Talk Database (PostgreSQL)
-- Assumes the database `history_talk_db` already exists.
-- Run with psql: \i docs/database-setup.sql
-- ============================================================

-- Switch to target database
\c history_talk_db;

-- ============================================================
-- Table: Role
-- ============================================================
CREATE TABLE IF NOT EXISTS "role" (
    role_id VARCHAR(50) PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- ============================================================
-- Table: Staff
-- ============================================================
CREATE TABLE IF NOT EXISTS staff (
    staff_id VARCHAR(50) PRIMARY KEY,
    role_id VARCHAR(50) NOT NULL REFERENCES "role"(role_id),
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE
);

-- ============================================================
-- Table: User
-- ============================================================
CREATE TABLE IF NOT EXISTS "user" (
    uid VARCHAR(50) PRIMARY KEY,
    staff_id VARCHAR(50) REFERENCES staff(staff_id),
    user_name VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    user_type VARCHAR(50) NOT NULL DEFAULT 'GUEST'
);

-- ============================================================
-- Table: HistoricalContext
-- ============================================================
CREATE TABLE IF NOT EXISTS historical_context (
    context_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    staff_id VARCHAR(50) NOT NULL REFERENCES staff(staff_id),
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_context_name ON historical_context(LOWER(name));
CREATE INDEX IF NOT EXISTS idx_context_staff_id ON historical_context(staff_id);

-- ============================================================
-- Table: HistoricalContextDocument
-- ============================================================
CREATE TABLE IF NOT EXISTS historical_context_document (
    doc_id VARCHAR(50) PRIMARY KEY,
    context_id VARCHAR(50) NOT NULL REFERENCES historical_context(context_id),
    staff_id VARCHAR(50) NOT NULL REFERENCES staff(staff_id),
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    upload_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_hcd_context_id ON historical_context_document(context_id);
CREATE INDEX IF NOT EXISTS idx_hcd_staff_id ON historical_context_document(staff_id);

-- ============================================================
-- Table: Character
-- ============================================================
CREATE TABLE IF NOT EXISTS "character" (
    character_id VARCHAR(50) PRIMARY KEY,
    context_id VARCHAR(50) NOT NULL REFERENCES historical_context(context_id),
    staff_id VARCHAR(50) NOT NULL REFERENCES staff(staff_id),
    name VARCHAR(100) NOT NULL,
    background TEXT NOT NULL,
    image VARCHAR(255),
    personality VARCHAR(500)
);

CREATE INDEX IF NOT EXISTS idx_character_context_id ON "character"(context_id);
CREATE INDEX IF NOT EXISTS idx_character_staff_id ON "character"(staff_id);

-- ============================================================
-- Table: CharacterDocument
-- ============================================================
CREATE TABLE IF NOT EXISTS character_document (
    doc_id VARCHAR(50) PRIMARY KEY,
    character_id VARCHAR(50) NOT NULL REFERENCES "character"(character_id),
    staff_id VARCHAR(50) NOT NULL REFERENCES staff(staff_id),
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    upload_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_character_doc_character_id ON character_document(character_id);
CREATE INDEX IF NOT EXISTS idx_character_doc_staff_id ON character_document(staff_id);

-- ============================================================
-- Table: ChatSession
-- ============================================================
CREATE TABLE IF NOT EXISTS chat_session (
    session_id VARCHAR(50) PRIMARY KEY,
    uid VARCHAR(50) NOT NULL REFERENCES "user"(uid),
    character_id VARCHAR(50) NOT NULL REFERENCES "character"(character_id),
    create_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_session_uid ON chat_session(uid);
CREATE INDEX IF NOT EXISTS idx_session_character_id ON chat_session(character_id);

-- ============================================================
-- Table: Message
-- ============================================================
CREATE TABLE IF NOT EXISTS message (
    message_id VARCHAR(50) PRIMARY KEY,
    session_id VARCHAR(50) NOT NULL REFERENCES chat_session(session_id),
    content TEXT NOT NULL,
    is_from_ai BOOLEAN NOT NULL DEFAULT false,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_message_session_id ON message(session_id);
CREATE INDEX IF NOT EXISTS idx_message_timestamp ON message(timestamp);

-- ============================================================
-- Table: Quiz
-- ============================================================
CREATE TABLE IF NOT EXISTS quiz (
    quiz_id VARCHAR(50) PRIMARY KEY,
    context_id VARCHAR(50) NOT NULL REFERENCES historical_context(context_id),
    staff_id VARCHAR(50) NOT NULL REFERENCES staff(staff_id),
    title VARCHAR(255) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_quiz_context_id ON quiz(context_id);
CREATE INDEX IF NOT EXISTS idx_quiz_staff_id ON quiz(staff_id);

-- ============================================================
-- Table: Question
-- ============================================================
CREATE TABLE IF NOT EXISTS question (
    question_id VARCHAR(50) PRIMARY KEY,
    quiz_id VARCHAR(50) NOT NULL REFERENCES quiz(quiz_id),
    content TEXT NOT NULL,
    options TEXT NOT NULL,
    correct_answer VARCHAR(255) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_question_quiz_id ON question(quiz_id);

-- ============================================================
-- Table: QuizResult
-- ============================================================
CREATE TABLE IF NOT EXISTS quiz_result (
    result_id VARCHAR(50) PRIMARY KEY,
    uid VARCHAR(50) NOT NULL REFERENCES "user"(uid),
    quiz_id VARCHAR(50) NOT NULL REFERENCES quiz(quiz_id),
    score INTEGER NOT NULL,
    taken_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_result_uid ON quiz_result(uid);
CREATE INDEX IF NOT EXISTS idx_result_quiz_id ON quiz_result(quiz_id);

-- ============================================================
-- Table: QuizAnswerDetail
-- ============================================================
CREATE TABLE IF NOT EXISTS quiz_answer_detail (
    detail_id VARCHAR(50) PRIMARY KEY,
    result_id VARCHAR(50) NOT NULL REFERENCES quiz_result(result_id),
    question_id VARCHAR(50) NOT NULL REFERENCES question(question_id),
    selected_option VARCHAR(255) NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX IF NOT EXISTS idx_answer_result_id ON quiz_answer_detail(result_id);
CREATE INDEX IF NOT EXISTS idx_answer_question_id ON quiz_answer_detail(question_id);

-- ============================================================
-- Seed Data
-- ============================================================
INSERT INTO "role" (role_id, role_name, description)
VALUES
    ('ROLE_ADMIN', 'Administrator', 'System administrator with full access'),
    ('ROLE_STAFF', 'Staff', 'Content staff member'),
    ('ROLE_USER', 'User', 'Registered user')
ON CONFLICT (role_id) DO NOTHING;

INSERT INTO staff (staff_id, role_id, name, email)
VALUES ('staff_001', 'ROLE_STAFF', 'System Admin', 'admin@historytalk.com')
ON CONFLICT (staff_id) DO NOTHING;

INSERT INTO historical_context (context_id, name, description, staff_id)
VALUES (
    'ctx_001',
    'Battle of Dien Bien Phu',
    'A decisive battle in the First Indochina War that ended French colonial rule in Indochina.',
    'staff_001'
)
ON CONFLICT (context_id) DO NOTHING;

-- End of script
COMMIT;

\echo 'Database setup completed successfully!'
