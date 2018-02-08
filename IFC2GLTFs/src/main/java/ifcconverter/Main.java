package ifcconverter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.bimserver.client.BimServerClient;
import org.bimserver.client.json.JsonBimServerClientFactory;
import org.bimserver.shared.ChannelConnectionException;
import org.bimserver.shared.UsernamePasswordAuthenticationInfo;
import org.bimserver.shared.exceptions.BimServerClientException;
import org.bimserver.shared.exceptions.PublicInterfaceNotFoundException;
import org.bimserver.shared.exceptions.ServerException;
import org.bimserver.shared.exceptions.ServiceException;
import org.bimserver.shared.exceptions.UserException;

import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
/**This script uses BimServer API to split an IFC model into its constituent components.
 * It then uses IfcConvert to convert each IFC file to OBJ format
 * Finally, the OBJ files are converted to GLTF using obj2gltf
 * 
 * @author Erfan Shooraj
 * 
 */
public class Main {
	private static String ADDRESS = "http://localhost:2020"; // Enter your BimServer address
	private static String USERNAME = ""; //BimServer Username
	private static String PASSWORD = ""; //BimServer Password
	private static String PROJECT = "";  // Model name on BimServer
	public static String CONVERTER_PATH = ""; //Enter your converter path
	public static String JSON_PATH = CONVERTER_PATH + PROJECT + "\\" + PROJECT + ".json";
	public static String IFCS_PATH = CONVERTER_PATH + PROJECT + "\\" + "IFCs\\";
	public static String OBJS_PATH = CONVERTER_PATH + PROJECT + "\\" + "OBJs\\";
	public static String GLTFS_PATH = CONVERTER_PATH + PROJECT + "\\" + "GLTFs\\";
	public static String SJSONS_PATH = CONVERTER_PATH + PROJECT + "\\" + "SJSONs\\";
	public static String OBJ2GLTFPATH = CONVERTER_PATH + "obj2gltf\\";
	public static String NODE_PATH = "\"C:\\Program Files\\nodejs\\node.exe\"";
	public static void main(String[] args) {
		try {
			// Connect to the server
			JsonBimServerClientFactory clientFactory = new JsonBimServerClientFactory(ADDRESS);
			BimServerClient client = clientFactory.create(new UsernamePasswordAuthenticationInfo(USERNAME, PASSWORD));
			
			// Create project folder
			File project_dir = new File(CONVERTER_PATH + PROJECT);
			project_dir.mkdir();
			
			// Get the Project Json
			Long roid = client.getServiceInterface().getTopLevelProjectByName(PROJECT).getLastRevisionId();
			//DownloadProjectJSON(client, roid); //Downloads malformed json on large models sometime
												 //Better to download the json file directly from bimserver and put in the model folder

			// Split the IFC files
			IFCSplitter splitter = new IFCSplitter(client);
			
			// Create the folder to store split IFC files
			File ifcs_dir = new File(IFCS_PATH);
			ifcs_dir.mkdir();
			// And Json (Streaming)
			File sjsons_dir = new File(SJSONS_PATH);
			sjsons_dir.mkdir();
			
			long ifcstartTime = System.currentTimeMillis();
			
			// Read the Json file and query download the IFC file of each component of the model 
			JsonParser parser = new JsonParser();
			JsonReader reader = new JsonReader(new FileReader(JSON_PATH));
			reader.setLenient(true);
			
			try {
				reader.beginObject();
				System.out.println(reader.nextName());
				reader.beginArray();

				while (reader.hasNext()) {
					reader.beginObject();
					reader.nextName(); // _i
					long oid = reader.nextLong(); // index value
					
					reader.nextName(); //_t
					String type = reader.nextString(); //type
					
					System.out.println(oid);
					System.out.println(type);

					
					if (type.equals("IfcWallStandardCase") || type.equals("IfcWall")) {
						splitter.splitWall(roid, Long.toString(oid), IFCS_PATH, SJSONS_PATH);
					}
					if (type.toString().equals("IfcWindow")) {
						splitter.splitWindow(roid, Long.toString(oid), IFCS_PATH, SJSONS_PATH);
					}
					
					if (type.toString().equals("IfcColumn")) {
						splitter.splitColumn(roid, Long.toString(oid), IFCS_PATH, SJSONS_PATH);
					}
					if (type.toString().equals("IfcSlab")) {
						splitter.splitSlab(roid, Long.toString(oid), IFCS_PATH, SJSONS_PATH);
					}
					if (type.toString().equals("IfcStair")) {
						splitter.splitStair(roid, Long.toString(oid), IFCS_PATH, SJSONS_PATH);
					}
					if (type.toString().equals("IfcDoor")) {
						splitter.splitDoor(roid, Long.toString(oid), IFCS_PATH, SJSONS_PATH);
					}
					if (type.toString().equals("IfcBuildingElementProxy")) {
						splitter.splitBuildingElementProxy(roid, Long.toString(oid), IFCS_PATH, SJSONS_PATH);
					}
					if (type.toString().equals("IfcBeam")) {
						splitter.splitBeam(roid, Long.toString(oid), IFCS_PATH, SJSONS_PATH);
					}
					if (type.toString().equals("IfcCovering")) {
						splitter.splitCovering(roid, Long.toString(oid), IFCS_PATH, SJSONS_PATH);
					}
					if (type.toString().equals("IfcRailing")) {
						splitter.splitRailing(roid, Long.toString(oid), IFCS_PATH, SJSONS_PATH);
					}
					if (type.toString().equals("IfcRamp")) {
						splitter.splitRamp(roid, Long.toString(oid), IFCS_PATH, SJSONS_PATH);
					}
					if (type.toString().equals("IfcFlowTerminal")) {
						splitter.splitFlowTerminal(roid, Long.toString(oid), IFCS_PATH, SJSONS_PATH);
					}
					if (type.toString().equals("IfcFurnishingElement")) {
						splitter.splitFurnishingElement(roid, Long.toString(oid), IFCS_PATH, SJSONS_PATH);
					}
					if (type.toString().equals("IfcCurtainWall")) {
						splitter.splitCurtainWalls(roid, Long.toString(oid), IFCS_PATH, SJSONS_PATH);
					}
					
					reader.skipValue();
					reader.skipValue();
					reader.endObject();
				}
				reader.endArray();
				reader.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			long ifcendTime = System.currentTimeMillis();
			
			System.out.println("Number of Walls Found : " + splitter.WallsCount);
			System.out.println("Number of Windows Found : " + splitter.WindowsCount);
			System.out.println("Number of Doors Found : " + splitter.DoorsCount);
			System.out.println("Number of Columns Found : " + splitter.ColumnsCount);
			System.out.println("Number of Slabs Found : " + splitter.SlabsCount);
			System.out.println("Number of Stairs Found : " + splitter.StairsCount);
			
			System.out.println("Number of BuildingElementProxies Found : " + splitter.BuildingElementProxiesCount);
			System.out.println("Number of Beams Found : " + splitter.BeamsCount);
			System.out.println("Number of Coverings Found : " + splitter.CoveringsCount);
			System.out.println("Number of CurtainWallsFound : " + splitter.CurtainWallsCount);
			System.out.println("Number of Railings Found : " + splitter.RailingsCount);
			
			System.out.println("Number of Ramps Found : " + splitter.RampsCount);
			System.out.println("Number of FlowTerminals Found : " + splitter.FlowTerminalsCount);
			System.out.println("Number of FurnishingElements Found : " + splitter.FurnishingElementsCount);

			long totalifcTime = ifcendTime - ifcstartTime;
			System.out.println("Total Time to Split the IFC File : " + totalifcTime + "ms");
			
			// Convert the IFC files to OBJ
			
			// Create the folder to store OBJ files
			File objs_dir = new File(OBJS_PATH);
			objs_dir.mkdir();
			
	        File[] ifcs = ifcs_dir.listFiles();

	        long objstartTime = System.currentTimeMillis();
	        //******* Note: IfcConvert needs to be in the converter folder
	        try {
	        	if (!((new File (OBJS_PATH + "IfcConvert.exe")).exists())) {
			        // init shell
			        ProcessBuilder ifcconvertcopy_builder = new ProcessBuilder( "cmd.exe" );
			        Process ifcconvertcopy_p=null;
			        
			        ifcconvertcopy_p = ifcconvertcopy_builder.start();
		            BufferedWriter ifcconvertcopy_p_stdin = 
		  		          new BufferedWriter(new OutputStreamWriter(ifcconvertcopy_p.getOutputStream()));
		            ifcconvertcopy_p_stdin.write("cd " + CONVERTER_PATH);
		            ifcconvertcopy_p_stdin.newLine();
		            ifcconvertcopy_p_stdin.flush();
			        
			        // copy IfcConvert to the OBJs folder to get the materials path right 
		            ifcconvertcopy_p_stdin.write("copy IfcConvert.exe \"" + OBJS_PATH + "\"");
		            ifcconvertcopy_p_stdin.newLine();
		            ifcconvertcopy_p_stdin.flush();
		            
			        // finally close the shell by exit command
		            ifcconvertcopy_p_stdin.write("exit");
		            ifcconvertcopy_p_stdin.newLine();
		            ifcconvertcopy_p_stdin.flush();
		            
		            // Write the output to the console (essential or the rest would not work for some reason)
		 	        BufferedReader ifcconvertcopy_br = new BufferedReader(new InputStreamReader(ifcconvertcopy_p.getInputStream()));
		 	        String ifcconvertcopy_thisLine = null;
	
		 			while ((ifcconvertcopy_thisLine = ifcconvertcopy_br.readLine()) != null) {
		 				System.out.println(ifcconvertcopy_thisLine);
		 			}
	        	}
	        	
		        // Convert the each IFC file to OBJ

	        	//******* Note: obj2gltf needs to be in the converter folder
		        int fifcstart=0;
				int fifcend;
				int ifcstepsize=50;
				for (int j=0; j<= ifcs.length/ifcstepsize;j++) {
					fifcstart= (ifcstepsize*j);
					if(j==ifcs.length/ifcstepsize)
						fifcend = fifcstart + ((ifcs.length)%ifcstepsize);
					else
						fifcend = fifcstart + ifcstepsize;
					
					//System.out.println("fifcstart = " + fifcstart);
					//System.out.println("fifcend = " + fifcend);
					
					// init shell
			        ProcessBuilder ifcconvert_builder = new ProcessBuilder( "cmd.exe" );
			        Process ifcconvert_p=null;
			        ifcconvert_p = ifcconvert_builder.start();
		            BufferedWriter ifcconvert_p_stdin = 
		  		          new BufferedWriter(new OutputStreamWriter(ifcconvert_p.getOutputStream()));
		          
			        
		            ifcconvert_p_stdin.write("cd " + OBJS_PATH);
		            ifcconvert_p_stdin.newLine();
		            ifcconvert_p_stdin.flush();
			        for (int i=fifcstart; i<fifcend;i++) {
			        	if (!((new File (OBJS_PATH + ifcs[i].getName().split(".ifc")[0] + ".obj")).exists())) {
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
				}
				
				// init shell
		        ProcessBuilder ifcconvertdel_builder = new ProcessBuilder( "cmd.exe" );
		        Process ifcconvertdel_p=null;
		        
		        ifcconvertdel_p = ifcconvertdel_builder.start();
	            BufferedWriter ifcconvertdel_p_stdin = 
	  		          new BufferedWriter(new OutputStreamWriter(ifcconvertdel_p.getOutputStream()));
	            ifcconvertdel_p_stdin.write("cd " + OBJS_PATH);
	            ifcconvertdel_p_stdin.newLine();
	            ifcconvertdel_p_stdin.flush();
		        
		        // Delete IfcConvert from the OBJs folder
	            ifcconvertdel_p_stdin.write("del IfcConvert.exe");
	            ifcconvertdel_p_stdin.newLine();
	            ifcconvertdel_p_stdin.flush();
	            
		        // finally close the shell by execution exit command
	            ifcconvertdel_p_stdin.write("exit");
	            ifcconvertdel_p_stdin.newLine();
	            ifcconvertdel_p_stdin.flush();
	            
	            // Write the output to the console (essential or the rest would not work for some reason)
	 	        BufferedReader ifcconvertdel_br = new BufferedReader(new InputStreamReader(ifcconvertdel_p.getInputStream()));
	 	        String ifcconvertdel_thisLine = null;

	 			while ((ifcconvertdel_thisLine = ifcconvertdel_br.readLine()) != null) {
	 				System.out.println(ifcconvertdel_thisLine);
	 			}
	        } catch (IOException e) {
	            System.out.println(e);
	        }
			long objendTime = System.currentTimeMillis();
			long totalobjTime = objendTime - objstartTime;
			System.out.println("Total Time to convert the IFC files to OBJ : " + totalobjTime + "ms");

			
			//Create the folder to store GLTF files
			
	        File gltfs_dir = new File(GLTFS_PATH);
			gltfs_dir.mkdir();
			
	        // Convert the OBJ files to GLTF
	        long gltfstartTime = System.currentTimeMillis();
			int fobjstart=0;
			int fobjend;
			int objstepsize=35;
			for (int j=0; j<= ifcs.length/objstepsize;j++) {
				fobjstart= (objstepsize*j);
				if(j==ifcs.length/objstepsize)
					fobjend = fobjstart + ((ifcs.length)%objstepsize);
				else
					fobjend = fobjstart + objstepsize;
				
				// init shell
				ProcessBuilder gltf_builder = new ProcessBuilder( "cmd.exe" );
				Process gltf_p=null;
	        
	        
		        try {
		            gltf_p = gltf_builder.start();
		            BufferedWriter gltf_p_stdin = 
		  		          new BufferedWriter(new OutputStreamWriter(gltf_p.getOutputStream()));
		            gltf_p_stdin.write("cd " + OBJ2GLTFPATH);
					gltf_p_stdin.newLine();
			        gltf_p_stdin.flush();
		        
			        for (int i=fobjstart; i<fobjend;i++) {
			        	System.out.println(ifcs[i].getName().split(".ifc")[0]);
			        	if (!((new File (GLTFS_PATH + ifcs[i].getName().split(".ifc")[0] + ".gltf")).exists())) {
				        	gltf_p_stdin.write(NODE_PATH + " bin\\\\obj2gltf.js -i \"" + OBJS_PATH + ifcs[i].getName().split(".ifc")[0] +".obj\" -o \"" + GLTFS_PATH + ifcs[i].getName().split(".ifc")[0] + ".gltf\" --materialsCommon");
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
		        	System.out.println(e);
		        }
	        }
			long gltfendTime = System.currentTimeMillis();
			long totalgltfTime = gltfendTime - gltfstartTime;
			System.out.println("Total Time to convert the OBJ files to GLTF : " + totalgltfTime + "ms");
			

			//long totalTime = totalifcTime + totalobjTime + totalgltfTime;
			//System.out.println("Total Conversion Time : " + totalTime + "ms");

		} catch (BimServerClientException | ServiceException | ChannelConnectionException e) {
			e.printStackTrace();
		} catch (PublicInterfaceNotFoundException e) {
			e.printStackTrace();
		} catch (JsonIOException e1) {
			e1.printStackTrace();
		} catch (JsonSyntaxException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
	}
	
	public static void DownloadProjectJSON(BimServerClient client, Long roid) {
		try {
			long JsonSerializerOid = client.getServiceInterface().getSerializerByName("Json").getOid();
			Path file = Paths.get(JSON_PATH);
			client.download(roid, JsonSerializerOid, file);
		} catch (ServerException | UserException | PublicInterfaceNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (BimServerClientException e) {
			e.printStackTrace();
		}


	}
}