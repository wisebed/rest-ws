WISEBED REST/WebSocket API
======
This project provides an RESTful HTTP Web service and WebSocket API for WISEBED Testbeds.

Clone
======

To clone the [wisegui submodule](https://github.com/wisebed/wisegui/) as well, use `--recursive`:

```
git clone --recursive git@github.com:wisebed/rest-ws.git
```

Building 
======
No installation is required. To build, you need 
Java 6 or higher and [Maven 2](http://maven.apache.org/) or higher. 

Before cloning this repository, be sure to enable automatic conversion 
of CRLF/LF on your machine using ```git config --global core.autocrlf input```. 
For more information, please refer to [this article](http://help.github.com/dealing-with-lineendings/).

Clone the repository using ```git clone git://github.com/wisebed/rest-ws.git```.
To build, run ```mvn install```, this will build the program and place the 
generated jar file in target/ and in your local Maven repository.

Running
======
The project uses the Java endorsed standards override mechanism to use newer versions of libraries included in the JVM.
If you run the project in an IDE or from command line make sure to add

```
-Djava.endorsed.dirs=target/endorsed
```

as a VM parameter after building the project at least once using maven.

Use in your Maven project
======

Add the following dependency to your pom.xml:

```XML
<dependency>
	<groupId>eu.wisebed</groupId>
	<artifactId>rest-ws</artifactId>
	<version>1.0-SNAPSHOT</version>
</dependency>
```
	
Add the following repository to your pom.xml:

```XML
<repositories>
	...
	<repository>
		<id>wisebed-maven-repository-releases</id>
		<url>http://wisebed.eu/maven/releases/</url>
		<releases>
			<enabled>true</enabled>
		</releases>
		<snapshots>
			<enabled>false</enabled>
		</snapshots>
	</repository>
	...
</repositories>
```
  
If you also want to work with SNAPSHOT dependencies, also add:

```XML
<repositories>
	...
	<repository>
		<id>wisebed-maven-repository-snapshots</id>
		<url>http://wisebed.eu/maven/snapshots/</url>
		<releases>
			<enabled>false</enabled>
		</releases>
		<snapshots>
			<enabled>true</enabled>
		</snapshots>
	</repository>
	...
</repositories>
```

Contact
======
Any feedback will be greatly appreciated, at the
[rest-ws GitHub project page](https://github.com/wisebed/rest-ws)
or by contacting [danbim](mailto:bimschas@itm.uni-luebeck.de) or
[pfisterer](mailto:pfisterer@itm.uni-luebeck.de).
