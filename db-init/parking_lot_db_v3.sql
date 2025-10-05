DROP SCHEMA IF EXISTS public CASCADE;
CREATE SCHEMA public;

CREATE TYPE lot_status AS ENUM ('PENDING', 'INACTIVE', 'MAINTENANCE');
CREATE TYPE spot_status AS ENUM ('AVAILABLE', 'OCCUPIED', 'RESERVED', 'MAINTENANCE', 'DISABLED');
CREATE TYPE vehicle_type AS ENUM (
	'BIKE',           -- Bicycle
    'MOTORBIKE',      -- Motorcycle/scooter  
    'CAR_4_SEATS',    -- 4-seater cars (most common)
    'CAR_7_SEATS',    -- 7-seater cars (SUV, MPV)
    'CAR_9_SEATS',    -- 9-seater cars (minivan, large SUV)
    'OTHER'           -- Commercial vehicles
);
CREATE TYPE session_type AS ENUM ('MEMBER', 'OCCASIONAL');
CREATE TYPE auth_method AS ENUM (
	'BLE_PRESENCE',
	'NFC_CARD',
	'QR_CODE',
	'OTHER'
);

CREATE TYPE session_status AS ENUM (
	'ACTIVE',
	'COMPLETED',
	'OVERSTAY'
);
CREATE TYPE sync_status AS ENUM (
	'PENDING',
	'SYNCED',
	'FAILED'
);

CREATE TYPE rule_scope AS ENUM (
	'LOT_WIDE',
	'AREA_SPECIFIC'
);


-- Main parking lot table (keep the original name)
CREATE TABLE parking_lot (
	id BIGSERIAL PRIMARY KEY,
	partner_id BIGINT NOT NULL,
	name VARCHAR(255) NOT NULL,
	street_address VARCHAR(255) NOT NULL,
	ward VARCHAR(100) NOT NULL,
	city VARCHAR(100) NOT NULL,
	latitude DECIMAL(12, 8) NOT NULL,
	longitude DECIMAL(12, 8) NOT NULL,
	total_floor INTEGER NOT NULL DEFAULT 1,
	operating_hours_start TIME NOT NULL,
	operating_hours_end TIME NOT NULL,
	is_24_hour BOOLEAN DEFAULT FALSE,
	boundary_top_left_x DECIMAL(10, 2) DEFAULT 0.00,
	boundary_top_left_y DECIMAL(10, 2) DEFAULT 0.00,
	boundary_width DECIMAL(10, 2) DEFAULT 0.00,
	boundary_height DECIMAL(10, 2) DEFAULT 0.00,
	status lot_status NOT NULL DEFAULT 'PENDING',
	created_at TIMESTAMP DEFAULT NOW(),
	updated_at TIMESTAMP DEFAULT NOW()
);

-- Lot-level capacity by vehicle type
CREATE TABLE lot_capacity (
	id BIGSERIAL PRIMARY KEY,
	capacity BIGINT NOT NULL DEFAULT 0,
	support_electric_vehicle BOOLEAN NOT NULL DEFAULT FALSE,
	vehicle_type vehicle_type NOT NULL,
	lot_id BIGINT NOT NULL,

	is_active BOOLEAN DEFAULT TRUE,
	created_at TIMESTAMP DEFAULT NOW(),
	updated_at TIMESTAMP DEFAULT NOW()
);

-- Floors within a parking lot
CREATE TABLE floor (
	id BIGSERIAL PRIMARY KEY,
	floor_number INTEGER NOT NULL,
	floor_name VARCHAR(100) NOT NULL,
	is_active BOOLEAN DEFAULT TRUE,
	lot_id BIGINT NOT NULL,

	created_at TIMESTAMP DEFAULT NOW(),
	updated_at TIMESTAMP DEFAULT NOW()
);

-- Floor-level capacity by vehicle type
CREATE TABLE floor_capacity (
	id BIGSERIAL PRIMARY KEY,
	capacity BIGINT NOT NULL DEFAULT 0,
	support_electric_vehicle BOOLEAN NOT NULL DEFAULT FALSE,
	vehicle_type vehicle_type NOT NULL,
	floor_id BIGINT NOT NULL,

	is_active BOOLEAN DEFAULT TRUE,
	created_at TIMESTAMP DEFAULT NOW(),
	updated_at TIMESTAMP DEFAULT NOW()
);

-- Areas within a floor (e.g., sections A, B, VIP area)
CREATE TABLE area (
	id BIGSERIAL PRIMARY KEY,
	name VARCHAR(100) NOT NULL,
	vehicle_type vehicle_type NOT NULL,
	total_spots INTEGER NOT NULL DEFAULT 0,
	area_top_left_x DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
	area_top_left_y DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
	area_width DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
	area_height DECIMAL(10, 2) NOT NULL DEFAULT 0.00,

	floor_id BIGINT NOT NULL,
	is_active BOOLEAN NOT NULL DEFAULT TRUE,
	support_electric_vehicle BOOLEAN NOT NULL DEFAULT FALSE,
	created_at TIMESTAMP DEFAULT NOW(),
	updated_at TIMESTAMP DEFAULT NOW()
);

-- Individual parking spots
CREATE TABLE spot (
	id BIGSERIAL PRIMARY KEY,
	name VARCHAR(20) NOT NULL,
	spot_top_left_x DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
	spot_top_left_y DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
	spot_width DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
	spot_height DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
	status spot_status NOT NULL DEFAULT 'AVAILABLE',
	block_reason VARCHAR(255),

	area_id BIGINT NOT NULL,
	created_at TIMESTAMP DEFAULT NOW(),
	updated_at TIMESTAMP DEFAULT NOW()
);

-- Pricing rules for different scopes
CREATE TABLE pricing_rule (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lot_id BIGINT,
    area_id BIGINT,
    vehicle_type vehicle_type,
    rule_name VARCHAR(100) NOT NULL,
	rule_description VARCHAR(255),
    base_rate DECIMAL(10,2) NOT NULL,
	deposit_fee DECIMAL(10, 2) DEFAULT 0.00,

	
    
    -- Flexible initial charging
    initial_charge DECIMAL(10,2) DEFAULT 0,
    initial_duration_minute INTEGER DEFAULT 60,
    
    free_minute INTEGER DEFAULT 0,
    grace_period_minute INTEGER DEFAULT 60,
    is_active BOOLEAN DEFAULT TRUE,
    valid_from TIMESTAMP NOT NULL,
    valid_until TIMESTAMP,
	rule_scope rule_scope NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
	updated_at TIMESTAMP DEFAULT NOW()
);

-- Parking sessions
CREATE TABLE session (
	id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
	user_id BIGINT,
	lot_id BIGINT,
	spot_id BIGINT,
	vehicle_id UUID,
	license_plate VARCHAR(30) NOT NULL,
	session_type session_type,
	auth_method auth_method,
	entry_time TIMESTAMP NOT NULL DEFAULT NOW(),
	exit_time TIMESTAMP,
	duration_minute INTEGER,
	total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
	status session_status DEFAULT 'ACTIVE',
	sync_status sync_status DEFAULT 'PENDING',
	synced_from_local TIMESTAMP,
	note TEXT,

	created_at TIMESTAMP DEFAULT NOW(),
	updated_at TIMESTAMP DEFAULT NOW()
);

-- Foreign key constraints
ALTER TABLE lot_capacity
ADD CONSTRAINT fk_capacity_lot
FOREIGN KEY (lot_id) REFERENCES parking_lot(id);

ALTER TABLE floor
ADD CONSTRAINT fk_floor_lot
FOREIGN KEY (lot_id) REFERENCES parking_lot(id);

ALTER TABLE floor_capacity
ADD CONSTRAINT fk_capacity_floor
FOREIGN KEY (floor_id) REFERENCES floor(id);

ALTER TABLE area
ADD CONSTRAINT fk_area_floor
FOREIGN KEY (floor_id) REFERENCES floor(id);

ALTER TABLE spot
ADD CONSTRAINT fk_spot_area
FOREIGN KEY (area_id) REFERENCES area(id);

ALTER TABLE pricing_rule
ADD CONSTRAINT fk_price_lot
FOREIGN KEY (lot_id) REFERENCES parking_lot(id);

ALTER TABLE pricing_rule
ADD CONSTRAINT fk_price_area
FOREIGN KEY (area_id) REFERENCES area(id);

ALTER TABLE session
ADD CONSTRAINT fk_session_lot
FOREIGN KEY (lot_id) REFERENCES parking_lot(id);

ALTER TABLE session
ADD CONSTRAINT fk_session_spot
FOREIGN KEY (spot_id) REFERENCES spot(id);

-- Sample data with updated table names
INSERT INTO parking_lot (id, partner_id, name, street_address, ward, city, latitude, longitude, total_floor, operating_hours_start, operating_hours_end, is_24_hour, boundary_top_left_x, boundary_top_left_y, boundary_width, boundary_height, status) VALUES
(1, 101, 'Diamond Plaza Parking', '34 Le Duan Street', 'Ben Nghe Ward', 'Ho Chi Minh City', 10.7827500, 106.6986700, 3, '06:00:00', '23:00:00', FALSE, 0.00, 0.00, 100.00, 80.00, 'PENDING'),
(2, 102, 'Bitexco Financial Tower Parking', '2 Hai Trieu Street', 'Ben Nghe Ward', 'Ho Chi Minh City', 10.7716800, 106.7041900, 5, '00:00:00', '23:59:59', TRUE, 0.00, 0.00, 120.00, 90.00, 'PENDING'),
(3, 103, 'Vincom Center Parking', '70-72 Le Thanh Ton Street', 'Ben Nghe Ward', 'Ho Chi Minh City', 10.7795300, 106.7020400, 2, '07:00:00', '22:00:00', FALSE, 0.00, 0.00, 80.00, 60.00, 'INACTIVE'),
(4, 104, 'Saigon Centre Parking', '65 Le Loi Boulevard', 'Ben Nghe Ward', 'Ho Chi Minh City', 10.7738200, 106.7009800, 4, '06:30:00', '22:30:00', FALSE, 0.00, 0.00, 110.00, 85.00, 'PENDING'),
(5, 105, 'Landmark 81 Parking', '208 Nguyen Huu Canh Street', 'Ward 22', 'Ho Chi Minh City', 10.7945600, 106.7218900, 6, '00:00:00', '23:59:59', TRUE, 0.00, 0.00, 150.00, 100.00, 'MAINTENANCE');

-- Insert lot capacities
INSERT INTO lot_capacity (lot_id, vehicle_type, capacity, support_electric_vehicle) VALUES
-- Diamond Plaza Parking
(1, 'MOTORBIKE', 150, FALSE),
(1, 'CAR_4_SEATS', 80, FALSE),
(1, 'CAR_7_SEATS', 20, FALSE),
(1, 'CAR_4_SEATS', 10, TRUE), -- Electric car spots
-- Bitexco Financial Tower Parking
(2, 'MOTORBIKE', 200, FALSE),
(2, 'CAR_4_SEATS', 120, FALSE),
(2, 'CAR_7_SEATS', 30, FALSE),
(2, 'CAR_9_SEATS', 15, FALSE),
(2, 'CAR_4_SEATS', 20, TRUE), -- Electric car spots
-- Vincom Center Parking
(3, 'MOTORBIKE', 100, FALSE),
(3, 'CAR_4_SEATS', 60, FALSE),
(3, 'CAR_7_SEATS', 15, FALSE),
(3, 'CAR_4_SEATS', 8, TRUE), -- Electric car spots
-- Saigon Centre Parking
(4, 'MOTORBIKE', 180, FALSE),
(4, 'CAR_4_SEATS', 100, FALSE),
(4, 'CAR_7_SEATS', 25, FALSE),
(4, 'CAR_9_SEATS', 10, FALSE),
(4, 'CAR_4_SEATS', 15, TRUE), -- Electric car spots
-- Landmark 81 Parking
(5, 'MOTORBIKE', 300, FALSE),
(5, 'CAR_4_SEATS', 200, FALSE),
(5, 'CAR_7_SEATS', 50, FALSE),
(5, 'CAR_9_SEATS', 25, FALSE),
(5, 'CAR_4_SEATS', 30, TRUE); -- Electric car spots