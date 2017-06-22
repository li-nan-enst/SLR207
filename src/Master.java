import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Master {
	
	public static Master MasterImpl = new Master();
	public static deploy deployImpl = new deploy();
	
	public static List<String> machines = new ArrayList<String>();
	
	public static void main(String[] paramArrayOfString) throws IOException {
		
		// copy S1, S2, S3 to /tmp/nali/splits/
		Master.copySlits(); 
		// every slave do the "mapping" 
		// give the list "UMx - machines" for knowing Sx - UMx (1 - 1)
		// show the key value
		// show the list "clés - UMx" ()
		
	}
	
	public static void copySlits() throws NumberFormatException, IOException {
		int node_need_num = 3;
		List<String> list = new ArrayList<String>();
		
		for(int i = 0; i < node_need_num; i ++) {
			list.add("mkdir -p /tmp/nali/splits/; cp -n /cal/homes/nali/workspace/P4/SLR207/S" + i + ".txt /tmp/nali/splits/");
		}
		
		machines = deployImpl.run(node_need_num, list);
        for(String machine : machines) {
            System.out.println("work on the machine: "+machine);
        }
	}

}