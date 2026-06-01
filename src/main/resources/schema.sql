CREATE TABLE IF NOT EXISTS users (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	email TEXT NOT NULL UNIQUE,
	password TEXT NOT NULL,
	display_name TEXT NOT NULL,
	age INTEGER NOT NULL,
	role TEXT NOT NULL CHECK (role IN ('USER')),
	created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS profiles (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	user_id INTEGER NOT NULL UNIQUE,
	bio TEXT,
	photo_url TEXT,
	city TEXT NOT NULL,
	latitude REAL NOT NULL,
	longitude REAL NOT NULL,
	precise_latitude REAL,
	precise_longitude REAL,
	precise_location_enabled INTEGER NOT NULL DEFAULT 0,
	state TEXT NOT NULL CHECK (state IN ('DEEP_SEARCH', 'LIGHT_TALK', 'GHOST_MODE')),
	last_active_at TIMESTAMP NOT NULL,
	last_activity_date DATE NOT NULL,
	activity_streak_days INTEGER NOT NULL DEFAULT 1,
	matching_preferences_enabled INTEGER NOT NULL DEFAULT 0,
	interest_priority INTEGER NOT NULL DEFAULT 100,
	distance_priority INTEGER NOT NULL DEFAULT 100,
	intention_priority INTEGER NOT NULL DEFAULT 100,
	activity_priority INTEGER NOT NULL DEFAULT 100,
	social_priority INTEGER NOT NULL DEFAULT 100,
	FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS profile_interests (
	profile_id INTEGER NOT NULL,
	interest TEXT NOT NULL,
	PRIMARY KEY (profile_id, interest),
	FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS profile_likes (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	liker_id INTEGER NOT NULL,
	liked_id INTEGER NOT NULL,
	created_at TIMESTAMP NOT NULL,
	UNIQUE (liker_id, liked_id),
	FOREIGN KEY (liker_id) REFERENCES users(id),
	FOREIGN KEY (liked_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS chat_messages (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	sender_id INTEGER NOT NULL,
	recipient_id INTEGER NOT NULL,
	content TEXT NOT NULL,
	created_at TIMESTAMP NOT NULL,
	FOREIGN KEY (sender_id) REFERENCES users(id),
	FOREIGN KEY (recipient_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS user_notifications (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	recipient_user_id INTEGER NOT NULL,
	type TEXT NOT NULL CHECK (type IN ('MATCH', 'MESSAGE', 'SYSTEM')),
	message TEXT NOT NULL,
	created_at TIMESTAMP NOT NULL,
	dismissed_at TIMESTAMP,
	FOREIGN KEY (recipient_user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS user_achievements (
	id INTEGER PRIMARY KEY AUTOINCREMENT,
	user_id INTEGER NOT NULL,
	type TEXT NOT NULL CHECK (type IN ('FIRST_MATCH', 'FIRST_DIALOG', 'TEN_MUTUAL_LIKES', 'PROFILE_COMPLETE', 'ACTIVE_WEEK')),
	unlocked_at TIMESTAMP NOT NULL,
	UNIQUE (user_id, type),
	FOREIGN KEY (user_id) REFERENCES users(id)
);
