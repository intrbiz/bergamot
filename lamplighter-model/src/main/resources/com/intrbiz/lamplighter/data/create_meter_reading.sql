CREATE OR REPLACE FUNCTION lamplighter.create_meter_reading(p_site_id UUID, p_reading_id UUID)
RETURNS INTEGER
LANGUAGE plpgsql
AS $body$
DECLARE
  v_table TEXT;
BEGIN
    v_table := quote_ident(lamplighter.get_schema(p_site_id)) || '.' || quote_ident(lamplighter.get_table_name('meter_reading', p_reading_id));
    EXECUTE $$ CREATE TABLE $$ || v_table || $$ (
      "site_id" uuid NOT NULL,
      "reading_id" uuid NOT NULL,
      "collected_at" timestamp with time zone NOT NULL,
      "count" BIGINT,
      "mean_rate" DOUBLE PRECISION,
      "one_minute_rate" DOUBLE PRECISION,
      "five_minute_rate" DOUBLE PRECISION,
      "fifteen_minute_rate" DOUBLE PRECISION,
      CONSTRAINT $$ || quote_ident(lamplighter.get_table_name('meter_reading', p_reading_id) || '_pk') || $$ PRIMARY KEY ("reading_id", "collected_at")
    ); $$;
    EXECUTE $$ALTER TABLE $$ || v_table || $$ OWNER TO $$ || quote_ident(lamplighter.get_default_owner()) || $$;$$;
    -- site constraint
    EXECUTE $$ALTER  TABLE $$ || v_table || $$ ADD CONSTRAINT "meter_reading_site_ck" CHECK (site_id = $$ || quote_literal(p_site_id) || $$::UUID);$$;
    -- reading specific
    IF (p_reading_id IS NOT NULL) THEN
      -- reading constraint
      EXECUTE $$ALTER  TABLE $$ || v_table || $$ ADD CONSTRAINT $$ || quote_ident(lamplighter.get_table_name('meter_reading', p_reading_id) || '_reading_ck') || $$ CHECK (reading_id = $$ || quote_literal(p_reading_id) || $$::UUID);$$;
      -- inherit
      EXECUTE $$ALTER  TABLE $$ || v_table || $$ INHERIT $$ || quote_ident(lamplighter.get_schema(p_site_id)) || '.' || quote_ident(lamplighter.get_table_name('meter_reading', NULL)) || $$;$$;
    ELSE
      -- inherit from the root table
      EXECUTE $$ALTER  TABLE $$ || v_table || $$ INHERIT "lamplighter"."meter_reading";$$;
    END IF;
    -- all done
    RETURN 1;
END;
$body$;