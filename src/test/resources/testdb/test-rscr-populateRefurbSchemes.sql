-- Clear the "RefurbSchemes" TEST table
DELETE FROM LabSvcSchema.RefurbSchemes;

-- Insert statements for the "RefurbSchemes" TEST table
INSERT INTO LabSvcSchema.RefurbSchemes (refurb_scheme_id, resolder, repack, processor_swap, capacitor_swap) VALUES
(1, TRUE, FALSE, TRUE, FALSE),
(2, FALSE, TRUE, FALSE, TRUE),
(3, FALSE, FALSE, FALSE, TRUE);