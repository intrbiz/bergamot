CREATE OR REPLACE FUNCTION lamplighter.new_reading(p_site_id UUID, p_reading_id UUID, p_check_id UUID, p_name TEXT, p_summary TEXT, p_description TEXT, p_unit TEXT, p_reading_type TEXT, p_poll_interval BIGINT)
RETURNS INTEGER
LANGUAGE plpgsql
AS $body$
DECLARE
BEGIN
    -- add the metadata
    INSERT INTO lamplighter.check_reading (id, site_id, check_id, "name", "summary", "description", "unit", reading_type, "schema", "table", created, updated, poll_interval)
      VALUES (p_reading_id, p_site_id, p_check_id, p_name, p_summary, p_description, p_unit, p_reading_type, lamplighter.get_schema(p_site_id), lamplighter.get_table_name(p_reading_type, p_reading_id), clock_timestamp(), NULL, p_poll_interval);
    -- create the metric table
    IF (p_reading_type = 'double_gauge_reading') THEN
      PERFORM lamplighter.create_double_gauge_reading(p_site_id, p_reading_id);
    ELSIF (p_reading_type = 'long_gauge_reading') THEN
      PERFORM lamplighter.create_long_gauge_reading(p_site_id, p_reading_id);
    ELSIF (p_reading_type = 'int_gauge_reading') THEN
      PERFORM lamplighter.create_int_gauge_reading(p_site_id, p_reading_id);
    ELSIF (p_reading_type = 'float_gauge_reading') THEN
      PERFORM lamplighter.create_float_gauge_reading(p_site_id, p_reading_id);      
    ELSE
      RAISE EXCEPTION 'No such reading type: %', p_reading_type;
    END IF;
    -- all done
    RETURN 1;
END;
$body$;