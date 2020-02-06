# LIGHTest TPL Interpreter

![LIGHTest](https://www.lightest.eu/static/LIGHTestLogo.png)

### Disclaimer 

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND.
This software is the output of a research project. 
It was developed as proof-of-concept to explore, test & verify various components
created in the LIGHTest project. It can thus be used to show the concepts
of LIGHTest, and as a reference implementation. 

# LIGHTest

Lightweight Infrastructure for Global Heterogeneous Trust management in support of an open Ecosystem of Stakeholders and Trust schemes.

For more information please visit the LIGHTest website: https://www.lightest.eu/

# Documentation

See the [LIGHTest Deliverables](https://www.lightest.eu/downloads/pub_deliverables/index.html).

# Requirements

* Java 1.8
* Maven ~3.6.0

## Features

* Interpreting
* Syntax checking
* Lexical checking

## How to use it

* Look at the Junit tests for examples
 (src/test/java/TestGenerator.java)
* You can call the interpreter with:
```java
Interpreter m = new Interpreter(IAtvApiListenerImpl);
m.run(new String[]{pathToTpl, query, rootVariable})
 ```
You need to implement a ```IAtvApiListener``` first (IAtvApiListenerImpl).
Provide the path to you TPL file relative to your project's root in ```pathToTpl```.
And if your ```query``` is ```main(root).```, then ```root``` is your ```rootVariable```.

## Maven

You can use the ATV library as a Maven dependency
```XML
        <dependency>
            <groupId>eu.lightest</groupId>
            <artifactId>horn</artifactId>
            <version>1.x-SNAPSHOT</version>
        </dependency>
```

# Licence

* Apache License 2.0 (see [LICENSE](./LICENSE))
* © LIGHTest Consortium
