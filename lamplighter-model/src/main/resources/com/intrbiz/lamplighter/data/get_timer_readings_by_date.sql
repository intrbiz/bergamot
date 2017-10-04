CREATE OR REPLACE FUNCTION "lamplighter"."get_timer_readings_by_date"("p_site_id" UUID, "p_reading_id" UUID, "p_start" TIMESTAMP WITH TIME ZONE, "p_end" TIMESTAMP WITH TIME ZONE, "p_rollup" BIGINT DEFAULT 300000, "p_agg" TEXT DEFAULT 'avg')
RETURNS SETOF "lamplighter"."t_timer_reading" AS
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
  v_table := quote_ident(lamplighter.get_schema(p_site_id)) || '.' || quote_ident(lamplighter.get_table_name('timer_reading', p_reading_id));
  -- query
  v_query := $$ SELECT 
    $1 AS site_id, 
    $2 AS reading_id, 
    i.v AS collected_at, 
    coalesce(q."count", 0),
    coalesce(q."mean_rate", 0),
    coalesce(q."one_minute_rate", 0),
    coalesce(q."five_minute_rate", 0),
    coalesce(q."fifteen_minute_rate", 0),
    coalesce(q."median", 0),
    coalesce(q."mean", 0),
    coalesce(q."min", 0),
    coalesce(q."max", 0),
    coalesce(q."std_dev", 0),
    coalesce(q."the_75th_percentile", 0),
    coalesce(q."the_95th_percentile", 0),
    coalesce(q."the_98th_percentile", 0),
    coalesce(q."the_99th_percentile", 0),
    coalesce(q."the_999th_percentile", 0)
   FROM generate_series($3, ($4 - (($5 / 1000) ||' seconds')::interval), (($5 / 1000) ||' seconds')::interval) i(v) 
   LEFT JOIN ( 
    SELECT 
     lamplighter.round_time(collected_at, $5) as collected_at, 
     $$ || quote_ident(p_agg) || $$("count")::BIGINT AS "count",
     $$ || quote_ident(p_agg) || $$("mean_rate")::DOUBLE PRECISION AS "mean_rate",
     $$ || quote_ident(p_agg) || $$("one_minute_rate")::DOUBLE PRECISION AS "one_minute_rate",
     $$ || quote_ident(p_agg) || $$("five_minute_rate")::DOUBLE PRECISION AS "five_minute_rate",
     $$ || quote_ident(p_agg) || $$("fifteen_minute_rate")::DOUBLE PRECISION AS "fifteen_minute_rate",
     $$ || quote_ident(p_agg) || $$("median")::DOUBLE PRECISION AS "median",
     $$ || quote_ident(p_agg) || $$("mean")::DOUBLE PRECISION AS "mean",
     $$ || 'min'              || $$("min")::DOUBLE PRECISION AS "min",
     $$ || 'max'              || $$("max")::DOUBLE PRECISION AS "max",
     $$ || quote_ident(p_agg) || $$("std_dev")::DOUBLE PRECISION AS "std_dev",
     $$ || quote_ident(p_agg) || $$("the_75th_percentile")::DOUBLE PRECISION AS "the_75th_percentile",
     $$ || quote_ident(p_agg) || $$("the_95th_percentile")::DOUBLE PRECISION AS "the_95th_percentile",
     $$ || quote_ident(p_agg) || $$("the_98th_percentile")::DOUBLE PRECISION AS "the_98th_percentile",
     $$ || quote_ident(p_agg) || $$("the_99th_percentile")::DOUBLE PRECISION AS "the_99th_percentile",
     $$ || quote_ident(p_agg) || $$("the_999th_percentile")::DOUBLE PRECISION AS "the_999th_percentile"
     //
    FROM $$ || v_table || $$ 
    WHERE collected_at BETWEEN ($3 - (($5 / 1000) ||' seconds')::interval) AND ($4 + (($5 / 1000) ||' seconds')::interval) 
    GROUP BY 1 
   ) q 
   ON (q.collected_at = i.v) 
   ORDER BY collected_at ASC $$;
  RETURN QUERY EXECUTE v_query USING p_site_id, p_reading_id, v_start_time, v_end_time, p_rollup;
END;
$BODY$
LANGUAGE plpgsql;
