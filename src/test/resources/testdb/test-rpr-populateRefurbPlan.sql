-- Clear the "RefurbPlans" TEST table
DELETE FROM LabSvcSchema.RefurbPlans;

-- Insert statements for the "RefurbPlans" TEST table
INSERT INTO LabSvcSchema.RefurbPlans (refurb_plan_id, battery_id, refurb_plan_priority, refurb_plan_start_date, refurb_plan_end_date, available, resolder, resolder_record_id, repack, repack_record_id, processor_swap, processor_swap_record_id, capacitor_swap, capacitor_swap_record_id) VALUES
(1, 1, 40, '2023-01-13 09:00:00', NULL, TRUE, TRUE, 1, TRUE, NULL, TRUE, 3, TRUE, 4),
(2, 2, 70, '2023-06-10 06:00:00', '2023-06-11 13:00:00', FALSE, FALSE, NULL, FALSE, NULL, FALSE, NULL, FALSE, NULL);