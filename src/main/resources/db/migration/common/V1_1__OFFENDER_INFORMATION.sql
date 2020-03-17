CREATE TABLE OFFENDER_PATIENT_RECORD
(
    NOMS_ID             VARCHAR(10) NOT NULL PRIMARY KEY,
    PATIENT_RECORD      TEXT NOT NULL,
    UPDATED_TIMESTAMP   TIMESTAMP NOT NULL
);

GRANT SELECT, INSERT, UPDATE, DELETE ON OFFENDER_PATIENT_RECORD TO nhs_offender;
