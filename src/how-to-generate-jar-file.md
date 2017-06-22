ref: https://stackoverflow.com/questions/10125639/how-to-create-a-jar-file-using-the-terminal

1.
javac HelloWorld.java

2.
in Manifest.txt: 
Main-Class: HelloWorld

3. 
jar cfm HelloWorld.jar Manifest.txt HelloWorld.class

4. 
java -jar HelloWorld.jar
