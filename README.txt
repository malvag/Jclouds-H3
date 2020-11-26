#
# The jclouds API for accessing H3 as a blobstore.
#
# TODO: Implementation status.
> S3proxy:1.7.1: it can't run in JDK 7 (based on instructions). 
(change_JDK:JDK 8)


> S3proxy:1.7.1: JRE has to run with JRE 9+ to pass class mismatch with JH3.
(change_JRE:JDK 9+)


> Jclouds:2.2.1: It uses animal-sniffer-plugin that can check signatures up to JDK 8 but our JH3 has JDK 9 as a target so we can’t compile them with the animal-sniffer-plugin active.
(maven-plugin: animal-sniffer-plugin:skipped)


> Our jclouds:H3 api provider is meant to be installed on its own with JDK 7 explicitly.

>RSA
Generating 2,048 bit RSA key pair and self-signed certificate (SHA256withRSA) with a validity of 90 days for: CN=Evangelos Maliaroudakis, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=GR

> Pass: CARVICS


# TODO: Supported features.
# TODO: Usage example.
