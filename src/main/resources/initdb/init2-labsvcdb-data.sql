-- Insert statements for the "TestSchemes" table
INSERT INTO LabSvcSchema.TestSchemes (test_scheme_id, checkerboard, null_line, vapor_sim, blackout, oven_screen)
SELECT * FROM (VALUES
	(1, false, true, false, false, false),
	(2, false, false, true, false, false),
	(3, false, false, false, true, false),
	(4, false, false, false, false, true),
	(5, true, false, false, false, false),
	(6, false, true, false, true, false),
	(7, true, false, true, false, true),
	(8, true, true, true, true, true),
	(9, false, true, true, true, true)
) AS v (test_scheme_id, checkerboard, null_line, vapor_sim, blackout, oven_screen)
WHERE NOT EXISTS (
    SELECT 1 FROM LabSvcSchema.TestSchemes
);

-- Insert statements for the "RefurbSchemes" table
INSERT INTO LabSvcSchema.RefurbSchemes (refurb_scheme_id, resolder, repack, processor_swap, capacitor_swap)
SELECT * FROM (VALUES
	(1, true, false, false, false),
	(2, false, true, false, false),
	(3, false, false, true, false),
	(4, false, false, false, true),
	(5, false, false, false, false),
	(6, true, false, true, false),
	(7, false, true, false, true),
	(8, true, true, true, false),
	(9, false, true, true, true),
	(10, true, true, true, true)
) AS v (refurb_scheme_id, resolder, repack, processor_swap, capacitor_swap)
WHERE NOT EXISTS (
    SELECT 1 FROM LabSvcSchema.RefurbSchemes
);

-- Insert statements for the "RefurbStationClasses" table
INSERT INTO LabSvcSchema.RefurbStationClasses (refurb_station_class_id, station_class)
SELECT * FROM (VALUES
	(1, 'RESOLDER'),
	(2, 'REPACK'),
	(3, 'PROCESSOR_SWAP'),
	(4, 'CAPACITOR_SWAP')
) AS v (refurb_station_class_id, station_class)
WHERE NOT EXISTS (
    SELECT 1 FROM LabSvcSchema.RefurbStationClasses
);

-- Insert statements for the "TesterStations" table
INSERT INTO LabSvcSchema.TesterStations (tester_stn_id, terminal_layout_id, in_use, active_battery_id, last_active_date, last_calibration_date, next_calibration_date)
SELECT * FROM (VALUES
	(1, 1, false, NULL::integer, '2024-01-27 17:30:54.168Z'::timestamp, '2024-01-27 17:30:54.168Z'::timestamp, '2024-07-27 16:30:54.168Z'::timestamp),
	(2, 4, false, NULL::integer, '2024-01-17 18:25:54.234Z'::timestamp, '2024-01-17 18:25:54.234Z'::timestamp, '2024-07-17 17:25:54.234Z'::timestamp),
	(3, 2, false, NULL::integer, '2024-01-12 01:00:54.754Z'::timestamp, '2024-01-12 01:00:54.754Z'::timestamp, '2024-07-12 00:00:54.754Z'::timestamp),
	(4, 5, false, NULL::integer, '2024-01-03 22:15:54.121Z'::timestamp, '2024-01-03 22:15:54.121Z'::timestamp, '2024-07-03 21:15:54.121Z'::timestamp),
	(5, 3, false, NULL::integer, '2024-01-19 12:11:54.121Z'::timestamp, '2024-01-19 12:11:54.121Z'::timestamp, '2024-07-19 11:11:54.121Z'::timestamp),
	(6, 1, false, NULL::integer, '2024-01-09 02:05:54.121Z'::timestamp, '2024-01-09 02:05:54.121Z'::timestamp, '2024-07-09 02:05:54.121Z'::timestamp)
) AS v (tester_stn_id, terminal_layout_id, in_use, active_battery_id, last_active_date, last_calibration_date, next_calibration_date)
WHERE NOT EXISTS (
    SELECT 1 FROM LabSvcSchema.TesterStations
);

-- Insert statements for the "RefurbStations" table
INSERT INTO LabSvcSchema.RefurbStations (refurb_stn_id, refurb_station_class_id, in_use, active_battery_id, last_active_date, last_calibration_date, next_calibration_date)
SELECT * FROM (VALUES
	(1, 1, false, NULL::integer, '2023-10-28 22:10:54.168Z'::timestamp, '2023-10-28 22:10:54.168Z'::timestamp, '2024-04-28 22:10:54.168Z'::timestamp),
	(2, 4, false, NULL::integer, '2023-10-21 18:10:54.234Z'::timestamp, '2023-10-21 18:10:54.234Z'::timestamp, '2024-04-21 18:10:54.234Z'::timestamp),
	(3, 2, false, NULL::integer, '2023-10-19 02:10:54.754Z'::timestamp, '2023-10-19 02:10:54.754Z'::timestamp, '2024-04-19 02:10:54.754Z'::timestamp),
	(4, 3, false, NULL::integer, '2023-10-08 13:10:54.121Z'::timestamp, '2023-10-08 13:10:54.121Z'::timestamp, '2024-04-08 13:10:54.121Z'::timestamp)
) AS v (refurb_stn_id, refurb_station_class_id, in_use, active_battery_id, last_active_date, last_calibration_date, next_calibration_date)
WHERE NOT EXISTS (
    SELECT 1 FROM LabSvcSchema.RefurbStations
);

-- Insert statements for the "ResultTypes" table
INSERT INTO LabSvcSchema.ResultTypes (result_type_id, result_type)
SELECT * FROM (VALUES
	(1, 'Pass'),
	(2, 'Fail-Retry'),
	(3, 'Fail-Reject'),
	(4, 'Exception')
) AS v (result_type_id, result_type)
WHERE NOT EXISTS (
    SELECT 1 FROM LabSvcSchema.ResultTypes
);