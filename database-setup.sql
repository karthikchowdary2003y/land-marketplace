-- =====================================================
-- LAND MARKETPLACE DATABASE SETUP
-- Run this in MySQL before starting the Spring app
-- =====================================================

-- Create Database
CREATE DATABASE IF NOT EXISTS land_marketplace
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE land_marketplace;

-- The tables will be auto-created by Spring JPA (ddl-auto=update)
-- This script is for initial setup and sample data

-- =====================================================
-- SAMPLE DATA (Run AFTER starting the app once)
-- =====================================================

-- Sample Admin User (password: admin123)
-- INSERT INTO users (full_name, email, phone, password, role, city, state, active)
-- VALUES ('Admin User', 'admin@landmarket.com', '9876543210',
--         '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhrO',
--         'ADMIN', 'Chennai', 'Tamil Nadu', true);

-- Sample Seller (password: seller123)
-- INSERT INTO users (full_name, email, phone, password, role, city, state, active)
-- VALUES ('Ravi Kumar', 'ravi@seller.com', '9876543211',
--         '$2a$10$somehashedpassword', 'SELLER', 'Coimbatore', 'Tamil Nadu', true);

-- =====================================================
-- USEFUL QUERIES
-- =====================================================

-- View all land listings with owner info
-- SELECT l.id, l.title, l.price, l.city, l.state, l.area_in_acres,
--        l.land_type, l.status, u.full_name, u.phone, u.email
-- FROM lands l
-- JOIN users u ON l.owner_id = u.id
-- WHERE l.active = true;

-- View all inquiries
-- SELECT i.id, l.title as land_title, i.inquirer_name,
--        i.inquirer_phone, i.message, i.status, i.created_at
-- FROM inquiries i
-- JOIN lands l ON i.land_id = l.id;
