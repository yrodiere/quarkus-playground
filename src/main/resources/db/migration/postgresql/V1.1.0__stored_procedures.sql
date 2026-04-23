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

CREATE OR REPLACE PROCEDURE sp_count_active_users(OUT p_count INTEGER) LANGUAGE plpgsql AS $$
BEGIN
    SELECT COUNT(DISTINCT username)
    INTO p_count
    FROM useractivity;
END;
$$;

-- Returns result set as table
CREATE OR REPLACE FUNCTION sp_get_active_users_result_set() RETURNS TABLE(username VARCHAR, fullname VARCHAR) LANGUAGE plpgsql AS $$
BEGIN
    RETURN QUERY
    SELECT DISTINCT ua.username, up.fullname
    FROM useractivity ua
    INNER JOIN userprofile up ON ua.username = up.username
    ORDER BY ua.username;
END;
$$;

-- Returns count as a basic type (function return value, not output parameter)
CREATE OR REPLACE FUNCTION sp_count_active_users_as_return() RETURNS INTEGER LANGUAGE plpgsql AS $$
DECLARE
    v_count INTEGER;
BEGIN
    SELECT COUNT(DISTINCT username)
    INTO v_count
    FROM useractivity;
    RETURN v_count;
END;
$$;

-- Returns active profiles as entities
CREATE OR REPLACE FUNCTION sp_get_active_profiles()
RETURNS TABLE(
    id BIGINT,
    username VARCHAR,
    fullname VARCHAR
) LANGUAGE plpgsql AS $$
BEGIN
    RETURN QUERY
    SELECT up.id, up.username, up.fullname
    FROM useractivity ua
    INNER JOIN userprofile up ON ua.username = up.username
    ORDER BY up.username, ua.activitytime;
END;
$$;

-- Returns activities with their profile as entities
CREATE OR REPLACE FUNCTION sp_get_activities_with_profiles()
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
       up.id, up.fullname
FROM useractivity ua
         INNER JOIN userprofile up ON ua.username = up.username
ORDER BY up.username, ua.activitytime;
END;
$$;

CREATE OR REPLACE PROCEDURE sp_get_users_cursor(OUT p_count refcursor) LANGUAGE plpgsql AS $$
BEGIN
    OPEN p_count FOR
    SELECT DISTINCT ua.username, up.fullname
    FROM useractivity ua
    INNER JOIN userprofile up ON ua.username = up.username
    ORDER BY ua.username;
END;
$$;
