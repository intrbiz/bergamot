CREATE OR REPLACE FUNCTION "lamplighter"."get_latest_timer_readings"("p_site_id" UUID, "p_reading_id" UUID, "p_limit" INTEGER)
RETURNS SETOF "lamplighter"."t_timer_reading" AS
$BODY$
DECLARE
  v_table TEXT;
BEGIN
  v_table := 'lamplighter.timer_reading';
  RETURN QUERY EXECUTE $$SELECT q.* FROM (SELECT * FROM $$ || v_table || $$ WHERE reading_id = $1 ORDER BY collected_at DESC LIMIT $2) q ORDER BY q.collected_at ASC$$ USING p_reading_id, p_limit;
END;
$BODY$
LANGUAGE plpgsql;
