-- ============================================================
-- SEED DATA — DormitoryManagement (SQL Server)
-- Generated: 2026-06-05  (v2 — fixed)
-- Fixes:
--   1. Removed DELETE FROM refresh_token (table may not exist)
--   2. Fixed room_images: url→image_url, removed uploaded_at
--   3. All UUIDs use only valid hex chars (0-9, a-f)
-- ============================================================

USE DormitoryManagement;
GO

-- ──────────────────────────────────────────────
-- UUID KEY REFERENCE (hex-only prefixes)
-- ──────────────────────────────────────────────
-- users         : 1111...  2222...  3333...
-- properties    : aaaa0001-...
-- block_floors  : bbbb0001-...
-- rooms         : cccc0001-...  cccc0002-...  cccc0003-...
-- room_images   : dddd0001-...
-- utility_prices: eeee0001-...
-- utility_meters: ffff0001-...
-- rental_requests: a0a00001-0000-0000-0000-00000000000x
-- contracts     : b0b00001-0000-0000-0000-00000000000x
-- bills         : c0c00001-0000-0000-000y-00000000000x (y=contract#)
-- payments      : d0d00001-0000-0000-000y-00000000000x
-- payment_rcpts : e0e00001-...
-- util_readings : f0f00001-...
-- viewing_appts : a1a10001-...
-- maint_tickets : b1b10001-...
-- reviews       : c1c10001-...
-- complaints    : d1d10001-...
-- notifications : e1e10001-...
-- audit_logs    : f1f10001-...
-- ──────────────────────────────────────────────

-- ──────────────────────────────────────────────
-- 0. CLEANUP (comment out in PROD)
-- ──────────────────────────────────────────────
DELETE FROM audit_logs;
DELETE FROM notifications;
DELETE FROM complaints;
DELETE FROM reviews;
DELETE FROM maintenance_tickets;
DELETE FROM viewing_appointments;
DELETE FROM utility_readings;
DELETE FROM payment_receipts;
DELETE FROM payments;
DELETE FROM bills;
DELETE FROM contracts;
DELETE FROM rental_requests;
DELETE FROM utility_meters;
DELETE FROM utility_prices;
DELETE FROM room_images;
DELETE FROM rooms;
DELETE FROM block_floors;
DELETE FROM properties;
DELETE FROM users;
GO

-- ══════════════════════════════════════════════
-- 1. USERS
--    password_hash = BCrypt("Password@123")
-- ══════════════════════════════════════════════
INSERT INTO users (id, name, email, password_hash, phone, avatar_url, google_id,
                   identity_number, tax_code, business_license, landlord_verified,
                   role, status, created_at, updated_at)
VALUES
-- ── ADMIN ────────────────────────────────────
('11111111-0000-0000-0000-000000000001',
 N'Nguyễn Quản Trị',
 'admin@dormitory.vn',
 '$2a$12$7Jkk7j3O5K8Qw9Rv1UeXeOKn4Mg6G2ZqX9pE3LMnHs8DxFiPqBkO',
 '0900000001',
 'https://ui-avatars.com/api/?name=Admin&background=6366f1&color=fff',
 NULL, NULL, NULL, NULL, 0,
 'ADMIN', 'ACTIVE', GETDATE(), GETDATE()),

-- ── LANDLORDS ────────────────────────────────
('22222222-0000-0000-0000-000000000001',
 N'Trần Văn Minh',
 'landlord1@example.com',
 '$2a$12$7Jkk7j3O5K8Qw9Rv1UeXeOKn4Mg6G2ZqX9pE3LMnHs8DxFiPqBkO',
 '0901111001',
 'https://ui-avatars.com/api/?name=Tran+Van+Minh&background=10b981&color=fff',
 NULL, '079201012345', '0100123456', NULL, 1,
 'LANDLORD', 'ACTIVE', GETDATE(), GETDATE()),

('22222222-0000-0000-0000-000000000002',
 N'Lê Thị Hoa',
 'landlord2@example.com',
 '$2a$12$7Jkk7j3O5K8Qw9Rv1UeXeOKn4Mg6G2ZqX9pE3LMnHs8DxFiPqBkO',
 '0901111002',
 'https://ui-avatars.com/api/?name=Le+Thi+Hoa&background=f59e0b&color=fff',
 NULL, '079201054321', '0100654321', NULL, 1,
 'LANDLORD', 'ACTIVE', GETDATE(), GETDATE()),

('22222222-0000-0000-0000-000000000003',
 N'Phạm Đức Long',
 'landlord3@example.com',
 '$2a$12$7Jkk7j3O5K8Qw9Rv1UeXeOKn4Mg6G2ZqX9pE3LMnHs8DxFiPqBkO',
 '0901111003',
 'https://ui-avatars.com/api/?name=Pham+Duc+Long&background=3b82f6&color=fff',
 NULL, '079201099999', NULL, NULL, 0,
 'LANDLORD', 'ACTIVE', GETDATE(), GETDATE()),

-- ── TENANTS ──────────────────────────────────
('33333333-0000-0000-0000-000000000001',
 N'Nguyễn Thị Lan',
 'tenant1@example.com',
 '$2a$12$7Jkk7j3O5K8Qw9Rv1UeXeOKn4Mg6G2ZqX9pE3LMnHs8DxFiPqBkO',
 '0912345001',
 'https://ui-avatars.com/api/?name=Nguyen+Thi+Lan&background=ec4899&color=fff',
 NULL, NULL, NULL, NULL, 0,
 'TENANT', 'ACTIVE', GETDATE(), GETDATE()),

('33333333-0000-0000-0000-000000000002',
 N'Đặng Văn Hùng',
 'tenant2@example.com',
 '$2a$12$7Jkk7j3O5K8Qw9Rv1UeXeOKn4Mg6G2ZqX9pE3LMnHs8DxFiPqBkO',
 '0912345002',
 'https://ui-avatars.com/api/?name=Dang+Van+Hung&background=8b5cf6&color=fff',
 NULL, NULL, NULL, NULL, 0,
 'TENANT', 'ACTIVE', GETDATE(), GETDATE()),

('33333333-0000-0000-0000-000000000003',
 N'Bùi Thị Phương',
 'tenant3@example.com',
 '$2a$12$7Jkk7j3O5K8Qw9Rv1UeXeOKn4Mg6G2ZqX9pE3LMnHs8DxFiPqBkO',
 '0912345003',
 'https://ui-avatars.com/api/?name=Bui+Thi+Phuong&background=06b6d4&color=fff',
 NULL, NULL, NULL, NULL, 0,
 'TENANT', 'ACTIVE', GETDATE(), GETDATE()),

('33333333-0000-0000-0000-000000000004',
 N'Hoàng Minh Tuấn',
 'tenant4@example.com',
 '$2a$12$7Jkk7j3O5K8Qw9Rv1UeXeOKn4Mg6G2ZqX9pE3LMnHs8DxFiPqBkO',
 '0912345004',
 'https://ui-avatars.com/api/?name=Hoang+Minh+Tuan&background=f97316&color=fff',
 NULL, NULL, NULL, NULL, 0,
 'TENANT', 'ACTIVE', GETDATE(), GETDATE()),

('33333333-0000-0000-0000-000000000005',
 N'Vũ Thị Ngọc',
 'tenant5@example.com',
 '$2a$12$7Jkk7j3O5K8Qw9Rv1UeXeOKn4Mg6G2ZqX9pE3LMnHs8DxFiPqBkO',
 '0912345005',
 'https://ui-avatars.com/api/?name=Vu+Thi+Ngoc&background=84cc16&color=fff',
 NULL, NULL, NULL, NULL, 0,
 'TENANT', 'ACTIVE', GETDATE(), GETDATE()),

('33333333-0000-0000-0000-000000000006',
 N'Lý Văn Đức',
 'tenant6@example.com',
 '$2a$12$7Jkk7j3O5K8Qw9Rv1UeXeOKn4Mg6G2ZqX9pE3LMnHs8DxFiPqBkO',
 '0912345006',
 'https://ui-avatars.com/api/?name=Ly+Van+Duc&background=a855f7&color=fff',
 NULL, NULL, NULL, NULL, 0,
 'TENANT', 'ACTIVE', GETDATE(), GETDATE()),

('33333333-0000-0000-0000-000000000007',
 N'Mai Thị Thu',
 'tenant7@example.com',
 '$2a$12$7Jkk7j3O5K8Qw9Rv1UeXeOKn4Mg6G2ZqX9pE3LMnHs8DxFiPqBkO',
 '0912345007',
 'https://ui-avatars.com/api/?name=Mai+Thi+Thu&background=14b8a6&color=fff',
 NULL, NULL, NULL, NULL, 0,
 'TENANT', 'PENDING', GETDATE(), GETDATE()),

-- Google OAuth tenant (no password)
('33333333-0000-0000-0000-000000000008',
 N'Phan Thị Hà',
 'ha.phan@gmail.com',
 NULL, NULL,
 'https://lh3.googleusercontent.com/a/example_avatar',
 'google_oauth_id_00001',
 NULL, NULL, NULL, 0,
 'TENANT', 'ACTIVE', GETDATE(), GETDATE());
GO

-- ══════════════════════════════════════════════
-- 2. PROPERTIES
-- ══════════════════════════════════════════════
INSERT INTO properties (id, owner_id, name, address, description, geo_lat, geo_lng, status, created_at)
VALUES
('aaaa0001-0000-0000-0000-000000000001',
 '22222222-0000-0000-0000-000000000001',
 N'KTX Minh Khai A',
 N'123 Minh Khai, Phường 3, Quận Bình Thạnh, TP.HCM',
 N'Khu nhà trọ cao cấp gần trường ĐH Bách Khoa, đầy đủ tiện nghi, bảo vệ 24/7, camera an ninh.',
 10.7870, 106.6730, 'ACTIVE', GETDATE()),

('aaaa0001-0000-0000-0000-000000000002',
 '22222222-0000-0000-0000-000000000001',
 N'KTX Minh Khai B',
 N'125 Minh Khai, Phường 3, Quận Bình Thạnh, TP.HCM',
 N'Tòa nhà mới xây 2023, phòng đơn và phòng đôi, có thang máy, giờ giấc tự do.',
 10.7872, 106.6732, 'ACTIVE', GETDATE()),

('aaaa0002-0000-0000-0000-000000000001',
 '22222222-0000-0000-0000-000000000002',
 N'Nhà trọ Hoa Đào',
 N'45 Đinh Tiên Hoàng, Phường Đa Kao, Quận 1, TP.HCM',
 N'Phòng trọ giá sinh viên, gần bến xe buýt, wifi miễn phí, giặt sấy riêng mỗi phòng.',
 10.7836, 106.6942, 'ACTIVE', GETDATE()),

('aaaa0003-0000-0000-0000-000000000001',
 '22222222-0000-0000-0000-000000000003',
 N'Phòng trọ Long Thịnh',
 N'88 Lê Văn Việt, Phường Hiệp Phú, Quận 9, TP.HCM',
 N'Khu yên tĩnh, phù hợp sinh viên, điện nước tính theo thực tế.',
 10.8464, 106.7762, 'DRAFT', GETDATE());
GO

-- ══════════════════════════════════════════════
-- 3. BLOCK / FLOORS
-- ══════════════════════════════════════════════
INSERT INTO block_floors (id, property_id, block_name, floor_number)
VALUES
-- KTX Minh Khai A — Block A (floors 1-3)
('bbbb0001-0000-0000-0000-000000000001', 'aaaa0001-0000-0000-0000-000000000001', N'Block A', 1),
('bbbb0001-0000-0000-0000-000000000002', 'aaaa0001-0000-0000-0000-000000000001', N'Block A', 2),
('bbbb0001-0000-0000-0000-000000000003', 'aaaa0001-0000-0000-0000-000000000001', N'Block A', 3),
-- KTX Minh Khai A — Block B (floors 1-3)
('bbbb0001-0000-0000-0000-000000000004', 'aaaa0001-0000-0000-0000-000000000001', N'Block B', 1),
('bbbb0001-0000-0000-0000-000000000005', 'aaaa0001-0000-0000-0000-000000000001', N'Block B', 2),
('bbbb0001-0000-0000-0000-000000000006', 'aaaa0001-0000-0000-0000-000000000001', N'Block B', 3),
-- KTX Minh Khai B — Block C (floors 1-2)
('bbbb0002-0000-0000-0000-000000000001', 'aaaa0001-0000-0000-0000-000000000002', N'Block C', 1),
('bbbb0002-0000-0000-0000-000000000002', 'aaaa0001-0000-0000-0000-000000000002', N'Block C', 2),
-- Nhà trọ Hoa Đào — Tòa chính (floors 1-2)
('bbbb0003-0000-0000-0000-000000000001', 'aaaa0002-0000-0000-0000-000000000001', N'Tòa chính', 1),
('bbbb0003-0000-0000-0000-000000000002', 'aaaa0002-0000-0000-0000-000000000001', N'Tòa chính', 2);
GO

-- ══════════════════════════════════════════════
-- 4. ROOMS
-- ══════════════════════════════════════════════
INSERT INTO rooms (id, property_id, block_floor_id, room_number, room_type, code,
                   capacity, gender_type, price_per_month, amenities, description, status)
VALUES
-- ── KTX Minh Khai A — Block A, Floor 1 ──────
('cccc0001-0000-0000-0000-000000000001',
 'aaaa0001-0000-0000-0000-000000000001',
 'bbbb0001-0000-0000-0000-000000000001',
 '101', N'Phòng đơn', 'MKA-101', 1, 'FEMALE', 2500000.00,
 '["wifi","ac","private_bathroom","wardrobe"]',
 N'Phòng đơn thoáng mát, có cửa sổ hướng Đông, nhà vệ sinh riêng.',
 'AVAILABLE'),

('cccc0001-0000-0000-0000-000000000002',
 'aaaa0001-0000-0000-0000-000000000001',
 'bbbb0001-0000-0000-0000-000000000001',
 '102', N'Phòng đôi', 'MKA-102', 2, 'FEMALE', 1800000.00,
 '["wifi","ac","shared_bathroom","wardrobe","desk"]',
 N'Phòng đôi, chia sẻ nhà vệ sinh với phòng 103.',
 'OCCUPIED'),

('cccc0001-0000-0000-0000-000000000003',
 'aaaa0001-0000-0000-0000-000000000001',
 'bbbb0001-0000-0000-0000-000000000001',
 '103', N'Phòng đôi', 'MKA-103', 2, 'MALE', 1800000.00,
 '["wifi","ac","shared_bathroom","wardrobe"]',
 N'Phòng đôi dành cho nam, view sân trong.',
 'OCCUPIED'),

-- ── KTX Minh Khai A — Block A, Floor 2 ──────
('cccc0001-0000-0000-0000-000000000004',
 'aaaa0001-0000-0000-0000-000000000001',
 'bbbb0001-0000-0000-0000-000000000002',
 '201', N'Phòng Studio', 'MKA-201', 1, 'MIXED', 3500000.00,
 '["wifi","ac","private_bathroom","kitchen","desk","balcony"]',
 N'Studio cao cấp, có bếp và ban công, tầng 2 view đường.',
 'AVAILABLE'),

('cccc0001-0000-0000-0000-000000000005',
 'aaaa0001-0000-0000-0000-000000000001',
 'bbbb0001-0000-0000-0000-000000000002',
 '202', N'Phòng đôi', 'MKA-202', 2, 'FEMALE', 2000000.00,
 '["wifi","ac","private_bathroom","wardrobe","desk"]',
 N'Phòng đôi nữ tầng 2, yên tĩnh.',
 'RESERVED'),

-- ── KTX Minh Khai A — Block B, Floor 1 ──────
('cccc0001-0000-0000-0000-000000000006',
 'aaaa0001-0000-0000-0000-000000000001',
 'bbbb0001-0000-0000-0000-000000000004',
 'B101', N'Phòng ký túc xá 4 người', 'MKA-B101', 4, 'MALE', 1200000.00,
 '["wifi","fan","shared_bathroom","locker"]',
 N'Phòng ký túc xá 4 người, phù hợp sinh viên tiết kiệm.',
 'OCCUPIED'),

('cccc0001-0000-0000-0000-000000000007',
 'aaaa0001-0000-0000-0000-000000000001',
 'bbbb0001-0000-0000-0000-000000000004',
 'B102', N'Phòng ký túc xá 4 người', 'MKA-B102', 4, 'FEMALE', 1200000.00,
 '["wifi","fan","shared_bathroom","locker"]',
 N'Phòng ký túc xá 4 người dành cho nữ.',
 'AVAILABLE'),

-- ── KTX Minh Khai B ──────────────────────────
('cccc0002-0000-0000-0000-000000000001',
 'aaaa0001-0000-0000-0000-000000000002',
 'bbbb0002-0000-0000-0000-000000000001',
 'C101', N'Phòng đơn cao cấp', 'MKB-C101', 1, 'MIXED', 4200000.00,
 '["wifi","ac","private_bathroom","smart_tv","kitchen","desk","balcony"]',
 N'Phòng đơn tiêu chuẩn cao cấp, đầy đủ nội thất hiện đại, view hồ bơi.',
 'AVAILABLE'),

('cccc0002-0000-0000-0000-000000000002',
 'aaaa0001-0000-0000-0000-000000000002',
 'bbbb0002-0000-0000-0000-000000000001',
 'C102', N'Phòng đôi cao cấp', 'MKB-C102', 2, 'MIXED', 2800000.00,
 '["wifi","ac","private_bathroom","smart_tv","desk"]',
 N'Phòng đôi cao cấp, 2 giường đơn, tủ quần áo riêng.',
 'OCCUPIED'),

-- ── Nhà trọ Hoa Đào ──────────────────────────
('cccc0003-0000-0000-0000-000000000001',
 'aaaa0002-0000-0000-0000-000000000001',
 'bbbb0003-0000-0000-0000-000000000001',
 '01', N'Phòng trọ thường', 'HD-01', 1, 'MIXED', 1500000.00,
 '["wifi","fan","shared_bathroom"]',
 N'Phòng trọ bình dân, tiện ích cơ bản, gần chợ Đa Kao.',
 'OCCUPIED'),

('cccc0003-0000-0000-0000-000000000002',
 'aaaa0002-0000-0000-0000-000000000001',
 'bbbb0003-0000-0000-0000-000000000001',
 '02', N'Phòng trọ thường', 'HD-02', 1, 'MIXED', 1500000.00,
 '["wifi","fan","shared_bathroom"]',
 N'Phòng trọ giá rẻ, phù hợp sinh viên năm 1.',
 'AVAILABLE'),

('cccc0003-0000-0000-0000-000000000003',
 'aaaa0002-0000-0000-0000-000000000001',
 'bbbb0003-0000-0000-0000-000000000002',
 '03', N'Phòng trọ có gác', 'HD-03', 1, 'MIXED', 1800000.00,
 '["wifi","ac","private_bathroom","loft"]',
 N'Phòng gác lửng, không gian riêng tư tốt hơn.',
 'AVAILABLE');
GO

-- ══════════════════════════════════════════════
-- 5. ROOM IMAGES
--    Columns: id, room_id, image_url, is_primary, sort_order
-- ══════════════════════════════════════════════
INSERT INTO room_images (id, room_id, image_url, is_primary, sort_order)
VALUES
-- Room 101
('dddd0001-0000-0000-0000-000000000001', 'cccc0001-0000-0000-0000-000000000001',
 'https://images.unsplash.com/photo-1555854877-bab0e564b8d5?w=800', 1, 1),
('dddd0001-0000-0000-0000-000000000002', 'cccc0001-0000-0000-0000-000000000001',
 'https://images.unsplash.com/photo-1598928506311-c55ded91a20c?w=800', 0, 2),

-- Room 201 (Studio)
('dddd0001-0000-0000-0000-000000000003', 'cccc0001-0000-0000-0000-000000000004',
 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800', 1, 1),
('dddd0001-0000-0000-0000-000000000004', 'cccc0001-0000-0000-0000-000000000004',
 'https://images.unsplash.com/photo-1540518614846-7eded433c457?w=800', 0, 2),

-- Room C101
('dddd0002-0000-0000-0000-000000000001', 'cccc0002-0000-0000-0000-000000000001',
 'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=800', 1, 1),
('dddd0002-0000-0000-0000-000000000002', 'cccc0002-0000-0000-0000-000000000001',
 'https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=800', 0, 2),

-- Room HD-01
('dddd0003-0000-0000-0000-000000000001', 'cccc0003-0000-0000-0000-000000000001',
 'https://images.unsplash.com/photo-1493809842364-78817add7ffb?w=800', 1, 1);
GO

-- ══════════════════════════════════════════════
-- 6. UTILITY PRICES
-- ══════════════════════════════════════════════
INSERT INTO utility_prices (id, utility_type, unit_price, unit_label, effective_date)
VALUES
('eeee0001-0000-0000-0000-000000000001', 'ELECTRICITY', 3500.00, 'kWh', '2024-01-01'),
('eeee0001-0000-0000-0000-000000000002', 'ELECTRICITY', 3800.00, 'kWh', '2025-01-01'),
('eeee0001-0000-0000-0000-000000000003', 'ELECTRICITY', 4000.00, 'kWh', '2026-01-01'),
('eeee0002-0000-0000-0000-000000000001', 'WATER', 10000.00, N'm3',   '2024-01-01'),
('eeee0002-0000-0000-0000-000000000002', 'WATER', 11000.00, N'm3',   '2025-01-01'),
('eeee0002-0000-0000-0000-000000000003', 'WATER', 12000.00, N'm3',   '2026-01-01');
GO

-- ══════════════════════════════════════════════
-- 7. UTILITY METERS
-- ══════════════════════════════════════════════
INSERT INTO utility_meters (id, room_id, type, tariff_rate, unit_label, readings)
VALUES
-- Room 102 (Lan — OCCUPIED)
('ffff0001-0000-0000-0000-000000000001', 'cccc0001-0000-0000-0000-000000000002',
 'ELECTRICITY', 4000.00, 'kWh',
 '[{"date":"2026-04-01","value":1200.0},{"date":"2026-05-01","value":1310.5}]'),
('ffff0001-0000-0000-0000-000000000002', 'cccc0001-0000-0000-0000-000000000002',
 'WATER', 12000.00, N'm3',
 '[{"date":"2026-04-01","value":45.0},{"date":"2026-05-01","value":49.5}]'),

-- Room 103 (Hùng — OCCUPIED)
('ffff0001-0000-0000-0000-000000000003', 'cccc0001-0000-0000-0000-000000000003',
 'ELECTRICITY', 4000.00, 'kWh',
 '[{"date":"2026-04-01","value":2100.0},{"date":"2026-05-01","value":2265.0}]'),
('ffff0001-0000-0000-0000-000000000004', 'cccc0001-0000-0000-0000-000000000003',
 'WATER', 12000.00, N'm3',
 '[{"date":"2026-04-01","value":30.0},{"date":"2026-05-01","value":33.8}]'),

-- Room B101 (Tuấn — OCCUPIED)
('ffff0001-0000-0000-0000-000000000005', 'cccc0001-0000-0000-0000-000000000006',
 'ELECTRICITY', 4000.00, 'kWh',
 '[{"date":"2026-05-01","value":5800.0}]'),
('ffff0001-0000-0000-0000-000000000006', 'cccc0001-0000-0000-0000-000000000006',
 'WATER', 12000.00, N'm3',
 '[{"date":"2026-05-01","value":90.0}]'),

-- Room C102 (Phương — OCCUPIED)
('ffff0002-0000-0000-0000-000000000001', 'cccc0002-0000-0000-0000-000000000002',
 'ELECTRICITY', 4000.00, 'kWh',
 '[{"date":"2026-05-01","value":3400.0}]'),
('ffff0002-0000-0000-0000-000000000002', 'cccc0002-0000-0000-0000-000000000002',
 'WATER', 12000.00, N'm3',
 '[{"date":"2026-05-01","value":60.0}]'),

-- Room HD-01 (Ngọc — OCCUPIED)
('ffff0003-0000-0000-0000-000000000001', 'cccc0003-0000-0000-0000-000000000001',
 'ELECTRICITY', 4000.00, 'kWh',
 '[{"date":"2026-05-01","value":900.0}]'),
('ffff0003-0000-0000-0000-000000000002', 'cccc0003-0000-0000-0000-000000000001',
 'WATER', 12000.00, N'm3',
 '[{"date":"2026-05-01","value":20.0}]');
GO

-- ══════════════════════════════════════════════
-- 8. RENTAL REQUESTS
--    IDs: a0a00001-0000-0000-0000-00000000000x
-- ══════════════════════════════════════════════
INSERT INTO rental_requests (id, room_id, tenant_id, start_date, duration_months, status, note, created_at)
VALUES
-- APPROVED (will have contracts)
('a0a00001-0000-0000-0000-000000000001',
 'cccc0001-0000-0000-0000-000000000002',
 '33333333-0000-0000-0000-000000000001',
 '2026-01-01', 6, 'APPROVED',
 N'Mình cần ở gần trường ĐH Bách Khoa, ưu tiên phòng yên tĩnh.',
 '2025-12-15 09:00:00'),

('a0a00001-0000-0000-0000-000000000002',
 'cccc0001-0000-0000-0000-000000000003',
 '33333333-0000-0000-0000-000000000002',
 '2026-01-01', 6, 'APPROVED',
 N'Sinh viên năm 3 ĐH Bách Khoa, cần phòng từ tháng 1/2026.',
 '2025-12-16 10:30:00'),

('a0a00001-0000-0000-0000-000000000003',
 'cccc0001-0000-0000-0000-000000000006',
 '33333333-0000-0000-0000-000000000004',
 '2026-02-01', 12, 'APPROVED',
 N'Cần phòng ký túc xá từ đầu học kỳ 2.',
 '2026-01-10 08:00:00'),

('a0a00001-0000-0000-0000-000000000004',
 'cccc0002-0000-0000-0000-000000000002',
 '33333333-0000-0000-0000-000000000003',
 '2026-03-01', 6, 'APPROVED',
 N'Nhân viên văn phòng, cần phòng ở Quận 1.',
 '2026-02-20 14:00:00'),

('a0a00001-0000-0000-0000-000000000005',
 'cccc0003-0000-0000-0000-000000000001',
 '33333333-0000-0000-0000-000000000005',
 '2026-02-01', 6, 'APPROVED',
 N'Sinh viên đang học gần đây, cần phòng tiết kiệm.',
 '2026-01-20 11:00:00'),

-- PENDING
('a0a00001-0000-0000-0000-000000000006',
 'cccc0001-0000-0000-0000-000000000001',
 '33333333-0000-0000-0000-000000000006',
 '2026-07-01', 3, 'PENDING',
 N'Thực tập sinh 3 tháng, cần phòng đơn giá hợp lý.',
 '2026-06-01 09:00:00'),

('a0a00001-0000-0000-0000-000000000007',
 'cccc0001-0000-0000-0000-000000000004',
 '33333333-0000-0000-0000-000000000008',
 '2026-07-01', 6, 'PENDING',
 N'Làm việc remote, cần phòng có không gian làm việc.',
 '2026-06-02 15:00:00'),

-- REJECTED
('a0a00001-0000-0000-0000-000000000008',
 'cccc0001-0000-0000-0000-000000000002',
 '33333333-0000-0000-0000-000000000007',
 '2026-06-01', 3, 'REJECTED',
 N'Phòng đã có người thuê, mong chủ trọ sắp xếp.',
 '2026-05-25 10:00:00');
GO

-- ══════════════════════════════════════════════
-- 9. CONTRACTS
--    IDs: b0b00001-0000-0000-0000-00000000000x
-- ══════════════════════════════════════════════
INSERT INTO contracts (id, rental_request_id, tenant_id, room_id,
                       terms, status, effective_from, effective_to,
                       deposit_amount, monthly_rent)
VALUES
-- Lan — Room 102 (ACTIVE)
('b0b00001-0000-0000-0000-000000000001',
 'a0a00001-0000-0000-0000-000000000001',
 '33333333-0000-0000-0000-000000000001',
 'cccc0001-0000-0000-0000-000000000002',
 N'Hợp đồng thuê phòng theo tháng. Tiền thuê 1.800.000đ/tháng. Điện nước theo đồng hồ. Cọc 2 tháng. Thông báo trước 30 ngày khi chấm dứt.',
 'ACTIVE', '2026-01-01', '2026-06-30', 3600000.00, 1800000.00),

-- Hùng — Room 103 (ACTIVE)
('b0b00001-0000-0000-0000-000000000002',
 'a0a00001-0000-0000-0000-000000000002',
 '33333333-0000-0000-0000-000000000002',
 'cccc0001-0000-0000-0000-000000000003',
 N'Hợp đồng thuê phòng đôi. Tiền thuê 1.800.000đ/tháng. Điện nước theo thực tế. Cọc 1 tháng. Không nuôi thú cưng.',
 'ACTIVE', '2026-01-01', '2026-06-30', 1800000.00, 1800000.00),

-- Tuấn — Room B101 KTX (ACTIVE)
('b0b00001-0000-0000-0000-000000000003',
 'a0a00001-0000-0000-0000-000000000003',
 '33333333-0000-0000-0000-000000000004',
 'cccc0001-0000-0000-0000-000000000006',
 N'Hợp đồng ký túc xá. Giá 1.200.000đ/tháng. Cọc 1 tháng. Nội quy KTX áp dụng.',
 'ACTIVE', '2026-02-01', '2027-01-31', 1200000.00, 1200000.00),

-- Phương — Room C102 cao cấp (ACTIVE)
('b0b00001-0000-0000-0000-000000000004',
 'a0a00001-0000-0000-0000-000000000004',
 '33333333-0000-0000-0000-000000000003',
 'cccc0002-0000-0000-0000-000000000002',
 N'Hợp đồng thuê phòng đôi cao cấp. Giá 2.800.000đ/tháng. Cọc 2 tháng. Điện nước tính riêng.',
 'ACTIVE', '2026-03-01', '2026-08-31', 5600000.00, 2800000.00),

-- Ngọc — Room HD-01 (ACTIVE)
('b0b00001-0000-0000-0000-000000000005',
 'a0a00001-0000-0000-0000-000000000005',
 '33333333-0000-0000-0000-000000000005',
 'cccc0003-0000-0000-0000-000000000001',
 N'Hợp đồng thuê phòng trọ. Giá 1.500.000đ/tháng. Điện 4.000đ/kWh, nước 12.000đ/m³. Cọc 1 tháng.',
 'ACTIVE', '2026-02-01', '2026-07-31', 1500000.00, 1500000.00);
GO

-- ══════════════════════════════════════════════
-- 10. BILLS
--     IDs: c0c00001-0000-0000-000y-00000000000x
--     y = contract# (1-5), x = bill# per contract
-- ══════════════════════════════════════════════
INSERT INTO bills (id, contract_id, amount, billing_period,
                   room_rent, electricity_fee, water_fee, service_fee, total_amount,
                   issue_date, due_date, status, line_items)
VALUES
-- ── Contract 1 — Lan (Room 102) ─────────────
('c0c00001-0000-0000-0001-000000000001',
 'b0b00001-0000-0000-0000-000000000001',
 2242000.00, '2026-01',
 1800000.00, 368000.00, 54000.00, 20000.00, 2242000.00,
 '2026-01-01', '2026-01-15', 'PAID',
 N'[{"label":"Tiền phòng tháng 1","amount":1800000},{"label":"Điện 92kWh×4000đ","amount":368000},{"label":"Nước 4.5m³×12000đ","amount":54000},{"label":"Phí dịch vụ","amount":20000}]'),

('c0c00001-0000-0000-0001-000000000002',
 'b0b00001-0000-0000-0000-000000000001',
 2176000.00, '2026-02',
 1800000.00, 320000.00, 36000.00, 20000.00, 2176000.00,
 '2026-02-01', '2026-02-15', 'PAID',
 N'[{"label":"Tiền phòng tháng 2","amount":1800000},{"label":"Điện 80kWh×4000đ","amount":320000},{"label":"Nước 3m³×12000đ","amount":36000},{"label":"Phí dịch vụ","amount":20000}]'),

('c0c00001-0000-0000-0001-000000000003',
 'b0b00001-0000-0000-0000-000000000001',
 2258000.00, '2026-03',
 1800000.00, 392000.00, 48000.00, 18000.00, 2258000.00,
 '2026-03-01', '2026-03-15', 'PAID',
 N'[{"label":"Tiền phòng tháng 3","amount":1800000},{"label":"Điện 98kWh×4000đ","amount":392000},{"label":"Nước 4m³×12000đ","amount":48000},{"label":"Phí dịch vụ","amount":18000}]'),

('c0c00001-0000-0000-0001-000000000004',
 'b0b00001-0000-0000-0000-000000000001',
 2218000.00, '2026-04',
 1800000.00, 360000.00, 48000.00, 10000.00, 2218000.00,
 '2026-04-01', '2026-04-15', 'PAID',
 N'[{"label":"Tiền phòng tháng 4","amount":1800000},{"label":"Điện 90kWh×4000đ","amount":360000},{"label":"Nước 4m³×12000đ","amount":48000},{"label":"Phí dịch vụ","amount":10000}]'),

('c0c00001-0000-0000-0001-000000000005',
 'b0b00001-0000-0000-0000-000000000001',
 2282000.00, '2026-05',
 1800000.00, 420000.00, 54000.00, 8000.00, 2282000.00,
 '2026-05-01', '2026-05-15', 'PENDING',
 N'[{"label":"Tiền phòng tháng 5","amount":1800000},{"label":"Điện 105kWh×4000đ","amount":420000},{"label":"Nước 4.5m³×12000đ","amount":54000},{"label":"Phí dịch vụ","amount":8000}]'),

-- ── Contract 2 — Hùng (Room 103) ────────────
('c0c00001-0000-0000-0002-000000000001',
 'b0b00001-0000-0000-0000-000000000002',
 2104000.00, '2026-01',
 1800000.00, 260000.00, 24000.00, 20000.00, 2104000.00,
 '2026-01-01', '2026-01-15', 'PAID',
 N'[{"label":"Tiền phòng","amount":1800000},{"label":"Điện 65kWh×4000đ","amount":260000},{"label":"Nước 2m³×12000đ","amount":24000},{"label":"Phí DV","amount":20000}]'),

('c0c00001-0000-0000-0002-000000000002',
 'b0b00001-0000-0000-0000-000000000002',
 2100000.00, '2026-02',
 1800000.00, 240000.00, 36000.00, 24000.00, 2100000.00,
 '2026-02-01', '2026-02-15', 'PAID',
 N'[{"label":"Tiền phòng","amount":1800000},{"label":"Điện 60kWh×4000đ","amount":240000},{"label":"Nước 3m³×12000đ","amount":36000},{"label":"Phí DV","amount":24000}]'),

('c0c00001-0000-0000-0002-000000000003',
 'b0b00001-0000-0000-0000-000000000002',
 2228000.00, '2026-03',
 1800000.00, 380000.00, 24000.00, 24000.00, 2228000.00,
 '2026-03-01', '2026-03-15', 'PAID',
 N'[{"label":"Tiền phòng","amount":1800000},{"label":"Điện 95kWh×4000đ","amount":380000},{"label":"Nước 2m³×12000đ","amount":24000},{"label":"Phí DV","amount":24000}]'),

('c0c00001-0000-0000-0002-000000000004',
 'b0b00001-0000-0000-0000-000000000002',
 2148000.00, '2026-04',
 1800000.00, 280000.00, 48000.00, 20000.00, 2148000.00,
 '2026-04-01', '2026-04-15', 'PAID',
 N'[{"label":"Tiền phòng","amount":1800000},{"label":"Điện 70kWh×4000đ","amount":280000},{"label":"Nước 4m³×12000đ","amount":48000},{"label":"Phí DV","amount":20000}]'),

('c0c00001-0000-0000-0002-000000000005',
 'b0b00001-0000-0000-0000-000000000002',
 2340000.00, '2026-05',
 1800000.00, 440000.00, 60000.00, 40000.00, 2340000.00,
 '2026-05-01', '2026-05-15', 'OVERDUE',
 N'[{"label":"Tiền phòng","amount":1800000},{"label":"Điện 110kWh×4000đ","amount":440000},{"label":"Nước 5m³×12000đ","amount":60000},{"label":"Phí DV","amount":40000}]'),

-- ── Contract 3 — Tuấn (Room B101 KTX) ───────
('c0c00001-0000-0000-0003-000000000001',
 'b0b00001-0000-0000-0000-000000000003',
 1580000.00, '2026-02',
 1200000.00, 280000.00, 60000.00, 40000.00, 1580000.00,
 '2026-02-01', '2026-02-15', 'PAID',
 N'[{"label":"Tiền phòng KTX tháng 2","amount":1200000},{"label":"Điện 70kWh×4000đ","amount":280000},{"label":"Nước 5m³×12000đ","amount":60000},{"label":"Phí DV","amount":40000}]'),

('c0c00001-0000-0000-0003-000000000002',
 'b0b00001-0000-0000-0000-000000000003',
 1560000.00, '2026-03',
 1200000.00, 260000.00, 60000.00, 40000.00, 1560000.00,
 '2026-03-01', '2026-03-15', 'PAID',
 N'[{"label":"Tiền phòng KTX tháng 3","amount":1200000},{"label":"Điện 65kWh×4000đ","amount":260000},{"label":"Nước 5m³×12000đ","amount":60000},{"label":"Phí DV","amount":40000}]'),

('c0c00001-0000-0000-0003-000000000003',
 'b0b00001-0000-0000-0000-000000000003',
 1540000.00, '2026-04',
 1200000.00, 240000.00, 60000.00, 40000.00, 1540000.00,
 '2026-04-01', '2026-04-15', 'PAID',
 N'[{"label":"Tiền phòng KTX tháng 4","amount":1200000},{"label":"Điện 60kWh×4000đ","amount":240000},{"label":"Nước 5m³×12000đ","amount":60000},{"label":"Phí DV","amount":40000}]'),

('c0c00001-0000-0000-0003-000000000004',
 'b0b00001-0000-0000-0000-000000000003',
 1620000.00, '2026-05',
 1200000.00, 320000.00, 60000.00, 40000.00, 1620000.00,
 '2026-05-01', '2026-05-20', 'PENDING',
 N'[{"label":"Tiền phòng KTX tháng 5","amount":1200000},{"label":"Điện 80kWh×4000đ","amount":320000},{"label":"Nước 5m³×12000đ","amount":60000},{"label":"Phí DV","amount":40000}]'),

-- ── Contract 4 — Phương (Room C102) ─────────
('c0c00001-0000-0000-0004-000000000001',
 'b0b00001-0000-0000-0000-000000000004',
 3160000.00, '2026-03',
 2800000.00, 280000.00, 60000.00, 20000.00, 3160000.00,
 '2026-03-01', '2026-03-15', 'PAID',
 N'[{"label":"Tiền phòng tháng 3","amount":2800000},{"label":"Điện 70kWh×4000đ","amount":280000},{"label":"Nước 5m³×12000đ","amount":60000},{"label":"Phí DV","amount":20000}]'),

('c0c00001-0000-0000-0004-000000000002',
 'b0b00001-0000-0000-0000-000000000004',
 3200000.00, '2026-04',
 2800000.00, 320000.00, 60000.00, 20000.00, 3200000.00,
 '2026-04-01', '2026-04-15', 'PAID',
 N'[{"label":"Tiền phòng tháng 4","amount":2800000},{"label":"Điện 80kWh×4000đ","amount":320000},{"label":"Nước 5m³×12000đ","amount":60000},{"label":"Phí DV","amount":20000}]'),

('c0c00001-0000-0000-0004-000000000003',
 'b0b00001-0000-0000-0000-000000000004',
 3240000.00, '2026-05',
 2800000.00, 360000.00, 60000.00, 20000.00, 3240000.00,
 '2026-05-01', '2026-05-15', 'PENDING',
 N'[{"label":"Tiền phòng tháng 5","amount":2800000},{"label":"Điện 90kWh×4000đ","amount":360000},{"label":"Nước 5m³×12000đ","amount":60000},{"label":"Phí DV","amount":20000}]'),

-- ── Contract 5 — Ngọc (Room HD-01) ──────────
('c0c00001-0000-0000-0005-000000000001',
 'b0b00001-0000-0000-0000-000000000005',
 1780000.00, '2026-02',
 1500000.00, 200000.00, 60000.00, 20000.00, 1780000.00,
 '2026-02-01', '2026-02-15', 'PAID',
 N'[{"label":"Tiền phòng tháng 2","amount":1500000},{"label":"Điện 50kWh×4000đ","amount":200000},{"label":"Nước 5m³×12000đ","amount":60000},{"label":"Phí DV","amount":20000}]'),

('c0c00001-0000-0000-0005-000000000002',
 'b0b00001-0000-0000-0000-000000000005',
 1820000.00, '2026-03',
 1500000.00, 240000.00, 60000.00, 20000.00, 1820000.00,
 '2026-03-01', '2026-03-15', 'PAID',
 N'[{"label":"Tiền phòng tháng 3","amount":1500000},{"label":"Điện 60kWh×4000đ","amount":240000},{"label":"Nước 5m³×12000đ","amount":60000},{"label":"Phí DV","amount":20000}]'),

('c0c00001-0000-0000-0005-000000000003',
 'b0b00001-0000-0000-0000-000000000005',
 1800000.00, '2026-04',
 1500000.00, 220000.00, 60000.00, 20000.00, 1800000.00,
 '2026-04-01', '2026-04-15', 'PAID',
 N'[{"label":"Tiền phòng tháng 4","amount":1500000},{"label":"Điện 55kWh×4000đ","amount":220000},{"label":"Nước 5m³×12000đ","amount":60000},{"label":"Phí DV","amount":20000}]'),

('c0c00001-0000-0000-0005-000000000004',
 'b0b00001-0000-0000-0000-000000000005',
 1840000.00, '2026-05',
 1500000.00, 260000.00, 60000.00, 20000.00, 1840000.00,
 '2026-05-01', '2026-05-15', 'PENDING',
 N'[{"label":"Tiền phòng tháng 5","amount":1500000},{"label":"Điện 65kWh×4000đ","amount":260000},{"label":"Nước 5m³×12000đ","amount":60000},{"label":"Phí DV","amount":20000}]');
GO

-- ══════════════════════════════════════════════
-- 11. PAYMENTS
--     IDs: d0d00001-0000-0000-000y-00000000000x
-- ══════════════════════════════════════════════
INSERT INTO payments (id, bill_id, amount, method, status, transaction_ref, paid_at, created_at)
VALUES
-- Lan — bills 1-4 (PAID)
('d0d00001-0000-0000-0001-000000000001', 'c0c00001-0000-0000-0001-000000000001',
 2242000.00, 'VNPAY', 'SUCCESS', 'VNP20260101001', '2026-01-08 14:22:00', '2026-01-08 14:20:00'),
('d0d00001-0000-0000-0001-000000000002', 'c0c00001-0000-0000-0001-000000000002',
 2176000.00, 'VNPAY', 'SUCCESS', 'VNP20260201001', '2026-02-10 10:05:00', '2026-02-10 10:03:00'),
('d0d00001-0000-0000-0001-000000000003', 'c0c00001-0000-0000-0001-000000000003',
 2258000.00, 'BANK_TRANSFER', 'SUCCESS', 'BTF20260301001', '2026-03-05 09:30:00', '2026-03-05 09:28:00'),
('d0d00001-0000-0000-0001-000000000004', 'c0c00001-0000-0000-0001-000000000004',
 2218000.00, 'VNPAY', 'SUCCESS', 'VNP20260401001', '2026-04-12 16:45:00', '2026-04-12 16:43:00'),

-- Hùng — bills 1-4 (PAID) + bill 5 FAILED
('d0d00001-0000-0000-0002-000000000001', 'c0c00001-0000-0000-0002-000000000001',
 2104000.00, 'CASH', 'SUCCESS', NULL, '2026-01-10 11:00:00', '2026-01-10 11:00:00'),
('d0d00001-0000-0000-0002-000000000002', 'c0c00001-0000-0000-0002-000000000002',
 2100000.00, 'CASH', 'SUCCESS', NULL, '2026-02-12 11:00:00', '2026-02-12 11:00:00'),
('d0d00001-0000-0000-0002-000000000003', 'c0c00001-0000-0000-0002-000000000003',
 2228000.00, 'E_WALLET', 'SUCCESS', 'EW20260301001', '2026-03-07 08:15:00', '2026-03-07 08:13:00'),
('d0d00001-0000-0000-0002-000000000004', 'c0c00001-0000-0000-0002-000000000004',
 2148000.00, 'VNPAY', 'SUCCESS', 'VNP20260401002', '2026-04-09 13:20:00', '2026-04-09 13:18:00'),
('d0d00001-0000-0000-0002-000000000005', 'c0c00001-0000-0000-0002-000000000005',
 2340000.00, 'VNPAY', 'FAILED', 'VNP20260503001', NULL, '2026-05-03 20:00:00'),

-- Tuấn — bills 1-3 (PAID)
('d0d00001-0000-0000-0003-000000000001', 'c0c00001-0000-0000-0003-000000000001',
 1580000.00, 'BANK_TRANSFER', 'SUCCESS', 'BTF20260201001', '2026-02-08 09:00:00', '2026-02-08 09:00:00'),
('d0d00001-0000-0000-0003-000000000002', 'c0c00001-0000-0000-0003-000000000002',
 1560000.00, 'BANK_TRANSFER', 'SUCCESS', 'BTF20260301002', '2026-03-06 09:00:00', '2026-03-06 09:00:00'),
('d0d00001-0000-0000-0003-000000000003', 'c0c00001-0000-0000-0003-000000000003',
 1540000.00, 'VNPAY', 'SUCCESS', 'VNP20260401003', '2026-04-10 10:30:00', '2026-04-10 10:28:00'),

-- Phương — bills 1-2 (PAID)
('d0d00001-0000-0000-0004-000000000001', 'c0c00001-0000-0000-0004-000000000001',
 3160000.00, 'VNPAY', 'SUCCESS', 'VNP20260301002', '2026-03-10 15:00:00', '2026-03-10 14:58:00'),
('d0d00001-0000-0000-0004-000000000002', 'c0c00001-0000-0000-0004-000000000002',
 3200000.00, 'VNPAY', 'SUCCESS', 'VNP20260401004', '2026-04-08 09:45:00', '2026-04-08 09:43:00'),

-- Ngọc — bills 1-3 (PAID)
('d0d00001-0000-0000-0005-000000000001', 'c0c00001-0000-0000-0005-000000000001',
 1780000.00, 'CASH', 'SUCCESS', NULL, '2026-02-14 10:00:00', '2026-02-14 10:00:00'),
('d0d00001-0000-0000-0005-000000000002', 'c0c00001-0000-0000-0005-000000000002',
 1820000.00, 'CASH', 'SUCCESS', NULL, '2026-03-11 10:00:00', '2026-03-11 10:00:00'),
('d0d00001-0000-0000-0005-000000000003', 'c0c00001-0000-0000-0005-000000000003',
 1800000.00, 'E_WALLET', 'SUCCESS', 'EW20260401001', '2026-04-13 11:30:00', '2026-04-13 11:28:00');
GO

-- ══════════════════════════════════════════════
-- 12. PAYMENT RECEIPTS (offline/manual payments)
--     IDs: e0e00001-...
-- ══════════════════════════════════════════════
INSERT INTO payment_receipts (id, payment_id, file_url, uploaded_at)
VALUES
('e0e00001-0000-0000-0000-000000000001',
 'd0d00001-0000-0000-0002-000000000001',
 'https://cdn.dormitory.vn/receipts/2026/01/hung_bill01.jpg',
 '2026-01-10 11:05:00'),
('e0e00001-0000-0000-0000-000000000002',
 'd0d00001-0000-0000-0002-000000000002',
 'https://cdn.dormitory.vn/receipts/2026/02/hung_bill02.jpg',
 '2026-02-12 11:05:00'),
('e0e00001-0000-0000-0000-000000000003',
 'd0d00001-0000-0000-0005-000000000001',
 'https://cdn.dormitory.vn/receipts/2026/02/ngoc_bill01.jpg',
 '2026-02-14 10:05:00'),
('e0e00001-0000-0000-0000-000000000004',
 'd0d00001-0000-0000-0005-000000000002',
 'https://cdn.dormitory.vn/receipts/2026/03/ngoc_bill02.jpg',
 '2026-03-11 10:05:00');
GO

-- ══════════════════════════════════════════════
-- 13. UTILITY READINGS
--     IDs: f0f00001-...
-- ══════════════════════════════════════════════
INSERT INTO utility_readings (id, room_id, utility_type,
                              previous_reading, current_reading,
                              reading_date, is_estimated, entered_by)
VALUES
-- Room 102 — Electricity
('f0f00001-0000-0000-0000-000000000001', 'cccc0001-0000-0000-0000-000000000002',
 'ELECTRICITY', 1108.000, 1200.000, '2026-04-01', 0, '22222222-0000-0000-0000-000000000001'),
('f0f00001-0000-0000-0000-000000000002', 'cccc0001-0000-0000-0000-000000000002',
 'ELECTRICITY', 1200.000, 1310.500, '2026-05-01', 0, '22222222-0000-0000-0000-000000000001'),
-- Room 102 — Water
('f0f00001-0000-0000-0000-000000000003', 'cccc0001-0000-0000-0000-000000000002',
 'WATER', 40.500, 45.000, '2026-04-01', 0, '22222222-0000-0000-0000-000000000001'),
('f0f00001-0000-0000-0000-000000000004', 'cccc0001-0000-0000-0000-000000000002',
 'WATER', 45.000, 49.500, '2026-05-01', 0, '22222222-0000-0000-0000-000000000001'),

-- Room 103 — Electricity
('f0f00001-0000-0000-0000-000000000005', 'cccc0001-0000-0000-0000-000000000003',
 'ELECTRICITY', 2010.000, 2100.000, '2026-04-01', 0, '22222222-0000-0000-0000-000000000001'),
('f0f00001-0000-0000-0000-000000000006', 'cccc0001-0000-0000-0000-000000000003',
 'ELECTRICITY', 2100.000, 2265.000, '2026-05-01', 0, '22222222-0000-0000-0000-000000000001'),
-- Room 103 — Water
('f0f00001-0000-0000-0000-000000000007', 'cccc0001-0000-0000-0000-000000000003',
 'WATER', 26.000, 30.000, '2026-04-01', 0, '22222222-0000-0000-0000-000000000001'),
('f0f00001-0000-0000-0000-000000000008', 'cccc0001-0000-0000-0000-000000000003',
 'WATER', 30.000, 33.800, '2026-05-01', 0, '22222222-0000-0000-0000-000000000001'),

-- Room B101 — Electricity
('f0f00001-0000-0000-0000-000000000009', 'cccc0001-0000-0000-0000-000000000006',
 'ELECTRICITY', 5640.000, 5800.000, '2026-05-01', 0, '22222222-0000-0000-0000-000000000001'),

-- Room C102 — Electricity
('f0f00002-0000-0000-0000-000000000001', 'cccc0002-0000-0000-0000-000000000002',
 'ELECTRICITY', 3310.000, 3400.000, '2026-05-01', 0, '22222222-0000-0000-0000-000000000002'),

-- Room HD-01 — Electricity (estimated)
('f0f00003-0000-0000-0000-000000000001', 'cccc0003-0000-0000-0000-000000000001',
 'ELECTRICITY', 835.000, 900.000, '2026-05-01', 1, '22222222-0000-0000-0000-000000000002');
GO

-- ══════════════════════════════════════════════
-- 14. VIEWING APPOINTMENTS
--     IDs: a1a10001-...
-- ══════════════════════════════════════════════
INSERT INTO viewing_appointments (id, tenant_id, room_id, appointment_date, note, status, created_at)
VALUES
('a1a10001-0000-0000-0000-000000000001',
 '33333333-0000-0000-0000-000000000006',
 'cccc0001-0000-0000-0000-000000000001',
 '2026-06-03 09:00:00',
 N'Muốn xem phòng vào buổi sáng, có thể tới sớm.',
 'COMPLETED', '2026-05-30 10:00:00'),

('a1a10001-0000-0000-0000-000000000002',
 '33333333-0000-0000-0000-000000000008',
 'cccc0001-0000-0000-0000-000000000004',
 '2026-06-07 14:00:00',
 N'Muốn xem phòng studio trước khi quyết định thuê.',
 'CONFIRMED', '2026-06-02 09:00:00'),

('a1a10001-0000-0000-0000-000000000003',
 '33333333-0000-0000-0000-000000000007',
 'cccc0001-0000-0000-0000-000000000007',
 '2026-06-10 10:00:00',
 N'Xem phòng KTX nữ, cần đặt trước.',
 'PENDING', '2026-06-04 15:30:00'),

('a1a10001-0000-0000-0000-000000000004',
 '33333333-0000-0000-0000-000000000006',
 'cccc0002-0000-0000-0000-000000000001',
 '2026-05-28 11:00:00',
 N'Bận đột xuất, không thể đến được.',
 'CANCELLED', '2026-05-20 16:00:00');
GO

-- ══════════════════════════════════════════════
-- 15. MAINTENANCE TICKETS
--     IDs: b1b10001-...
-- ══════════════════════════════════════════════
INSERT INTO maintenance_tickets (id, room_id, reporter_id, assignee_id, category,
                                  priority, description, status, sla_deadline, created_at)
VALUES
('b1b10001-0000-0000-0000-000000000001',
 'cccc0001-0000-0000-0000-000000000002',
 '33333333-0000-0000-0000-000000000001',
 NULL,
 N'Điện',
 'MEDIUM',
 N'Đèn phòng bị chập chờn, nhất là ban đêm. Cần kiểm tra hệ thống điện.',
 'OPEN', '2026-06-08 10:00:00', '2026-06-05 10:00:00'),

('b1b10001-0000-0000-0000-000000000002',
 'cccc0001-0000-0000-0000-000000000003',
 '33333333-0000-0000-0000-000000000002',
 '22222222-0000-0000-0000-000000000001',
 N'Nước',
 'HIGH',
 N'Vòi nước bồn rửa bị rỉ, nước chảy xuống sàn. Cần sửa gấp.',
 'IN_PROGRESS', '2026-06-06 11:00:00', '2026-06-05 11:00:00'),

('b1b10001-0000-0000-0000-000000000003',
 'cccc0001-0000-0000-0000-000000000006',
 '33333333-0000-0000-0000-000000000004',
 '22222222-0000-0000-0000-000000000001',
 N'Điều hoà',
 'LOW',
 N'Điều hoà không lạnh, cần vệ sinh hoặc nạp gas.',
 'RESOLVED', '2026-05-26 08:00:00', '2026-05-19 08:00:00'),

('b1b10001-0000-0000-0000-000000000004',
 'cccc0002-0000-0000-0000-000000000002',
 '33333333-0000-0000-0000-000000000003',
 '22222222-0000-0000-0000-000000000002',
 N'An toàn',
 'EMERGENCY',
 N'Ổ điện bị cháy, có khói bay ra từ ổ cắm gần giường ngủ. CẦN XỬ LÝ NGAY!',
 'OPEN', '2026-06-05 15:00:00', '2026-06-05 13:00:00'),

('b1b10001-0000-0000-0000-000000000005',
 'cccc0003-0000-0000-0000-000000000001',
 '33333333-0000-0000-0000-000000000005',
 '22222222-0000-0000-0000-000000000002',
 N'Khoá cửa',
 'MEDIUM',
 N'Khoá cửa phòng bị kẹt, không mở được từ bên ngoài.',
 'CLOSED', '2026-05-10 09:00:00', '2026-05-07 09:00:00');
GO

-- ══════════════════════════════════════════════
-- 16. REVIEWS
--     IDs: c1c10001-...
-- ══════════════════════════════════════════════
INSERT INTO reviews (id, tenant_id, room_id, rating, comment, moderation_status, created_at)
VALUES
('c1c10001-0000-0000-0000-000000000001',
 '33333333-0000-0000-0000-000000000001',
 'cccc0001-0000-0000-0000-000000000002',
 5,
 N'Phòng rất sạch sẽ và thoáng mát. Chủ trọ nhiệt tình, phản hồi nhanh khi có sự cố. Giá cả hợp lý so với khu vực. Sẽ tiếp tục thuê dài hạn!',
 'VISIBLE', '2026-04-15 09:00:00'),

('c1c10001-0000-0000-0000-000000000002',
 '33333333-0000-0000-0000-000000000002',
 'cccc0001-0000-0000-0000-000000000003',
 4,
 N'Phòng ổn, vị trí thuận tiện gần trường. Có một vài vấn đề nhỏ về điện nước nhưng chủ trọ đã xử lý kịp thời. Nhìn chung hài lòng.',
 'VISIBLE', '2026-04-20 14:30:00'),

('c1c10001-0000-0000-0000-000000000003',
 '33333333-0000-0000-0000-000000000004',
 'cccc0001-0000-0000-0000-000000000006',
 4,
 N'KTX sạch sẽ, an ninh tốt. Điều hoà hoạt động tốt sau khi được bảo trì. Giá phù hợp sinh viên.',
 'VISIBLE', '2026-05-01 10:00:00'),

('c1c10001-0000-0000-0000-000000000004',
 '33333333-0000-0000-0000-000000000005',
 'cccc0003-0000-0000-0000-000000000001',
 3,
 N'Phòng bình thường, giá rẻ phù hợp sinh viên. Nhà vệ sinh chung hơi bẩn cuối tuần. Wifi ổn định.',
 'VISIBLE', '2026-04-30 11:00:00');
GO

-- ══════════════════════════════════════════════
-- 17. COMPLAINTS
--     IDs: d1d10001-...
-- ══════════════════════════════════════════════
INSERT INTO complaints (id, reporter_id, target_type, target_id, category, description, status, created_at)
VALUES
('d1d10001-0000-0000-0000-000000000001',
 '33333333-0000-0000-0000-000000000006',
 'LISTING',
 'cccc0001-0000-0000-0000-000000000004',
 N'Thông tin sai lệch',
 N'Ảnh phòng trên hệ thống không khớp với thực tế. Phòng thực tế nhỏ hơn và không có ban công như trong ảnh quảng cáo.',
 'OPEN', '2026-06-01 10:00:00'),

('d1d10001-0000-0000-0000-000000000002',
 '33333333-0000-0000-0000-000000000007',
 'USER',
 '22222222-0000-0000-0000-000000000003',
 N'Hành vi không phù hợp',
 N'Chủ trọ tự ý vào phòng mà không thông báo trước. Đây là vi phạm quyền riêng tư của người thuê.',
 'UNDER_REVIEW', '2026-05-28 15:00:00'),

('d1d10001-0000-0000-0000-000000000003',
 '33333333-0000-0000-0000-000000000003',
 'REVIEW',
 'c1c10001-0000-0000-0000-000000000004',
 N'Đánh giá không hợp lệ',
 N'Đánh giá này vi phạm nội quy, chứa thông tin cá nhân của người khác.',
 'RESOLVED', '2026-05-20 09:00:00');
GO

-- ══════════════════════════════════════════════
-- 18. NOTIFICATIONS
--     IDs: e1e10001-...
-- ══════════════════════════════════════════════
INSERT INTO notifications (id, recipient_id, type, payload, read_at, created_at)
VALUES
-- Lan: hóa đơn tháng 5 chưa đọc
('e1e10001-0000-0000-0000-000000000001',
 '33333333-0000-0000-0000-000000000001',
 'PAYMENT_DUE',
 N'{"billId":"c0c00001-0000-0000-0001-000000000005","amount":2282000,"dueDate":"2026-05-15","billingPeriod":"2026-05"}',
 NULL, '2026-05-01 08:00:00'),

-- Lan: thanh toán thành công tháng 4 (đã đọc)
('e1e10001-0000-0000-0000-000000000002',
 '33333333-0000-0000-0000-000000000001',
 'PAYMENT_CONFIRMED',
 N'{"billId":"c0c00001-0000-0000-0001-000000000004","amount":2218000,"transactionRef":"VNP20260401001","paidAt":"2026-04-12T16:45:00"}',
 '2026-04-12 17:00:00', '2026-04-12 16:50:00'),

-- Lan: yêu cầu được duyệt (đã đọc)
('e1e10001-0000-0000-0000-000000000003',
 '33333333-0000-0000-0000-000000000001',
 'REQUEST_APPROVED',
 N'{"requestId":"a0a00001-0000-0000-0000-000000000001","roomCode":"MKA-102","startDate":"2026-01-01"}',
 '2025-12-20 10:00:00', '2025-12-18 14:00:00'),

-- Lan: hợp đồng sẵn sàng (đã đọc)
('e1e10001-0000-0000-0000-000000000004',
 '33333333-0000-0000-0000-000000000001',
 'CONTRACT_READY',
 N'{"contractId":"b0b00001-0000-0000-0000-000000000001","roomCode":"MKA-102","effectiveFrom":"2026-01-01"}',
 '2025-12-28 09:00:00', '2025-12-27 16:00:00'),

-- Hùng: hóa đơn quá hạn (chưa đọc)
('e1e10001-0000-0000-0000-000000000005',
 '33333333-0000-0000-0000-000000000002',
 'PAYMENT_OVERDUE',
 N'{"billId":"c0c00001-0000-0000-0002-000000000005","amount":2340000,"dueDate":"2026-05-15","daysOverdue":21}',
 NULL, '2026-05-16 08:00:00'),

-- Hùng: thanh toán thất bại (chưa đọc)
('e1e10001-0000-0000-0000-000000000006',
 '33333333-0000-0000-0000-000000000002',
 'PAYMENT_FAILED',
 N'{"billId":"c0c00001-0000-0000-0002-000000000005","amount":2340000,"transactionRef":"VNP20260503001","reason":"Insufficient funds"}',
 NULL, '2026-05-03 20:05:00'),

-- Hùng: cập nhật ticket bảo trì (chưa đọc)
('e1e10001-0000-0000-0000-000000000007',
 '33333333-0000-0000-0000-000000000002',
 'TICKET_UPDATE',
 N'{"ticketId":"b1b10001-0000-0000-0000-000000000002","status":"IN_PROGRESS","message":"Ky thuat vien se den sua vao chieu ngay mai"}',
 NULL, '2026-06-05 12:00:00'),

-- Landlord 1: nhận yêu cầu mới (chưa đọc)
('e1e10001-0000-0000-0000-000000000008',
 '22222222-0000-0000-0000-000000000001',
 'REQUEST_RECEIVED',
 N'{"requestId":"a0a00001-0000-0000-0000-000000000006","tenantName":"Ly Van Duc","roomCode":"MKA-101"}',
 NULL, '2026-06-01 09:05:00'),

-- Thông báo toàn hệ thống: bảo trì điện
('e1e10001-0000-0000-0000-000000000009',
 '33333333-0000-0000-0000-000000000003',
 'ANNOUNCEMENT',
 N'{"title":"Bao tri he thong dien","body":"KTX se cat dien 8h-12h ngay 10/06/2026 de bao tri.","affectsAllRooms":true}',
 NULL, '2026-06-04 17:00:00'),

('e1e10001-0000-0000-0000-000000000010',
 '33333333-0000-0000-0000-000000000004',
 'ANNOUNCEMENT',
 N'{"title":"Bao tri he thong dien","body":"KTX se cat dien 8h-12h ngay 10/06/2026 de bao tri.","affectsAllRooms":true}',
 NULL, '2026-06-04 17:00:00'),

-- Landlord 2: khẩn cấp ticket (chưa đọc)
('e1e10001-0000-0000-0000-00000000000b',
 '22222222-0000-0000-0000-000000000002',
 'TICKET_UPDATE',
 N'{"ticketId":"b1b10001-0000-0000-0000-000000000004","priority":"EMERGENCY","message":"O dien bi chay tai phong C102 - CAN XU LY NGAY","slaDeadline":"2026-06-05T15:00:00"}',
 NULL, '2026-06-05 13:01:00');
GO

-- ══════════════════════════════════════════════
-- 19. AUDIT LOGS
--     IDs: f1f10001-...
-- ══════════════════════════════════════════════
INSERT INTO audit_logs (id, actor_id, action, entity_name, entity_id, ip_address, meta, created_at)
VALUES
-- Login
('f1f10001-0000-0000-0000-000000000001',
 '11111111-0000-0000-0000-000000000001',
 'LOGIN', 'User', '11111111-0000-0000-0000-000000000001',
 '192.168.1.1',
 N'{"method":"PASSWORD","success":true}',
 '2026-06-05 08:00:00'),

('f1f10001-0000-0000-0000-000000000002',
 '22222222-0000-0000-0000-000000000001',
 'LOGIN', 'User', '22222222-0000-0000-0000-000000000001',
 '113.190.45.123',
 N'{"method":"PASSWORD","success":true}',
 '2026-06-05 08:30:00'),

('f1f10001-0000-0000-0000-000000000003',
 '33333333-0000-0000-0000-000000000008',
 'LOGIN', 'User', '33333333-0000-0000-0000-000000000008',
 '27.72.101.55',
 N'{"method":"GOOGLE_OAUTH","success":true}',
 '2026-06-05 09:15:00'),

-- Payment events
('f1f10001-0000-0000-0000-000000000004',
 '33333333-0000-0000-0000-000000000001',
 'PAYMENT_CONFIRMED', 'Payment', 'd0d00001-0000-0000-0001-000000000004',
 '118.70.33.99',
 N'{"billId":"c0c00001-0000-0000-0001-000000000004","amount":2218000,"method":"VNPAY","transactionRef":"VNP20260401001"}',
 '2026-04-12 16:46:00'),

('f1f10001-0000-0000-0000-000000000005',
 '33333333-0000-0000-0000-000000000002',
 'PAYMENT_FAILED', 'Payment', 'd0d00001-0000-0000-0002-000000000005',
 '118.70.11.22',
 N'{"billId":"c0c00001-0000-0000-0002-000000000005","amount":2340000,"method":"VNPAY","transactionRef":"VNP20260503001","reason":"Insufficient funds"}',
 '2026-05-03 20:01:00'),

-- Request approvals
('f1f10001-0000-0000-0000-000000000006',
 '22222222-0000-0000-0000-000000000001',
 'REQUEST_APPROVED', 'RentalRequest', 'a0a00001-0000-0000-0000-000000000001',
 '113.190.45.123',
 N'{"tenantId":"33333333-0000-0000-0000-000000000001","roomCode":"MKA-102","startDate":"2026-01-01"}',
 '2025-12-18 13:55:00'),

('f1f10001-0000-0000-0000-000000000007',
 '22222222-0000-0000-0000-000000000001',
 'REQUEST_APPROVED', 'RentalRequest', 'a0a00001-0000-0000-0000-000000000002',
 '113.190.45.123',
 N'{"tenantId":"33333333-0000-0000-0000-000000000002","roomCode":"MKA-103","startDate":"2026-01-01"}',
 '2025-12-18 14:00:00'),

-- Room published
('f1f10001-0000-0000-0000-000000000008',
 '22222222-0000-0000-0000-000000000001',
 'ROOM_PUBLISHED', 'Room', 'cccc0001-0000-0000-0000-000000000001',
 '113.190.45.123',
 N'{"roomCode":"MKA-101","oldStatus":"DRAFT","newStatus":"AVAILABLE"}',
 '2025-11-01 10:00:00'),

-- Admin moderation
('f1f10001-0000-0000-0000-000000000009',
 '11111111-0000-0000-0000-000000000001',
 'MODERATION_ACTION', 'Complaint', 'd1d10001-0000-0000-0000-000000000003',
 '192.168.1.1',
 N'{"action":"RESOLVED","reason":"Review khong vi pham noi quy","complaintId":"d1d10001-0000-0000-0000-000000000003"}',
 '2026-05-21 09:00:00'),

-- Contract signed
('f1f10001-0000-0000-0000-00000000000a',
 '33333333-0000-0000-0000-000000000001',
 'CONTRACT_SIGNED', 'Contract', 'b0b00001-0000-0000-0000-000000000001',
 '118.70.33.99',
 N'{"contractId":"b0b00001-0000-0000-0000-000000000001","signedAt":"2025-12-25T10:00:00","ip":"118.70.33.99"}',
 '2025-12-25 10:00:00'),

('f1f10001-0000-0000-0000-00000000000b',
 '22222222-0000-0000-0000-000000000001',
 'CONTRACT_SIGNED', 'Contract', 'b0b00001-0000-0000-0000-000000000001',
 '113.190.45.123',
 N'{"contractId":"b0b00001-0000-0000-0000-000000000001","signedAt":"2025-12-26T14:30:00","ip":"113.190.45.123"}',
 '2025-12-26 14:30:00');
GO

-- ══════════════════════════════════════════════
PRINT '=========================================';
PRINT 'SEED DATA v2 INSERTED SUCCESSFULLY';
PRINT '-----------------------------------------';
PRINT 'Users        : 11 (1 ADMIN, 3 LANDLORD, 7 TENANT)';
PRINT 'Properties   : 4  (3 ACTIVE, 1 DRAFT)';
PRINT 'BlockFloors  : 10';
PRINT 'Rooms        : 12';
PRINT 'RoomImages   : 7';
PRINT 'UtilPrices   : 6';
PRINT 'UtilMeters   : 10';
PRINT 'RentalReqs   : 8  (APPROVED/PENDING/REJECTED)';
PRINT 'Contracts    : 5  (all ACTIVE)';
PRINT 'Bills        : 22 (PAID/PENDING/OVERDUE)';
PRINT 'Payments     : 17 (SUCCESS/FAILED)';
PRINT 'PayReceipts  : 4';
PRINT 'UtilReadings : 11';
PRINT 'ViewingAppts : 4';
PRINT 'MaintTickets : 5';
PRINT 'Reviews      : 4';
PRINT 'Complaints   : 3';
PRINT 'Notifications: 11';
PRINT 'AuditLogs    : 11';
PRINT '=========================================';
GO
