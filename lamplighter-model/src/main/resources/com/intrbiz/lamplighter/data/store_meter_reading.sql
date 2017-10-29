CREATE OR REPLACE FUNCTION "lamplighter"."store_meter_reading"("p_site_id" UUID, "p_reading_id" UUID, "p_collected_at" TIMESTAMP WITH TIME ZONE, "p_count" BIGINT, "p_mean_rate" DOUBLE PRECISION, "p_one_minute_rate" DOUBLE PRECISION, "p_five_minute_rate" DOUBLE PRECISION, "p_fifteen_minute_rate" DOUBLE PRECISION)
RETURNS SETOF INTEGER AS
$BODY$
DECLARE
  v_table TEXT;
BEGIN
  v_table := quote_ident(lamplighter.get_schema(p_site_id)) || '.' || quote_ident(lamplighter.get_table_name('meter_reading', p_reading_id));
  EXECUTE $$INSERT INTO $$ || v_table || $$ ("site_id", "reading_id", "collected_at", "count", "mean_rate", "one_minute_rate", "five_minute_rate", "fifteen_minute_rate") 
             VALUES ($1, $2, $3, $4, $5, $6, $7, $8)$$
   USING p_site_id, p_reading_id, p_collected_at, p_count, p_mean_rate, p_one_minute_rate, p_five_minute_rate, p_fifteen_minute_rate;
  RETURN NEXT 1;
  RETURN;
END;
$BODY$
LANGUAGE plpgsql;
