-- Clear the "TesterStations" TEST table
DELETE FROM LabSvcSchema.TesterStations;

-- Insert statements for the "TesterStations" TEST table
INSERT INTO LabSvcSchema.TesterStations (tester_stn_id, terminal_layout_id, in_use, active_battery_id, last_active_date, last_calibration_date, next_calibration_date) VALUES
(1, 5, TRUE, 1, '2023-06-13 09:00:00', '2023-05-13 09:00:00', '2025-05-13 09:00:00'),
(2, 6, FALSE, NULL, '2023-09-23 06:00:00', '2023-05-16 09:00:00', '2025-05-16 09:00:00');