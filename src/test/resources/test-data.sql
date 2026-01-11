-- Очистка таблиц
DELETE FROM complaint;
DELETE FROM convoy;
DELETE FROM roles;
DELETE FROM summon;
DELETE FROM users;

-- Создание тестовых пользователей
INSERT INTO users (id, username, password) VALUES 
(1, 'recruit1', '$2a$10$...'),  -- password
(2, 'commissar1', '$2a$10$...'),  -- password
(3, 'escort1', '$2a$10$...'),  -- password
(4, 'admin1', '$2a$10$...');  -- password

-- Создание ролей
INSERT INTO roles (user_id, name) VALUES 
(1, 'RECRUIT'),
(2, 'COMMISSAR'),
(3, 'ESCORT'),
(4, 'ADMIN');

-- Создание конвоя
INSERT INTO convoy (id, escort_id) VALUES 
(1, 3);

-- Создание записей призывников
INSERT INTO summon (id, user_id, status) VALUES 
(1, 1, 'NOT_STARTED');