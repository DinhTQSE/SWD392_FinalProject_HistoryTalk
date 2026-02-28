-- ============================================================
-- Database Setup Script for History Talk Database
-- ============================================================

-- Create database
CREATE DATABASE IF NOT EXISTS history_talk_db;

-- Use the database
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
CREATE TABLE IF NOT EXISTS "staff" (
    staff_id VARCHAR(36) PRIMARY KEY,
    role_id VARCHAR(50) NOT NULL REFERENCES "role"(role_id),
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE
);

-- ============================================================
-- Table: User
-- ============================================================
CREATE TABLE IF NOT EXISTS "user" (
    uid VARCHAR(36) PRIMARY KEY,
    staff_id VARCHAR(36) REFERENCES "staff"(staff_id),
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(100),
    password VARCHAR(255) NOT NULL,
    user_type VARCHAR(20) NOT NULL DEFAULT 'GUEST',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT true
);

-- ============================================================
-- Table: HistoricalContext
-- ============================================================
CREATE TABLE IF NOT EXISTS historical_context (
    context_id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    staff_id VARCHAR(36) NOT NULL,
    staff_name VARCHAR(100),
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT false
);

-- Create indices for HistoricalContext
CREATE INDEX IF NOT EXISTS idx_context_created_date ON historical_context(created_date);
CREATE INDEX IF NOT EXISTS idx_context_status ON historical_context(status);
CREATE INDEX IF NOT EXISTS idx_context_staff_id ON historical_context(staff_id);
CREATE INDEX IF NOT EXISTS idx_context_is_deleted ON historical_context(is_deleted);
CREATE INDEX IF NOT EXISTS idx_context_name ON historical_context(LOWER(name));

-- ============================================================
-- Table: Document
-- ============================================================
CREATE TABLE IF NOT EXISTS document (
    doc_id VARCHAR(36) PRIMARY KEY,
    context_id VARCHAR(36) NOT NULL REFERENCES historical_context(context_id),
    staff_id VARCHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT false
);

CREATE INDEX IF NOT EXISTS idx_doc_context_id ON document(context_id);
CREATE INDEX IF NOT EXISTS idx_doc_staff_id ON document(staff_id);
CREATE INDEX IF NOT EXISTS idx_doc_is_deleted ON document(is_deleted);

-- ============================================================
-- Table: Character
-- ============================================================
CREATE TABLE IF NOT EXISTS character (
    character_id VARCHAR(36) PRIMARY KEY,
    context_id VARCHAR(36) NOT NULL REFERENCES historical_context(context_id),
    staff_id VARCHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    background TEXT NOT NULL,
    personality VARCHAR(500),
    image VARCHAR(255),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT false
);

CREATE INDEX IF NOT EXISTS idx_character_context_id ON character(context_id);
CREATE INDEX IF NOT EXISTS idx_character_staff_id ON character(staff_id);
CREATE INDEX IF NOT EXISTS idx_character_is_deleted ON character(is_deleted);

-- ============================================================
-- Table: ChatSession
-- ============================================================
CREATE TABLE IF NOT EXISTS chat_session (
    session_id VARCHAR(36) PRIMARY KEY,
    uid VARCHAR(36) NOT NULL REFERENCES "user"(uid),
    character_id VARCHAR(36) NOT NULL REFERENCES character(character_id),
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_date TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_session_uid ON chat_session(uid);
CREATE INDEX IF NOT EXISTS idx_session_character_id ON chat_session(character_id);

-- ============================================================
-- Table: Message
-- ============================================================
CREATE TABLE IF NOT EXISTS message (
    message_id VARCHAR(36) PRIMARY KEY,
    session_id VARCHAR(36) NOT NULL REFERENCES chat_session(session_id),
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
    quiz_id VARCHAR(36) PRIMARY KEY,
    context_id VARCHAR(36) NOT NULL REFERENCES historical_context(context_id),
    staff_id VARCHAR(36) NOT NULL,
    title VARCHAR(100) NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT false
);

CREATE INDEX IF NOT EXISTS idx_quiz_context_id ON quiz(context_id);
CREATE INDEX IF NOT EXISTS idx_quiz_staff_id ON quiz(staff_id);
CREATE INDEX IF NOT EXISTS idx_quiz_is_deleted ON quiz(is_deleted);

-- ============================================================
-- Table: Question
-- ============================================================
CREATE TABLE IF NOT EXISTS question (
    question_id VARCHAR(36) PRIMARY KEY,
    quiz_id VARCHAR(36) NOT NULL REFERENCES quiz(quiz_id),
    content TEXT NOT NULL,
    options VARCHAR(MAX),
    correct_answer VARCHAR(255) NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_question_quiz_id ON question(quiz_id);

-- ============================================================
-- Table: QuizResult
-- ============================================================
CREATE TABLE IF NOT EXISTS quiz_result (
    result_id VARCHAR(36) PRIMARY KEY,
    uid VARCHAR(36) NOT NULL REFERENCES "user"(uid),
    quiz_id VARCHAR(36) NOT NULL REFERENCES quiz(quiz_id),
    score INTEGER NOT NULL,
    taken_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_result_uid ON quiz_result(uid);
CREATE INDEX IF NOT EXISTS idx_result_quiz_id ON quiz_result(quiz_id);
CREATE INDEX IF NOT EXISTS idx_result_taken_date ON quiz_result(taken_date);

-- ============================================================
-- Table: QuizAnswerDetail
-- ============================================================
CREATE TABLE IF NOT EXISTS quiz_answer_detail (
    detail_id VARCHAR(36) PRIMARY KEY,
    result_id VARCHAR(36) NOT NULL REFERENCES quiz_result(result_id),
    question_id VARCHAR(36) NOT NULL REFERENCES question(question_id),
    selected_option VARCHAR(255),
    is_correct BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX IF NOT EXISTS idx_answer_result_id ON quiz_answer_detail(result_id);
CREATE INDEX IF NOT EXISTS idx_answer_question_id ON quiz_answer_detail(question_id);

-- ============================================================
-- Insert Sample Data (optional)
-- ============================================================

-- Insert roles
INSERT INTO "role" (role_id, role_name, description) 
VALUES 
    ('ROLE_ADMIN', 'Administrator', 'System administrator with full access'),
    ('ROLE_STAFF', 'Staff', 'Staff member who can manage content'),
    ('ROLE_USER', 'User', 'Regular user')
ON CONFLICT DO NOTHING;

-- Insert sample staff member
INSERT INTO "staff" (staff_id, role_id, name, email) 
VALUES ('staff_001', 'ROLE_STAFF', 'System Admin', 'admin@historytalk.com')
ON CONFLICT DO NOTHING;

-- Insert sample historical context
INSERT INTO historical_context (context_id, name, description, status, staff_id, staff_name, is_deleted) 
VALUES (
    'ctx_001',
    'Battle of Dien Bien Phu',
    'A decisive battle in the First Indochina War fought from 1950 to 1954, marking the end of French colonial rule in Indochina.',
    'PUBLISHED',
    'staff_001',
    'System Admin',
    false
)
ON CONFLICT DO NOTHING;

-- ============================================================
-- End of Database Setup
-- ============================================================

COMMIT;

-- Print success message
\echo 'Database setup completed successfully!'
