CSCI572 HW3 assignment 

## Note
This is a step-by-step note about how this assignment was completed.

1. Create a new maven project using Intellij IDEA
2. add **hadoop-common** dependency in **pom.xml** file.


---

## Create a file


---

## Setup on Google Cloud

**Type the following commands everytime when starting the SSH shell:**

export PATH=${JAVA_HOME}/bin:${PATH}
export HADOOP_CLASSPATH=${JAVA_HOME}/lib/tools.jar

**Compile and create the jar file:**

hadoop com.sun.tools.javac.Main InvertedIndexJob.java
jar cf InvertedIndex.jar InvertedIndexJob*.class


hadoop com.sun.tools.javac.Main InvertedIndexBigrams.java
jar cf InvertedIndexBigrams.jar InvertedIndexBigrams*.class


**Copy the jar file into the master node:**
hadoop fs -copyFromLocal ./InvertedIndex.jar
hadoop fs -cp InvertedIndex.jar gs://dataproc-staging-us-west1-25488136709-sunxmi43/JAR

hadoop fs -copyFromLocal ./InvertedIndexBigrams.jar
hadoop fs -cp InvertedIndexBigrams.jar gs://dataproc-staging-us-west1-25488136709-sunxmi43/JAR


InvertedIndexJob

gs://dataproc-staging-us-west1-25488136709-sunxmi43/JAR/InvertedIndex.jar
gs://dataproc-staging-us-west1-25488136709-sunxmi43/fulldata
gs://dataproc-staging-us-west1-25488136709-sunxmi43/fulloutput

InvertedIndexBigrams

gs://dataproc-staging-us-west1-25488136709-sunxmi43/JAR/InvertedIndexBigrams.jar
gs://dataproc-staging-us-west1-25488136709-sunxmi43/devdata
gs://dataproc-staging-us-west1-25488136709-sunxmi43/devoutput
---

##Error occured


org.apache.hadoop.ipc.RemoteException(java.io.IOException): File /user/liupeiye1993/InvertedIndex.jar._COPYING_ could only be written to 0 of the 1 m
inReplication nodes. There are 0 datanode(s) running and 0 node(s) are excluded in this operation.
**Solution: make sure all nodes are already started**


/usr/lib/hadoop/libexec//hadoop-functions.sh: line 2365: HADOOP_COM.SUN.TOOLS.JAVAC.MAIN_USER: invalid variable nam
e
/usr/lib/hadoop/libexec//hadoop-functions.sh: line 2460: HADOOP_COM.SUN.TOOLS.JAVAC.MAIN_OPTS: invalid variable nam
e
Error: Could not find or load main class com.sun.tools.javac.Main
**Solution: make sure to type these two export commands before compiling java file**

/usr/lib/hadoop/libexec//hadoop-functions.sh: line 2365: HADOOP_COM.SUN.TOOLS.JAVAC.MAIN_USER: invalid variable name
/usr/lib/hadoop/libexec//hadoop-functions.sh: line 2460: HADOOP_COM.SUN.TOOLS.JAVAC.MAIN_OPTS: invalid variable name
Note: InvertedIndexJob.java uses or overrides a deprecated API.
Note: Recompile with -Xlint:deprecation for details.
**This is ok?**



hadoop fs -cp ./bigramoutput.txt gs://dataproc-staging-us-west1-25488136709-sunxmi43/devoutput/bigramoutput.txt

