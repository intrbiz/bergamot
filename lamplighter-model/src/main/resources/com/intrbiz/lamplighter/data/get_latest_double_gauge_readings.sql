CREATE OR REPLACE FUNCTION "lamplighter"."get_latest_double_gauge_readings"("p_site_id" UUID, "p_reading_id" UUID, "p_limit" INTEGER)
RETURNS SETOF "lamplighter"."t_double_gauge_reading" AS
$BODY$
DECLARE
  v_table TEXT;
BEGIN
  v_table := quote_ident(lamplighter.get_schema(p_site_id)) || '.' || quote_ident(lamplighter.get_table_name('double_gauge_reading', p_reading_id));
  RETURN QUERY EXECUTE $$SELECT q.* FROM (SELECT * FROM $$ || v_table || $$ WHERE site_id = ? AND reading_id = ? ORDER BY collated_at DESC LIMIT ?) q ORDER BY q.collated_at ASC$$ USING p_site_id, p_reading_id, p_limit;
END;
$BODY$
LANGUAGE plpgsql;