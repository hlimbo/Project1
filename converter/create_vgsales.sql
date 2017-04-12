CREATE TABLE publishers (
   id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
   publisher VARCHAR(200)
);

CREATE TABLE platforms (
   id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
   platform VARCHAR(200)
);

CREATE TABLE games (
   id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
   rank INTEGER,
   name VARCHAR(200),
   year YEAR,
   globalsales VARCHAR(200)
);

CREATE TABLE genres (
   id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
   genre VARCHAR(200)
);

CREATE TABLE genres_of_games (
   game_id INTEGER NOT NULL,
   genre_id INTEGER NOT NULL,
   CONSTRAINT fk_genres_of_games_games FOREIGN KEY (`game_id`) REFERENCES `games`(id) ON DELETE CASCADE,
   CONSTRAINT fk_genres_of_games_genres FOREIGN KEY (`genre_id`) REFERENCES `genres`(id) ON DELETE CASCADE
);

CREATE TABLE platforms_of_games (
   game_id INTEGER NOT NULL,
   platform_id INTEGER NOT NULL,
   CONSTRAINT fk_platforms_of_games_games FOREIGN KEY (`game_id`) REFERENCES `games`(id) ON DELETE CASCADE,
   CONSTRAINT fk_platforms_of_games_platforms FOREIGN KEY (`platform_id`) REFERENCES `platforms`(id) ON DELETE CASCADE
);

CREATE TABLE publishers_of_games (
   game_id INTEGER NOT NULL,
   publisher_id INTEGER NOT NULL,
   CONSTRAINT fk_publishers_of_games_games FOREIGN KEY (`game_id`) REFERENCES `games`(id) ON DELETE CASCADE,
   CONSTRAINT fk_publishers_of_games_publishers FOREIGN KEY (`publisher_id`) REFERENCES `publishers`(id) ON DELETE CASCADE
);

CREATE TABLE creditcards (
   id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
   first_name VARCHAR(200) NOT NULL,
   last_name VARCHAR(200) NOT NULL,
   expiration DATE NOT NULL
);

CREATE TABLE customers (
   id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
   cc_id INTEGER NOT NULL,
   first_name VARCHAR(200) NOT NULL,
   last_name VARCHAR(200) NOT NULL,
   address VARCHAR(200) NOT NULL,
   email VARCHAR(200) NOT NULL,
   password VARCHAR(200) NOT NULL,
   CONSTRAINT fk_customers_creditcards FOREIGN KEY (`cc_id`) REFERENCES `creditcards`(id) ON DELETE CASCADE
);

CREATE TABLE sales (
   id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
   customer_id INTEGER NOT NULL,
   salesdate DATE NOT NULL,
   game_id INTEGER NOT NULL,
   CONSTRAINT fk_sales_customers FOREIGN KEY (`customer_id`) REFERENCES `customers`(id) ON DELETE CASCADE,
   CONSTRAINT fk_sales_games FOREIGN KEY (`game_id`) REFERENCES `games`(id) ON DELETE CASCADE
);

