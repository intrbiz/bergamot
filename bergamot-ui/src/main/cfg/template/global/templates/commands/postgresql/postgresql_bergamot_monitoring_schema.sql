-- ############################################################################
-- ###  Bergamot Monitoring functions for PostgreSQL                        ###
-- ###                                                                      ###
-- ###  This schema provides some simple functions which allow Bergamot     ###
-- ###  Monitoring to securely monitor a PostgreSQL database with least     ###
-- ###  priviledge.                                                         ###
-- ###                                                                      ###
-- ############################################################################

-- Monitoring Role

CREATE ROLE bergamot_monitoring NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE NOREPLICATION;

-- Monitoring Schema

CREATE SCHEMA bergamot_monitoring AUTHORIZATION postgres;

GRANT  USAGE ON SCHEMA bergamot_monitoring TO bergamot_monitoring;
REVOKE USAGE ON SCHEMA bergamot_monitoring TO public;

-- Replication Lag

CREATE OR REPLACE FUNCTION bergamot_monitoring.get_replication_lag()
RETURNS TABLE (slave TEXT, state TEXT, sync_state TEXT, sent_lag NUMERIC, write_lag NUMERIC, flush_lag NUMERIC, replay_lag NUMERIC)
LANGUAGE sql AS $$
    SELECT host(client_addr)::TEXT, state, sync_state, x.l - sent_location, x.l - write_location, x.l - flush_location, x.l - replay_location
    FROM pg_stat_replication, pg_current_xlog_location() x(l)
    WHERE application_name = 'walreceiver'
$$
SECURITY DEFINER;

ALTER FUNCTION bergamot_monitoring.get_replication_lag() OWNER TO postgres;

GRANT  EXECUTE ON FUNCTION bergamot_monitoring.get_replication_lag() TO bergamot_monitoring;
REVOKE EXECUTE ON FUNCTION bergamot_monitoring.get_replication_lag() FROM public;

-- Replication Slaves

CREATE OR REPLACE FUNCTION bergamot_monitoring.get_replication_slaves()
RETURNS TABLE (slave TEXT, state TEXT, sync_state TEXT)
LANGUAGE sql AS $$
    SELECT host(client_addr)::TEXT, state, sync_state
    FROM pg_stat_replication
    WHERE application_name = 'walreceiver'
$$
SECURITY DEFINER;

ALTER FUNCTION bergamot_monitoring.get_replication_slaves() OWNER TO postgres;

GRANT  EXECUTE ON FUNCTION bergamot_monitoring.get_replication_slaves() TO bergamot_monitoring;
REVOKE EXECUTE ON FUNCTION bergamot_monitoring.get_replication_slaves() FROM public;

-- Replication Streaming Slaves

CREATE OR REPLACE FUNCTION bergamot_monitoring.get_replication_streaming_slaves()
RETURNS BIGINT
LANGUAGE sql AS $$
    SELECT count(*)
    FROM pg_stat_replication
    WHERE state = 'streaming'
    AND application_name = 'walreceiver'
$$
SECURITY DEFINER;

ALTER FUNCTION bergamot_monitoring.get_replication_streaming_slaves() OWNER TO postgres;

GRANT  EXECUTE ON FUNCTION bergamot_monitoring.get_replication_streaming_slaves() TO bergamot_monitoring;
REVOKE EXECUTE ON FUNCTION bergamot_monitoring.get_replication_streaming_slaves() FROM public;

-- Database Size

CREATE OR REPLACE FUNCTION bergamot_monitoring.get_database_size(p_database_name TEXT)
RETURNS NUMERIC
LANGUAGE sql AS $$
    SELECT 
        pg_database_size(oid) / 1024.0 / 1024.0
    FROM pg_database
    WHERE datname = $1
$$
SECURITY DEFINER;

ALTER FUNCTION bergamot_monitoring.get_database_size(TEXT) OWNER TO postgres;

GRANT  EXECUTE ON FUNCTION bergamot_monitoring.get_database_size(TEXT) TO bergamot_monitoring;
REVOKE EXECUTE ON FUNCTION bergamot_monitoring.get_database_size(TEXT) FROM public;

-- Tables Size

CREATE OR REPLACE FUNCTION bergamot_monitoring.get_table_size(p_schema_name TEXT DEFAULT NULL, p_table_name TEXT DEFAULT NULL)
RETURNS TABLE (schema_name TEXT, table_name TEXT, total_size NUMERIC, table_size NUMERIC, relation_size NUMERIC, indexes_size NUMERIC, tuples BIGINT, pages BIGINT)
LANGUAGE sql AS $$
    SELECT 
        n.nspname::TEXT, 
        c.relname::TEXT, 
        pg_total_relation_size(c.oid) / 1024.0 / 1024.0, 
        pg_table_size(c.oid) / 1024.0 / 1024.0, 
        pg_relation_size(c.oid) / 1024.0 / 1024.0, 
        pg_indexes_size(c.oid) / 1024.0 / 1024.0, 
        c.reltuples::BIGINT, 
        c.relpages::BIGINT
    FROM pg_class c
    JOIN pg_namespace n ON (c.relnamespace = n.oid)
    WHERE c.relkind = 'r'
    AND n.nspname !~ '^pg_'
    AND n.nspname <> 'information_schema'
    AND ($1 IS NULL OR n.nspname = $1)
    AND ($2 IS NULL OR c.relname = $2)
$$
SECURITY DEFINER;

ALTER FUNCTION bergamot_monitoring.get_table_size(TEXT, TEXT) OWNER TO postgres;

GRANT  EXECUTE ON FUNCTION bergamot_monitoring.get_table_size(TEXT, TEXT) TO bergamot_monitoring;
REVOKE EXECUTE ON FUNCTION bergamot_monitoring.get_table_size(TEXT, TEXT) FROM public;

-- Slave or Master

CREATE OR REPLACE FUNCTION bergamot_monitoring.get_recovery()
RETURNS TABLE (in_recovery BOOLEAN, last_recovery_timestamp TIMESTAMP WITH TIME ZONE)
LANGUAGE sql AS $$
    SELECT
        pg_is_in_recovery(),
        pg_last_xact_replay_timestamp()
$$
SECURITY DEFINER;

ALTER FUNCTION bergamot_monitoring.get_recovery() OWNER TO postgres;

GRANT  EXECUTE ON FUNCTION bergamot_monitoring.get_recovery() TO bergamot_monitoring;
REVOKE EXECUTE ON FUNCTION bergamot_monitoring.get_recovery() FROM public;

-- Grant monitoring role to your monitoring user
-- GRANT bergamot_monitoring TO app_monitoring;
