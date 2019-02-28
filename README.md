
# Simple Search Application

## Requirements

- Java 8
- sbt (Scala Build Tool) : [Installation instructions] (https://docs.scala-lang.org/getting-started-sbt-track/getting-started-with-scala-and-sbt-on-the-command-line.html)
- NOTE: sbt installation is a nice-to-have for running tests etc (you can use fat jar instead with standard java if you don't want to install it)


## Usage

Using sbt

    sbt "run --help"
    e.g. sbt "run --search-terms=114  --field-name=_id --doc-type=ORGANIZATION"

Using pre-built fat jar
    
    java -jar simple-search-fat.jar --help
    e.g. java -jar simple-search-fat.jar --search-terms=114  --field-name=_id --doc-type=ORGANIZATION

## Build, Test & Package

    sbt test it:test assembly
    
## Notes
- For search, field Names are case sensitive, search terms are not.
- Debug Logging is on by default (in case things go pear shaped).
