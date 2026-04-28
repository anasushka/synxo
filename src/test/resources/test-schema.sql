DROP TABLE IF EXISTS chat_messages;
DROP TABLE IF EXISTS profile_likes;
DROP TABLE IF EXISTS profile_interests;
DROP TABLE IF EXISTS profiles;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	email TEXT NOT NULL UNIQUE,
	password TEXT NOT NULL,
	display_name TEXT NOT NULL,
	age INTEGER NOT NULL,
	role TEXT NOT NULL CHECK (role IN ('USER')),
	created_at TIMESTAMP NOT NULL
);

CREATE TABLE profiles (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	user_id INTEGER NOT NULL UNIQUE,
	bio TEXT,
	photo_url TEXT,
	city TEXT NOT NULL,
	latitude REAL NOT NULL,
	longitude REAL NOT NULL,
	state TEXT NOT NULL CHECK (state IN ('DEEP_SEARCH', 'LIGHT_TALK', 'GHOST_MODE')),
	last_active_at TIMESTAMP NOT NULL,
	FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE profile_interests (
	profile_id INTEGER NOT NULL,
	interest TEXT NOT NULL,
	PRIMARY KEY (profile_id, interest),
	FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE CASCADE
);

CREATE TABLE profile_likes (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	liker_id INTEGER NOT NULL,
	liked_id INTEGER NOT NULL,
	created_at TIMESTAMP NOT NULL,
	UNIQUE (liker_id, liked_id),
	FOREIGN KEY (liker_id) REFERENCES users(id),
	FOREIGN KEY (liked_id) REFERENCES users(id)
);

CREATE TABLE chat_messages (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	sender_id INTEGER NOT NULL,
	recipient_id INTEGER NOT NULL,
	content TEXT NOT NULL,
	created_at TIMESTAMP NOT NULL,
	FOREIGN KEY (sender_id) REFERENCES users(id),
	FOREIGN KEY (recipient_id) REFERENCES users(id)
);
