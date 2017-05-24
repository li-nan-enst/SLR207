import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.concurrent.*;

class multithread{
	
	public final static int THREAD_POOL_SIZE = 100;
	public static multithread MyMultithread = new multithread();

	// read from the file into the string array data
	// put all the informations into a TreeMap structure named chm
	String data = null;
	ConcurrentHashMap<String, Integer> chm = new ConcurrentHashMap<String, Integer>();
	
	// read the information from the file "input.txt as an array into data[]"
	public void read_file(String file_name){
		try {
			byte[] row_data = Files.readAllBytes(Paths.get(file_name));
			data = new String(row_data);
			} catch (IOException e) {
				System.out.println(e);
		    }	
	}
	
	public void split(int index){
		
		int beginIndex = 0;
		int endIndex = 0;
		String tmp_data = null;
		
		//System.out.println(data);
		System.out.println(index);
		
		beginIndex = data.length()/THREAD_POOL_SIZE*index;
		
		if (index != THREAD_POOL_SIZE) endIndex = data.length()/THREAD_POOL_SIZE*(index+1);
		else endIndex = data.length();
		
		//ConcurrentHashMap<String, Integer> tmp_chm = new ConcurrentHashMap<String, Integer>();
		
		tmp_data = data.substring(beginIndex, endIndex);
		// System.out.println(data.length());
		// System.out.println(tmp_data.length());
		
		// use the function string.split to split the string using the symbols [' ' \n , ; . ' " ( ) .- : \t]
		// if there is no this element (use the name as the key), we add to the tmp_chm
		// if not, we update the the value (+1)
	    for (String retval: tmp_data.split("\n| |,|;|\'|\"|\\.|\\)|\\(|[.-]|:")) {
	    	// don not take blank line into consideration
	    	if (Pattern.matches("^( )*", retval)) continue;
	    	if (!chm.containsKey(retval)){
	    		// test for the correctness
	    		// System.out.println("not containsKey: "+retval);
	    		chm.put(retval, 1);	
	    	} else {
	    		// test for the correctness
	    		// System.out.println("containsKey: "+retval);
	    		int count = chm.get(retval);
	    		chm.put(retval, count + 1);	    	
	    	} 
	    }
	}
	
	public void print_result(String file_name){
		
		// result by user-defined order: desc by value and asc by key
		// define a list to store the all entries
        List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String,Integer>>(chm.entrySet());
        
        // redefine the sort method (asc by key)
        Collections.sort(list,new Comparator<Map.Entry<String,Integer>>() {

			@Override
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				return o1.getKey().compareTo(o2.getKey());
			}
        });
        // redefine the sort method (desc by value)
        Collections.sort(list,new Comparator<Map.Entry<String,Integer>>() {

			@Override
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}
        });
        // print the result
        int counter = 0;
        List<String> ignore_list = new ArrayList<String>(Arrays.asList("je", "tu", "il", "elle", 
        		"nous", "vous", "ils", "elles", "le", "la", "l", "lui", "les", "leur", "eux", "des",
        		"celui", "celle", "celui-ci", "celui-là", "celle-ci", "celle-là", "ceci", "cela", "ça",
        		"ceux", "ceux-ci", "ceux-là", "celles-ci", "celles-là",
        		"mien", "tien", "sien", "nôtre", "vôtre", "mienne", "tienne", "sienne", 
        		"miens", "tiens", "siens", "nôtres", "vôtres", "leurs", "miennes", "tiennes", "siennes", 
        		"on", "personne", "rien", "aucun", "aucune", "nul", "nule", "un", "une", "autre", "ni", "pas", "tout", "quelqu", "quelque",
        		"certains", "certaines", "plusieurs", "tous", "autres",
        		"qui", "que", "quoi", "dont", "où",
        		"lequel", "laquelle", "duquel", "auquel",
        		"lesquels", "desquels", "auxquels", "lesquelles", "desquelles", "auxquelles",
        		"mais", "ou", "et", "donc", "or", "ni", "car",
        		"ne", "eux", "aux", "à", "au", "de", "↬", "a", "ce",
        		"en", "des", "du", "d", "se", "qu",
        		"est", "sont", "pour", "dans", "son", "par", "avec", "sur", "ces", "cette", "être", "après"));
        List<String> write_data = new ArrayList<String>();
        for(Map.Entry<String,Integer> mapping:list){ 
            // System.out.println(mapping.getKey()+":"+mapping.getValue()); 
            // select top50
            if (counter < 50) {
            	// filter the pron. and conj.
            	if (!ignore_list.contains(mapping.getKey().toLowerCase())) {
                	write_data.add(mapping.getKey()+":"+mapping.getValue());
                	counter++;
            	}
            }     
        } 
        
        try {
			Files.write(Paths.get(file_name + "_output.txt"), write_data, Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public static class MyRunnable implements Runnable {
		private int index;
		
		MyRunnable(int index) {
			this.index = index;
		}
 
		@Override
		public void run() {
 
			try {
				MyMultithread.split(index);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public static void main(String args[]) {	
		long startTime = System.currentTimeMillis();
		
		// the input args[0] will be the name of source file
		MyMultithread.read_file(args[0]);
		long timePoint1 = System.currentTimeMillis();
		System.out.print("Time for reading the file(s):");
		System.out.println((timePoint1 - startTime)/1000.);

		// assign the tasks
		ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
		for(int i = 0; i<THREAD_POOL_SIZE; i++) {
			Runnable worker = new MyRunnable(i);
			executor.execute(worker);
		}
		executor.shutdown();
		
		try {
			while (!executor.awaitTermination(10, TimeUnit.MICROSECONDS));  
		}
        catch (InterruptedException e)  
        {  
            e.printStackTrace();  
        }  
		
		long timePoint2 = System.currentTimeMillis();
		System.out.print("Time for spliting and counting(s):");
		System.out.println((timePoint2 - timePoint1)/1000.);
		
		MyMultithread.print_result(args[0]);
		long endTime   = System.currentTimeMillis();
		System.out.print("Time for ordering and printing(s):");
		System.out.println((endTime - timePoint2)/1000.);
		
		System.out.print("Total time(s):");
		System.out.println((endTime - startTime)/1000.);
	}
}