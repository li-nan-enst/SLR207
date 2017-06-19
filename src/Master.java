import java.io.*;
import java.util.*;
import java.util.concurrent.*;

// This program is based on the method given here: 
// https://stackoverflow.com/questions/14165517/processbuilder-forwarding-stdout-and-stderr-of-started-processes-without-blocki
// and this page:
// http://tutorials.jenkov.com/java-util-concurrent/blockingqueue.html

public class Master {

	public static void main(String args[]) throws IOException {
		
		BlockingQueue queue = new ArrayBlockingQueue(2);
		
        String [] command = {"java", "-jar", "/tmp/slave.jar"};
        ProcessBuilder pb = new ProcessBuilder(command);
        
	    Process p = pb.start();
	    
	    inheritIO(p.getInputStream(), System.out, queue);
	    inheritIO(p.getErrorStream(), System.err, queue);
	    
	    try {
			if (queue.poll(12, TimeUnit.SECONDS) == null) {
				System.out.println("timeout");
			};
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
	
	private static void inheritIO(final InputStream src, final PrintStream dest, final BlockingQueue queue) {
	    new Thread(
	    	new Runnable() {
		        public void run() {		     	
					
		            Scanner sc = new Scanner(src);
		            while (sc.hasNextLine()) {
		            	String h = sc.nextLine();
		                dest.println(h);
						try {
							queue.put(h);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
		            }
					
					try {
						queue.take();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}	
		            
		        }
	    	}
	    ).start();
	}
}
