
## The jclouds API for accessing H3 as a blobstore.


* Waiting to get Etags and partEtags from H3 team


* Have to check on list - filter when deleting a directory of incomplete uploads


* Had to manually merge bytes from data packets in getBlob (getting 100MB file was SIGSEGV-ing the JVM) so we get small chunks as long as we get JH3_CONTINUE as Status


####S3proxy:1.7.1:
 
 - It says we should compile it with JDK 7 and their tests can't run in JDK 7.(change_JDK:JDK 8)
 
 - S3proxy:1.7.1: JRE has to run with JRE 9+ to pass class mismatch with JH3.(change_JRE:JDK 9+)


####Jclouds:2.2.1: 

 - It uses animal-sniffer-plugin that can check signatures up to JDK 8 but our JH3 has JDK 9 as a target so we can’t compile them with the animal-sniffer-plugin    active. (maven-plugin: animal-sniffer-plugin:skipped)
 
 - It is meant to be installed on its own with JDK 7 explicitly.


* RSA for the s3proxy’s keystore needed for a self-signed certificate
 Generating 2,048 bit RSA key pair and self-signed certificate (SHA256withRSA) with a validity of 90 days for: CN=Evangelos Maliaroudakis, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=GR
Pass: CARVICS

### s3proxy.conf
```properties
s3proxy.secure-endpoint=https://0.0.0.0:8080
s3proxy.keystore-path=keystore.jks
s3proxy.keystore-password=CARVICS
s3proxy.authorization=aws-v2-or-v4

s3proxy.identity=test:tester
s3proxy.credential=testing
jclouds.provider=h3
jclouds.h3.basedir=file:///tmp/demo_h3via_jclouds
```
