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

CREATE OR REPLACE FUNCTION sp_get_active_users() RETURNS TABLE(username VARCHAR, fullname VARCHAR) LANGUAGE plpgsql AS $$
BEGIN
    RETURN QUERY
    SELECT DISTINCT ua.username, up.fullname
    FROM useractivity ua
    INNER JOIN userprofile up ON ua.username = up.username
    ORDER BY ua.username;
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
