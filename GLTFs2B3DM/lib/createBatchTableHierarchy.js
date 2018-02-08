'use strict';
var Cesium = require('cesium');
var fsExtra = require('fs-extra');
var path = require('path');
var Promise = require('bluebird');
var createB3dm = require('./createB3dm');
var createGltf = require('./createGltf');
var createTilesetJsonSingle = require('./createTilesetJsonSingle');
var getBufferPadded = require('./getBufferPadded');
var Material = require('./Material');
var Mesh = require('./Mesh');
var saveTile = require('./saveTile');
var saveTilesetJson = require('./saveTilesetJson');

// added
var fs = require('fs');

var Cartesian3 = Cesium.Cartesian3;
var CesiumMath = Cesium.Math;
var defaultValue = Cesium.defaultValue;
var defined = Cesium.defined;
var Matrix4 = Cesium.Matrix4;
var Quaternion = Cesium.Quaternion;

module.exports = createBatchTableHierarchy;

var sizeOfFloat = 4;
var sizeOfUint16 = 2;

var whiteOpaqueMaterial = new Material({
    diffuse : [1.0, 1.0, 1.0, 1.0],
    ambient : [0.2, 0.2, 0.2, 1.0]
});

var modelFolder = ''; // Enter the model folder name

/**
 * Create a tileset that uses a batch table hierarchy.
 *
 * @param {Object} options An object with the following properties:
 * @param {String} options.directory Directory in which to save the tileset.
 * @param {Boolean} [options.batchTableBinary=false] Create a batch table binary for the b3dm tile.
 * @param {Boolean} [options.noParents=false] Don't set any instance parents.
 * @param {Boolean} [options.multipleParents=false] Set multiple parents to some instances.
 * @param {Matrix4] [options.transform=Matrix4.IDENTITY] The tile transform.
 * @param {Boolean} [options.optimizeForCesium=false] Optimize the glTF for Cesium by using the sun as a default light source.
 * @param {Boolean} [options.gzip=false] Gzip the saved tile.
 * @param {Boolean} [options.prettyJson=true] Whether to prettify the JSON.
 * @returns {Promise} A promise that resolves when the tileset is saved.
 */

function createBatchTableHierarchy(options) {
    var useBatchTableBinary = defaultValue(options.batchTableBinary, false);
    var noParents = defaultValue(options.noParents, false);
    var multipleParents = defaultValue(options.multipleParents, false);
    var transform = defaultValue(options.transform, Matrix4.IDENTITY);

    var instances = createInstances(noParents, multipleParents);
    var batchTableJson = createBatchTable(instances);

    var batchTableBinary;
    if (useBatchTableBinary) {
        batchTableBinary = createBatchTableBinary(batchTableJson);  // Modifies the json in place
    }

    // Mesh urls listed in the same order as features in the classIds arrays
    var urls = [];
   
    var files = fs.readdirSync('./data/' + modelFolder + '/GLTFs/');
    for (var i = 0; i < files.length; i++) {
        urls.push('data/' + modelFolder + '/GLTFs/' + files[i]);
    }
    
    var buildingPositions = [
        new Cartesian3(0, 0, 0)
    ];
   
    // glTF models are initially y-up, transform to z-up
    //var yUpToZUp = Quaternion.fromAxisAngle(Cartesian3.UNIT_X, CesiumMath.PI_OVER_TWO);
    var yUpToZUp = Quaternion.fromAxisAngle(Cartesian3.UNIT_X, 0); //don't transform
    //var scale = new Cartesian3(5.0, 5.0, 5.0); // Scale the models up a bit
    var scale = new Cartesian3(1.0, 1.0, 1.0); // don't scale

    // Local transforms of the buildings within the tile
    var buildingTransforms = [];
    for (var i = 0; i < buildingPositions.length; i++) {
        buildingTransforms.push(Matrix4.fromTranslationQuaternionRotationScale(buildingPositions[i], yUpToZUp, scale));
    }

    var tileName = 'tile.b3dm';
    var directory = path.join(options.directory, modelFolder);
    var tilePath = path.join(directory, tileName);
    var tilesetJsonPath = path.join(directory, 'tileset.json');

    //var buildingsLength = 1;
    var buildingsLength = buildingPositions.length;
    var meshesLength = urls.length;
    var batchLength = buildingsLength * meshesLength;
    var geometricError = 100.0;

    var box = [
        0, 0, 30,
        100, 0, 0,
        0, 100, 0,
        0, 0, 30
    ];

    var tilesetJson = createTilesetJsonSingle({
        tileName : tileName,
        geometricError : geometricError,
        box : box,
        transform : transform
    });

    var featureTableJson = {
        BATCH_LENGTH : batchLength
    };

    return Promise.map(urls, function(url) {
        return fsExtra.readJson(url)
            .then(function (gltf) {
                //return Mesh.fromGltf(gltf);
                var meshes = [];
                for (var mesh_index = 0; mesh_index < gltf.meshes.length; mesh_index++) {   //added to account for multiple meshes
                    for (var primitive_index = 0; primitive_index < gltf.meshes[Object.keys(gltf.meshes)[mesh_index]].primitives.length;primitive_index++){ //added to account for multiple primitives
                        meshes.push(Mesh.fromGltf(gltf, mesh_index,primitive_index));
                    }
                }
                return meshes;
            });
    }).then(function (meshes) {
        var meshesLength = meshes.length;
        
        var clonedMeshesperObject = []; //added to account for multiple meshes per object
        for (var i = 0; i < buildingsLength; ++i) {
            for (var j = 0; j < meshes.length; ++j) {
                var clonedMeshes = [];
                for (var k = 0; k < meshes[j].length; ++k) {
                    var mesh = Mesh.clone(meshes[j][k]);
                    //mesh.material = whiteOpaqueMaterial;
                    mesh.transform(buildingTransforms[i]);
                    clonedMeshes.push(mesh);
                }
                clonedMeshesperObject.push(clonedMeshes);
            }
        }
        //var batchedMesh = Mesh.batch(clonedMeshes);
        var batchedMesh = Mesh.batch(clonedMeshesperObject);
        return createGltf({
            mesh : batchedMesh,
            optimizeForCesium : options.optimizeForCesium
        });
    }).then(function(glb) {
        var b3dm = createB3dm({
            glb : glb,
            featureTableJson : featureTableJson,
            batchTableJson : batchTableJson,
            batchTableBinary : batchTableBinary
        });
        return Promise.all([
            saveTilesetJson(tilesetJsonPath, tilesetJson, options.prettyJson),
            saveTile(tilePath, b3dm, options.gzip)
        ]);
    });
}

function createFloatBuffer(values) {
    var buffer = Buffer.alloc(values.length * sizeOfFloat);
    var length = values.length;
    for (var i = 0; i < length; ++i) {
        buffer.writeFloatLE(values[i], i * sizeOfFloat);
    }
    return buffer;
}

function createUInt16Buffer(values) {
    var buffer = Buffer.alloc(values.length * sizeOfUint16);
    var length = values.length;
    for (var i = 0; i < length; ++i) {
        buffer.writeUInt16LE(values[i], i * sizeOfUint16);
    }
    return buffer;
}

function createBatchTableBinary(batchTable) {
    var byteOffset = 0;
    var buffers = [];

    function createBinaryProperty(values, componentType, type) {
        var buffer;
        if (componentType === 'FLOAT') {
            buffer = createFloatBuffer(values);
        } else if (componentType === 'UNSIGNED_SHORT') {
            buffer = createUInt16Buffer(values);
        }
        buffer = getBufferPadded(buffer);
        buffers.push(buffer);
        var binaryReference = {
            byteOffset : byteOffset,
            componentType : componentType,
            type : type
        };
        byteOffset += buffer.length;
        return binaryReference;
    }

    // Convert regular batch table properties to binary
    var propertyName;
    for (propertyName in batchTable) {
        if (batchTable.hasOwnProperty(propertyName) && propertyName !== 'HIERARCHY') {
            if (typeof batchTable[propertyName][0] === 'number') {
                batchTable[propertyName] = createBinaryProperty(batchTable[propertyName], 'FLOAT', 'SCALAR');
            }
        }
    }

    // Convert instance properties to binary
    var hierarchy = batchTable.HIERARCHY;
    var classes = hierarchy.classes;
    var classesLength = classes.length;
    for (var i = 0; i < classesLength; ++i) {
        var instances = classes[i].instances;
        for (propertyName in instances) {
            if (instances.hasOwnProperty(propertyName)) {
                if (typeof instances[propertyName][0] === 'number') {
                    instances[propertyName] = createBinaryProperty(instances[propertyName], 'FLOAT', 'SCALAR');
                }
            }
        }
    }

    // Convert classIds to binary
    hierarchy.classIds = createBinaryProperty(hierarchy.classIds, 'UNSIGNED_SHORT');

    // Convert parentCounts to binary (if they exist)
    if (defined(hierarchy.parentCounts)) {
        hierarchy.parentCounts = createBinaryProperty(hierarchy.parentCounts, 'UNSIGNED_SHORT');
    }

    // Convert parentIds to binary (if they exist)
    if (defined(hierarchy.parentIds)) {
        hierarchy.parentIds = createBinaryProperty(hierarchy.parentIds, 'UNSIGNED_SHORT');
    }

    return Buffer.concat(buffers);
}

function createBatchTable(instances) {
    // Create batch table from the instances' regular properties
    var batchTable = {};
    var instancesLength = instances.length;
    for (var i = 0; i < instancesLength; ++i) {
        var instance = instances[i];
        var properties = instance.properties;
        if (defined(properties)) {
            for (var propertyName in properties) {
                if (properties.hasOwnProperty(propertyName)) {
                    if (!defined(batchTable[propertyName])) {
                        batchTable[propertyName] = [];
                    }
                    batchTable[propertyName].push(properties[propertyName]);
                }
            }
        }
    }

    // Add HIERARCHY object
    batchTable.HIERARCHY = createHierarchy(instances);

    return batchTable;
}

function createHierarchy(instances) {
    var i;
    var j;
    var classes = [];
    var classIds = [];
    var parentCounts = [];
    var parentIds = [];
    var instancesLength = instances.length;
    var classId;
    var classData;

    for (i = 0; i < instancesLength; ++i) {
        var instance = instances[i].instance;
        var className = instance.className;
        var properties = instance.properties;
        var parents = defaultValue(instance.parents, []);
        var parentsLength = parents.length;

        // Get class id
        classId = undefined;
        classData = undefined;
        var classesLength = classes.length;
        for (j = 0; j < classesLength; ++j) {
            if (classes[j].name === className) {
                classId = j;
                classData = classes[j];
                break;
            }
        }

        // Create class if it doesn't already exist
        if (!defined(classId)) {
            classData = {
                name : className,
                length : 0,
                instances : {}
            };
            classId = classes.length;
            classes.push(classData);
            var propertyNames = Object.keys(properties);
            var propertyNamesLength = propertyNames.length;
            for (j = 0; j < propertyNamesLength; ++j) {
                classData.instances[propertyNames[j]] = [];
            }
        }

        // Add properties to class
        for (var propertyName in properties) {
            if (properties.hasOwnProperty(propertyName)) {
                if (defined(classData.instances[propertyName])) {
                    classData.instances[propertyName].push(properties[propertyName]);
                } else {
                    classData.instances[propertyName] = [];
                    classData.instances[propertyName].push(properties[propertyName]);
                }

            }
        }

        // Increment class instances length
        classData.length++;

        // Add to classIds
        classIds.push(classId);

        // Add to parentCounts
        parentCounts.push(parentsLength);

        // Add to parent ids
        for (j = 0; j < parentsLength; ++j) {
            var parent = parents[j];
            var parentId = instances.indexOf(parent);
            parentIds.push(parentId);
        }
    }

    // Check if any of the instances have multiple parents, or if none of the instances have parents
    var singleParents = true;
    var noParents = true;
    for (i = 0; i < instancesLength; ++i) {
        if (parentCounts[i] > 0) {
            noParents = false;
        }
        if (parentCounts[i] > 1) {
            singleParents = false;
        }
    }

    if (noParents) {
        // Unlink parentCounts and parentIds
        parentCounts = undefined;
        parentIds = undefined;
    } else if (singleParents) {
        // Unlink parentCounts and add missing parentIds that point to themselves
        for (i = 0; i < instancesLength; ++i) {
            if (parentCounts[i] === 0) {
                parentIds.splice(i, 0, i);
            }
        }
        parentCounts = undefined;
    }

    return {
        instancesLength : instancesLength,
        classes : classes,
        classIds : classIds,
        parentIds : parentIds,
        parentCounts : parentCounts
    };
}

function createInstances(noParents, multipleParents) {    
    var result = [];
    var files = fs.readdirSync('./data/' + modelFolder + '/GLTFs/');

    for (var i = 0; i < files.length; i++) {
        
        var jsonFile = fsExtra.readFileSync('./data/' + modelFolder + '/SJSONs/' + files[i].slice(0, -5) + '.json'); 
        //console.log(files[i].slice(0, -5));
        var jsonContent = JSON.parse(jsonFile);
        var data_oids = [];
        var data_types = [];
        var info = [];
        for (var j = 0; j < jsonContent.objects.length; j++) {
            
            if (jsonContent.objects[j]._t == "IfcPropertySet") {
                if (jsonContent.objects[j].Name == "Dimensions") {
                    for (var index in jsonContent.objects[j]._rHasProperties) {
                        data_oids.push(jsonContent.objects[j]._rHasProperties[index]._i);
                    }
                }
                if (jsonContent.objects[j].Name == "Constraints") {
                    for (var index in jsonContent.objects[j]._rHasProperties) {
                        data_oids.push(jsonContent.objects[j]._rHasProperties[index]._i);
                    }
                }
            }
            for (var index in data_oids){
                if (jsonContent.objects[j]._i == data_oids[index]) {
                    data_types.push(jsonContent.objects[j].Name);
                    info.push(jsonContent.objects[j]._eNominalValue._v);
                }
            }
            
        }

        var object = {
            instance: {
                className: files[i].slice(0, -5),
                properties: {
                    'File Name': files[i].slice(0, -5),
                    'Object Name': jsonContent.objects[0].Name,
                    'IFC Type': jsonContent.objects[0]._t,
                    GUID: jsonContent.objects[0].GlobalId,
                    BATID: jsonContent.objects[0].Tag 
                }
            }
        };
        for (var k = 0; k < data_types.length; k++) {
            object.instance.properties[data_types[k]] = info[k];
        }
        result.push(object);
    }

    var building0 = {
        instance: {
            className: 'building',
            properties: {
            //    building_name: 'building0',
            //    building_area: 20.0
            }
        }
    };

    if (noParents) {
        return result;
    }
    for (var i = 0; i < result.length; i++) {
        result[i].instance.parents = [building0];
    }
    result.push(building0);
    return result;
}
