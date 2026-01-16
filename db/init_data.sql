TRUNCATE TABLE convoy CASCADE;
TRUNCATE TABLE complaint CASCADE;
TRUNCATE TABLE roles CASCADE;
TRUNCATE TABLE summon CASCADE;
TRUNCATE TABLE users CASCADE;

INSERT INTO users(password, username)
values ('$2a$10$eiE.Yxfplitmpm1fMR4jy.heWCiO5JumEv7XG7vn0q.ukyP.cmU0a', 'ivan');
INSERT INTO users(password, username)
values ('$2a$10$yIW7qWnQS2OWfhUjppt3aeDT/rvS3.EhkA0iAriU6t4PYcwEHHU1e', 'pavel');
INSERT INTO users(password, username)
values ('$2a$10$D6wkkOJ6uw4iIg7PRMyDU.1saDZJPKr0ib26YB91lbSTDZuO7rTiK', 'erik');
INSERT INTO users(password, username)
values ('$2a$10$3iZN3HAQcyc9MsLO17FO8.zd27vKpGFjdAi0rxJmiiJE/ppk9wA/O', 'commissar');
INSERT INTO users(password, username)
values ('$2a$10$K2cO9mBxCWZL06iVYMcQJuixYgq0yPSKyecEnAw4KNABGkfVlXcFe', 'escort');
INSERT INTO users(password, username)
values ('$2a$10$SDmClo4xz8.bYDYM5QmRUerjUGFk81V3jDlKos.iEvZD0qmzHoPPG', 'military_police');

INSERT INTO summon (user_id, status)
SELECT
    id as user_id,
    'NOT_STARTED' as status
FROM users;

INSERT INTO roles (user_id, name)
SELECT id,
       CASE
           WHEN username IN ('ivan', 'pavel', 'erik') THEN 'RECRUIT'
           WHEN username = 'commissar' THEN 'COMMISSAR'
           WHEN username = 'escort' THEN 'ESCORT'
           WHEN username = 'military_police' THEN 'MILITARY_POLICE'
           END as role_name
FROM users;


