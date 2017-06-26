import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;


public class Master {
	
	static int node_need_num = 3;
	static int SM_RM_Idx_cmpt = 0;
	
	public static Master MasterImpl = new Master();
	public static deploy deployImpl = new deploy();
	
	public static HashMap<String, String> map_between_UMx_Machine = new HashMap<String, String>();
	public static HashMap<String, List<String>> map_between_Key_UMx = new HashMap<String, List<String>>();
	public static HashMap<String, String> map_between_RMx_Machine = new HashMap<String, String>();

	public static List<String> machines = new ArrayList<String>();
	public static List<String> keys = new ArrayList<String>();
	
	public static void main(String[] paramArrayOfString) throws IOException {
		
		// Split big data into node_need_num segmentations 
		Master.split("input.txt");
		
		// Copy S1, S2, S3 to /tmp/nali/splits/
		Master.CopySlplittingMappint(); 
		
		// Show the map "UMx - machines" for knowing Sx - UMx (1 - 1)
		// Master.show_map_between_UMx_Machine();
		
		// Create the list "clés - UMx" ()
		Master.create_map_between_Key_UMx();
		
		// Wait the end of SplingMapping
		Master.SplingMappingEtat(node_need_num);
		
		// Launch the ShufflingReducing
		Master.ShufflingReducing();
		
		// Summary
		Master.merge();
	}
	
	public static void split(String input_file_name) {
		
		String data = null;
		
		try {
			byte[] row_data = Files.readAllBytes(Paths.get(input_file_name));
			data = new String(row_data);				
		} catch (IOException e) {
			System.out.println(e);
		}
		
		int n_cmpt = 0;
		int[] n_containor = new int[data.length()];
		int last_index = -1;
		int current_index = 0;
				
		n_containor[0] = 0;
		while ( (current_index = data.indexOf("\n", last_index+1)) != -1) {
			n_cmpt++;
			n_containor[n_cmpt] = current_index;
			last_index = current_index;	
		}
				
		int beginIndex = 0;
		int endIndex = 0;
		int outputIdexCmpt = 0;
		while (outputIdexCmpt < node_need_num) {
			
			beginIndex = n_containor[n_cmpt/node_need_num*outputIdexCmpt];

			if (outputIdexCmpt != node_need_num-1) {
				endIndex = n_containor[n_cmpt/node_need_num*(outputIdexCmpt + 1)];
			}
			else endIndex = n_containor[n_cmpt];
						
			System.out.println("Split index: "+beginIndex+"---"+endIndex);

			String flit_data = data.substring(beginIndex, endIndex);
			
			List<String> write_data = new ArrayList<String>();
			write_data.add(flit_data);
		    try {
				Files.write(Paths.get("S"+outputIdexCmpt+".txt"), write_data, Charset.forName("UTF-8"));
			} catch (IOException e) {
				e.printStackTrace();
			} 
			
			outputIdexCmpt++;
		}
	}
	
	public static void CopySlplittingMappint() throws NumberFormatException, IOException {
		
		List<String> list = new ArrayList<String>();
		
		for(int i = 0; i < node_need_num; i ++) {
			list.add("rm -rf /tmp/nali/*; "
					+ "mkdir -p /tmp/nali/;" 
					+ "mkdir -p /tmp/nali/splits/; cp /cal/homes/nali/workspace/P4/SLR207/S" + i + ".txt /tmp/nali/splits/;" 
					+ "cp /cal/homes/nali/workspace/P4/SLR207/src/Slave.jar /tmp/nali/;"
					+ "mkdir -p /tmp/nali/maps/;"
					+ "java -jar /tmp/nali/Slave.jar 0 '/tmp/nali/splits/S" + i + ".txt' " + " '/tmp/nali/maps/UM" + i + ".txt' ");
		}
		
		machines = deployImpl.run(node_need_num, list);
        for(String machine : machines) {
            System.out.println("work on the machine: "+machine);
			map_between_UMx_Machine.put("UM"+(machines.indexOf(machine)), machine);
        }
	}
	
	public static void show_map_between_UMx_Machine() {
		for (Entry<String, String> element : map_between_UMx_Machine.entrySet()) {
		    System.out.println(element.getKey() + " - " + element.getValue());
		}
	}
	
	public static void create_map_between_Key_UMx() {
		keys = deployImpl.get_inputStream();
        for(String keyset : keys) {
        	
        	String UMx = keyset.split(":")[0]; 
        	String key = keyset.split(":")[1]; 
        	
        	List<String> UMx_list;
        	if (map_between_Key_UMx.get(key) != null)  {
        		UMx_list =  new ArrayList<String>(map_between_Key_UMx.get(key));
        		UMx_list.add(UMx);
        	} else {
        		UMx_list =  Arrays.asList(UMx);
        	}
        	
        	map_between_Key_UMx.put(key, UMx_list);
        }
        
    /*  
     * for(Entry<String, List<String>> element : map_between_Key_UMx.entrySet()) {
        	System.out.println(element.getKey() + " - UM" + element.getValue());
        }
     */
	}
	
	public static void SplingMappingEtat(int node_need_num) {
		if (deployImpl.SMEtat(node_need_num)) System.out.println("SplitingMapping has finished!");
		else {
			System.err.println("SplitingMapping has not finished yet!");
		}
	}
	
	public static void ShufflingReducing() throws IOException {
		
		System.out.println("ShufflingReducing starts...");

		int partitionIdx;
		String partition_node_name;
		
		List<String> copy_cmd = new ArrayList<String>();

		for(Entry<String, List<String>> element : map_between_Key_UMx.entrySet()) {
				 
			partitionIdx = (element.getKey().hashCode() & Integer.MAX_VALUE) % element.getValue().size();
			
			partition_node_name = map_between_UMx_Machine.get("UM" + element.getValue().get(partitionIdx));
		
			// System.out.println("For this key - " + element.getKey() + "\t - we use - " + partition_node_name + " to do ShufflingReducing");
			
			// COPY UMx TO @ partition_node_name
			copy_cmd.addAll(cmdSetting("ssh nali@" + partition_node_name + " mkdir -p /tmp/nali/maps/;"));
			for(String related_node: element.getValue()) {
				copy_cmd.addAll(cmdSetting("scp nali@" +  map_between_UMx_Machine.get("UM" + related_node) + ":/tmp/nali/maps/UM" + related_node + ".txt " 
						+ "nali@" +  partition_node_name + ":/tmp/nali/maps/;"));
			}

		}

		// System.out.println(copy_cmd.toString());
		processStarter(copy_cmd, "Copy /splits/UMx to /maps/  @Master", "Copy /splits/UMx to /maps/  @Master");
		
		Map<String, List<String>> treeMap = new TreeMap<String, List<String>>(map_between_Key_UMx);
		
		// RUN SlaveImpl.shuffle/reduce
		List<String> sr_cmd = new ArrayList<String>();
		for(Entry<String, List<String>> element : treeMap.entrySet()) {
						 
			partitionIdx = (element.getKey().hashCode() & Integer.MAX_VALUE) % element.getValue().size();
			partition_node_name = map_between_UMx_Machine.get("UM" + element.getValue().get(partitionIdx));
			
							
			sr_cmd.addAll(cmdSetting("ssh nali@" + partition_node_name + " java -jar /tmp/nali/Slave.jar 1 "
							+ element.getKey()+ " " + SM_RM_Idx_cmpt));
			
			map_between_RMx_Machine.put(Integer.toString(SM_RM_Idx_cmpt), partition_node_name);
			
			SM_RM_Idx_cmpt++;
			
			String tmp_data = "";
			for(String related_node: element.getValue()) {
				tmp_data += "/tmp/nali/maps/" + "UM" + related_node+ ".txt ";
			}		
			sr_cmd.addAll(cmdSetting(tmp_data.substring(0, tmp_data.length()-1)+";"));
			
			// System.out.println("Shuffle/reduce @ "+ partition_node_name +" starts... "+sr_cmd.toString());
	    }
		
		processStarter(sr_cmd, "Shuffle/reduce @Slave ", "Shuffle/reduce  @Slave ");
		
		System.out.println("ShufflingReducing finish! ");
	}
	
	public static void merge() throws IOException {
		System.out.println("Final merge starts...");	
		
		// Final step: do the summary! 
		List<String> write_data = new ArrayList<String>();
		
		List<String> sum_cmd = new ArrayList<String>();
		for(int i=0; i<SM_RM_Idx_cmpt; i++) {
			// System.out.println("i: " +i);
			
			sum_cmd = cmdSetting("scp nali@" + map_between_RMx_Machine.get(Integer.toString(i)) 
							+ ":/tmp/nali/maps/RM" + i + ".txt .");	
			
			// System.out.println(sum_cmd);
			
			try {
				byte[] row_data = Files.readAllBytes(Paths.get("RM"+i+".txt"));
				String data = new String(row_data);		
				write_data.add((data.split("\r|\n|\t"))[0]);
			} catch (IOException e) {
				System.out.println(e);
			}	
			
			try {
				Files.write(Paths.get("Fin.txt"), write_data, Charset.forName("UTF-8"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		processStarter(sum_cmd, "FinCopy @Master", "FinCopy @Master");

		System.out.println("Final merge success! ");	
	}
	
	public static void processStarter(List<String> cmd, String ErrMessage, String NormMessage) throws IOException {
		ProcessBuilder process = new ProcessBuilder(cmd);
		Process p = process.start();
		
		InputStream ErrStream = p.getErrorStream();
		BufferedReader ErrReader = new BufferedReader(new InputStreamReader(ErrStream));

		String line;
		
		if ((line = ErrReader.readLine())!= null ) 
			System.err.println(ErrMessage + " - Error: " + line + "\n"); // +cmd);
		// else System.out.println(NormMessage + " - Success !: " + cmd);

	}
	
	public static List<String> cmdSetting(String inputCmd) {
		List<String> cmd_list = new ArrayList<String>();
 		for (String cmd_retval: inputCmd.split(" ")) {
 			cmd_list.add(cmd_retval);
 		}
 		
 		return cmd_list;
	}
	


}