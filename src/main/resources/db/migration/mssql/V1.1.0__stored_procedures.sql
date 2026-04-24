CREATE OR ALTER PROCEDURE sp_add_activity
AS
BEGIN
    INSERT INTO useractivity (username, activitytime)
    VALUES ('anonymous', CURRENT_TIMESTAMP);
END;
GO

CREATE OR ALTER PROCEDURE sp_add_activity_with_user
    @p_username VARCHAR(255)
AS
BEGIN
    INSERT INTO useractivity (username, activitytime)
    VALUES (@p_username, CURRENT_TIMESTAMP);
END;
GO

-- ===== Functions (with return values) =====

-- Returns count as a basic type (scalar function)
CREATE OR ALTER FUNCTION fn_count_active_users()
RETURNS INT
AS
BEGIN
    DECLARE @v_count INT;
    SELECT @v_count = COUNT(DISTINCT username)
    FROM useractivity;
    RETURN @v_count;
END;
GO

-- Returns tuples as table
CREATE OR ALTER FUNCTION fn_get_active_users()
RETURNS TABLE
AS
RETURN (
    SELECT DISTINCT ua.username, up.fullname
    FROM useractivity ua
    INNER JOIN userprofile up ON ua.username = up.username
);
GO

-- Returns active profiles as entities
CREATE OR ALTER FUNCTION fn_get_active_profiles()
RETURNS TABLE
AS
RETURN (
    SELECT up.id, up.username, up.fullname
    FROM useractivity ua
    INNER JOIN userprofile up ON ua.username = up.username
);
GO

-- Returns activities with their profile as entities
CREATE OR ALTER FUNCTION fn_get_activities_with_profiles()
RETURNS TABLE
AS
RETURN (
    SELECT ua.id, ua.username, ua.activitytime,
           up.id as profile_id, up.fullname as profile_fullname
    FROM useractivity ua
    INNER JOIN userprofile up ON ua.username = up.username
);
GO

-- ===== Procedures (with output parameters or result sets) =====

-- Returns count as a basic type output parameter
CREATE OR ALTER PROCEDURE sp_count_active_users
    @p_count INT OUTPUT
    AS
BEGIN
SELECT @p_count = COUNT(DISTINCT username)
FROM useractivity;
END;
GO

-- Returns tuples via result set (MSSQL doesn't support REF_CURSOR, so returns directly)
CREATE OR ALTER PROCEDURE sp_get_active_users
AS
BEGIN
    SELECT DISTINCT ua.username, up.fullname
    FROM useractivity ua
    INNER JOIN userprofile up ON ua.username = up.username
    ORDER BY ua.username;
END;
GO

-- Returns active profiles via result set
CREATE OR ALTER PROCEDURE sp_get_active_profiles
AS
BEGIN
    SELECT up.id, up.username, up.fullname
    FROM useractivity ua
    INNER JOIN userprofile up ON ua.username = up.username;
END;
GO

-- Returns activities with profiles via result set
CREATE OR ALTER PROCEDURE sp_get_activities_with_profiles
AS
BEGIN
    SELECT ua.id, ua.username, ua.activitytime,
           up.id as profile_id, up.fullname as profile_fullname
    FROM useractivity ua
    INNER JOIN userprofile up ON ua.username = up.username;
END;
GO
