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
	
	public static Master MasterImpl = new Master();
	public static deploy deployImpl = new deploy();
	
	public static HashMap<String, String> map_between_UMx_Machine = new HashMap<String, String>();
	public static HashMap<String, List<String>> map_between_Key_UMx = new HashMap<String, List<String>>();
	public static HashMap<String, String> map_between_RMx_Machine = new HashMap<String, String>();

	public static List<String> machines = new ArrayList<String>();
	public static List<String> keys = new ArrayList<String>();
	
	public static void main(String[] paramArrayOfString) throws IOException {
		
		// Copy S1, S2, S3 to /tmp/nali/splits/
		Master.CopySlplittingMappint(); 
		
		// Show the map "UMx - machines" for knowing Sx - UMx (1 - 1)
		Master.show_map_between_UMx_Machine();
		
		// Create the list "clés - UMx" ()
		Master.create_map_between_Key_UMx();
		
		// Wait the end of SplingMapping
		Master.SplingMappingEtat(node_need_num);
		
		// Launch the ShufflingReducing
		Master.ShufflingReducing();
	}
	
	public static void ShufflingReducing() throws IOException {
		
		int partitionIdx;
		String partition_node_name;
		
		int SM_RM_Idx_cmpt = 0;
		
		 for(Entry<String, List<String>> element : map_between_Key_UMx.entrySet()) {
				 
			partitionIdx = (element.getKey().hashCode() & Integer.MAX_VALUE) % element.getValue().size();
			
			partition_node_name = map_between_UMx_Machine.get("UM" + element.getValue().get(partitionIdx));
			
			System.out.println("For this key - " + element.getKey() + "\t - we use - " 
					+ partition_node_name + " to do ShufflingReducing");
			
			// COPY UMx TO @ partition_node_name
			List<String> copy_cmd = new ArrayList<String>();
			copy_cmd = cmdSetting("ssh nali@" + partition_node_name + " mkdir -p /tmp/nali/maps/;");
			for(String related_node: element.getValue()) {
				copy_cmd.addAll(cmdSetting("scp nali@" +  map_between_UMx_Machine.get("UM" + related_node) + ":/tmp/nali/maps/UM" + related_node + ".txt " 
							+ "nali@" +  partition_node_name + ":/tmp/nali/maps/;"));
			}
			
			ProcessBuilder process = new ProcessBuilder(copy_cmd);
			Process p = process.start();
			
			InputStream ErrStream = p.getErrorStream();
			BufferedReader ErrReader = new BufferedReader(new InputStreamReader(ErrStream));

			String line = null;
			
			if ((line = ErrReader.readLine())!= null ) 
				System.err.println("Copy /splits/UMx to /maps/  @Master - Error: " + line + "\n"+copy_cmd);
			else System.out.println("Copy /splits/UMx to /maps/  @Master - Success !: " + copy_cmd);
		 }
		
		 Map<String, List<String>> treeMap = new TreeMap<String, List<String>>(map_between_Key_UMx);
		 // RUN SlaveImpl.shuffle/reduce
		 for(Entry<String, List<String>> element : treeMap.entrySet()) {
						 
			partitionIdx = (element.getKey().hashCode() & Integer.MAX_VALUE) % element.getValue().size();
			partition_node_name = map_between_UMx_Machine.get("UM" + element.getValue().get(partitionIdx));
		
				
			System.out.println("Shuffle/reduce @ "+ partition_node_name +" starts...");
			
			List<String> sr_cmd = new ArrayList<String>();
			sr_cmd = cmdSetting("ssh nali@" + partition_node_name + " cd /tmp/nali/; java -jar Slave.jar 1 "
							+ element.getKey()+ " " + SM_RM_Idx_cmpt);
			
			map_between_RMx_Machine.put(Integer.toString(SM_RM_Idx_cmpt), partition_node_name);
			
			SM_RM_Idx_cmpt++;
			
			for(String related_node: element.getValue()) {
				sr_cmd.add(" /tmp/nali/maps/" + "UM" + related_node+ ".txt");
			}		
			
			ProcessBuilder sr_process = new ProcessBuilder(sr_cmd);
			Process sr_p = sr_process.start();
			
			InputStream sr_ErrStream = sr_p.getErrorStream();
			BufferedReader sr_ErrReader = new BufferedReader(new InputStreamReader(sr_ErrStream));

			String sr_line = null;
			
			if ((sr_line = sr_ErrReader.readLine())!= null ) 
				System.err.println("Shuffle/reduce @Slave " + partition_node_name + " - Error: " + sr_line + "\n"+sr_cmd);
			else System.out.println("Shuffle/reduce  @Slave " + partition_node_name + " - Success !: " + sr_cmd);
			
	     }
		 
		// Final step: do the summary! 
		List<String> write_data = new ArrayList<String>();
		
		for(int i=0; i<SM_RM_Idx_cmpt; i++) {
			
			List<String> sum_cmd = new ArrayList<String>();
			sum_cmd = cmdSetting("scp nali@" + map_between_RMx_Machine.get(Integer.toString(i)) 
							+ ":/tmp/nali/maps/RM" + i + ".txt .");	
			
			ProcessBuilder sum_process = new ProcessBuilder(sum_cmd);
			Process sum_p = sum_process.start();
			
			InputStream sum_ErrStream = sum_p.getErrorStream();
			BufferedReader sum_ErrReader = new BufferedReader(new InputStreamReader(sum_ErrStream));

			String sr_line = null;
			
			if ((sr_line = sum_ErrReader.readLine())!= null ) 
				System.err.println("FinCopy @Master - Error: " + sr_line + "\n"+sum_cmd);
			else System.out.println("FinCopy @Master - Success !: " + sum_cmd);
				
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
	}
	
	public static List<String> cmdSetting(String inputCmd) {
		List<String> cmd_list = new ArrayList<String>();
 		for (String cmd_retval: inputCmd.split(" ")) {
 			cmd_list.add(cmd_retval);
 		}
 		
 		return cmd_list;
	}
	
	public static void SplingMappingEtat(int node_need_num) {
		if (deployImpl.SMEtat(node_need_num)) System.out.println("SplitingMapping has finished!");
		else {
			System.err.println("SplitingMapping has not finished yet!");
		}
	}
	
	public static void CopySlplittingMappint() throws NumberFormatException, IOException {
		
		List<String> list = new ArrayList<String>();
		
		for(int i = 0; i < node_need_num; i ++) {
			list.add("rm -rf /tmp/nali/*;"
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
        
        for(Entry<String, List<String>> element : map_between_Key_UMx.entrySet()) {
        	System.out.println(element.getKey() + " - UM" + element.getValue());
        }
	}
}