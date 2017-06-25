import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class Slave {
	
	static Slave SlaveImpl = new Slave();
	String data = null;
	TreeMap<String, Integer> tmap = new TreeMap<String, Integer>();
	
	public static void main(String[] args) throws Exception {
				
		if (args[0].equals("0")) {
			
			SlaveImpl.read_file_and_split_and_map(args[1], args[2]);
			SlaveImpl.show_map_between_Key_UMx();
			
		} else if (args[0].equals("1")) {
						
			List<String> args_list = new ArrayList<String>();
			
			for(int i=3; i<args.length; i++) {
				args_list.add(args[i]);
			}
						
			SlaveImpl.shuffle(args[1], args_list, Integer.parseInt(args[2]));
			SlaveImpl.reduce(args[1],  Integer.parseInt(args[2]));
		}		
	}
	
	public void reduce(String key, int InputNameIdx) {
		String data = null;
		
		try {
			byte[] row_data = Files.readAllBytes(Paths.get("/tmp/nali/maps/SM"+InputNameIdx+".txt"));
			data = new String(row_data);				
		} catch (IOException e) {
			System.out.println(e);
		}	
		
		String[] lines = data.split("\r\n|\r|\n");
		List<String> write_data = new ArrayList<String>();
		write_data.add(key + " " + Integer.toString(lines.length));
		
		try {
			Files.write(Paths.get("/tmp/nali/maps/RM"+InputNameIdx + ".txt"), write_data, Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public void shuffle(String key, List<String> args_list, int OutputNameIdx) {
		
		List<String> write_data = new ArrayList<String>();
		
		for(String input_file_name : args_list) {
			
			String data = null;
			
			try {
				byte[] row_data = Files.readAllBytes(Paths.get(input_file_name));
				data = new String(row_data);				
			} catch (IOException e) {
				System.out.println(e);
			}
			
			for (String retval: data.split("\n| ")) {
				if (key.compareTo(retval) == 0) {
					write_data.add(key + " 1");
				}
			}
		}
			    
	    try {
			Files.write(Paths.get("/tmp/nali/maps/SM"+OutputNameIdx+".txt"), write_data, Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		} 
	
	}

	public void show_map_between_Key_UMx() {
		for (Entry<String, Integer> element : tmap.entrySet()) {
		    System.out.println(element.getKey());
		}
	}
	
	// read the information from the file "input.txt as an array into data[]"
	public void read_file_and_split_and_map(String input_file_name, String output_file_name){
		
		try {
			byte[] row_data = Files.readAllBytes(Paths.get(input_file_name));
			data = new String(row_data);
		} catch (IOException e) {
			System.out.println(e);
		}	
		
		List<String> write_data = new ArrayList<String>();
		for (String retval: data.split("\n| |,|;|\'|\"|\\.|\\)|\\(|[.-]|:")) {
			// don not take blank line into consideration
		    if (Pattern.matches("^( )*", retval)) continue;
		    write_data.add(retval+" 1");
		    tmap.put(retval, 1);
		}
		 
		try {
			Files.write(Paths.get(output_file_name), write_data, Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
}
