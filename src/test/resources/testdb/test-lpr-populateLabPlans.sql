-- Clear the "LabPlans" TEST table
DELETE FROM LabSvcSchema.LabPlans;

-- Insert statements for the "LabPlans" TEST table
INSERT INTO LabSvcSchema.LabPlans (lab_plan_id, lab_plan_start_date, lab_plan_end_date, battery_id, tester_record_id, refurb_plan_id) VALUES
(1, '2023-02-13 09:00:00', '2024-02-13 17:00:00', 1, 2, 3),
(2, '2023-03-23 06:00:00', NULL, 2, 3, 4);