INSERT INTO study_rooms (
    name,
    room_type,
    min_capacity,
    max_capacity,
    open_time,
    close_time,
    active
)
VALUES
    ('4인 1호실', 'ROOM_4', 1, 4, '06:00', '23:00', TRUE),
    ('4인 2호실', 'ROOM_4', 1, 4, '06:00', '23:00', TRUE),
    ('4인 3호실', 'ROOM_4', 1, 4, '06:00', '23:00', TRUE),
    ('6인 1호실', 'ROOM_6', 3, 6, '06:00', '23:00', TRUE),
    ('6인 2호실', 'ROOM_6', 3, 6, '06:00', '23:00', TRUE),
    ('6인 3호실', 'ROOM_6', 3, 6, '06:00', '23:00', TRUE),
    ('8인 1호실', 'ROOM_8', 5, 8, '06:00', '23:00', TRUE),
    ('8인 2호실', 'ROOM_8', 5, 8, '06:00', '23:00', TRUE),
    ('8인 3호실', 'ROOM_8', 5, 8, '06:00', '23:00', TRUE),
    ('10인 1호실', 'ROOM_10', 6, 10, '06:00', '23:00', TRUE),
    ('10인 2호실', 'ROOM_10', 6, 10, '06:00', '23:00', TRUE),
    ('10인 3호실', 'ROOM_10', 6, 10, '06:00', '23:00', TRUE)
ON CONFLICT (name) DO NOTHING;
