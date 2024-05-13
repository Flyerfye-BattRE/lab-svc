-- Clear the "TesterBacklog" TEST table
DELETE FROM LabSvcSchema.TesterBacklog;

-- Insert statements for the "TesterBacklog" TEST table
INSERT INTO LabSvcSchema.TesterBacklog (tester_backlog_id, terminal_layout_id, test_scheme_id, battery_id, tester_backlog_priority, tester_backlog_start_date, tester_backlog_end_date) VALUES
(1, 2, 3, 4, 50, '2024-05-13 09:00:00', '2024-05-14 17:00:00'),
(2, 4, 5, 6, 60, '2024-05-13 09:00:00', NULL);