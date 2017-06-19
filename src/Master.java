import java.io.*;
import java.util.concurrent.*;
import java.util.Scanner;

public class Master
{
  public Master() {}
  
  public static void main(String[] paramArrayOfString) throws IOException
  {
	final ArrayBlockingQueue<String> InputQueue = new ArrayBlockingQueue<String>(1024);
	
	String[] cmd = { "java", "-jar", "/tmp/slave.jar" };
	ProcessBuilder process = new ProcessBuilder(cmd);
	
	Process p = process.start();
	
	/*
	final InputStream src = p.getInputStream();
	final PrintStream dest = System.out;
	
	Thread InputStreamThread = new Thread(
			new Runnable() {
		        public void run() {		     	
					
		            Scanner sc = new Scanner(src);
		            
		            while (sc.hasNextLine()) {
		            	String h = sc.nextLine();
		                dest.println(h);
						InputQueue.add(h);
		            }
		            
		            sc.close();
		        }
	    	});
	
	InputStreamThread.start();
	
	try
	{
	  if (InputQueue.poll(2, TimeUnit.SECONDS) == null) {
	    System.err.println("timeout(2s)");
	    InputStreamThread.stop();
	  }
	} catch (InterruptedException e) {
	  e.printStackTrace();
	}
	*/
	
	final ArrayBlockingQueue<String> ErrorQueue = new ArrayBlockingQueue<String>(1024);

	
	final InputStream src_err = p.getInputStream();
	final PrintStream dest_err = System.out;
	
	Thread ErrorStreamThread = new Thread(
			new Runnable() {
		        public void run() {		     	
					
		            Scanner sc_err = new Scanner(src_err);
		            
		            while (sc_err.hasNextLine()) {
		            	String h = sc_err.nextLine();
		            	dest_err.println(h);
		            	ErrorQueue.add(h);
		            }
		            
		            sc_err.close();		    
		        }
	    	});
	
	ErrorStreamThread.start();
	
	try
	{
	  if (ErrorQueue.poll(2, TimeUnit.SECONDS) == null) {
	    System.err.println("timeout(2s)");
	    p.destroy();
	  }
	} catch (InterruptedException e) {
	  e.printStackTrace();
	}
  }
}