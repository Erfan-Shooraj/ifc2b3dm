package ifcconverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.bimserver.client.BimServerClient;
import org.bimserver.shared.exceptions.PublicInterfaceNotFoundException;
import org.bimserver.shared.exceptions.ServerException;
import org.bimserver.shared.exceptions.UserException;


public class IFCSplitter {
	BimServerClient client;
	public long IFCserializerOid;
	public long JSONserializerOid;
	
	int WallsCount = 0;
	int WindowsCount = 0;
	int DoorsCount = 0;
	int ColumnsCount = 0;
	int SlabsCount = 0;
	int StairsCount = 0;
	int BuildingElementProxiesCount = 0;
	int BeamsCount = 0;
	int CoveringsCount = 0;
	int CurtainWallsCount = 0;
	int RailingsCount = 0;
	int RampsCount = 0;
	int FlowTerminalsCount = 0;
	int FurnishingElementsCount = 0; 
	
	public IFCSplitter(BimServerClient client) {
		this.client = client;
		try {
			IFCserializerOid = client.getServiceInterface().getSerializerByName("Ifc2x3tc1").getOid();
			JSONserializerOid = client.getServiceInterface().getSerializerByName("Json (Streaming)").getOid();
			System.out.println(JSONserializerOid);
		} catch (ServerException | UserException | PublicInterfaceNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void splitWall(Long roid, String oid, String ifcpath, String jsonpath) {
		// download the specific wall with its openings
		String query = "{\r\n" + 
				"  \"type\": {\r\n" + 
				"    \"name\": \"IfcProduct\",\r\n" + 
				"    \"includeAllSubTypes\": true\r\n" + 
				"  },\r\n" + 
				"\"include\": {\r\n" + 
				"    \"type\": \"IfcWall\",\r\n" + 
				"    \"field\": \"HasOpenings\",\r\n" + 
				"    \"include\": {\r\n" + 
				"      \"type\": \"IfcRelVoidsElement\",\r\n" + 
				"      \"field\": \"RelatedOpeningElement\",\r\n" + 
				"      \"include\": {\r\n" + 
				"        \"type\": \"IfcOpeningElement\",\r\n" + 
				"        \"includes\": [\r\n" + 
				"          \"validifc:AllProperties\",\r\n" + 				
				"          \"validifc:ContainedInStructure\",\r\n" + 
				"          \"validifc:OwnerHistory\",\r\n" + 
				"          \"validifc:Representation\",\r\n" + 
				"          \"validifc:ObjectPlacement\"\r\n" + 
				"        ]\r\n" + 
				"      },\r\n" + 
				"      \"includes\": [\r\n" +
				"        \"validifc:AllProperties\",\r\n" + 	
				"        \"validifc:ContainedInStructure\",\r\n" + 
				"        \"validifc:OwnerHistory\",\r\n" + 
				"        \"validifc:Representation\",\r\n" + 
				"        \"validifc:ObjectPlacement\"\r\n" + 
				"      ]\r\n" + 
				"    },\r\n" + 
				"    \"includes\": [\r\n" + 
				"      \"validifc:AllProperties\",\r\n" + 	
				"      \"validifc:ContainedInStructure\",\r\n" + 
				"      \"validifc:OwnerHistory\",\r\n" + 
				"      \"validifc:Representation\",\r\n" + 
				"      \"validifc:ObjectPlacement\"\r\n" + 
				"    ]\r\n" + 
				"  },\r\n" + 
				"\"includes\": [\r\n" + 
				"    \"validifc:AllProperties\",\r\n" + 	
				"    \"validifc:ContainedInStructure\",\r\n" + 
				"    \"validifc:OwnerHistory\",\r\n" + 
				"    \"validifc:Representation\",\r\n" + 
				"    \"validifc:ObjectPlacement\"\r\n" + 
				"  ],\r\n" + 
				"\"oid\":" + oid + "\r\n" + 
				"}";
		Path file = Paths.get(ifcpath,"wall" + WallsCount +".ifc");
		Path jsonfile = Paths.get(jsonpath,"wall" + WallsCount +".json");
		try {
			if (!((new File (ifcpath + "wall" + WallsCount +".ifc")).exists()))
				client.download(roid, query , IFCserializerOid , file );
			if (!((new File (jsonpath + "wall" + WallsCount +".json")).exists()))
				client.download(roid, query , JSONserializerOid , jsonfile );
			WallsCount++;
			
		} catch (ServerException | UserException | PublicInterfaceNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public void splitWindow(Long roid, String oid, String ifcpath, String jsonpath) {
		String query = oidQuery(oid);
		Path file = Paths.get(ifcpath,"window" + WindowsCount +".ifc");
		Path jsonfile = Paths.get(jsonpath,"window" + WindowsCount +".json");
		try {
			if (!((new File (ifcpath + "window" + WindowsCount +".ifc")).exists()))
				client.download(roid, query , IFCserializerOid , file );
			if (!((new File (jsonpath + "window" + WindowsCount +".json")).exists()))
				client.download(roid, query , JSONserializerOid , jsonfile );
			WindowsCount++;
		} catch (ServerException | UserException | PublicInterfaceNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public void splitDoor (Long roid, String oid, String ifcpath, String jsonpath) {
		String query = oidQuery(oid);
		Path file = Paths.get(ifcpath,"door" + DoorsCount +".ifc");
		Path jsonfile = Paths.get(jsonpath,"door" + DoorsCount +".json");
		try {
			if (!((new File (ifcpath + "door" + DoorsCount +".ifc")).exists()))
				client.download(roid, query , IFCserializerOid , file );
			if (!((new File (jsonpath + "door" + DoorsCount +".json")).exists()))
				client.download(roid, query , JSONserializerOid , jsonfile );
			DoorsCount++;
		} catch (ServerException | UserException | PublicInterfaceNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public void splitColumn (Long roid, String oid, String ifcpath, String jsonpath) {
		String query = oidQuery(oid);
		Path file = Paths.get(ifcpath,"column" + ColumnsCount +".ifc");
		Path jsonfile = Paths.get(jsonpath,"column" + ColumnsCount +".json");
		try {
			if (!((new File (ifcpath + "column" + ColumnsCount +".ifc")).exists()))
				client.download(roid, query , IFCserializerOid , file );
			if (!((new File (jsonpath + "column" + ColumnsCount +".json")).exists()))
				client.download(roid, query , JSONserializerOid , jsonfile );
			ColumnsCount++;
		} catch (ServerException | UserException | PublicInterfaceNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public void splitSlab (Long roid, String oid, String ifcpath, String jsonpath) {
		// download the specific slab with its openings
		String query = "\r\n" + 
				"{\r\n" + 
				"  \"type\": {\r\n" + 
				"    \"name\": \"IfcSlab\",\r\n" + 
				"    \"includeAllSubTypes\": true\r\n" + 
				"  },\r\n" + 
				"  \"include\": {\r\n" + 
				"    \"type\": \"IfcSlab\",\r\n" + 
				"    \"field\": \"HasOpenings\",\r\n" + 
				"    \"include\": {\r\n" + 
				"      \"type\": \"IfcRelVoidsElement\",\r\n" + 
				"      \"field\": \"RelatedOpeningElement\",\r\n" + 
				"      \"include\": {\r\n" + 
				"        \"type\": \"IfcOpeningElement\"\r\n" + 
				"      },\r\n" + 
				"      \"includes\": [\r\n" + 
				"        \"validifc:ContainedInStructure\",\r\n" + 
				"        \"validifc:OwnerHistory\",\r\n" + 
				"        \"validifc:Representation\",\r\n" + 
				"        \"validifc:ObjectPlacement\",\r\n" + 
				"        \"validifc:AllProperties\"\r\n" + 
				"      ]\r\n" + 
				"    },\r\n" + 
				"    \"includes\": [\r\n" + 
				"      \"validifc:ContainedInStructure\",\r\n" + 
				"      \"validifc:OwnerHistory\",\r\n" + 
				"      \"validifc:Representation\",\r\n" + 
				"      \"validifc:ObjectPlacement\",\r\n" + 
				"      \"validifc:AllProperties\"\r\n" + 
				"    ]\r\n" + 
				"  },\r\n" + 
				"  \"includes\": [\r\n" + 
				"    \"validifc:ContainedInStructure\",\r\n" + 
				"    \"validifc:OwnerHistory\",\r\n" + 
				"    \"validifc:Representation\",\r\n" + 
				"    \"validifc:ObjectPlacement\",\r\n" + 
				"    \"validifc:AllProperties\"\r\n" + 
				"  ],\r\n" + 
				"\"oid\":" + oid + "\r\n" + 
				"}";
		Path file = Paths.get(ifcpath,"slab" + SlabsCount +".ifc");
		Path jsonfile = Paths.get(jsonpath,"slab" + SlabsCount +".json");
		try {
			if (!((new File (ifcpath + "slab" + SlabsCount +".ifc")).exists()))
				client.download(roid, query , IFCserializerOid , file );
			if (!((new File (jsonpath + "slab" + SlabsCount +".json")).exists()))
				client.download(roid, query , JSONserializerOid , jsonfile );
			SlabsCount++;
		} catch (ServerException | UserException | PublicInterfaceNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public void splitStair (Long roid, String oid, String ifcpath, String jsonpath) {
		// download the specific stair with all of its aggregate components (e.g. slab, stair flight, etc.)
		String query = "{\r\n" + 
				"  \"type\": {\r\n" + 
				"    \"name\": \"IfcStair\",\r\n" + 
				"    \"includeAllSubTypes\": true\r\n" + 
				"  },\r\n" + 
				"  \"include\": {\r\n" + 
				"    \"type\": \"IfcStair\",\r\n" + 
				"    \"field\": \"IsDecomposedBy\",\r\n" + 
				"    \"include\": {\r\n" + 
				"      \"type\": \"IfcRelAggregates\",\r\n" + 
				"      \"field\": \"RelatedObjects\",\r\n" + 
				"      \"includes\": [\r\n" + 
				"        \"validifc:AllProperties\",\r\n" +
				"        \"validifc:ContainedInStructure\",\r\n" + 
				"        \"validifc:OwnerHistory\",\r\n" + 
				"        \"validifc:Representation\",\r\n" + 
				"        \"validifc:ObjectPlacement\"\r\n" + 
				"      ]\r\n" + 
				"\r\n" + 
				"    },\r\n" + 
				"    \"includes\": [\r\n" + 
				"      \"validifc:AllProperties\",\r\n" +
				"      \"validifc:ContainedInStructure\",\r\n" + 
				"      \"validifc:OwnerHistory\",\r\n" + 
				"      \"validifc:Representation\",\r\n" + 
				"      \"validifc:ObjectPlacement\"\r\n" + 
				"    ]\r\n" + 
				"  },\r\n" + 
				"  \"includes\": [\r\n" + 
				"    \"validifc:AllProperties\",\r\n" +
				"    \"validifc:ContainedInStructure\",\r\n" + 
				"    \"validifc:OwnerHistory\",\r\n" + 
				"    \"validifc:Representation\",\r\n" + 
				"    \"validifc:ObjectPlacement\"\r\n" + 
				"  ],\r\n" + 
				"\"oid\":" + oid + "\r\n" + 
				"}";
		Path file = Paths.get(ifcpath,"stair" + StairsCount +".ifc");
		Path jsonfile = Paths.get(jsonpath,"stair" + StairsCount +".json");
		try {
			if (!((new File (ifcpath + "stair" + StairsCount +".ifc")).exists()))
				client.download(roid, query , IFCserializerOid , file );
			if (!((new File (jsonpath + "stair" + StairsCount +".json")).exists()))
				client.download(roid, query , JSONserializerOid , jsonfile );
			StairsCount++;
		} catch (ServerException | UserException | PublicInterfaceNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public void splitBuildingElementProxy (Long roid, String oid, String ifcpath, String jsonpath) {
		String query = oidQuery(oid);
		Path file = Paths.get(ifcpath,"buildingElementProxy" + BuildingElementProxiesCount +".ifc");
		Path jsonfile = Paths.get(jsonpath,"buildingElementProxy" + BuildingElementProxiesCount +".json");
		try {
			if (!((new File (ifcpath + "buildingElementProxy" + BuildingElementProxiesCount +".ifc")).exists()))
				client.download(roid, query , IFCserializerOid , file );
			if (!((new File (jsonpath + "buildingElementProxy" + BuildingElementProxiesCount +".json")).exists()))
				client.download(roid, query , JSONserializerOid , jsonfile );
			BuildingElementProxiesCount++;
		} catch (ServerException | UserException | PublicInterfaceNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public void splitBeam (Long roid, String oid, String ifcpath, String jsonpath) {
		String query = oidQuery(oid);
		Path file = Paths.get(ifcpath,"beam" + BeamsCount +".ifc");
		Path jsonfile = Paths.get(jsonpath,"beam" + BeamsCount +".json");
		try {
			if (!((new File (ifcpath + "beam" + BeamsCount +".ifc")).exists()))
				client.download(roid, query , IFCserializerOid , file );
			if (!((new File (jsonpath + "beam" + BeamsCount +".json")).exists()))
				client.download(roid, query , JSONserializerOid , jsonfile );
			BeamsCount++;
		} catch (ServerException | UserException | PublicInterfaceNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public void splitCovering (Long roid, String oid, String ifcpath, String jsonpath) {
		String query = oidQuery(oid);
		Path file = Paths.get(ifcpath,"covering" + CoveringsCount +".ifc");
		Path jsonfile = Paths.get(jsonpath,"covering" + CoveringsCount +".json");
		try {
			if (!((new File (ifcpath + "covering" + CoveringsCount +".ifc")).exists()))
				client.download(roid, query , IFCserializerOid , file );
			if (!((new File (jsonpath + "covering" + CoveringsCount +".json")).exists()))
				client.download(roid, query , JSONserializerOid , jsonfile );
			CoveringsCount++;
		} catch (ServerException | UserException | PublicInterfaceNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
	

	
	public void splitRailing(Long roid, String oid, String ifcpath, String jsonpath) {
		String query = oidQuery(oid);
		Path file = Paths.get(ifcpath,"railing" + RailingsCount +".ifc");
		Path jsonfile = Paths.get(jsonpath,"railing" + RailingsCount +".json");
		try {
			if (!((new File (ifcpath + "railing" + RailingsCount +".ifc")).exists()))
				client.download(roid, query , IFCserializerOid , file );
			if (!((new File (jsonpath + "railing" + RailingsCount +".json")).exists()))
				client.download(roid, query , JSONserializerOid , jsonfile );
			RailingsCount++;
		} catch (ServerException | UserException | PublicInterfaceNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public void splitRamp(Long roid, String oid, String ifcpath, String jsonpath) {
		// download the specific ramp with all of its aggregate components (e.g. railings)
		String query = "{\r\n" + 
				"  \"type\": {\r\n" + 
				"    \"name\": \"IfcRamp\",\r\n" + 
				"    \"includeAllSubTypes\": true\r\n" + 
				"  },\r\n" + 
				"  \"include\": {\r\n" + 
				"    \"type\": \"IfcRamp\",\r\n" + 
				"    \"field\": \"IsDecomposedBy\",\r\n" + 
				"    \"include\": {\r\n" + 
				"      \"type\": \"IfcRelAggregates\",\r\n" + 
				"      \"field\": \"RelatedObjects\",\r\n" + 
				"      \"includes\": [\r\n" + 
				"        \"validifc:AllProperties\",\r\n" +
				"        \"validifc:ContainedInStructure\",\r\n" + 
				"        \"validifc:OwnerHistory\",\r\n" + 
				"        \"validifc:Representation\",\r\n" + 
				"        \"validifc:ObjectPlacement\"\r\n" + 
				"      ]\r\n" + 
				"\r\n" + 
				"    },\r\n" + 
				"    \"includes\": [\r\n" + 
				"      \"validifc:AllProperties\",\r\n" +
				"      \"validifc:ContainedInStructure\",\r\n" + 
				"      \"validifc:OwnerHistory\",\r\n" + 
				"      \"validifc:Representation\",\r\n" + 
				"      \"validifc:ObjectPlacement\"\r\n" + 
				"    ]\r\n" + 
				"  },\r\n" + 
				"  \"includes\": [\r\n" + 
				"    \"validifc:AllProperties\",\r\n" +
				"    \"validifc:ContainedInStructure\",\r\n" + 
				"    \"validifc:OwnerHistory\",\r\n" + 
				"    \"validifc:Representation\",\r\n" + 
				"    \"validifc:ObjectPlacement\"\r\n" + 
				"  ],\r\n" + 
				"\"oid\":" + oid + "\r\n" + 
				"}";;
		Path file = Paths.get(ifcpath,"ramp" + RampsCount +".ifc");
		Path jsonfile = Paths.get(jsonpath,"ramp" + RampsCount +".json");
		try {
			if (!((new File (ifcpath + "ramp" + RampsCount +".ifc")).exists()))
				client.download(roid, query , IFCserializerOid , file );
			if (!((new File (jsonpath + "ramp" + RampsCount +".json")).exists()))
				client.download(roid, query , JSONserializerOid , jsonfile );
			RampsCount++;
		} catch (ServerException | UserException | PublicInterfaceNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public void splitFlowTerminal(Long roid, String oid, String ifcpath, String jsonpath) {
		String query = oidQuery(oid);
		Path file = Paths.get(ifcpath,"flowTerminal" + FlowTerminalsCount +".ifc");
		Path jsonfile = Paths.get(jsonpath,"flowTerminal" + FlowTerminalsCount +".json");
		try {
			if (!((new File (ifcpath + "flowTerminal" + FlowTerminalsCount +".ifc")).exists()))
				client.download(roid, query , IFCserializerOid , file );
			if (!((new File (jsonpath + "flowTerminal" + FlowTerminalsCount +".json")).exists()))
				client.download(roid, query , JSONserializerOid , jsonfile );
			FlowTerminalsCount++;
		} catch (ServerException | UserException | PublicInterfaceNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public void splitFurnishingElement(Long roid, String oid, String ifcpath, String jsonpath) {
		String query = oidQuery(oid);
		Path file = Paths.get(ifcpath,"furnishingElement" + FurnishingElementsCount +".ifc");
		Path jsonfile = Paths.get(jsonpath,"furnishingElement" + FurnishingElementsCount +".json");
		try {
			if (!((new File (ifcpath + "furnishingElement" + FurnishingElementsCount +".ifc")).exists()))
				client.download(roid, query , IFCserializerOid , file );
			if (!((new File (jsonpath + "furnishingElement" + FurnishingElementsCount +".json")).exists()))
				client.download(roid, query , JSONserializerOid , jsonfile );
			FurnishingElementsCount++;
		} catch (ServerException | UserException | PublicInterfaceNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
	
	private String oidQuery(String oid) {
		// General query for most objects
		String query = "{\r\n" + 
				"  \"type\": {\r\n" + 
				"    \"name\": \"IfcProduct\",\r\n" + 
				"    \"includeAllSubTypes\": true\r\n" + 
				"  },\r\n" + 
				"  \"includes\": [\r\n" + 
				"        \"validifc:AllProperties\",\r\n" + 	
				"        \"validifc:ContainedInStructure\",\r\n" + 
				"        \"validifc:OwnerHistory\",\r\n" + 
				"        \"validifc:Representation\",\r\n" + 
				"        \"validifc:ObjectPlacement\"\r\n" + 
				"  ],\r\n" + 
				"\"oid\":" + oid + "\r\n" +  
				"}";
		return query;
	}

	public void splitCurtainWalls(Long roid, String oid, String ifcpath, String jsonpath) {
		// download the specific curtain wall with all of its aggregate components (e.g. members and plates)
		String query = "{\r\n" + 
				"  \"type\": {\r\n" + 
				"    \"name\": \"IfcCurtainWall\",\r\n" + 
				"    \"includeAllSubTypes\": true\r\n" + 
				"  },\r\n" + 
				"  \"include\": {\r\n" + 
				"    \"type\": \"IfcCurtainWall\",\r\n" + 
				"    \"field\": \"IsDecomposedBy\",\r\n" + 
				"    \"include\": {\r\n" + 
				"      \"type\": \"IfcRelAggregates\",\r\n" + 
				"      \"field\": \"RelatedObjects\",\r\n" + 
				"      \"includes\": [\r\n" + 
				"        \"validifc:AllProperties\",\r\n" +
				"        \"validifc:ContainedInStructure\",\r\n" + 
				"        \"validifc:OwnerHistory\",\r\n" + 
				"        \"validifc:Representation\",\r\n" + 
				"        \"validifc:ObjectPlacement\"\r\n" + 
				"      ]\r\n" + 
				"\r\n" + 
				"    },\r\n" + 
				"    \"includes\": [\r\n" + 
				"      \"validifc:AllProperties\",\r\n" +
				"      \"validifc:ContainedInStructure\",\r\n" + 
				"      \"validifc:OwnerHistory\",\r\n" + 
				"      \"validifc:Representation\",\r\n" + 
				"      \"validifc:ObjectPlacement\"\r\n" + 
				"    ]\r\n" + 
				"  },\r\n" + 
				"  \"includes\": [\r\n" + 
				"    \"validifc:AllProperties\",\r\n" +
				"    \"validifc:ContainedInStructure\",\r\n" + 
				"    \"validifc:OwnerHistory\",\r\n" + 
				"    \"validifc:Representation\",\r\n" + 
				"    \"validifc:ObjectPlacement\"\r\n" + 
				"  ],\r\n" + 
				"\"oid\":" + oid + "\r\n" + 
				"}";
		Path file = Paths.get(ifcpath,"curtainWall" + CurtainWallsCount +".ifc");
		Path jsonfile = Paths.get(jsonpath,"curtainWall" + CurtainWallsCount +".json");
		try {
			if (!((new File (ifcpath + "curtainWall" + CurtainWallsCount +".ifc")).exists()))
				client.download(roid, query , IFCserializerOid , file );
			if (!((new File (jsonpath + "curtainWall" + CurtainWallsCount +".json")).exists()))
				client.download(roid, query , JSONserializerOid , jsonfile );
			CurtainWallsCount++;
			
		} catch (ServerException | UserException | PublicInterfaceNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
}
