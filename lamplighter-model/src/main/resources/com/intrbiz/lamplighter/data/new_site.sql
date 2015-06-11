CREATE OR REPLACE FUNCTION lamplighter.new_site(p_site_id UUID)
RETURNS INTEGER
LANGUAGE plpgsql
AS $body$
DECLARE
BEGIN
    -- create the site schema
    EXECUTE $$CREATE SCHEMA $$ || quote_ident(lamplighter.get_schema(p_site_id)) || $$ AUTHORIZATION $$ || quote_ident(lamplighter.get_default_owner()) || $$;$$;
    -- create the site metric tables
    PERFORM lamplighter.create_double_gauge_reading(p_site_id, NULL);
    PERFORM lamplighter.create_long_gauge_reading(p_site_id, NULL);
    PERFORM lamplighter.create_int_gauge_reading(p_site_id, NULL);
    PERFORM lamplighter.create_float_gauge_reading(p_site_id, NULL);
    -- all done
    RETURN 1;
END;
$body$;