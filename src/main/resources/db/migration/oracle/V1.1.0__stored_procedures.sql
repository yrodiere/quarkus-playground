CREATE OR REPLACE PROCEDURE sp_add_activity
IS
BEGIN
    INSERT INTO useractivity (username, activitytime)
    VALUES ('anonymous', CURRENT_TIMESTAMP);
END;
/

CREATE OR REPLACE PROCEDURE sp_add_activity_with_user(p_username IN VARCHAR2)
IS
BEGIN
    INSERT INTO useractivity (username, activitytime)
    VALUES (p_username, CURRENT_TIMESTAMP);
END;
/

-- ===== Functions (with return values) =====

-- Returns count as a basic type (scalar function)
CREATE OR REPLACE FUNCTION fn_count_active_users
RETURN NUMBER
IS
    v_count NUMBER;
BEGIN
    SELECT COUNT(DISTINCT username) INTO v_count
    FROM useractivity;
    RETURN v_count;
END;
/

-- Returns tuples via cursor
CREATE OR REPLACE FUNCTION fn_get_active_users
RETURN SYS_REFCURSOR
IS
    v_cursor SYS_REFCURSOR;
BEGIN
    OPEN v_cursor FOR
    SELECT DISTINCT ua.username, up.fullname
    FROM useractivity ua
    INNER JOIN userprofile up ON ua.username = up.username
    ORDER BY ua.username;
    RETURN v_cursor;
END;
/

-- Returns active profiles via cursor
CREATE OR REPLACE FUNCTION fn_get_active_profiles
RETURN SYS_REFCURSOR
IS
    v_cursor SYS_REFCURSOR;
BEGIN
    OPEN v_cursor FOR
    SELECT up.id, up.username, up.fullname
    FROM useractivity ua
    INNER JOIN userprofile up ON ua.username = up.username;
    RETURN v_cursor;
END;
/

-- Returns activities with profiles via cursor
CREATE OR REPLACE FUNCTION fn_get_activities_with_profiles
RETURN SYS_REFCURSOR
IS
    v_cursor SYS_REFCURSOR;
BEGIN
    OPEN v_cursor FOR
    SELECT ua.id, ua.username, ua.activitytime,
           up.id as profile_id, up.fullname as profile_fullname
    FROM useractivity ua
    INNER JOIN userprofile up ON ua.username = up.username;
    RETURN v_cursor;
END;
/

-- ===== Procedures (with output parameters using SYS_REFCURSOR) =====

-- Returns count as a basic type output parameter
CREATE OR REPLACE PROCEDURE sp_count_active_users(p_count OUT NUMBER)
IS
BEGIN
SELECT COUNT(DISTINCT username)
INTO p_count
FROM useractivity;
END;
/

-- Returns tuples via cursor output parameter
CREATE OR REPLACE PROCEDURE sp_get_active_users(
    p_cursor OUT SYS_REFCURSOR
)
IS
BEGIN
    OPEN p_cursor FOR
    SELECT DISTINCT ua.username, up.fullname
    FROM useractivity ua
    INNER JOIN userprofile up ON ua.username = up.username
    ORDER BY ua.username;
END;
/

-- Returns active profiles via cursor output parameter
CREATE OR REPLACE PROCEDURE sp_get_active_profiles(
    p_cursor OUT SYS_REFCURSOR
)
IS
BEGIN
    OPEN p_cursor FOR
    SELECT up.id, up.username, up.fullname
    FROM useractivity ua
    INNER JOIN userprofile up ON ua.username = up.username;
END;
/

-- Returns activities with profiles via cursor output parameter
CREATE OR REPLACE PROCEDURE sp_get_activities_with_profiles(
    p_cursor OUT SYS_REFCURSOR
)
IS
BEGIN
    OPEN p_cursor FOR
    SELECT ua.id, ua.username, ua.activitytime,
           up.id as profile_id, up.fullname as profile_fullname
    FROM useractivity ua
    INNER JOIN userprofile up ON ua.username = up.username;
END;
/
