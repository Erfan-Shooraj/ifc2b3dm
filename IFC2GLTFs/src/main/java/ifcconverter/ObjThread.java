package ifcconverter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class ObjThread extends Thread {
	ProcessBuilder ifcconvert_builder;
	Process ifcconvert_p;
	BufferedWriter ifcconvert_p_stdin;
    int fifcstart;
	int fifcend;
	File[] ifcs;
	
	ObjThread(File ifcs[], int start, int end){
		this.ifcs = ifcs;
		this.fifcstart = start;
		this.fifcend = end;
		
		// init shell
        ifcconvert_builder = new ProcessBuilder( "cmd.exe" );
        ifcconvert_p=null;
        
        try {
			ifcconvert_p = ifcconvert_builder.start();
			ifcconvert_p_stdin = 
			          new BufferedWriter(new OutputStreamWriter(ifcconvert_p.getOutputStream()));
	        ifcconvert_p_stdin.write("cd " + Main.OBJS_PATH);
	        ifcconvert_p_stdin.newLine();
	        ifcconvert_p_stdin.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		try {
	        for (int i=fifcstart; i<fifcend;i++) {
	        	
	        	if (!((new File (Main.OBJS_PATH + ifcs[i].getName().split(".ifc")[0] + ".obj")).exists())) {
	        		
					ifcconvert_p_stdin.write("IfcConvert \"../IFCs/" + ifcs[i].getName() + "\" \"./" + ifcs[i].getName().split(".ifc")[0] + ".obj\"");
					ifcconvert_p_stdin.newLine();
					ifcconvert_p_stdin.flush();
	        	}
	        }
	        // finally close the shell by execution exit command
	        ifcconvert_p_stdin.write("exit");
	        ifcconvert_p_stdin.newLine();
	        ifcconvert_p_stdin.flush(); 
	        // Write the output to the console (essential or the rest would not work for some reason)
	        BufferedReader ifcconvert_br = new BufferedReader(new InputStreamReader(ifcconvert_p.getInputStream()));
	        String ifcconvert_thisLine = null;

			while ((ifcconvert_thisLine = ifcconvert_br.readLine()) != null) {
				System.out.println(ifcconvert_thisLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
