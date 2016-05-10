CREATE TABLE eap_jars
(
   "release" VARCHAR(30) NOT NULL, 
   file VARCHAR(60) NOT NULL, 
   "group" VARCHAR(60),
   artifact  VARCHAR(60), 
   version VARCHAR(20),
   name VARCHAR(60),
   md5_unsigned CHAR(32),
   md5 CHAR(32),
   sha CHAR(40)
) 
WITHOUT OIDS;
ALTER TABLE eap_jars OWNER TO ozizka;
COMMENT ON COLUMN eap_jars."release" IS 'EAP release version';
COMMENT ON COLUMN eap_jars.file IS 'Base file name, e.g. quartz.jar';
COMMENT ON COLUMN eap_jars."group" IS 'Maven groupId.';
COMMENT ON COLUMN eap_jars.artifact IS 'Maven artifactId.';
COMMENT ON COLUMN eap_jars.version IS 'Maven version.';

COMMENT ON TABLE eap_jars IS 'EAP releases metadata.
See https://docspace.corp.redhat.com/docs/DOC-53203';


