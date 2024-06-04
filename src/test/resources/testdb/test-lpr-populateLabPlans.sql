-- Clear the "TesterRecords" TEST table
DELETE FROM LabSvcSchema.TesterRecords;

-- Insert statements for the "TesterRecords" TEST table
INSERT INTO LabSvcSchema.TesterRecords (tester_record_id, tester_stn_id, battery_id, test_scheme_id, refurb_scheme_id, result_type_id, test_date) VALUES
(2, 1, 1, 1, 1, 1, '2023-02-13 13:00:00'),
(3, 2, 2, 2, 2, 2, '2023-03-23 16:00:00'),
(4, 1, 3, 3, 3, 3, '2023-03-23 16:00:00');

-- Clear the "RefurbPlans" TEST table
DELETE FROM LabSvcSchema.RefurbPlans;

-- Insert statements for the "RefurbPlans" TEST table
INSERT INTO LabSvcSchema.RefurbPlans (refurb_plan_id, battery_id, refurb_plan_priority, refurb_plan_start_date, refurb_plan_end_date, available, resolder, resolder_record_id, repack, repack_record_id, processor_swap, processor_swap_record_id, capacitor_swap, capacitor_swap_record_id) VALUES
(3, 1, 1, '2023-02-13 15:00:00', '2024-02-13 17:00:00', TRUE, FALSE, NULL, FALSE, NULL, FALSE, NULL, FALSE, NULL),
(4, 2, 2, '2023-03-23 17:00:00', NULL, TRUE, TRUE, 1, TRUE, 2, FALSE, NULL, FALSE, NULL),
(5, 2, 2, '2023-03-23 18:00:00', NULL, TRUE, TRUE, 1, TRUE, 2, FALSE, NULL, FALSE, NULL);


-- Clear the "LabPlans" TEST table
DELETE FROM LabSvcSchema.LabPlans;

-- Insert statements for the "LabPlans" TEST table
INSERT INTO LabSvcSchema.LabPlans (lab_plan_id, lab_plan_status_id, lab_plan_start_date, lab_plan_end_date, battery_id, tester_record_id, refurb_plan_id) VALUES
(1, 1, '2023-02-13 09:00:00', '2024-02-13 17:00:00', 1, 2, 3),
(2, 1, '2023-03-23 06:00:00', NULL, 2, 3, 4);