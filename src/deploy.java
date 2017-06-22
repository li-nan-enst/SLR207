import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
	// 1st para: node_index; 2nd para: MAX-length of cmd; 3rd para: type of cmd
	String[][][] cmd_data = new String[100][100][100]; 
	// the array for process of nodes
	Process p[] = new Process[100];
	// to record the cmd length for each node; 1st para: num of node; 2nd para: type of cmd 
	int CmdLengthRecord[][] = new int[100][100];
	// list of nodes who does not response in the limit time
	List<Integer> black_list = new ArrayList<Integer>();
	
	List<Integer> white_list = new ArrayList<Integer>();
	int valid_node_num = 0;
	
	List<String> node_list = new ArrayList<String>();
	
	List<String> info_list = new ArrayList<String>();
		
	boolean flag[] =new boolean[100];
	
	//public static void main(String args[]) throws IOException {
	public List<String> run(int node_need_num, List<String> args) throws NumberFormatException, IOException {
		MyDeploy.read_file("pc_name.txt");		
		MyDeploy.set_cmd(args);
		MyDeploy.execute(node_need_num, node_list, info_list);  
		
        System.out.println(info_list.toString());
        
		return node_list;
	}
	
	public List<String> get_inputStream() {
		return info_list;
	}
	
	public boolean SMEtat(){
		boolean etat = true;
		for(int i=0; i < node_num; i++) {
			etat = etat & flag[i];
		}
		return etat;
	}
	
	public void execute(int node_need_num, List<String> node_list, List<String> info_list) throws IOException {	
				
		// test whether the nodes could be connected
		for(int node_index = 0; node_index<node_num; node_index++) {
			ProcessBuilder process = new ProcessBuilder(cmd_data[node_index][0][0], cmd_data[node_index][1][0], cmd_data[node_index][2][0]);
			p[node_index] = process.start();
			
			if (get_response(node_index)) {
				if (valid_node_num < node_need_num) {
					white_list.add(node_index);
					valid_node_num++;
					if (valid_node_num == node_need_num) break;
				}
			}
			
		}
		
		// execute all the rest cmds
		int machine_order = 0;
		flag[machine_order] = false;
		for(int node_index = 0; node_index<node_num; node_index++) { 
			if (!black_list.contains(node_index) && white_list.contains(node_index)) {
				node_list.add(cmd_data[node_index][1][machine_order]);
				
				p[node_index].destroy();
							
				List<String> cmd_list = new ArrayList<String>();
				cmd_list.clear();
				for(int cmd_index = 0; cmd_index< CmdLengthRecord[node_index][machine_order]; cmd_index++) {
					cmd_list.add(cmd_data[node_index][cmd_index][machine_order]);
				}
				
				ProcessBuilder process = new ProcessBuilder(cmd_list);
				p[node_index] = process.start();
				
				InputStream ErrStream = p[node_index].getErrorStream();
				BufferedReader ErrReader = new BufferedReader(new InputStreamReader(ErrStream));
				InputStream InfoStream = p[node_index].getInputStream();
				BufferedReader InfoReader = new BufferedReader(new InputStreamReader(InfoStream));

				String line = null;
				
				// if there is no err info, it is successful, if not, we give the err tips
				if ((line = ErrReader.readLine())!= null ) 
					System.err.println("Error @ " + cmd_data[node_index][1][machine_order] + ": " + line + "\n"+cmd_list);
				else System.out.println("Task @ " + cmd_data[node_index][1][machine_order] + " success !: " + cmd_list);
				
				int cmpt = 0;
				while ((line = InfoReader.readLine())!= null) {
					if (cmpt == 0 ) cmpt++;
					else info_list.add(machine_order+":"+line);
				}
				
				flag[machine_order] = true;
				machine_order ++;
			}
			
		}
		
	}
	
	// response info for the test of connection
	public boolean get_response(final int index) {
		
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
		  if (InputQueue.poll(5, TimeUnit.SECONDS) == null) {
		    System.err.println("Link to " + cmd_data[index][1][0] + " timeout(5s).\nThis node will be given up.");
		    black_list.add(index);
		    p[index].destroy();
		  } else {
			  System.out.println("Link to " + cmd_data[index][1][0] + " success !");
			  return true;
		  }
		} catch (InterruptedException e) {
		  e.printStackTrace();
		}
		
		return false;
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
	public void set_cmd(List<String> inputCmds) {
		
		int inputCmd_cmpt = 0;
		
		for (String inputCmd: inputCmds) {
			node_num = 0;
			
			for (String pc_name_retval: pc_name_data.split("\n")) {
			
				int length_of_cmd = 0;
				
				String cmd = "ssh " + pc_name_retval + " hostname;";
				
				// ex inputCmd: "mkdir -p /tmp/nali/; cp -n /cal/homes/nali/workspace/P4/SLR207/src/Slave.jar /tmp/nali/"
				cmd += " " + inputCmd; 
				
				for (String cmd_retval: cmd.split(" ")) {
					cmd_data[node_num][length_of_cmd][inputCmd_cmpt] = cmd_retval;		
					length_of_cmd ++;	
				}
				
				CmdLengthRecord[node_num][inputCmd_cmpt] = length_of_cmd;
				node_num++;
			}
			
			inputCmd_cmpt ++;
		}

	}
}
