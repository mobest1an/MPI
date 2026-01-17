-- db/seed.sql
-- Generates test data for:
--   users(id, username, password)
--   roles(user_id, name)
--   convoy(id, escort_id)
--   summon(id, military_branch, status, convoy_id, user_id)  -- user_id UNIQUE
--   complaint(id, created_at, status, assigned_to_id, convoy_id)
--
-- Usage example:
--   psql ... -v N_USERS=1000000 -v N_CONVOY=200000 -v N_COMPLAINT=500000 -f seed.sql
--
-- Meanings:
--   N_USERS      = number of GENERATED users in addition to the fixed ones below
--   N_CONVOY     = number of convoy rows
--   N_COMPLAINT  = number of complaint rows

\if :{?N_USERS} \else \set N_USERS 1000000 \endif
\if :{?N_CONVOY} \else \set N_CONVOY 1000000 \endif
\if :{?N_COMPLAINT} \else \set N_COMPLAINT 1000000 \endif

\timing on
\echo Seeding with N_USERS=:N_USERS, N_CONVOY=:N_CONVOY, N_COMPLAINT=:N_COMPLAINT

-- Speed (ok for load-test data)
SET synchronous_commit = OFF;
SET lock_timeout = '30s';
SET statement_timeout = '0';

-- Start clean every time
TRUNCATE TABLE complaint, summon, convoy, roles, users RESTART IDENTITY CASCADE;

-- -------------------------------------------------------------------
-- 0) Fixed users (your provided rows)
-- -------------------------------------------------------------------
INSERT INTO users(password, username) VALUES
('$2a$10$eiE.Yxfplitmpm1fMR4jy.heWCiO5JumEv7XG7vn0q.ukyP.cmU0a', 'ivan'),
('$2a$10$yIW7qWnQS2OWfhUjppt3aeDT/rvS3.EhkA0iAriU6t4PYcwEHHU1e', 'pavel'),
('$2a$10$D6wkkOJ6uw4iIg7PRMyDU.1saDZJPKr0ib26YB91lbSTDZuO7rTiK', 'erik'),
('$2a$10$3iZN3HAQcyc9MsLO17FO8.zd27vKpGFjdAi0rxJmiiJE/ppk9wA/O', 'commissar'),
('$2a$10$K2cO9mBxCWZL06iVYMcQJuixYgq0yPSKyecEnAw4KNABGkfVlXcFe', 'escort'),
('$2a$10$SDmClo4xz8.bYDYM5QmRUerjUGFk81V3jDlKos.iEvZD0qmzHoPPG', 'military_police');

-- -------------------------------------------------------------------
-- 1) Generated users (user_1 .. user_N)
-- -------------------------------------------------------------------
INSERT INTO users(username, password)
SELECT
  'user_' || gs::text,
  md5(random()::text)              -- quick random-ish password (not bcrypt)
FROM generate_series(1, :N_USERS) AS gs;

-- -------------------------------------------------------------------
-- 2) Roles (must satisfy roles_name_check)
--   - fixed users get fixed roles
--   - generated users: 10% ESCORT, 1% MILITARY_POLICE, rest RECRUIT
-- -------------------------------------------------------------------
INSERT INTO roles(user_id, name)
SELECT
  u.id,
  CASE
    WHEN u.username IN ('ivan','pavel','erik') THEN 'RECRUIT'
    WHEN u.username = 'commissar' THEN 'COMMISSAR'
    WHEN u.username = 'escort' THEN 'ESCORT'
    WHEN u.username = 'military_police' THEN 'MILITARY_POLICE'
    ELSE
      CASE
        WHEN (u.id % 100) = 0 THEN 'MILITARY_POLICE'
        WHEN (u.id % 10)  = 0 THEN 'ESCORT'
        ELSE 'RECRUIT'
      END
  END
FROM users u;

-- Helpful numbers for later (assumes identity ids are dense after TRUNCATE)
-- max user id:
\set QUIET 1
SELECT max(id) AS max_user_id FROM users \gset
SELECT count(*) AS escort_count FROM roles WHERE name='ESCORT' \gset
SELECT count(*) AS officer_count FROM roles WHERE name IN ('MILITARY_POLICE','COMMISSAR','ADMIN') \gset
\set QUIET 0

\echo max_user_id=:max_user_id escort_count=:escort_count officer_count=:officer_count

-- Safety: ensure we have at least 1 escort and 1 officer
DO $$
BEGIN
  IF (SELECT count(*) FROM roles WHERE name='ESCORT') = 0 THEN
    RAISE EXCEPTION 'No ESCORT users generated; cannot create convoy';
  END IF;
  IF (SELECT count(*) FROM roles WHERE name IN ('MILITARY_POLICE','COMMISSAR','ADMIN')) = 0 THEN
    RAISE EXCEPTION 'No officer users generated; cannot assign complaints';
  END IF;
END $$;

-- -------------------------------------------------------------------
-- 3) Convoys (escort_id NOT NULL FK -> users)
-- Efficient escort selection without ORDER BY random() per row:
-- pick escort users deterministically from the "every 10th id is escort" rule.
-- Also includes the fixed 'escort' user; but we don't rely on it.
-- -------------------------------------------------------------------
-- We’ll compute an escort_id that is a multiple of 10, within [1..max_user_id].
-- If max_user_id < 10, fallback to the first available escort.
WITH params AS (
  SELECT :max_user_id::bigint AS max_uid
),
fallback AS (
  SELECT user_id AS escort_id
  FROM roles
  WHERE name='ESCORT'
  ORDER BY user_id
  LIMIT 1
)
INSERT INTO convoy(escort_id)
SELECT
  CASE
    WHEN p.max_uid >= 10 THEN
      -- 10,20,30,... cycling
      LEAST(p.max_uid, (1 + ((gs - 1) % GREATEST(1, (p.max_uid/10))) ) * 10)
    ELSE
      (SELECT escort_id FROM fallback)
  END AS escort_id
FROM generate_series(1, :N_CONVOY) gs
CROSS JOIN params p;

-- -------------------------------------------------------------------
-- 4) Summon: exactly one row per user (user_id UNIQUE), status constrained
-- convoy_id populated only for IN_CONVOY / DONE, otherwise NULL
-- -------------------------------------------------------------------
INSERT INTO summon(military_branch, status, convoy_id, user_id)
SELECT
  (ARRAY['INFANTRY','NAVY','AIRFORCE','TEST'])[1 + (u.id % 4)] AS military_branch,
  st.status,
  CASE
    WHEN st.status IN ('IN_CONVOY','DONE')
      THEN 1 + ((u.id - 1) % :N_CONVOY::bigint)      -- map user->existing convoy id
    ELSE NULL
  END AS convoy_id,
  u.id AS user_id
FROM users u
CROSS JOIN LATERAL (
  SELECT (ARRAY['NOT_STARTED','IN_QUEUE','SUMMONED','WAITING_ESCORT','IN_CONVOY','DONE'])
         [1 + (u.id % 6)] AS status
) st;

-- -------------------------------------------------------------------
-- 5) Complaints: convoy_id NOT NULL; assigned_to_id nullable
-- We'll assign ~70% of complaints to an officer.
-- Officer selection without expensive ORDER BY random():
-- pick one of the first few officer ids by deterministic modulo.
-- -------------------------------------------------------------------
WITH officer_ids AS (
  SELECT user_id
  FROM roles
  WHERE name IN ('MILITARY_POLICE','COMMISSAR','ADMIN')
  ORDER BY user_id
  LIMIT 50              -- keep small list for assignment
),
officer_arr AS (
  SELECT array_agg(user_id) AS ids FROM officer_ids
)
INSERT INTO complaint(created_at, status, assigned_to_id, convoy_id)
SELECT
  now() - ((gs % 2592000) * interval '1 second'),   -- within ~30 days
  (ARRAY['NEW','IN_PROGRESS','COMPLETED'])[1 + (gs % 3)],
  CASE
    WHEN (random() < 0.7) THEN
      (oa.ids)[1 + ((gs - 1) % array_length(oa.ids, 1))]
    ELSE NULL
  END,
  1 + ((gs - 1) % :N_CONVOY::bigint)
FROM generate_series(1, :N_COMPLAINT) gs
CROSS JOIN officer_arr oa;

\echo Seed complete.
