-- schema_test_with_progress.sql (PostgreSQL)
-- Prints progress per stage using RAISE NOTICE 'OK: ...'
-- Does NOT rely on constraint names. Leaves no data behind (ROLLBACK).

BEGIN;

-- Optional: ensure NOTICE messages are shown in most clients
SET client_min_messages TO NOTICE;

DO $$
DECLARE
  v_cnt  int;
  v_cols text;

  u1 bigint;
  u2 bigint;
  u3 bigint;
  c1 bigint;

  s text;
BEGIN
  ---------------------------------------------------------------------------
  -- 1) TABLES EXIST
  ---------------------------------------------------------------------------
  RAISE NOTICE '1) Checking tables exist...';

  PERFORM 1 FROM information_schema.tables WHERE table_schema='public' AND table_name='users';
  IF NOT FOUND THEN RAISE EXCEPTION 'Missing table public.users'; END IF;

  PERFORM 1 FROM information_schema.tables WHERE table_schema='public' AND table_name='convoy';
  IF NOT FOUND THEN RAISE EXCEPTION 'Missing table public.convoy'; END IF;

  PERFORM 1 FROM information_schema.tables WHERE table_schema='public' AND table_name='complaint';
  IF NOT FOUND THEN RAISE EXCEPTION 'Missing table public.complaint'; END IF;

  PERFORM 1 FROM information_schema.tables WHERE table_schema='public' AND table_name='roles';
  IF NOT FOUND THEN RAISE EXCEPTION 'Missing table public.roles'; END IF;

  PERFORM 1 FROM information_schema.tables WHERE table_schema='public' AND table_name='summon';
  IF NOT FOUND THEN RAISE EXCEPTION 'Missing table public.summon'; END IF;

  RAISE NOTICE 'OK 1) Tables exist';

  ---------------------------------------------------------------------------
  -- 2) COLUMNS + TYPES/LENGTHS + NULLABILITY + IDENTITY
  ---------------------------------------------------------------------------
  RAISE NOTICE '2) Checking columns, types, lengths, nullability, identity...';

  -- users
  SELECT count(*) INTO v_cnt FROM information_schema.columns
   WHERE table_schema='public' AND table_name='users' AND column_name='id'
     AND data_type='bigint' AND is_identity='YES';
  IF v_cnt <> 1 THEN RAISE EXCEPTION 'users.id must be bigint identity'; END IF;

  SELECT count(*) INTO v_cnt FROM information_schema.columns
   WHERE table_schema='public' AND table_name='users' AND column_name='password'
     AND data_type='character varying' AND character_maximum_length=255
     AND is_nullable='YES';
  IF v_cnt <> 1 THEN RAISE EXCEPTION 'users.password must be varchar(255) NULL'; END IF;

  SELECT count(*) INTO v_cnt FROM information_schema.columns
   WHERE table_schema='public' AND table_name='users' AND column_name='username'
     AND data_type='character varying' AND character_maximum_length=255
     AND is_nullable='YES';
  IF v_cnt <> 1 THEN RAISE EXCEPTION 'users.username must be varchar(255) NULL'; END IF;

  -- convoy
  SELECT count(*) INTO v_cnt FROM information_schema.columns
   WHERE table_schema='public' AND table_name='convoy' AND column_name='id'
     AND data_type='bigint' AND is_identity='YES';
  IF v_cnt <> 1 THEN RAISE EXCEPTION 'convoy.id must be bigint identity'; END IF;

  SELECT count(*) INTO v_cnt FROM information_schema.columns
   WHERE table_schema='public' AND table_name='convoy' AND column_name='escort_id'
     AND data_type='bigint' AND is_nullable='NO';
  IF v_cnt <> 1 THEN RAISE EXCEPTION 'convoy.escort_id must be bigint NOT NULL'; END IF;

  -- complaint
  SELECT count(*) INTO v_cnt FROM information_schema.columns
   WHERE table_schema='public' AND table_name='complaint' AND column_name='id'
     AND data_type='bigint' AND is_identity='YES';
  IF v_cnt <> 1 THEN RAISE EXCEPTION 'complaint.id must be bigint identity'; END IF;

  SELECT count(*) INTO v_cnt FROM information_schema.columns
   WHERE table_schema='public' AND table_name='complaint' AND column_name='created_at'
     AND data_type='timestamp without time zone' AND datetime_precision=6
     AND is_nullable='NO';
  IF v_cnt <> 1 THEN RAISE EXCEPTION 'complaint.created_at must be timestamp(6) NOT NULL'; END IF;

  SELECT count(*) INTO v_cnt FROM information_schema.columns
   WHERE table_schema='public' AND table_name='complaint' AND column_name='status'
     AND data_type='character varying' AND character_maximum_length=255
     AND is_nullable='NO';
  IF v_cnt <> 1 THEN RAISE EXCEPTION 'complaint.status must be varchar(255) NOT NULL'; END IF;

  SELECT count(*) INTO v_cnt FROM information_schema.columns
   WHERE table_schema='public' AND table_name='complaint' AND column_name='assigned_to_id'
     AND data_type='bigint' AND is_nullable='YES';
  IF v_cnt <> 1 THEN RAISE EXCEPTION 'complaint.assigned_to_id must be bigint NULL'; END IF;

  SELECT count(*) INTO v_cnt FROM information_schema.columns
   WHERE table_schema='public' AND table_name='complaint' AND column_name='convoy_id'
     AND data_type='bigint' AND is_nullable='NO';
  IF v_cnt <> 1 THEN RAISE EXCEPTION 'complaint.convoy_id must be bigint NOT NULL'; END IF;

  -- roles
  SELECT count(*) INTO v_cnt FROM information_schema.columns
   WHERE table_schema='public' AND table_name='roles' AND column_name='user_id'
     AND data_type='bigint' AND is_nullable='NO';
  IF v_cnt <> 1 THEN RAISE EXCEPTION 'roles.user_id must be bigint NOT NULL'; END IF;

  SELECT count(*) INTO v_cnt FROM information_schema.columns
   WHERE table_schema='public' AND table_name='roles' AND column_name='name'
     AND data_type='character varying' AND character_maximum_length=255
     AND is_nullable='YES';
  IF v_cnt <> 1 THEN RAISE EXCEPTION 'roles.name must be varchar(255) NULL'; END IF;

  -- summon
  SELECT count(*) INTO v_cnt FROM information_schema.columns
   WHERE table_schema='public' AND table_name='summon' AND column_name='id'
     AND data_type='bigint' AND is_identity='YES';
  IF v_cnt <> 1 THEN RAISE EXCEPTION 'summon.id must be bigint identity'; END IF;

  SELECT count(*) INTO v_cnt FROM information_schema.columns
   WHERE table_schema='public' AND table_name='summon' AND column_name='military_branch'
     AND data_type='character varying' AND character_maximum_length=255
     AND is_nullable='YES';
  IF v_cnt <> 1 THEN RAISE EXCEPTION 'summon.military_branch must be varchar(255) NULL'; END IF;

  SELECT count(*) INTO v_cnt FROM information_schema.columns
   WHERE table_schema='public' AND table_name='summon' AND column_name='status'
     AND data_type='character varying' AND character_maximum_length=255
     AND is_nullable='NO';
  IF v_cnt <> 1 THEN RAISE EXCEPTION 'summon.status must be varchar(255) NOT NULL'; END IF;

  SELECT count(*) INTO v_cnt FROM information_schema.columns
   WHERE table_schema='public' AND table_name='summon' AND column_name='convoy_id'
     AND data_type='bigint' AND is_nullable='YES';
  IF v_cnt <> 1 THEN RAISE EXCEPTION 'summon.convoy_id must be bigint NULL'; END IF;

  SELECT count(*) INTO v_cnt FROM information_schema.columns
   WHERE table_schema='public' AND table_name='summon' AND column_name='user_id'
     AND data_type='bigint' AND is_nullable='NO';
  IF v_cnt <> 1 THEN RAISE EXCEPTION 'summon.user_id must be bigint NOT NULL'; END IF;

  RAISE NOTICE 'OK 2) Columns/types/nullability/identity';

  ---------------------------------------------------------------------------
  -- 3) PRIMARY KEYS (by table+columns)
  ---------------------------------------------------------------------------
  RAISE NOTICE '3) Checking primary keys (by columns)...';

  SELECT string_agg(kcu.column_name, ',' ORDER BY kcu.ordinal_position) INTO v_cols
  FROM information_schema.table_constraints tc
  JOIN information_schema.key_column_usage kcu
    ON tc.constraint_name = kcu.constraint_name AND tc.table_schema = kcu.table_schema
  WHERE tc.table_schema='public' AND tc.table_name='users' AND tc.constraint_type='PRIMARY KEY';
  IF v_cols IS DISTINCT FROM 'id' THEN RAISE EXCEPTION 'users must have PK(id); got: %', v_cols; END IF;

  SELECT string_agg(kcu.column_name, ',' ORDER BY kcu.ordinal_position) INTO v_cols
  FROM information_schema.table_constraints tc
  JOIN information_schema.key_column_usage kcu
    ON tc.constraint_name = kcu.constraint_name AND tc.table_schema = kcu.table_schema
  WHERE tc.table_schema='public' AND tc.table_name='convoy' AND tc.constraint_type='PRIMARY KEY';
  IF v_cols IS DISTINCT FROM 'id' THEN RAISE EXCEPTION 'convoy must have PK(id); got: %', v_cols; END IF;

  SELECT string_agg(kcu.column_name, ',' ORDER BY kcu.ordinal_position) INTO v_cols
  FROM information_schema.table_constraints tc
  JOIN information_schema.key_column_usage kcu
    ON tc.constraint_name = kcu.constraint_name AND tc.table_schema = kcu.table_schema
  WHERE tc.table_schema='public' AND tc.table_name='complaint' AND tc.constraint_type='PRIMARY KEY';
  IF v_cols IS DISTINCT FROM 'id' THEN RAISE EXCEPTION 'complaint must have PK(id); got: %', v_cols; END IF;

  SELECT string_agg(kcu.column_name, ',' ORDER BY kcu.ordinal_position) INTO v_cols
  FROM information_schema.table_constraints tc
  JOIN information_schema.key_column_usage kcu
    ON tc.constraint_name = kcu.constraint_name AND tc.table_schema = kcu.table_schema
  WHERE tc.table_schema='public' AND tc.table_name='summon' AND tc.constraint_type='PRIMARY KEY';
  IF v_cols IS DISTINCT FROM 'id' THEN RAISE EXCEPTION 'summon must have PK(id); got: %', v_cols; END IF;

  RAISE NOTICE 'OK 3) Primary keys';

  ---------------------------------------------------------------------------
  -- 4) FOREIGN KEYS (by column + referenced table/column)
  ---------------------------------------------------------------------------
  RAISE NOTICE '4) Checking foreign keys (by column and referenced table/column)...';

  -- convoy.escort_id -> users.id
  SELECT count(*) INTO v_cnt
  FROM information_schema.table_constraints tc
  JOIN information_schema.key_column_usage kcu
    ON tc.constraint_name = kcu.constraint_name AND tc.table_schema = kcu.table_schema
  JOIN information_schema.constraint_column_usage ccu
    ON tc.constraint_name = ccu.constraint_name AND tc.table_schema = ccu.table_schema
  WHERE tc.table_schema='public' AND tc.table_name='convoy' AND tc.constraint_type='FOREIGN KEY'
    AND kcu.column_name='escort_id'
    AND ccu.table_schema='public' AND ccu.table_name='users' AND ccu.column_name='id';
  IF v_cnt < 1 THEN RAISE EXCEPTION 'Missing FK: convoy.escort_id -> users.id'; END IF;

  -- complaint.assigned_to_id -> users.id
  SELECT count(*) INTO v_cnt
  FROM information_schema.table_constraints tc
  JOIN information_schema.key_column_usage kcu
    ON tc.constraint_name = kcu.constraint_name AND tc.table_schema = kcu.table_schema
  JOIN information_schema.constraint_column_usage ccu
    ON tc.constraint_name = ccu.constraint_name AND tc.table_schema = ccu.table_schema
  WHERE tc.table_schema='public' AND tc.table_name='complaint' AND tc.constraint_type='FOREIGN KEY'
    AND kcu.column_name='assigned_to_id'
    AND ccu.table_schema='public' AND ccu.table_name='users' AND ccu.column_name='id';
  IF v_cnt < 1 THEN RAISE EXCEPTION 'Missing FK: complaint.assigned_to_id -> users.id'; END IF;

  -- complaint.convoy_id -> convoy.id
  SELECT count(*) INTO v_cnt
  FROM information_schema.table_constraints tc
  JOIN information_schema.key_column_usage kcu
    ON tc.constraint_name = kcu.constraint_name AND tc.table_schema = kcu.table_schema
  JOIN information_schema.constraint_column_usage ccu
    ON tc.constraint_name = ccu.constraint_name AND tc.table_schema = ccu.table_schema
  WHERE tc.table_schema='public' AND tc.table_name='complaint' AND tc.constraint_type='FOREIGN KEY'
    AND kcu.column_name='convoy_id'
    AND ccu.table_schema='public' AND ccu.table_name='convoy' AND ccu.column_name='id';
  IF v_cnt < 1 THEN RAISE EXCEPTION 'Missing FK: complaint.convoy_id -> convoy.id'; END IF;

  -- roles.user_id -> users.id
  SELECT count(*) INTO v_cnt
  FROM information_schema.table_constraints tc
  JOIN information_schema.key_column_usage kcu
    ON tc.constraint_name = kcu.constraint_name AND tc.table_schema = kcu.table_schema
  JOIN information_schema.constraint_column_usage ccu
    ON tc.constraint_name = ccu.constraint_name AND tc.table_schema = ccu.table_schema
  WHERE tc.table_schema='public' AND tc.table_name='roles' AND tc.constraint_type='FOREIGN KEY'
    AND kcu.column_name='user_id'
    AND ccu.table_schema='public' AND ccu.table_name='users' AND ccu.column_name='id';
  IF v_cnt < 1 THEN RAISE EXCEPTION 'Missing FK: roles.user_id -> users.id'; END IF;

  -- summon.convoy_id -> convoy.id
  SELECT count(*) INTO v_cnt
  FROM information_schema.table_constraints tc
  JOIN information_schema.key_column_usage kcu
    ON tc.constraint_name = kcu.constraint_name AND tc.table_schema = kcu.table_schema
  JOIN information_schema.constraint_column_usage ccu
    ON tc.constraint_name = ccu.constraint_name AND tc.table_schema = ccu.table_schema
  WHERE tc.table_schema='public' AND tc.table_name='summon' AND tc.constraint_type='FOREIGN KEY'
    AND kcu.column_name='convoy_id'
    AND ccu.table_schema='public' AND ccu.table_name='convoy' AND ccu.column_name='id';
  IF v_cnt < 1 THEN RAISE EXCEPTION 'Missing FK: summon.convoy_id -> convoy.id'; END IF;

  -- summon.user_id -> users.id
  SELECT count(*) INTO v_cnt
  FROM information_schema.table_constraints tc
  JOIN information_schema.key_column_usage kcu
    ON tc.constraint_name = kcu.constraint_name AND tc.table_schema = kcu.table_schema
  JOIN information_schema.constraint_column_usage ccu
    ON tc.constraint_name = ccu.constraint_name AND tc.table_schema = ccu.table_schema
  WHERE tc.table_schema='public' AND tc.table_name='summon' AND tc.constraint_type='FOREIGN KEY'
    AND kcu.column_name='user_id'
    AND ccu.table_schema='public' AND ccu.table_name='users' AND ccu.column_name='id';
  IF v_cnt < 1 THEN RAISE EXCEPTION 'Missing FK: summon.user_id -> users.id'; END IF;

  RAISE NOTICE 'OK 4) Foreign keys';

  ---------------------------------------------------------------------------
  -- 5) UNIQUE (summon.user_id)
  ---------------------------------------------------------------------------
  RAISE NOTICE '5) Checking UNIQUE constraints...';

  SELECT string_agg(kcu.column_name, ',' ORDER BY kcu.ordinal_position) INTO v_cols
  FROM information_schema.table_constraints tc
  JOIN information_schema.key_column_usage kcu
    ON tc.constraint_name = kcu.constraint_name AND tc.table_schema = kcu.table_schema
  WHERE tc.table_schema='public' AND tc.table_name='summon' AND tc.constraint_type='UNIQUE'
  GROUP BY tc.constraint_name
  HAVING string_agg(kcu.column_name, ',' ORDER BY kcu.ordinal_position) = 'user_id'
  LIMIT 1;

  IF v_cols IS DISTINCT FROM 'user_id' THEN
    RAISE EXCEPTION 'Missing UNIQUE constraint on summon(user_id)';
  END IF;

  RAISE NOTICE 'OK 5) UNIQUE constraints';

  ---------------------------------------------------------------------------
  -- 6) Behavioral enforcement tests (NOT NULL / FK / UNIQUE / CHECK)
  ---------------------------------------------------------------------------
  RAISE NOTICE '6) Running enforcement tests (NOT NULL, FK, UNIQUE)...';

  INSERT INTO users(username, password) VALUES ('u1','p') RETURNING id INTO u1;
  INSERT INTO users(username, password) VALUES ('u2','p') RETURNING id INTO u2;
  INSERT INTO users(username, password) VALUES ('u3','p') RETURNING id INTO u3;
  INSERT INTO convoy(escort_id) VALUES (u1) RETURNING id INTO c1;

  -- NOT NULL checks
  BEGIN
    INSERT INTO convoy(escort_id) VALUES (NULL);
    RAISE EXCEPTION 'Expected NOT NULL violation for convoy.escort_id';
  EXCEPTION WHEN not_null_violation THEN NULL;
  END;

  BEGIN
    INSERT INTO complaint(created_at, status, convoy_id) VALUES (NULL, 'NEW', c1);
    RAISE EXCEPTION 'Expected NOT NULL violation for complaint.created_at';
  EXCEPTION WHEN not_null_violation THEN NULL;
  END;

  BEGIN
    INSERT INTO complaint(created_at, status, convoy_id) VALUES (clock_timestamp(), NULL, c1);
    RAISE EXCEPTION 'Expected NOT NULL violation for complaint.status';
  EXCEPTION WHEN not_null_violation THEN NULL;
  END;

  BEGIN
    INSERT INTO complaint(created_at, status, convoy_id) VALUES (clock_timestamp(), 'NEW', NULL);
    RAISE EXCEPTION 'Expected NOT NULL violation for complaint.convoy_id';
  EXCEPTION WHEN not_null_violation THEN NULL;
  END;

  BEGIN
    INSERT INTO roles(user_id, name) VALUES (NULL, 'RECRUIT');
    RAISE EXCEPTION 'Expected NOT NULL violation for roles.user_id';
  EXCEPTION WHEN not_null_violation THEN NULL;
  END;

  BEGIN
    INSERT INTO summon(user_id, status) VALUES (u2, NULL);
    RAISE EXCEPTION 'Expected NOT NULL violation for summon.status';
  EXCEPTION WHEN not_null_violation THEN NULL;
  END;

  BEGIN
    INSERT INTO summon(user_id, status) VALUES (NULL, 'NOT_STARTED');
    RAISE EXCEPTION 'Expected NOT NULL violation for summon.user_id';
  EXCEPTION WHEN not_null_violation THEN NULL;
  END;

  -- FK enforcement checks
  BEGIN
    INSERT INTO convoy(escort_id) VALUES (999999999999);
    RAISE EXCEPTION 'Expected FK violation for convoy.escort_id -> users.id';
  EXCEPTION WHEN foreign_key_violation THEN NULL;
  END;

  BEGIN
    INSERT INTO complaint(created_at, status, convoy_id) VALUES (clock_timestamp(), 'NEW', 999999999999);
    RAISE EXCEPTION 'Expected FK violation for complaint.convoy_id -> convoy.id';
  EXCEPTION WHEN foreign_key_violation THEN NULL;
  END;

  BEGIN
    INSERT INTO complaint(created_at, status, assigned_to_id, convoy_id)
    VALUES (clock_timestamp(), 'NEW', 999999999999, c1);
    RAISE EXCEPTION 'Expected FK violation for complaint.assigned_to_id -> users.id';
  EXCEPTION WHEN foreign_key_violation THEN NULL;
  END;

  BEGIN
    INSERT INTO roles(user_id, name) VALUES (999999999999, 'RECRUIT');
    RAISE EXCEPTION 'Expected FK violation for roles.user_id -> users.id';
  EXCEPTION WHEN foreign_key_violation THEN NULL;
  END;

  BEGIN
    INSERT INTO summon(user_id, status) VALUES (999999999999, 'NOT_STARTED');
    RAISE EXCEPTION 'Expected FK violation for summon.user_id -> users.id';
  EXCEPTION WHEN foreign_key_violation THEN NULL;
  END;

  BEGIN
    INSERT INTO summon(user_id, status, convoy_id) VALUES (u2, 'NOT_STARTED', 999999999999);
    RAISE EXCEPTION 'Expected FK violation for summon.convoy_id -> convoy.id';
  EXCEPTION WHEN foreign_key_violation THEN NULL;
  END;

  -- UNIQUE enforcement: summon.user_id
  INSERT INTO summon(user_id, status) VALUES (u2, 'NOT_STARTED');
  BEGIN
    INSERT INTO summon(user_id, status) VALUES (u2, 'IN_QUEUE');
    RAISE EXCEPTION 'Expected UNIQUE violation for summon.user_id';
  EXCEPTION WHEN unique_violation THEN NULL;
  END;

  RAISE NOTICE 'OK 6) Enforcement tests (NOT NULL/FK/UNIQUE)';

  ---------------------------------------------------------------------------
  -- 7) CUSTOM CHECK CONSTRAINTS (allowed sets)
  ---------------------------------------------------------------------------
  RAISE NOTICE '7) Checking custom CHECK constraints (allowed value sets)...';

  -- 7.1 complaint.status allowed values must be accepted
  FOR s IN SELECT unnest(ARRAY['NEW','IN_PROGRESS','COMPLETED']) LOOP
    BEGIN
      INSERT INTO complaint(created_at, status, convoy_id)
      VALUES (clock_timestamp(), s, c1);
    EXCEPTION WHEN check_violation THEN
      RAISE EXCEPTION 'complaint.status rejected allowed value: %', s;
    END;
  END LOOP;

  -- complaint.status must reject an invalid value
  BEGIN
    INSERT INTO complaint(created_at, status, convoy_id)
    VALUES (clock_timestamp(), 'BAD', c1);
    RAISE EXCEPTION 'Expected CHECK violation for complaint.status (invalid value)';
  EXCEPTION WHEN check_violation THEN NULL;
  END;

  RAISE NOTICE 'OK 7.1) complaint.status CHECK';

  -- 7.2 roles.name allowed values must be accepted (and NULL must be accepted)
  FOR s IN SELECT unnest(ARRAY['RECRUIT','ESCORT','COMMISSAR','ADMIN','MILITARY_POLICE']) LOOP
    BEGIN
      INSERT INTO roles(user_id, name) VALUES (u1, s);
    EXCEPTION WHEN check_violation THEN
      RAISE EXCEPTION 'roles.name rejected allowed value: %', s;
    END;
  END LOOP;

  BEGIN
    INSERT INTO roles(user_id, name) VALUES (u1, NULL); -- CHECK should allow NULL
  EXCEPTION WHEN check_violation THEN
    RAISE EXCEPTION 'roles.name CHECK should allow NULL but rejected it';
  END;

  BEGIN
    INSERT INTO roles(user_id, name) VALUES (u1, 'BAD');
    RAISE EXCEPTION 'Expected CHECK violation for roles.name (invalid value)';
  EXCEPTION WHEN check_violation THEN NULL;
  END;

  RAISE NOTICE 'OK 7.2) roles.name CHECK';

  -- 7.3 summon.status allowed values must be accepted
  -- summon.user_id is UNIQUE, so use u3 and delete between iterations
  FOR s IN SELECT unnest(ARRAY[
      'NOT_STARTED','IN_QUEUE','SUMMONED','WAITING_ESCORT','IN_CONVOY','DONE'
  ]) LOOP
    BEGIN
      INSERT INTO summon(user_id, status) VALUES (u3, s);
      DELETE FROM summon WHERE user_id = u3;
    EXCEPTION WHEN check_violation THEN
      RAISE EXCEPTION 'summon.status rejected allowed value: %', s;
    END;
  END LOOP;

  BEGIN
    INSERT INTO summon(user_id, status) VALUES (u3, 'BAD');
    RAISE EXCEPTION 'Expected CHECK violation for summon.status (invalid value)';
  EXCEPTION WHEN check_violation THEN NULL;
  END;

  RAISE NOTICE 'OK 7.3) summon.status CHECK';

  RAISE NOTICE 'OK 7) Custom CHECK constraints';

  ---------------------------------------------------------------------------
  -- DONE
  ---------------------------------------------------------------------------
  RAISE NOTICE 'ALL OK: schema verification completed successfully.';
END $$;

ROLLBACK;
