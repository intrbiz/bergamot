CREATE OR REPLACE FUNCTION "lamplighter"."get_int_gauge_readings_by_date"("p_site_id" UUID, "p_reading_id" UUID, "p_start" TIMESTAMP WITH TIME ZONE, "p_end" TIMESTAMP WITH TIME ZONE, "p_rollup" BIGINT DEFAULT 300000, "p_agg" TEXT DEFAULT 'avg')
RETURNS SETOF "lamplighter"."t_int_gauge_reading" AS
$BODY$
DECLARE
  v_table TEXT;
  v_query TEXT;
  v_poll_interval BIGINT;
  v_start_time TIMESTAMP WITH TIME ZONE;
  v_end_time TIMESTAMP WITH TIME ZONE;
BEGIN
  v_start_time := lamplighter.round_time(p_start, p_rollup);
  v_end_time := lamplighter.round_time(p_end, p_rollup);
  SELECT poll_interval INTO v_poll_interval FROM lamplighter.check_reading WHERE id = p_reading_id;
  v_table := 'lamplighter.int_gauge_reading';
  -- query
  v_query := $$ SELECT 
    $1 AS site_id, 
    $2 AS reading_id, 
    i.v AS collected_at, 
    CASE WHEN q."value" IS NULL THEN 0 ELSE q."value" END,
    CASE WHEN q."warning" IS NULL THEN 0 ELSE q."warning" END,
    CASE WHEN q."critical" IS NULL THEN 0 ELSE q."critical" END,
    CASE WHEN q."min" IS NULL THEN 0 ELSE q."min" END,
    CASE WHEN q."max" IS NULL THEN 0 ELSE q."max" END
   FROM generate_series($3, ($4 - (($5 / 1000) ||' seconds')::interval), (($5 / 1000) ||' seconds')::interval) i(v) 
   LEFT JOIN ( 
    SELECT 
 	 lamplighter.round_time(collected_at, $5) as collected_at, 
 	 $$ || quote_ident(p_agg) || $$("value")::INTEGER AS "value",
 	 $$ || quote_ident(p_agg) || $$("warning")::INTEGER AS "warning",
 	 $$ || quote_ident(p_agg) || $$("critical")::INTEGER AS "critical",
 	 $$ || quote_ident(p_agg) || $$("min")::INTEGER AS "min",
 	 $$ || quote_ident(p_agg) || $$("max")::INTEGER AS "max"
    FROM $$ || v_table || $$ 
    WHERE reading_id = $2
      AND collected_at BETWEEN ($3 - (($5 / 1000) ||' seconds')::interval) AND ($4 + (($5 / 1000) ||' seconds')::interval) 
    GROUP BY 1 
   ) q 
   ON (q.collected_at = i.v) 
   ORDER BY collected_at ASC $$;
  RETURN QUERY EXECUTE v_query USING p_site_id, p_reading_id, v_start_time, v_end_time, p_rollup;
END;
$BODY$
LANGUAGE plpgsql;
