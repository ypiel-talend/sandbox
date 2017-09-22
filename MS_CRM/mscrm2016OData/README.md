A maven plugin allows to generate an executable jar with all dependencies.
So execute: mvn clean compile assembly:single
Then run: java -jar target/OlingoSampleClient-jar-with-dependencies.jar

(https://stackoverflow.com/questions/574594/how-can-i-create-an-executable-jar-with-dependencies-using-maven)