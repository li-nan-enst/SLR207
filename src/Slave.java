import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public class Slave {
	
	static Slave MapImpl = new Slave();
	String data = null;
	
	public static void main(String[] args) throws Exception {
		
		MapImpl.read_file_and_split(args[0]);

	}
	
	// read the information from the file "input.txt as an array into data[]"
	public void read_file_and_split(String file_name){
		try {
			byte[] row_data = Files.readAllBytes(Paths.get(file_name));
			data = new String(row_data);
			} catch (IOException e) {
				System.out.println(e);
		    }	
		 for (String retval: data.split("\n| |,|;|\'|\"|\\.|\\)|\\(|[.-]|:")) {
		    	// don not take blank line into consideration
		    	if (Pattern.matches("^( )*", retval)) continue;
		    	System.out.println(retval+" 1");	
		    }
	}
}
