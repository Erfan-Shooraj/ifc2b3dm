# Instructions

In order to implement and make use of this script, you need to initially set up the BimServer API. Please follow the instructions here to set up the client:

https://github.com/opensourceBIM/BIMserver/wiki/BimServerClient

After the BimServer setup, a converter folder needs to be created and obj2gltf folder and IfcConvert.exe need to be downloaded and included in the folder.

Running the script will add another folder for your model which will have the following structure at the end of conversion:

--Converter:

--------------obj2gltf

--------------IfcConvert.exe

--------------ModelFolder:

--------------------------IFCs

--------------------------SJSONs

--------------------------OBJs

--------------------------GLTFs

--------------------------model.josn

