-- Clear the "RefurbStations" TEST table
DELETE FROM LabSvcSchema.RefurbStations;

-- Insert statements for the "RefurbStations" TEST table
INSERT INTO LabSvcSchema.RefurbStations (refurb_stn_id, refurb_station_class_id, in_use, active_battery_id, last_active_date, last_calibration_date, next_calibration_date) VALUES
(1, 3, TRUE, 1, '2023-06-13 09:00:00', '2023-05-13 09:00:00', '2025-05-13 09:00:00'),
(2, 4, FALSE, NULL, '2023-09-23 06:00:00', '2023-05-16 09:00:00', '2025-05-16 09:00:00');