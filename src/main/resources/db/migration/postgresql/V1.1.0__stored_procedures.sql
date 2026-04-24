CREATE OR REPLACE PROCEDURE sp_add_activity() LANGUAGE sql
BEGIN ATOMIC
INSERT INTO useractivity (username, activitytime)
VALUES ('anonymous', CURRENT_TIMESTAMP);
END;

CREATE OR REPLACE PROCEDURE sp_add_activity_with_user(p_username VARCHAR) LANGUAGE sql
BEGIN ATOMIC
INSERT INTO useractivity (username, activitytime)
VALUES (p_username, CURRENT_TIMESTAMP);
END;

-- ===== Functions (with return values) =====

-- Returns count as a basic type (scalar function)
CREATE OR REPLACE FUNCTION fn_count_active_users()
RETURNS INT LANGUAGE plpgsql AS $$
DECLARE
    v_count INT;
BEGIN
    SELECT COUNT(DISTINCT username) INTO v_count
    FROM useractivity;
    RETURN v_count;
END;
$$;

-- Returns tuples as table
CREATE OR REPLACE FUNCTION fn_get_active_users()
RETURNS TABLE(username VARCHAR, fullname VARCHAR) LANGUAGE plpgsql AS $$
BEGIN
    RETURN QUERY
    SELECT DISTINCT ua.username, up.fullname
    FROM useractivity ua
    INNER JOIN userprofile up ON ua.username = up.username
    ORDER BY ua.username;
END;
$$;

-- Returns active profiles as entities
CREATE OR REPLACE FUNCTION fn_get_active_profiles()
RETURNS TABLE(id BIGINT, username VARCHAR, fullname VARCHAR) LANGUAGE plpgsql AS $$
BEGIN
    RETURN QUERY
    SELECT up.id, up.username, up.fullname
    FROM useractivity ua
    INNER JOIN userprofile up ON ua.username = up.username;
END;
$$;

-- Returns activities with their profile as entities
CREATE OR REPLACE FUNCTION fn_get_activities_with_profiles()
RETURNS TABLE(
    id BIGINT,
    username VARCHAR,
    activitytime TIMESTAMP,
    profile_id BIGINT,
    profile_fullname VARCHAR
) LANGUAGE plpgsql AS $$
BEGIN
    RETURN QUERY
    SELECT ua.id, ua.username, ua.activitytime,
           up.id as profile_id, up.fullname as profile_fullname
    FROM useractivity ua
    INNER JOIN userprofile up ON ua.username = up.username;
END;
$$;

-- ===== Procedures (with output parameters using REF_CURSOR) =====

-- Returns count as a basic type output parameter
CREATE OR REPLACE PROCEDURE sp_count_active_users(OUT p_count INTEGER) LANGUAGE plpgsql AS $$
BEGIN
SELECT COUNT(DISTINCT username)
INTO p_count
FROM useractivity;
END;
$$;

-- Returns tuples via cursor output parameter
CREATE OR REPLACE PROCEDURE sp_get_active_users(
    p_cursor OUT REFCURSOR
) LANGUAGE plpgsql AS $$
BEGIN
    OPEN p_cursor FOR
    SELECT DISTINCT ua.username, up.fullname
    FROM useractivity ua
    INNER JOIN userprofile up ON ua.username = up.username
    ORDER BY ua.username;
END;
$$;

-- Returns active profiles via cursor output parameter
CREATE OR REPLACE PROCEDURE sp_get_active_profiles(
    p_cursor OUT REFCURSOR
) LANGUAGE plpgsql AS $$
BEGIN
    OPEN p_cursor FOR
    SELECT up.id, up.username, up.fullname
    FROM useractivity ua
    INNER JOIN userprofile up ON ua.username = up.username;
END;
$$;

-- Returns activities with profiles via cursor output parameter
CREATE OR REPLACE PROCEDURE sp_get_activities_with_profiles(
    p_cursor OUT REFCURSOR
) LANGUAGE plpgsql AS $$
BEGIN
    OPEN p_cursor FOR
    SELECT ua.id, ua.username, ua.activitytime,
           up.id as profile_id, up.fullname as profile_fullname
    FROM useractivity ua
    INNER JOIN userprofile up ON ua.username = up.username;
END;
$$;
