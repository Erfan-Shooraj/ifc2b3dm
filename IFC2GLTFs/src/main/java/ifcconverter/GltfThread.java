package ifcconverter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class GltfThread extends Thread {
	ProcessBuilder gltf_builder;
	Process gltf_p;
	BufferedWriter gltf_p_stdin;
	int fobjstart;
	int fobjend;
	File[] ifcs;
	GltfThread(File ifcs[], int start, int end) {
		this.ifcs = ifcs;
		this.fobjstart = start;
		this.fobjend = end;
		// init shell
		
		gltf_builder = new ProcessBuilder( "cmd.exe" );
		gltf_p=null;
		
        try {
			gltf_p = gltf_builder.start();
			gltf_p_stdin = 
	  		          new BufferedWriter(new OutputStreamWriter(gltf_p.getOutputStream()));
	        gltf_p_stdin.write("cd " + Main.OBJ2GLTFPATH);
	        gltf_p_stdin.newLine();
		    gltf_p_stdin.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void run() {
		/*
		System.out.println(fobjstart);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		try {
			for (int i=fobjstart; i<fobjend;i++) {
	        	System.out.println(ifcs[i].getName().split(".ifc")[0]);
	        	if (!((new File (Main.GLTFS_PATH + ifcs[i].getName().split(".ifc")[0] + ".gltf")).exists())) {
		        	gltf_p_stdin.write(Main.NODE_PATH + " bin\\\\obj2gltf.js -i \"" + Main.OBJS_PATH + ifcs[i].getName().split(".ifc")[0] +".obj\" -o \"" + Main.GLTFS_PATH + ifcs[i].getName().split(".ifc")[0] + ".gltf\" --materialsCommon");
		        	// --materialsCommon flag for compatibility with Cesium
	            	gltf_p_stdin.newLine();
		            gltf_p_stdin.flush();
	        	}
	        }
			// finally close the shell by execution exit command
	 	    gltf_p_stdin.write("exit");
	 	    gltf_p_stdin.newLine();
	 	    gltf_p_stdin.flush();
	        // Write the output to the console (essential or the rest would not work for some reason)
	 	    BufferedReader gltf_br = new BufferedReader(new InputStreamReader(gltf_p.getInputStream()));
	 	    String gltf_thisLine = null;

	 	    while ((gltf_thisLine = gltf_br.readLine()) != null) {
	 	    	System.out.println(gltf_thisLine);
	 		}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
