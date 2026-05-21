CREATE TABLE IF NOT EXISTS mboard_user (
    id BIGINT PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(128) NOT NULL,
    role VARCHAR(16) NOT NULL,
    created_at DATETIME NULL,
    updated_at DATETIME NULL
);

CREATE TABLE IF NOT EXISTS artist (
    id BIGINT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    avatar_url VARCHAR(512) NULL,
    bio TEXT NULL,
    created_at DATETIME NULL,
    updated_at DATETIME NULL
);

CREATE TABLE IF NOT EXISTS album (
    id BIGINT PRIMARY KEY,
    artist_id BIGINT NOT NULL,
    name VARCHAR(128) NOT NULL,
    genre VARCHAR(255) NULL,
    cover_url VARCHAR(512) NULL,
    summary TEXT NULL,
    release_date DATE NULL,
    trending BIT NOT NULL DEFAULT b'0',
    published BIT NOT NULL DEFAULT b'1',
    created_at DATETIME NULL,
    updated_at DATETIME NULL,
    INDEX idx_album_artist_id (artist_id)
);

CREATE TABLE IF NOT EXISTS song (
    id BIGINT PRIMARY KEY,
    album_id BIGINT NOT NULL,
    name VARCHAR(128) NOT NULL,
    track_no INT NULL,
    duration_seconds INT NULL,
    created_at DATETIME NULL,
    updated_at DATETIME NULL,
    INDEX idx_song_album_id (album_id)
);

CREATE TABLE IF NOT EXISTS album_rating_user (
    id BIGINT PRIMARY KEY,
    album_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    star INT NOT NULL,
    created_at DATETIME NULL,
    updated_at DATETIME NULL,
    UNIQUE KEY uk_album_rating_user (album_id, user_id)
);

CREATE TABLE IF NOT EXISTS song_rating_user (
    id BIGINT PRIMARY KEY,
    song_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    star INT NOT NULL,
    created_at DATETIME NULL,
    updated_at DATETIME NULL,
    UNIQUE KEY uk_song_rating_user (song_id, user_id)
);

CREATE TABLE IF NOT EXISTS album_rating_aggregate (
    album_id BIGINT PRIMARY KEY,
    rating_count BIGINT NOT NULL DEFAULT 0,
    rating_star_sum BIGINT NOT NULL DEFAULT 0,
    rating_star_avg DECIMAL(8,4) NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    updated_at DATETIME NULL
);

CREATE TABLE IF NOT EXISTS song_rating_aggregate (
    song_id BIGINT PRIMARY KEY,
    rating_count BIGINT NOT NULL DEFAULT 0,
    rating_star_sum BIGINT NOT NULL DEFAULT 0,
    rating_star_avg DECIMAL(8,4) NOT NULL DEFAULT 0,
    created_at DATETIME NULL,
    updated_at DATETIME NULL
);

CREATE TABLE IF NOT EXISTS album_comment (
    id BIGINT PRIMARY KEY,
    album_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    username VARCHAR(64) NOT NULL,
    content TEXT NOT NULL,
    rating_star_snapshot INT NULL,
    rating_score_snapshot INT NULL,
    created_at DATETIME NULL,
    updated_at DATETIME NULL,
    INDEX idx_album_comment_album_id (album_id)
);

CREATE TABLE IF NOT EXISTS rating_event_consume_log (
    event_id VARCHAR(64) PRIMARY KEY,
    topic VARCHAR(128) NOT NULL,
    created_at DATETIME NULL,
    updated_at DATETIME NULL
);
