-- -----------------------------------------------------
-- Schema LabSvcSchema
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS LabSvcSchema;

CREATE TABLE IF NOT EXISTS LabSvcSchema.TestSchemes (
  test_scheme_id SERIAL PRIMARY KEY,
  checkerboard BOOLEAN DEFAULT FALSE,
  null_line BOOLEAN DEFAULT FALSE,
  vapor_sim BOOLEAN DEFAULT FALSE,
  blackout BOOLEAN DEFAULT FALSE,
  oven_screen BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS LabSvcSchema.RefurbPlans (
  refurb_plan_id SERIAL PRIMARY KEY,
  battery_id INT NOT NULL,
  refurb_start_date TIMESTAMP NOT NULL,
  refurb_end_date TIMESTAMP,
  resolder BOOLEAN DEFAULT FALSE,
  repack BOOLEAN DEFAULT FALSE,
  processor_swap BOOLEAN DEFAULT FALSE,
  capacitor_swap BOOLEAN DEFAULT FALSE,
  retest BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS LabSvcSchema.RefurbStationTypes (
  refurb_station_type_id SERIAL PRIMARY KEY,
  station_type VARCHAR(45) NOT NULL
);

CREATE TABLE IF NOT EXISTS LabSvcSchema.TesterStations (
  tester_stn_id SERIAL PRIMARY KEY,
  terminal_layout_id INT NOT NULL,
  in_use BOOLEAN DEFAULT FALSE,
  active_battery_id INT,
  last_active_date TIMESTAMP,
  last_calibration_date TIMESTAMP NOT NULL,
  next_calibration_date TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS LabSvcSchema.RefurbStations (
  refurb_stn_id SERIAL PRIMARY KEY,
  refurb_station_type_id INT NOT NULL,
  in_use BOOLEAN DEFAULT FALSE,
  active_battery_id INT,
  last_active_date TIMESTAMP,
  last_calibration_date TIMESTAMP NOT NULL,
  next_calibration_date TIMESTAMP NOT NULL,
  CONSTRAINT rs_refurb_station_type_id FOREIGN KEY (refurb_station_type_id) REFERENCES LabSvcSchema.RefurbStationTypes(refurb_station_type_id) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS LabSvcSchema.ResultTypes (
  result_type_id SERIAL PRIMARY KEY,
  result_type VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS LabSvcSchema.RefurbActions (
  refurb_action_id SERIAL PRIMARY KEY,
  refurb_action VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS LabSvcSchema.TesterBacklog (
  tester_backlog_id SERIAL PRIMARY KEY,
  terminal_layout_id INT NOT NULL,
  battery_id INT NOT NULL,
  tester_backlog_priority INT NOT NULL,
  tester_backlog_start_date TIMESTAMP NOT NULL,
  tester_backlog_end_date TIMESTAMP
);

CREATE TABLE IF NOT EXISTS LabSvcSchema.RefurbBacklog (
  refurb_backlog_id SERIAL PRIMARY KEY,
  refurb_plan_id INT NOT NULL,
  battery_id INT NOT NULL,
  refurb_backlog_priority INT NOT NULL,
  refurb_backlog_start_date TIMESTAMP NOT NULL,
  refurb_backlog_end_date TIMESTAMP,
  CONSTRAINT refurb_plan_id FOREIGN KEY (refurb_plan_id) REFERENCES LabSvcSchema.RefurbPlans(refurb_plan_id) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS LabSvcSchema.RefurbRecords (
  refurb_record_id SERIAL PRIMARY KEY,
  battery_id INT NOT NULL,
  refurb_plan_id INT NOT NULL,
  refurb_stn_id INT NOT NULL,
  refurb_action_id INT NOT NULL,
  result_type_id INT NOT NULL,
  CONSTRAINT rr_refurb_plan_id FOREIGN KEY (refurb_plan_id) REFERENCES LabSvcSchema.RefurbPlans(refurb_plan_id) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT rr_refurb_stn_id FOREIGN KEY (refurb_stn_id) REFERENCES LabSvcSchema.RefurbStations(refurb_stn_id) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT rr_refurb_action_id FOREIGN KEY (refurb_action_id) REFERENCES LabSvcSchema.RefurbActions(refurb_action_id) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT rr_result_type_id FOREIGN KEY (result_type_id) REFERENCES LabSvcSchema.ResultTypes(result_type_id) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS LabSvcSchema.TesterRecords (
  tester_record_id SERIAL PRIMARY KEY,
  tester_stn_id INT NOT NULL,
  battery_id INT NOT NULL,
  test_scheme_id INT NOT NULL,
  test_date TIMESTAMP NOT NULL,
  result_type_id INT NOT NULL,
  CONSTRAINT tr_tester_stn_id FOREIGN KEY (tester_stn_id) REFERENCES LabSvcSchema.TesterStations(tester_stn_id) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT tr_test_scheme_id FOREIGN KEY (test_scheme_id) REFERENCES LabSvcSchema.TestSchemes(test_scheme_id) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT tr_result_type_id FOREIGN KEY (result_type_id) REFERENCES LabSvcSchema.ResultTypes(result_type_id) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS LabSvcSchema.LabPlans (
  lab_plan_id SERIAL PRIMARY KEY,
  lab_plan_start_date TIMESTAMP NOT NULL,
  lab_plan_end_date TIMESTAMP,
  battery_id INT NOT NULL,
  tester_record_id INT,
  refurb_plan_id INT
);