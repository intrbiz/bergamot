CREATE OR REPLACE FUNCTION "lamplighter"."get_latest_long_gauge_readings"("p_site_id" UUID, "p_reading_id" UUID, "p_limit" INTEGER)
RETURNS SETOF "lamplighter"."t_long_gauge_reading" AS
$BODY$
DECLARE
  v_table TEXT;
BEGIN
  v_table := 'lamplighter.long_gauge_reading';
  RETURN QUERY EXECUTE $$SELECT q.* FROM (SELECT * FROM $$ || v_table || $$ WHERE reading_id = $1 ORDER BY collected_at DESC LIMIT $2) q ORDER BY q.collected_at ASC$$ USING p_reading_id, p_limit;
END;
$BODY$
LANGUAGE plpgsql;
