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
	
	static int node_need_num = 10;
	static int SM_RM_Idx_cmpt = 0;
	
	public static Master MasterImpl = new Master();
	public static deploy deployImpl = new deploy();
	
	public static HashMap<String, String> map_between_UMx_Machine = new HashMap<String, String>();
	public static HashMap<String, List<String>> map_between_Key_UMx = new HashMap<String, List<String>>();
	public static HashMap<String, String> map_between_RMx_Machine = new HashMap<String, String>();

	public static HashMap<String, List<String>> map_Dest_and_Src = new HashMap<String, List<String>>();
	public static HashMap<String, List<String>> map_PharseShuffle_Machine_and_Key = new HashMap<String, List<String>>();
	
	public static List<String> machines = new ArrayList<String>();
	public static List<String> keys = new ArrayList<String>();
	
	public static void main(String[] paramArrayOfString) throws IOException {
		
		// Split big data into node_need_num segmentations 
		Master.split("input.bk.txt");
		
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
			
			if (outputIdexCmpt == 0) beginIndex = 0;
			else beginIndex = n_containor[n_cmpt/node_need_num*outputIdexCmpt]+1;

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
			
		    // System.out.println(write_data.toString());
			outputIdexCmpt++;
		}
	}
	
	public static void CopySlplittingMappint() throws NumberFormatException, IOException {
		
		List<String> list = new ArrayList<String>();
		
		for(int i = 0; i < node_need_num; i ++) {
			list.add("rm -rf /tmp/nali/; "
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
        
        // System.out.println(map_between_UMx_Machine.toString());
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
			System.out.println("SplitingMapping has finished!");
			// System.err.println("SplitingMapping has not finished yet!");
		}
	}
	
	public static void ShufflingReducing() throws IOException {
		
		System.out.println("ShufflingReducing starts...");

		int partitionIdx;
		String partition_node_name = null;
		
		List<String> copy_cmd = new ArrayList<String>();
		
		Map<String, List<String>> treeMap = new TreeMap<String, List<String>>(map_between_Key_UMx);
		
		int key_assign_cmpt = 0;

		for(Entry<String, List<String>> element : treeMap.entrySet()) {
			// partitionIdx = (element.getKey().hashCode() & Integer.MAX_VALUE) % element.getValue().size();	
			// partition_node_name = map_between_UMx_Machine.get("UM" + element.getValue().get(partitionIdx));
		
			partitionIdx = key_assign_cmpt++ % node_need_num;
			partition_node_name = machines.get(partitionIdx);
			// System.out.println("Key assign index: " + element.getKey() + ": " + partition_node_name);

        	List<String> MachinesRelateToTheKey_list = null;
        	if (map_PharseShuffle_Machine_and_Key.get(partition_node_name) != null)  {
        		MachinesRelateToTheKey_list =  new ArrayList<String>(map_PharseShuffle_Machine_and_Key.get(partition_node_name));
        		if (!MachinesRelateToTheKey_list.contains(element.getKey()))
        			MachinesRelateToTheKey_list.add(element.getKey());
        	} else {
        		MachinesRelateToTheKey_list =  Arrays.asList(element.getKey());
        	}
        	map_PharseShuffle_Machine_and_Key.put(partition_node_name, MachinesRelateToTheKey_list);
        	
			// System.out.println("For this key - " + element.getKey() + "\t - we use - " + partition_node_name + " to do ShufflingReducing");
			
			// COPY UMx TO @ partition_node_name
			for(String related_node: element.getValue()) {
	        	List<String> SrcMachine_list = null;
	        	if (map_Dest_and_Src.get(partition_node_name) != null)  {
	        		SrcMachine_list =  new ArrayList<String>(map_Dest_and_Src.get(partition_node_name));
	        		if (!SrcMachine_list.contains(related_node))
	        			SrcMachine_list.add(related_node);
	        	} else {
	        		SrcMachine_list =  Arrays.asList(related_node);
	        	}
	        	map_Dest_and_Src.put(partition_node_name, SrcMachine_list);
			}
			
			// System.out.println(map_Key_Dest.toString());

		}
	
		for(Entry<String, List<String>> element : map_Dest_and_Src.entrySet()) {
			
			copy_cmd.clear();
			copy_cmd.addAll(cmdSetting("ssh nali@" + element.getKey() + " mkdir -p /tmp/nali/maps/;"));
	
			for(String related_node: element.getValue()) 
				copy_cmd.addAll(cmdSetting("scp nali@" + map_between_UMx_Machine.get("UM" + related_node) + ":/tmp/nali/maps/UM" + related_node + ".txt nali@" +  element.getKey() + ":/tmp/nali/maps/;")); 
			
			// System.out.println(element.getKey()+" --- "+element.getValue());
			// System.out.println(copy_cmd.toString());
			processStarter(copy_cmd, "Copy /splits/UMx to /maps/ @Master", "Copy /splits/UMx to /maps/ @Master");
			
		}
		
		List<String> paraList = new ArrayList<String>();
		List<String> fileList = new ArrayList<String>();
		for(Entry<String, List<String>> element : map_PharseShuffle_Machine_and_Key.entrySet()) {
			
			partition_node_name = element.getKey();
			
			// Complete paraList
			for(String KeyWord: element.getValue()) {
				paraList.add(KeyWord);
			}
	
			for(String Key: element.getValue()){
				for(String UMx: map_between_Key_UMx.get(Key)) {
					if (!(fileList.contains(UMx))) {
						fileList.add(UMx);
					};
				}
			}
			
			List<String> sr_cmd = new ArrayList<String>();
			sr_cmd.clear();
			sr_cmd.addAll(cmdSetting("ssh nali@" + partition_node_name + " java -jar /tmp/nali/Slave.jar 1 " + partition_node_name));
			for (String para: paraList) {
				sr_cmd.addAll(cmdSetting(para));
			}
			sr_cmd.addAll(cmdSetting("SPLITS_SIGNAL"));
			for (String file: fileList) {
				sr_cmd.addAll(cmdSetting("UM"+file+".txt"));
			}
			
			// System.out.println("sr_cmd: " + sr_cmd.toString());
			processStarter(sr_cmd, "Shuffle/reduce @Slave ", "Shuffle/reduce  @Slave ");
			
			paraList.clear();			
			fileList.clear();
		}
		
		// Launch the Slave.jar on element.getKey(), i.e.: machine relate to the key

		
	}
		
	public static void merge() throws IOException {
		System.out.println("Final merge starts...");	
		
		// Final step: do the summary! 
		List<String> write_data = new ArrayList<String>();
		
		List<String> sum_cmd = new ArrayList<String>();
		
		for(Entry<String, List<String>> element : map_PharseShuffle_Machine_and_Key.entrySet()) {
			sum_cmd = cmdSetting("scp nali@" + element.getKey() 
				+ ":/tmp/nali/maps/RM" + element.getKey() + ".txt /tmp/nali");	
			// System.out.println("sum: "+sum_cmd);
			
			processStarter(sum_cmd, "FinCopy @Master", "FinCopy @Master");
			
			try {
				// System.out.println("path: " + Paths.get("/tmp/nali/RM"+element.getKey()+".txt"));
				
				byte[] row_data = Files.readAllBytes(Paths.get("/tmp/nali/RM"+element.getKey()+".txt"));
				String data = new String(row_data);		
				
				// System.out.println("data: " + data);
				
				for(String str: data.split("\n")) {
					write_data.add(str);
				}
			} catch (IOException e) {
				System.out.println(e);
			}
		}		
		
		try {
			Files.write(Paths.get("/tmp/nali/Fin.txt"), write_data, Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Final merge success! ");	
	}
	
	public static void processStarter(List<String> cmd, String ErrMessage, String NormMessage) throws IOException {
		ProcessBuilder process = new ProcessBuilder(cmd);
		Process p = process.start();
		
		InputStream ErrStream = p.getErrorStream();
		BufferedReader ErrReader = new BufferedReader(new InputStreamReader(ErrStream));

		String line;
		
		if ((line = ErrReader.readLine())!= null ) 
			System.err.println(ErrMessage + " - Error: " + line + "\n"); //+cmd);
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