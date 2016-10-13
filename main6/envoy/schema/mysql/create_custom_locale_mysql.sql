/***
This PL/SQL procedure creates a custom locale in GlobalSight. It
adds a locale record to LOCALE table and partition TM index tables.

The procedure takes 3 parameters, locale id, language code and country
code. Language code should be lower case two letter. Country code
should be upper case two letter. If these codes are not in LANGUAGE or
COUNTRY tables, they are added. Locale id must be greater than any
other existing ids.

Invoke sqlplus and type as follows for example.

SQL> @create_custom_locale 1001 en XX

***/


DECLARE
    TYPE string_array IS VARRAY(8) OF VARCHAR2(100);
    table_names string_array;
    partition_name VARCHAR2(10);
    partition_value INTEGER;

    counts INTEGER;
    locale_id INTEGER;
    lang_code VARCHAR2(3);
    country_code VARCHAR2(3);
BEGIN
    table_names := string_array('segment_tm_token_t', 'segment_tm_token_l',
				'page_tm_token_t', 'page_tm_token_l');

    locale_id := &1;
    lang_code := '&2';
    country_code := '&3';

    --  make sure LOCALE_SEQ is always greater than any locale id
    SELECT seq.count INTO counts FROM sequence seq
        WHERE name = 'LOCALE_SEQ';
    IF counts <= locale_id THEN
	UPDATE sequence SET count = locale_id + 1 WHERE name = 'LOCALE_SEQ';
    END IF;

    --  add lang_code if it's not in language table yet
    SELECT count(*) INTO counts FROM language
        WHERE iso_lang_code = lang_code;
    IF counts = 0 THEN
        INSERT INTO language VALUES (lang_code);
    END IF;

    --  add country_code if it's not in country table yet
    SELECT count(*) INTO counts FROM country
        WHERE iso_country_code = country_code;
    IF counts = 0 THEN
        INSERT INTO country VALUES (country_code);
    END IF;

    --  insert a row to locale table
    INSERT INTO locale VALUES (locale_id, lang_code, country_code, 'N');


    --  partition index tables
    partition_name := lang_code || '_' || country_code;
    partition_value := locale_id + 1;
    FOR i IN table_names.FIRST .. table_names.LAST LOOP
        EXECUTE IMMEDIATE
            'ALTER TABLE ' || table_names(i) || ' ADD PARTITION '
            || partition_name || ' VALUES LESS THAN ('
            || partition_value || ')';
    END LOOP;

    COMMIT;

END;
/
