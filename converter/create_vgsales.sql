CREATE TABLE Games (
   id INTEGER PRIMARY KEY NOT NULL,
   Rank INTEGER,
   Name VARCHAR(200),
   Platform VARCHAR(200),
   Year YEAR,
   NA_Sales VARCHAR(200),
   EU_Sales VARCHAR(200),
   JP_Sales VARCHAR(200),
   Other_Sales VARCHAR(200),
   Global_Sales VARCHAR(200)
);

CREATE TABLE Publishers (
   id INTEGER PRIMARY KEY NOT NULL,
   Publisher VARCHAR(200)
);

CREATE TABLE Genres (
   id INTEGER PRIMARY KEY NOT NULL,
   Genre VARCHAR(200)
);

CREATE TABLE GenresOfGames (
   Game_id INTEGER NOT NULL,
   Genre_id INTEGER NOT NULL
);

CREATE TABLE PublishersOfGames (
   Game_id INTEGER NOT NULL,
   Publisher_id INTEGER NOT NULL
);

