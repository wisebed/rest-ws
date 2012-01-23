WISEBED REST/WebSocket API
======
This project provides an RESTful HTTP Web service and WebSocket API for WISEBED Testbeds.


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

Use in your Maven project
======

Add the following dependency to your pom.xml:
  
	<dependency>
		<groupId>eu.wisebed</groupId>
		<artifactId>rest-ws</artifactId>
		<version>1.0-SNAPSHOT</version>
	</dependency>
	
Add the following repository to your pom.xml:

```
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

```
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
