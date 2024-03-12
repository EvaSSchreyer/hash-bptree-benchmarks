Here you can find the Benchmarktests used in my Bachelor Thesis.

They use a benchmarking library called JMH (https://github.com/openjdk/jmh).


In order to execute this project you first need to follow the instructions provided by https://github.com/openjdk/jmh and set up a benchmarking project.
After setting up the project you can copy the java files from this directory into your benchmarking project and execute them.

If you are using IntelliJ, execute the following steps:
"File" -> "New" -> "Project..." -> "Maven Archetype" then select "Maven Central" as the "Catalog" and "org.openjdk.jmh:jmh-java-benchmark-archetype" as the "Archetype"
Crate the new Project and copy all Java files into the project. Now you can execute the "MyBenchmark.java" file.
