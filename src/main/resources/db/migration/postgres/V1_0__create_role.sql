CREATE ROLE nhs_offender LOGIN PASSWORD '${database_password}';
GRANT USAGE ON SCHEMA nhs_offender TO nhs_offender;
