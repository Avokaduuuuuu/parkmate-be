DROP SCHEMA IF EXISTS public CASCADE;
CREATE SCHEMA public;

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =====================================================
-- ENUMS
-- =====================================================

CREATE TYPE device_os AS ENUM ('ANDROID', 'IOS');
CREATE TYPE vehicle_type AS ENUM (
    'BIKE',           -- Bicycle
    'MOTORBIKE',      -- Motorcycle/scooter  
    'CAR_4_SEATS',    -- 4-seater cars (most common)
    'CAR_7_SEATS',    -- 7-seater cars (SUV, MPV)
    'CAR_9_SEATS',    -- 9-seater cars (minivan, large SUV)
    'OTHER'           -- Commercial vehicles
);
CREATE TYPE reservation_status AS ENUM ('PENDING', 'ACTIVE', 'COMPLETED', 'CANCELLED', 'EXPIRED');
CREATE TYPE account_role AS ENUM ('ADMIN', 'PARTNER_OWNER', 'PARTNER_STAFF', 'MEMBER');
CREATE TYPE account_status AS ENUM ('PENDING_VERIFICATION', 'ACTIVE', 'SUSPENDED', 'DELETED');
CREATE TYPE partner_status AS ENUM ('APPROVED', 'SUSPENDED', 'DELETED');
CREATE TYPE registration_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED');

-- =====================================================
-- TABLES (Without Foreign Key Constraints)
-- =====================================================

CREATE TABLE partner_registration (
    id BIGSERIAL PRIMARY KEY,
    
    company_name VARCHAR(255) NOT NULL,
    tax_number VARCHAR(50) UNIQUE NOT NULL,
    business_license_number VARCHAR(100) NOT NULL,
    business_license_file_url VARCHAR(500),
    company_address TEXT NOT NULL,
    company_phone VARCHAR(20),
    company_email VARCHAR(255),
    
    business_description TEXT,
    contact_person_name VARCHAR(255) NOT NULL,
    contact_person_phone VARCHAR(255) NOT NULL,
    contact_person_email VARCHAR(255) NOT NULL,

    status registration_status NOT NULL DEFAULT 'PENDING',
    submitted_at TIMESTAMP DEFAULT NOW(),
    reviewed_by BIGINT,  -- Will reference account(id)
    reviewed_at TIMESTAMP,
    approval_notes TEXT,
    rejection_reason TEXT
);

CREATE TABLE partner (
    id BIGSERIAL PRIMARY KEY,

    approval_request_id BIGINT,  -- Will reference partner_request(id)
    company_name VARCHAR(255) NOT NULL,
    tax_number VARCHAR(50) UNIQUE NOT NULL,
    business_license_number VARCHAR(100) NOT NULL,
    business_license_file_url VARCHAR(500),
    company_address TEXT NOT NULL,
    company_phone VARCHAR(20),
    company_email VARCHAR(255),
    
    business_description TEXT,
    status partner_status NOT NULL,
    suspension_reason TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE account (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE,
    username VARCHAR(255) UNIQUE,
	phone VARCHAR(20) UNIQUE,
    password VARCHAR(255) NOT NULL,
    role account_role NOT NULL,
    status account_status NOT NULL DEFAULT 'PENDING_VERIFICATION',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    email_verification_token CHAR(10),
    phone_verified BOOLEAN NOT NULL DEFAULT FALSE,
    phone_verification_token CHAR(10),
    last_login_at TIMESTAMP,

    partner_id BIGINT,  -- Will reference partner(id)
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE "user" (
    id BIGSERIAL PRIMARY KEY,
    phone VARCHAR(20) UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE,
    address TEXT,
    profile_picture_url VARCHAR(500),
    id_number VARCHAR(12),
    issue_place VARCHAR(255),
    issue_date DATE,
    expiry_date DATE,
    front_photo_path VARCHAR(500),
    back_photo_path VARCHAR(500),

    account_id BIGINT UNIQUE NOT NULL,  -- Will reference account(id)
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE mobile_device (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id BIGINT NOT NULL,  -- Will reference user(id)
    device_id VARCHAR(100) UNIQUE NOT NULL,
    device_name VARCHAR(100),
    device_os device_os NOT NULL,
    push_token VARCHAR(500),
    is_active BOOLEAN DEFAULT true,
    last_active_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE vehicle (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id BIGINT NOT NULL,  -- Will reference user(id)
    vehicle_type vehicle_type NOT NULL,
    license_plate VARCHAR(20) UNIQUE NOT NULL,
    license_image VARCHAR(255),
    vehicle_brand VARCHAR(100),
    vehicle_model VARCHAR(100),
    vehicle_color VARCHAR(50),
    is_active BOOLEAN DEFAULT true,
	is_electric BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE reservation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id BIGINT NOT NULL,  -- Will reference user(id)
    parking_lot_id BIGINT NOT NULL, -- Reference to Parking Service
    lot_id UUID, -- Reference to specific parking spot
    reserved_from TIMESTAMP NOT NULL,
    reserved_until TIMESTAMP NOT NULL,
    reservation_fee DECIMAL(10,2) DEFAULT 0,
    status reservation_status DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- =====================================================
-- ADD FOREIGN KEY CONSTRAINTS
-- =====================================================

-- partner_request foreign keys
ALTER TABLE partner_registration
ADD CONSTRAINT fk_partner_request_reviewed_by 
FOREIGN KEY (reviewed_by) REFERENCES account(id) ON DELETE SET NULL;

-- partner foreign keys  
ALTER TABLE partner 
ADD CONSTRAINT fk_partner_approval_registration 
FOREIGN KEY (approval_request_id) REFERENCES partner_registration(id) ON DELETE CASCADE;

-- account foreign keys
ALTER TABLE account 
ADD CONSTRAINT fk_account_partner 
FOREIGN KEY (partner_id) REFERENCES partner(id) ON DELETE CASCADE;

-- user foreign keys
ALTER TABLE "user" 
ADD CONSTRAINT fk_user_account 
FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE CASCADE;

-- mobile_device foreign keys
ALTER TABLE mobile_device 
ADD CONSTRAINT fk_mobile_device_user 
FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE;

-- vehicle foreign keys
ALTER TABLE vehicle 
ADD CONSTRAINT fk_vehicle_user 
FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE;

-- reservation foreign keys
ALTER TABLE reservation 
ADD CONSTRAINT fk_reservation_user 
FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE;