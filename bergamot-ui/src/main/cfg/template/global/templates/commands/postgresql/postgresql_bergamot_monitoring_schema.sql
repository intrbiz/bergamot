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

GRANT USAGE ON SCHEMA bergamot_monitoring TO bergamot_monitoring;

-- Replication Lag

CREATE OR REPLACE FUNCTION bergamot_monitoring.get_replication_lag()
RETURNS TABLE (slave TEXT, state TEXT, sync_state TEXT, sent_lag NUMERIC, write_lag NUMERIC, flush_lag NUMERIC, replay_lag NUMERIC)
LANGUAGE sql AS $$
    SELECT host(client_addr)::TEXT, state, sync_state, x.l - sent_location, x.l - write_location, x.l - flush_location, x.l - replay_location
    FROM pg_stat_replication, pg_current_xlog_location() x(l)
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
$$
SECURITY DEFINER;

ALTER FUNCTION bergamot_monitoring.get_replication_streaming_slaves() OWNER TO postgres;

GRANT  EXECUTE ON FUNCTION bergamot_monitoring.get_replication_streaming_slaves() TO bergamot_monitoring;
REVOKE EXECUTE ON FUNCTION bergamot_monitoring.get_replication_streaming_slaves() FROM public;

-- Database Size

SELECT datname, pg_database_size(oid) / 1024.0 / 1024.0 FROM pg_database;

-- Grant monitoring role to your monitoring user
-- GRANT bergamot_monitoring TO app_monitoring;
