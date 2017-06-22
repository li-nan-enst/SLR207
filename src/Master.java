import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;


public class Master {
	
	public static Master MasterImpl = new Master();
	public static deploy deployImpl = new deploy();
	
	public static HashMap<String, String> map_between_Unx_Machine = new HashMap<String, String>();
	public static HashMap<String, List<String>> map_between_Key_UMx = new HashMap<String, List<String>>();

	public static List<String> machines = new ArrayList<String>();
	public static List<String> keys = new ArrayList<String>();
	
	public static void main(String[] paramArrayOfString) throws IOException {
		
		// Copy S1, S2, S3 to /tmp/nali/splits/
		Master.copySlits(); 
		
		// Show the map "UMx - machines" for knowing Sx - UMx (1 - 1)
		Master.show_map_between_UMx_Machine();
		
		// Create the list "clés - UMx" ()
		Master.create_map_between_Key_UMx();
		
	}
	
	public static void copySlits() throws NumberFormatException, IOException {
		int node_need_num = 3;
		List<String> list = new ArrayList<String>();
		
		for(int i = 0; i < node_need_num; i ++) {
			list.add("mkdir -p /tmp/nali/splits/; cp /cal/homes/nali/workspace/P4/SLR207/S" + i + ".txt /tmp/nali/splits/;" 
					+ "cd /tmp/nali/splits/;"
					+ "cp /cal/homes/nali/workspace/P4/SLR207/src/Slave.jar /tmp/nali/;"
					+ "java -jar /tmp/nali/Slave.jar '/cal/homes/nali/workspace/P4/SLR207/S" + i + ".txt' " + " 'UM" + i + ".txt' ");
		}
		
		machines = deployImpl.run(node_need_num, list);
        for(String machine : machines) {
            System.out.println("work on the machine: "+machine);
			map_between_Unx_Machine.put("UM"+(machines.indexOf(machine)+1), machine);
        }
	}
	
	public static void show_map_between_UMx_Machine() {
		for (Entry<String, String> element : map_between_Unx_Machine.entrySet()) {
		    System.out.println(element.getKey() + " - " + element.getValue());
		}
	}

	public static void create_map_between_Key_UMx() {
		keys = deployImpl.get_keys();
        for(String key : keys) {
        	System.out.println("Keys are: " + key);
        }
	}
}