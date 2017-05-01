import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

class monothread{

	public static void main(String args[]) throws IOException {	
		
		// read the infomation from the file "input.txt as an array into data[]"
		String data = null;
		try {
			Path path = Paths.get("defense.txt");
			byte[] row_data = Files.readAllBytes(path);
			data = new String(row_data);
			} catch (IOException e) {
				System.out.println(e);
		    }

		// put all the informations into a TreeMap structure named tmap
		TreeMap<String, Integer> tmap = new TreeMap<String, Integer>();
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
	    
	    /*
	    //result by default order: asc by key
	    // get a set of the entries for tmap and get an iterator
        Set<Entry<String, Integer>> set = tmap.entrySet();
        Iterator<Entry<String, Integer>> i = set.iterator();
        // print the result
        while(i.hasNext()) {
           Entry<String, Integer> me = i.next();
           System.out.print(me.getKey() + ": ");
           System.out.println(me.getValue());
        }
	    */
        
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
        List<String> write_data = new ArrayList<String>();
        for(Map.Entry<String,Integer> mapping:list){ 
            System.out.println(mapping.getKey()+":"+mapping.getValue()); 
            write_data.add(mapping.getKey()+":"+mapping.getValue());
        } 
        Files.write(Paths.get("output.txt"), write_data);
	}
}