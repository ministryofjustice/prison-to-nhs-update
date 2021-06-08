    {{/* vim: set filetype=mustache: */}}
{{/*
Environment variables for web and worker containers
*/}}
{{- define "deployment.envs" }}
env:
  - name: SERVER_PORT
    value: "{{ .Values.image.port }}"

  - name: JAVA_OPTS
    value: "{{ .Values.env.JAVA_OPTS }}"

  - name: JWT_PUBLIC_KEY
    value: "{{ .Values.env.JWT_PUBLIC_KEY }}"

  - name: SPRING_PROFILES_ACTIVE
    value: "postgres,logstash"

  - name: API_BASE_URL_OAUTH
    value: "{{ .Values.env.API_BASE_URL_OAUTH }}"

  - name: API_BASE_URL_NOMIS
    value: "{{ .Values.env.API_BASE_URL_NOMIS }}"

  - name: API_BASE_URL_PRISON_ESTATE
    value: "{{ .Values.env.API_BASE_URL_PRISON_ESTATE }}"

  - name: API_BASE_URL_NHS
    value: "{{ .Values.env.API_BASE_URL_NHS }}"

  - name: PRISONTONHS_ONLY_PRISONS
    value: "{{ .Values.env.PRISONTONHS_ONLY_PRISONS }}"

  - name: NHS_SERVER_ENABLED
    value: "{{ .Values.env.NHS_SERVER_ENABLED }}"

  - name: APPINSIGHTS_INSTRUMENTATIONKEY
    valueFrom:
      secretKeyRef:
        key: APPINSIGHTS_INSTRUMENTATIONKEY
        name: {{ template "app.name" . }}
  - name: APPLICATIONINSIGHTS_CONNECTION_STRING
    value: "InstrumentationKey=$(APPINSIGHTS_INSTRUMENTATIONKEY)"

  - name: OAUTH_CLIENT_ID
    valueFrom:
      secretKeyRef:
        name: {{ template "app.name" . }}
        key: PRISON_TO_NHS_CLIENT_ID

  - name: OAUTH_CLIENT_SECRET
    valueFrom:
      secretKeyRef:
        name: {{ template "app.name" . }}
        key: PRISON_TO_NHS_CLIENT_SECRET

  - name: SQS_AWS_ACCESS_KEY_ID
    valueFrom:
      secretKeyRef:
        name: ptnhs-sqs-instance-output
        key: access_key_id

  - name: SQS_AWS_SECRET_ACCESS_KEY
    valueFrom:
      secretKeyRef:
        name: ptnhs-sqs-instance-output
        key: secret_access_key

  - name: SQS_QUEUE_NAME
    valueFrom:
      secretKeyRef:
        name: ptnhs-sqs-instance-output
        key: sqs_ptnhs_name

  - name: SQS_AWS_DLQ_ACCESS_KEY_ID
    valueFrom:
      secretKeyRef:
        name: ptnhs-sqs-dl-instance-output
        key: access_key_id

  - name: SQS_AWS_DLQ_SECRET_ACCESS_KEY
    valueFrom:
      secretKeyRef:
        name: ptnhs-sqs-dl-instance-output
        key: secret_access_key

  - name: SQS_DLQ_NAME
    valueFrom:
      secretKeyRef:
        name: ptnhs-sqs-dl-instance-output
        key: sqs_ptnhs_name

  - name: DATABASE_PASSWORD
    valueFrom:
      secretKeyRef:
        name: dps-rds-instance-output
        key: prison_to_nhs_update_password

  - name: SUPERUSER_USERNAME
    valueFrom:
      secretKeyRef:
        name: dps-rds-instance-output
        key: database_username

  - name: SUPERUSER_PASSWORD
    valueFrom:
      secretKeyRef:
        name: dps-rds-instance-output
        key: database_password

  - name: DATABASE_NAME
    valueFrom:
      secretKeyRef:
        name: dps-rds-instance-output
        key: database_name

  - name: DATABASE_ENDPOINT
    valueFrom:
      secretKeyRef:
        name: dps-rds-instance-output
        key: rds_instance_endpoint

{{- end -}}
