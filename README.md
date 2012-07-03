# Scala Website Spider

## Overview

This provides a simple web spider written in Scala. Its primary purpose is to traverse a website looking for broken internal or external links,
which it will report at the end of processing. However, it has been designed so it can be extended and repurposed for related applications
as required. 

## Getting Started

### Eclipse Users

- Ensure you have the [Eclipse EGit](http://www.eclipse.org/egit/) and [Scala IDE](http://scala-ide.org/) plugins installed
- Copy the URL in the _Git Read-Only_ entry field at the top of this web page to the clipboard
- In Eclipse, execute _Window_ &rarr; _Show View_ &rarr; _Other..._ &rarr; _Git_ &rarr; _Git Repositories_ to make this view visible
- Activate the context menu (right-mouse button) on the _Git Repositories_ view and paste in the clipboard URL to start the EGit wizard
- Accept the default values in the subsequent _Source Git Repository_ and _Branch Selection_ dialogs
- In the final _Local Destination_ dialog, select an appropriate local directory using the _Browse_ button, then click _Finish_
- Activate the context menu on the newly imported project, then select _Run As_ &rarr; _Run Configurations..._ &rarr; _Maven Build_ &rarr; _New_
- Name the configuration **spider build** and set the goals as **clean verify scala:doc**, and in the _Refresh_ tab tick the resources checkbox 
- Run the new configuration to build the application; the project should auto-build from now on
- To run the application, similarly do context menu _Run As_ &rarr; _Run Configurations..._ &rarr; _Scala Application_ &rarr; _New_
- Name the new configuration **spider run** and set the main class as **web.satyagraha.spider.app.SpiderApp**; in the _Arguments_ tab,
add command line options (see below) and a URL for the web site to be scanned  

### Non-Eclipse Users

- Ensure you have [Git](http://git-scm.com/) and [Maven](http://maven.apache.org/) installed on your system
- Copy the URL in the _Git Read-Only_ entry field at the top of this web page to the clipboard
- Change working directory to an appropriate location for the checkout, then execute: `git clone url`
- Change working directory to the newly created _spider_ subdirectory
- Execute: `mvn clean verify scala:doc`

### Command Line Invocation

When successfully built, the executable may be invoked in a stand-alone way via the command:
`java -jar target/spider-1.0-SNAPSHOT-jar-with-dependencies.jar [options] url`

The available options are:

- `--cookies` _cookies_ - allows arbitrary cookies to be passed on the initial page GET, e.g. a session id captured via
[Firefox Live HTTP Headers](https://addons.mozilla.org/en-US/firefox/addon/live-http-headers/) 
- `--loglevel` _level_ - allows [SLF4J](http://www.slf4j.org/) logging level to be set, e.g. to _WARN_
- `--readers` _count_ - allows number of reader actors to be varied from default of 10 

## Implementation

### Codebase

The codebase uses the following key components:

- [Scala](http://www.scala-lang.org/) 2.9
- The [ScalaTest](http://www.scalatest.org/) testing framework
- The [Mockito](http://code.google.com/p/mockito/) mocking framwework for unit testing
- The [SubCut](https://github.com/dickwall/subcut) dependency injection framework
- The [Dispatch](http://dispatch.databinder.net/Dispatch.html) web framework
- The [Lift Actors](http://liftweb.net/) framework

### Principles of Operation

Essentially the application works by passing around _References_, which are simple objects encoding the relationship between a web page and a link
within that page: whenever we encounter a link, a _Reference_ is generated which may need to be followed. We have a number of types of actors 
who handle various roles in the processing, and the interactions between these actors is best shown in a diagram thus:

![diagram](raw/master/doc/actors.png)

The actors' responsibilities are as follows:

- _referenceActor_ - manages generation of _References_, which must be returned
- _targetActor_  - manages links previously seen
- _queuedActor_ - manages pool of _readerActors_
- _readerActor_ - reads and analyzes web page, returns itself to the _queuedActor_ on completion
- _successActor_ - handles good links
- _failureActor_ - handles bad links

### Scaladoc

The application's scaladoc will be found in `target/site/scaladocs` on completion of the Maven scaladoc action.

### Notes

- This application does not honour the [robots.txt](http://www.robotstxt.org/) convention for spiders, and thus can generate 
potentially a high load on a website by traversing all its pages. This is particularly true if you set a high value for the
`--readers` option. High loads can be unpopular and might lead to claims of Denial of Service or result in IP blocking, so
be warned. The workload generated could in principle be throttled by restricting the number of readers and/or introducing
a sleep period between HTTP requests.

- Websites providing open-ended dynamic content links, like calendars in particular, may well result in non-termination
of the application. Adding some kind of pattern match exclusion when determining whether links should be followed would
most likely be the solution here. 

- A wide variety of errors will be seen when running the application against typical commercial websites. This is the reality
of web content as seen in the wild! Naturally fixes to accommodate such anomalies are most welcome via the usual Github lifecycle,
with corresponding unit tests being advisable to validate the changes. 
