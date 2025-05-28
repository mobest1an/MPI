#!/bin/sh
set -e

psql --username postgres --dbname postgres <<-EOSQL
  CREATE DATABASE mpi;
  CREATE ROLE mpi WITH ENCRYPTED PASSWORD 'mpi' LOGIN;
  GRANT ALL PRIVILEGES ON DATABASE mpi TO mpi;
EOSQL

psql --username postgres --dbname mpi <<-EOSQL
  GRANT ALL ON SCHEMA public TO mpi;
EOSQL
