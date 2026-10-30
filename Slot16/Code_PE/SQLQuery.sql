CREATE DATABASE MSS301_2026_PE_MEDIA;
GO
USE MSS301_2026_PE_MEDIA;
GO

/* Table: users
   Target: MS SQL Server 2016
*/
USE [MSS301_2026_PE_MEDIA]
GO

CREATE TABLE [dbo].[users] (
    [user_id]         INT              IDENTITY(1,1) NOT NULL, -- Auto increment
    [username]        NVARCHAR(30)     NOT NULL,               -- Unique
    [email]           NVARCHAR(100)    NOT NULL,
    [role]            NVARCHAR(15)     NOT NULL,
    [rate_per_post]   DECIMAL(18,2)    NOT NULL,
    [max_daily_posts] INT              NOT NULL,
    [level]           INT              NOT NULL,
    [status]          NVARCHAR(15)     NOT NULL,

    -- Primary Key
    CONSTRAINT [PK_Users] PRIMARY KEY CLUSTERED ([user_id] ASC),

    -- Unique username
    CONSTRAINT [UQ_Users_Username] UNIQUE NONCLUSTERED ([username] ASC),

    -- Role allowed values
    CONSTRAINT [CK_Users_Role]
        CHECK ([role] IN ('USER', 'CREATOR', 'ADMIN')),

    -- Rate must be greater than 0
    CONSTRAINT [CK_Users_Rate]
        CHECK ([rate_per_post] > 0),

    -- max_daily_posts between 1 and 10
    CONSTRAINT [CK_Users_MaxDaily]
        CHECK ([max_daily_posts] >= 1 AND [max_daily_posts] <= 10),

    -- level must be >= 1
    CONSTRAINT [CK_Users_Level]
        CHECK ([level] >= 1),

    -- Status allowed values
    CONSTRAINT [CK_Users_Status]
        CHECK ([status] IN ('ACTIVE', 'SUSPENDED', 'DEACTIVATED'))
);
GO

USE [MSS301_2026_PE_MEDIA]
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[posts](
    [post_id]          [int]           IDENTITY(1,1) NOT NULL,
    [advertiser_name]  [nvarchar](100) NOT NULL,
    [advertiser_email] [nvarchar](100) NOT NULL,
    [advertiser_phone] [nvarchar](20)  NOT NULL,
    [start_date]       [date]          NOT NULL,
    [end_date]         [date]          NOT NULL,
    [number_of_posts]  [int]           NOT NULL,
    [total_cost]       [decimal](18,2) NOT NULL,
    [status]           [nvarchar](15)  NOT NULL,
    [user_id]          [int]           NOT NULL,

 CONSTRAINT [PK_Posts] PRIMARY KEY CLUSTERED
(
    [post_id] ASC
) ON [PRIMARY]
) ON [PRIMARY]
GO

-- end_date must be after start_date
ALTER TABLE [dbo].[posts]
    WITH CHECK ADD CONSTRAINT [CK_Posts_Dates]
    CHECK ([end_date] > [start_date]);
GO
ALTER TABLE [dbo].[posts] CHECK CONSTRAINT [CK_Posts_Dates];
GO

-- number_of_posts must be >= 1
ALTER TABLE [dbo].[posts]
    WITH CHECK ADD CONSTRAINT [CK_Posts_Number]
    CHECK ([number_of_posts] >= 1);
GO
ALTER TABLE [dbo].[posts] CHECK CONSTRAINT [CK_Posts_Number];
GO

-- total_cost must be >= 0
ALTER TABLE [dbo].[posts]
    WITH CHECK ADD CONSTRAINT [CK_Posts_Cost]
    CHECK ([total_cost] >= 0);
GO
ALTER TABLE [dbo].[posts] CHECK CONSTRAINT [CK_Posts_Cost];
GO

-- Status allowed values
ALTER TABLE [dbo].[posts]
    WITH CHECK ADD CONSTRAINT [CK_Posts_Status]
    CHECK ([status] IN ('DRAFT', 'SCHEDULED', 'PUBLISHED', 'CANCELLED'));
GO
ALTER TABLE [dbo].[posts] CHECK CONSTRAINT [CK_Posts_Status];
GO

-- Foreign key to users
ALTER TABLE [dbo].[posts]
    WITH CHECK ADD CONSTRAINT [FK_Posts_User]
    FOREIGN KEY ([user_id]) REFERENCES [dbo].[users]([user_id]);
GO
ALTER TABLE [dbo].[posts] CHECK CONSTRAINT [FK_Posts_User];
GO
