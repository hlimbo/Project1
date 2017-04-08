CREATE TABLE Games (
   id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
   Rank INTEGER,
   Name VARCHAR(200),
   Year YEAR,
   NA_Sales VARCHAR(200),
   EU_Sales VARCHAR(200),
   JP_Sales VARCHAR(200),
   Other_Sales VARCHAR(200),
   Global_Sales VARCHAR(200)
);

CREATE TABLE Platforms (
   id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
   Platform VARCHAR(200)
);

CREATE TABLE Genres (
   id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
   Genre VARCHAR(200)
);

CREATE TABLE Publishers (
   id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT,
   Publisher VARCHAR(200)
);

CREATE TABLE PublishersOfGames (
   Game_id INTEGER NOT NULL,
   Publisher_id INTEGER NOT NULL,
   CONSTRAINT fk_PublishersOfGames_Games FOREIGN KEY (`Game_id`) REFERENCES `Games`(id) ON DELETE CASCADE,
   CONSTRAINT fk_PublishersOfGames_Publishers FOREIGN KEY (`Publisher_id`) REFERENCES `Publishers`(id) ON DELETE CASCADE
);

CREATE TABLE GenresOfGames (
   Game_id INTEGER NOT NULL,
   Genre_id INTEGER NOT NULL,
   CONSTRAINT fk_GenresOfGames_Games FOREIGN KEY (`Game_id`) REFERENCES `Games`(id) ON DELETE CASCADE,
   CONSTRAINT fk_GenresOfGames_Genres FOREIGN KEY (`Genre_id`) REFERENCES `Genres`(id) ON DELETE CASCADE
);

CREATE TABLE PlatformsOfGames (
   Game_id INTEGER NOT NULL,
   Platform_id INTEGER NOT NULL,
   CONSTRAINT fk_PlatformsOfGames_Games FOREIGN KEY (`Game_id`) REFERENCES `Games`(id) ON DELETE CASCADE,
   CONSTRAINT fk_PlatformsOfGames_Platforms FOREIGN KEY (`Platform_id`) REFERENCES `Platforms`(id) ON DELETE CASCADE
);

