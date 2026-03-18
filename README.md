# jml-cloud-orders-serverless-service

# Seteo de variables en Git Bash:
export JAVA_HOME='C:\Program Files\Java\jdk25.0.2-zulu25.32.21'
export PATH="$JAVA_HOME/bin:$PATH"

export MAVEN_HOME='C:\Program Files\apache-maven-3.9.11'
export PATH="$MAVEN_HOME/bin:$PATH"

# Maven:
mvn clean install -DskipTests=true -U
mvn clean install -U

# Spring boot:
mvn spring-boot:run
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:1081"
