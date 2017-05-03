import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

class monothread{
	// read from the file into the string array data
	// put all the informations into a TreeMap structure named tmap
	String data = null;
	TreeMap<String, Integer> tmap = new TreeMap<String, Integer>();
	
	// read the infomation from the file "input.txt as an array into data[]"
	public void read_file(String file_name){
		try {
			byte[] row_data = Files.readAllBytes(Paths.get(file_name));
			data = new String(row_data);
			} catch (IOException e) {
				System.out.println(e);
		    }	
	}
	
	public void split(){
		// use the function string.split to split the string using the symbols [' ' \n , ; . ' " ( ) .- : \t]
		// if there is no this element (use the name as the key), we add to the tmap
		// if not, we update the the value (+1)
	    for (String retval: data.split("\n| |,|;|\'|\"|\\.|\\)|\\(|[.-]|:")) {
	    	// don not take blank line into consideration
	    	if (Pattern.matches("^( )*", retval)) continue;
	    	if (!tmap.containsKey(retval)){
	    		// test for the correctness
	    		// System.out.println("not containsKey: "+retval);
	    		tmap.put(retval, 1);	
	    	} else {
	    		// test for the correctness
	    		// System.out.println("containsKey: "+retval);
	    		int count = tmap.get(retval);
	    		tmap.put(retval, count + 1);	    	
	    	} 
	    }
	}
	
	public void print_result(String file_name){
		
		// result by user-defined order: desc by value and asc by key
		// define a list to store the all entries
        List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String,Integer>>(tmap.entrySet());
        
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
        		"ne", "Le", "La", "L", "Lui", "Les", "Leur", "eux",
        		"aux", "à", "au", "de", "↬", "a", "ce",
        		"en", "DE", "DES", "DU", "d", "du", "se", "Il", "Elle", "qu", "ET", "LA"));
        List<String> write_data = new ArrayList<String>();
        for(Map.Entry<String,Integer> mapping:list){ 
            System.out.println(mapping.getKey()+":"+mapping.getValue()); 
            // select top50
            if (counter < 50) {
            	// filter the pron. and conj.
            	if (!ignore_list.contains(mapping.getKey())) {
                	write_data.add(mapping.getKey()+":"+mapping.getValue());
                	counter++;
            	}
            }     
        } 
        
        try {
			Files.write(Paths.get(file_name + "_output.txt"), write_data);
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public static void main(String args[]) {	
		monothread MyMonothread = new monothread();
		// the input args[0] will be the name of source file
		MyMonothread.read_file(args[0]);
		MyMonothread.split();
		MyMonothread.print_result(args[0]);
	}
}