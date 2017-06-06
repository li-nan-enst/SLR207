import java.io.*;
import java.util.*;

public class MASTER {

	public static void main(String args[]) throws IOException {
		
		String [] command = {"java", "-jar", "/tmp/slave.jar"};
		
		ProcessBuilder pb = new ProcessBuilder(command);
		Process process = pb.start();
		
		// ref. : http://www.xyzws.com/javafaq/how-to-run-external-programs-by-using-java-processbuilder-class/189
		
        // Read out dir output
        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        
        String line;
        System.out.printf("Output of running %s is:\n", Arrays.toString(command));
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
        
        // Read error information
        InputStream iserr = process.getErrorStream();
        InputStreamReader iserrr = new InputStreamReader(iserr);
        BufferedReader brerr = new BufferedReader(iserrr);
        
        String lineerr;
        System.out.printf("Output of error of running %s is:\n", Arrays.toString(command));
        while ((lineerr = brerr.readLine()) != null) {
            System.out.println(lineerr);
        }       
        
        // Wait to get exit value
        try {
            int exitValue = process.waitFor();
            System.out.println("\n\nExit Value is " + exitValue);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
	}
	
}
