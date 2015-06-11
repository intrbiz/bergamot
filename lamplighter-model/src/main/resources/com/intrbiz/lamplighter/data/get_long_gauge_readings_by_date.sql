CREATE OR REPLACE FUNCTION "lamplighter"."get_long_gauge_readings_by_date"("p_site_id" UUID, "p_reading_id" UUID, "p_start" TIMESTAMP WITH TIME ZONE, "p_end" TIMESTAMP WITH TIME ZONE, "p_rollup" TEXT DEFAULT 'hour', "p_agg" TEXT DEFAULT 'avg')
RETURNS SETOF "lamplighter"."t_long_gauge_reading" AS
$BODY$
DECLARE
  v_table TEXT;
  v_query TEXT;
BEGIN
  v_table := quote_ident(lamplighter.get_schema(p_site_id)) || '.' || quote_ident(lamplighter.get_table_name('long_gauge_reading', p_reading_id));
  v_query := $$SELECT site_id, reading_id, date_trunc($$ || quote_literal(p_rollup) || $$, collected_at), $$ || quote_ident(p_agg) || $$(value)::BIGINT, max(warning), max(critical), min("min"), max("max")
                FROM $$ || v_table || $$ 
                WHERE site_id = $1 
                 AND reading_id = $2 
                 AND (collected_at BETWEEN $3 AND $4) 
                GROUP BY 1, 2, 3 
                ORDER BY 3 ASC$$;
  RETURN QUERY EXECUTE v_query USING p_site_id, p_reading_id, p_start, p_end;
END;
$BODY$
LANGUAGE plpgsql;