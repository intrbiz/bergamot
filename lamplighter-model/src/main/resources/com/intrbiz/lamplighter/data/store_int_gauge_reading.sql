CREATE OR REPLACE FUNCTION "lamplighter"."store_int_gauge_reading"("p_site_id" UUID, "p_reading_id" UUID, "p_collected_at" TIMESTAMP WITH TIME ZONE, "p_value" INTEGER, "p_warning" INTEGER, "p_critical" INTEGER, "p_min" INTEGER, "p_max" INTEGER)
RETURNS SETOF INTEGER AS
$BODY$
DECLARE
  v_table TEXT;
BEGIN
  v_table := quote_ident(lamplighter.get_schema(p_site_id)) || '.' || quote_ident(lamplighter.get_table_name('int_long_reading', p_reading_id));
  EXECUTE $$INSERT INTO $$ || v_table || $$ ("site_id", "reading_id", "collected_at", "value", "warning", "critical", "min", "max") 
             VALUES ($1, $2, $3, $4, $5, $6, $7, $8)$$
   USING p_site_id, p_reading_id, p_collected_at, p_value, p_warning, p_critical, p_min, p_max;
  RETURN NEXT 1;
  RETURN;
END;
$BODY$
LANGUAGE plpgsql;