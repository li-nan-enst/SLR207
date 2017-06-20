import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class deploy {
	
	public static deploy MyDeploy = new deploy();
	
	// to read all node name at one string
	String pc_name_data = null;
	// recode node number
	int node_num = 0;
	// 1st para: node_index; 2nd para: MAX-length of cmd
	String[][] cmd_data = new String[100][100]; 
	// the array for process of nodes
	Process p[] = new Process[100];
	// to record the cmd length for each node
	int CmdLengthRecord[] = new int[100];
	
	public static void main(String args[]) throws IOException {
		
		MyDeploy.read_file("pc_name.txt");		
		MyDeploy.set_cmd();
		MyDeploy.execute();
		
	}
	
	public void execute() throws IOException {	
		
		// test whether the nodes could be connected
		for(int node_index = 0; node_index<node_num; node_index++) {
			ProcessBuilder process = new ProcessBuilder(cmd_data[node_index][0], cmd_data[node_index][1], cmd_data[node_index][2]);
			p[node_index] = process.start();
			get_response(node_index);
		}
		
		// execute all the rest cmds
		for(int node_index = 0; node_index<node_num; node_index++) {
			p[node_index].destroy();
						
			List<String> cmd_list = new ArrayList<String>();
			cmd_list.clear();
			for(int cmd_index = 0; cmd_index< CmdLengthRecord[node_index]; cmd_index++) {
				cmd_list.add(cmd_data[node_index][cmd_index]);
			}
			
			ProcessBuilder process = new ProcessBuilder(cmd_list);
			p[node_index] = process.start();
			
			InputStream ErrStream = p[node_index].getErrorStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(ErrStream));
			String line = null;
			
			// if there is no err info, it is successful, if not, we give the err tips
			if ((line = reader.readLine())!= null ) System.err.println(line + "\n"+cmd_list);
			else System.out.println("Task @ " + cmd_data[node_index][1] + " success !: " + cmd_list);
		}
	}
	
	// response info for the test of connection
	public void get_response(final int index) {
		
		final ArrayBlockingQueue<String> InputQueue = new ArrayBlockingQueue<String>(1024);
		
		Thread InputStreamThread = new Thread(
				new Runnable() {
			        public void run() {		     	
						
			            Scanner sc = new Scanner(p[index].getInputStream());
			            
			            while (sc.hasNextLine()) {
			            	String h = sc.nextLine();
			                InputQueue.add(h);
			            }
			            
			            sc.close();
			        }
		    	});
		
		InputStreamThread.start();
		
		// limit the response time in 10s
		try
		{
		  if (InputQueue.poll(10, TimeUnit.SECONDS) == null) {
		    System.err.println("Link to " + cmd_data[index][1] + "timeout(10s).\nThis node will be given up.");
		    p[index].destroy();
		  } else {
			System.out.println("Link to " + cmd_data[index][1] + " success !");
		  }
		} catch (InterruptedException e) {
		  e.printStackTrace();
		}
	}
	
	// read node names
	public void read_file(String file_name){
		try {
			byte[] row_data = Files.readAllBytes(Paths.get(file_name));
			pc_name_data = new String(row_data);
			} catch (IOException e) {
				System.out.println(e);
		    }	
	}
	
	// set the cmd
	public void set_cmd() {
		
		for (String pc_name_retval: pc_name_data.split("\n")) {
			
			int length_of_cmd = 0;
			String cmd = "ssh " + pc_name_retval + " hostname;";
			cmd += " mkdir -p /tmp/nali/;"; 
			cmd += " cp -n /cal/homes/nali/workspace/P4/SLR207/src/Slave.jar /tmp/nali/";
						
			for (String cmd_retval: cmd.split(" ")) {
				cmd_data[node_num][length_of_cmd] = cmd_retval;
				length_of_cmd ++;	
			}
			
			CmdLengthRecord[node_num] = length_of_cmd;
			node_num++;
			
		}
	}
}
