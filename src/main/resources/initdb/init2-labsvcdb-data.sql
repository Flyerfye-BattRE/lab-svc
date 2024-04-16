-- Insert statements for the "TestSchemes" table
INSERT INTO LabSvcSchema.TestSchemes (test_scheme_id, checkerboard, null_line, vapor_sim, blackout, oven_screen) VALUES
(1, false, true, false, false, false),
(2, false, false, true, false, false),
(3, false, false, false, true, false),
(4, false, false, false, false, true),
(5, true, false, false, false, false),
(6, false, true, false, true, false),
(7, true, false, true, false, true),
(8, true, true, true, true, true),
(9, false, true, true, true, true);

-- Insert statements for the "RefurbStationTypes" table
INSERT INTO LabSvcSchema.RefurbStationTypes (refurb_station_type_id, station_type) VALUES
(1, 'Soldering'),
(2, 'Repackaging'),
(3, 'Processor Swap'),
(4, 'Capacitor Swap');

-- Insert statements for the "TesterStations" table
INSERT INTO LabSvcSchema.TesterStations (tester_stn_id, terminal_layout_id, last_calibration_date, next_calibration_date) VALUES
(1, 1, '2024-01-27 17:30:54.168Z', '2024-07-27 16:30:54.168Z'),
(2, 4, '2024-01-17 18:25:54.234Z', '2024-07-17 17:25:54.234Z'),
(3, 2, '2024-01-12 01:00:54.754Z', '2024-07-12 00:00:54.754Z'),
(4, 5, '2024-01-03 22:15:54.121Z', '2024-07-03 21:15:54.121Z'),
(5, 3, '2024-01-19 12:11:54.121Z', '2024-07-19 11:11:54.121Z'),
(6, 1, '2024-01-09 02:05:54.121Z', '2024-07-09 02:05:54.121Z');

-- Insert statements for the "RefurbStations" table
INSERT INTO LabSvcSchema.RefurbStations (refurb_stn_id, refurb_station_type_id, last_calibration_date, next_calibration_date) VALUES
(1, 1, '2023-10-28 22:10:54.168Z', '2024-04-28 22:10:54.168Z'),
(2, 4, '2023-10-21 18:10:54.234Z', '2024-04-21 18:10:54.234Z'),
(3, 2, '2023-10-19 02:10:54.754Z', '2024-04-19 02:10:54.754Z'),
(4, 3, '2023-10-08 13:10:54.121Z', '2024-04-08 13:10:54.121Z');

-- Insert statements for the "ResultTypes" table
INSERT INTO LabSvcSchema.ResultTypes (result_type_id, result_type) VALUES
(1, 'Pass'),
(2, 'Fail'),
(3, 'Exception'),
(4, 'Retry');

-- Insert statements for the "RefurbActions" table
INSERT INTO LabSvcSchema.RefurbActions (refurb_action_id, refurb_action) VALUES
(1, 'Retest'),
(2, 'Next Refurb Step'),
(3, 'Move to Storage'),
(4, 'Destroy');








