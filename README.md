# Bigellor

Branch Status | Current Version |
---|---|
Master: [![pipeline status](/../badges/master/pipeline.svg)](/../pipelines) | N.A. |
Develop: [![pipeline status](/../badges/develop/pipeline.svg)](/../pipelines) | 0.0.1-SNAPSHOT |
-----

**Bigellor** is a web-based modelling tool to graphically create and visualize bigraphs.
It uses Spring in combination with Thymeleaf as web development framework, and Cytoscape.js for the visualization of 
bigraphs in the browser.

![Bigraph Diagram Editor](./etc/bigraph-editor.png "Bigraph Diagram Editor")

![Bigraph Diagram Editor](./etc/manage-signatures.png "Bigraph Diagram Editor")

## Getting Started

**Requirements:** Java 11

### Building from Source

The following command must be executed from the root directory of this project:
```bash
$ mvn clean package -DskipTests
```

After the command successfully finishes, the compiled application is located in the directory `./target/`.

### Starting Bigellor

> **Note:** Ensure that there is read/write permission on the directory, where Bigellor is started.

After the [above command](#Building-from-Source) successfully finished, you can start using **Bigellor**.

Therefore, execute the following command:
```bash
$ cd ./target/
$ java -jar bigellor-0.0.1-SNAPSHOT-exec.jar
```

Then, just open the web browser and navigate to `http://localhost:8080`.

The correct server name and port will be displayed in the console if defaults were changed.

#### Properties to Configure

Properties can be configured when starting **Bigellor** in the following way:
```bash
$ java -jar bigellor-0.0.1-SNAPSHOT-exec.jar --PROPERTY=VALUE --PROPERTY2=VALUE2 ...
```

|Properties|Description|
|----------|-----------|
|`bigellor.model.storage.location`| Relative path. The 'upload' directory or storage directory of Bigellor model files|
|`server.port`| Port number. The server port of Bigellor|

<!--
## Deploy a Web ARchive to a Web Server

Even though **Bigellor** runs a standalone webserver itself, it can be deployed as `*.war` to Tomcat or other 
webservers that have a web container (i.e., servlet container) component, for example, Jetty and WildFly.

Two options are available in this project:
- Automatic deploy to Apache Tomcat
- Manual deploy: Just generate the `*.war` and deploy it manually to the desired webserver

### Automatic Deployment to Tomcat

First, the configuration file has to be adjusted:
See https://www.baeldung.com/tomcat-deploy-war
```bash
$ mvn ..........
```

Second, the following command must be executed:
```bash
# Deploy the Bigellor application 
$ mvn tomcat7:deploy

# Undeploy the Bigellor application 
$ mvn tomcat7:undeploy

# Redeploy the Bigellor application after making changes
$ mvn tomcat7:redeploy
```

### Manual Deployment
-->
