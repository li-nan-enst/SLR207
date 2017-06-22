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
	
	static Slave MapImpl = new Slave();
	String data = null;
	TreeMap<String, Integer> tmap = new TreeMap<String, Integer>();
	
	public static void main(String[] args) throws Exception {
		
		MapImpl.read_file_and_split(args[0], args[1]);
		MapImpl.show_map_between_Key_UMx();

	}
	
	public void show_map_between_Key_UMx() {
		for (Entry<String, Integer> element : tmap.entrySet()) {
		    System.out.println(element.getKey());
		}
	}
	
	// read the information from the file "input.txt as an array into data[]"
	public void read_file_and_split(String input_file_name, String output_file_name){
		
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
