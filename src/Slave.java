import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
					
			List<String> key_list = new ArrayList<String>();
			int i = 2;
			while (!args[i].equals("SPLITS_SIGNAL")) {
				key_list.add(args[i]);
				System.out.println(args[i]);
				i++;
			}
			
			List<String> args_list = new ArrayList<String>();
			
			for(int j=i+1; j<args.length; j++) {
				args_list.add(args[j]);
				System.out.println(args[j]);
			}	
			
			for(String key: key_list) {
				SlaveImpl.shuffle(key, args_list, args[1]);
			}
			
			SlaveImpl.reduce(args[1]);
		}		
	}
	
	public void reduce(String InputNameIdx) {
		String data = null;
		
		String tmp = null;
		int cmpt = 0;
		
		List<String> write_data = new ArrayList<String>();

		try {
			byte[] row_data = Files.readAllBytes(Paths.get("/tmp/nali/maps/SM"+InputNameIdx+".txt"));
			data = new String(row_data);
			String one = data.split("\n| ")[1];
			for (String str: data.split("\n| ")) {
				
				System.out.println("str: " +str + ", tmp:" +tmp + ", cmpt: " + cmpt+ ", one.equals(str): " + one.equals(str));
				
				if (!one.equals(str)) {
					if (!str.equals(tmp)) {
						if (tmp != null) {
							write_data.add(tmp+" "+cmpt);
							cmpt = 0;
						} 
						tmp = str;
					} 
				} else cmpt++;
			}
			write_data.add(tmp+" "+cmpt);
		} catch (IOException e) {
			System.out.println(e);
		}	
		
		// System.out.println(write_data);
		try {
			Files.write(Paths.get("/tmp/nali/maps/RM"+InputNameIdx + ".txt"), write_data, Charset.forName("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public void shuffle(String key, List<String> args_list, String OutputNameIdx) {
		
		List<String> write_data = new ArrayList<String>();
		
		for(String input_file_name : args_list) {
			
			String data = null;
			
			try {
				byte[] row_data = Files.readAllBytes(Paths.get("/tmp/nali/maps/" + input_file_name));
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
			Files.write(Paths.get("/tmp/nali/maps/SM"+OutputNameIdx+".txt"), write_data, Charset.forName("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
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
			Files.write(Paths.get(output_file_name), write_data, Charset.forName("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
}
