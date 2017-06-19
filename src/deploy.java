import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ArrayBlockingQueue;

public class deploy {
	
	public static deploy MyDeploy = new deploy();
	
	String pc_name_data = null;
	String[][] cmd_data = new String[100][10];
	
	int cmpt = 0;
	
	public static void main(String args[]) throws IOException {
		
		MyDeploy.read_file("pc_name.txt");		
		MyDeploy.set_cmd();
		MyDeploy.execute();
		
	}
	
	public void execute() throws IOException {
		Process p[] = new Process[100];
		for(int i = 0; i<cmpt; i++) {
			ProcessBuilder process = new ProcessBuilder(cmd_data[i][0], cmd_data[i][1], "; hostname");
			p[i] = process.start();
			InputStream inputStream = p[i].getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String line = null;
			System.out.println(line = reader.readLine());
		}
	}
	
	public void read_file(String file_name){
		try {
			byte[] row_data = Files.readAllBytes(Paths.get(file_name));
			pc_name_data = new String(row_data);
			} catch (IOException e) {
				System.out.println(e);
		    }	
	}
	
	public void set_cmd() {
		for (String retval: pc_name_data.split("\n")) {
			cmd_data[cmpt][0] = "ssh";
			cmd_data[cmpt][1] = retval;
			cmpt++;
		}
	}
}
