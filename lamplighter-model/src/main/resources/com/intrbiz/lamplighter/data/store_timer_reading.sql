CREATE OR REPLACE FUNCTION "lamplighter"."store_timer_reading"("p_site_id" UUID, "p_reading_id" UUID, "p_collected_at" TIMESTAMP WITH TIME ZONE, "p_count" BIGINT, "p_mean_rate" DOUBLE PRECISION, "p_one_minute_rate" DOUBLE PRECISION, "p_five_minute_rate" DOUBLE PRECISION, "p_fifteen_minute_rate" DOUBLE PRECISION, "p_median" DOUBLE PRECISION, "p_mean" DOUBLE PRECISION, "p_min" DOUBLE PRECISION, "p_max" DOUBLE PRECISION, "p_std_dev" DOUBLE PRECISION, "p_the_75th_percentile" DOUBLE PRECISION, "p_the_95th_percentile" DOUBLE PRECISION, "p_the_98th_percentile" DOUBLE PRECISION, "p_the_99th_percentile" DOUBLE PRECISION, "p_the_999th_percentile" DOUBLE PRECISION)
RETURNS SETOF INTEGER AS
$BODY$
DECLARE
  v_table TEXT;
BEGIN
  v_table := quote_ident(lamplighter.get_schema(p_site_id)) || '.' || quote_ident(lamplighter.get_table_name('timer_reading', p_reading_id));
  EXECUTE $$INSERT INTO $$ || v_table || $$ ("site_id", "reading_id", "collected_at", "count", "mean_rate", "one_minute_rate", "five_minute_rate", "fifteen_minute_rate", "median", "mean", "min", "max", "std_dev", "the_75th_percentile", "the_95th_percentile", "the_98th_percentile", "the_99th_percentile", "the_999th_percentile") 
             VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15, $16, $17, $18)$$
   USING p_site_id, p_reading_id, p_collected_at, p_count, p_mean_rate, p_one_minute_rate, p_five_minute_rate, p_fifteen_minute_rate, p_median, p_mean, p_min, p_max, p_std_dev, p_the_75th_percentile, p_the_95th_percentile, p_the_98th_percentile, p_the_99th_percentile, p_the_999th_percentile;
  RETURN NEXT 1;
  RETURN;
END;
$BODY$
LANGUAGE plpgsql;
