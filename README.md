
# Simple Search Application

## Requirements

- Java 8
- sbt (Build Tool) : [Installation instructions] (https://docs.scala-lang.org/getting-started-sbt-track/getting-started-with-scala-and-sbt-on-the-command-line.html)
- NOTE: sbt installation is a nice-to-have for running tests etc (you can use fat jar instead with standard java if you don't want to install it)


## Usage

Using sbt

    sbt "run [path to json files - defaults to src/main/resources]"    

Using pre-built fat jar
    
    java -jar simple-search-fat.jar [path to json files - defaults to src/main/resources]

## Build, Test & Package

    sbt [clean] test it:test assembly
    
## Notes
- For search, field Names are case sensitive, search terms are not.
- Debug Logging is on by default (in case things go pear shaped).
- Search will ignore common stopwords. e.g. in, the.

