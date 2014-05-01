#! /usr/bin/env python

#input: datacollectionid, folder name, reduced results filename, detector name 

#run the data analysis pipeline that Irakli set up
#parameters:  --rMaxStop 700 --rMaxIntervals 50 --threads 10 --nxsQ /entry/detector_result/q --nxsData /entry1/detector_result/data
# **** requires module load edna/sas to have been done
#remove the existing subtraction(s) - consider merging subtraction in the web service stage
#find the log file and file locations where the analysis stored stuff
#call ISPyB web service and store the results
import os, sys, json

additionalPath = "ControlSolutionScatteringv0_3"
def createWebService():
	from suds.client import Client
	from suds.transport.http import HttpAuthenticated
	host = "ispybb-test.diamond.ac.uk"
	URL = "http://"+host+":8080/ispyb-ejb3/ispybWS/ToolsForBiosaxsWebService?wsdl"
	sys.path.append("/dls_sw/dasc/important")
	from ispybbUserInfo import ispybbUser, ispybbPassword
	username = ispybbUser()
	userPassword = ispybbPassword()
	httpAuthenticatedWebService = HttpAuthenticated(username=username, password=userPassword)
	client = Client(URL, transport=httpAuthenticatedWebService)
	client.options.cache.clear() #TODO prevent caching while testing. remove when deployed
	return client

def getLastFolderCreated(outputFolderName):
	directoryList = os.listdir(outputFolderName)
	newestTime = 0
	newestFolder = ""
	for directory in directoryList:
		newDirectory = os.path.join(outputFolderName, directory)
		if os.path.isdir(newDirectory) and newDirectory.count("mostRecentEDNASasDirectory")==0 and newDirectory.count("extractData") == 0:
			newTime = os.stat(newDirectory).st_mtime
			if newestTime < newTime:
				newestTime = newTime
				newestFolder = newDirectory
	return newestFolder

def parseLogFile(logFileName, results):
	f=open(logFileName,'r')
	line = f.readline()
	while line:
		splitLine = line.split()
		if line.count("Real space: Rg =")>0:
			results["rgGnom"] = splitLine[5]
		if line.count("Total  estimate : ")>0:
			results["total"] = splitLine[4]
			if splitLine[8] == "REASONABLE" or splitLine[8] == "GOOD" or splitLine[8] == "EXCELLENT":
				results["isagregated"] = False
			else:
				results["isagregated"] = True
		if line.count("Results of DAMMIF run:")>0:
			f.readline()
			line = f.readline()
			splitLine = line.split()
			results["dammifchi"]=splitLine[3]
			results["dammifrfactor"]=splitLine[6]
		if line.count("Optimized value of RMax")>0:
			results["dmax"] = splitLine[6]
		if line.count("Number of DAMMIF jobs run : ")>0:
			numberOfDammifJobs = int(splitLine[7])
			for i in range(numberOfDammifJobs):
				line = f.readline()
				splitLine = line.split()
				results["chiSqrt"+str(i)] = splitLine[3]
				results["rfactor"+str(i)] = splitLine[6]
				results["dammifFile"+str(i)] = splitLine[7]
		line = f.readline()

def parseResults(outputFolderName, dataCollectionId):
	results = {}
	results["dataCollectionId"] = dataCollectionId
	folder = getLastFolderCreated(outputFolderName)
	#get filenames from last folder created
	results['firFile'] = os.path.join(folder, additionalPath, "Dammifv0_1", "dammif.fir")
	results['fitFile'] = os.path.join(folder, additionalPath, "Dammifv0_1", "dammif.fit")
	results['nsdPlot'] = os.path.join(folder, additionalPath, "dammifNSDResults.png")
	results['densityPlot'] = os.path.join(folder, additionalPath, "distributionPR.png")
	results['scatteringFilePath'] = os.path.join(folder, additionalPath, "gnomFittingResults.png")
	#get last log file created - then get some information from it
	lastLogName = folder + ".log"
	parseLogFile(lastLogName, results)
	
	#TODO fix these up later - not sure exactly how I can get these values
	results["rg"] = 0
	results["rgstdev"] = 0
	results["i0"] = 0
	results["i0stdev"] = 0
	results["quality"] = 0
	results["gnomFile"] = ""
	results["rgGuinier"] = 0
	results["volume"] = "0"
	results["filename"] = ""
	return results, folder

def createModels(outputFolderName,results):
	modelList = []
	model = {}
	for i in range(0,10):
		if "dammifFile"+str(i) in results:
			model["pdbFile"] = results["dammifFile"+str(i)]
			model["chiSqrt"] = results["chiSqrt"+str(i)]
			model["rfactor"] = results["rfactor"+str(i)]
			model["name"] = "dammif-"+str(i)
			modelList.append(model)
		else:
			continue

	dammifResultsModel = {}
	dammifResultsModel["firFile"] = os.path.join(outputFolderName,additionalPath, "Dammifv0_1","dammif.fir")
	dammifResultsModel["pdbFile"] = os.path.join(outputFolderName,additionalPath, "Damfiltv0_1","damfilt.pdb")
	dammifResultsModel["fitFile"] = os.path.join(outputFolderName,additionalPath, "Dammifv0_1","dammif.fit")
	#dammifResultsModel["chiSqrt"] = results["dammifchi"]
	#dammifResultsModel["rfactor"] = results["dammifrfactor"]
	damminResultsModel = {} #our "default" empty model because we do not run Dammin in the automated method
	damminResultsModel["firFile"] = ""
	damminResultsModel["pdbFile"] = ""
	damminResultsModel["fitFile"] = ""

	damaverResultsModel = {}
	damaverResultsModel["pdbFile"] = os.path.join(outputFolderName,additionalPath, "Damaverv0_1","damaver.pdb")
	return modelList, dammifResultsModel, damaverResultsModel, damminResultsModel

def storeAnalysis(client, filename, backgroundFilename, outputFolderName, detector, results):
	import extractDataFromNexus
	extractFolderName = outputFolderName + os.sep + "extractData_"+str(results["dataCollectionId"])
	filenames = extractDataFromNexus.directCall(filename, extractFolderName, detector, True)
	curvesFiles = ",".join(filenames)
	numFiles = len(filenames) #TODO assuming all files are merged

	results["guinierPlotPath"] = os.path.join(extractFolderName, "guinierPlot.png")
	results["kratkyPlotPath"] = os.path.join(extractFolderName, "kratkyPlot.png")

	if backgroundFilename != None:
		backgroundFilenames = extractDataFromNexus.directCall(backgroundFilename, outputFolderName + os.sep + "extractData_" + str(results["dataCollectionId"]), detector, True)
		backgroundCurveFiles = ",".join(backgroundFilenames)
		numBackgroundFiles = len(backgroundFilenames)
		client.service.storeDataAnalysisResultByDataCollectionId(results["dataCollectionId"], None, None, None, None, None, 0, 0, None, None, "", 0, None, None, None, None, "", None, numBackgroundFiles, numBackgroundFiles, backgroundCurveFiles, 0, "", "", "", "", None)
	#client.service.storeDataAnalysisResultByMeasurementId(None, None, None, None, None, None, 0, 0, None, None, "", 0, None, None, None, None, "", None, 0, 0, "", 1, "", "", "", "", None)
	client.service.storeDataAnalysisResultByDataCollectionId(results["dataCollectionId"], results["filename"],
		None, None, None, None, 0, 0,
		None, results["isagregated"], "", 0, results["gnomFile"], None, float(results["rgGnom"])/10, float(results["dmax"])/10, results["total"],
		results["volume"], numFiles, numFiles, curvesFiles, 2, "", results["scatteringFilePath"], results["guinierPlotPath"], results["kratkyPlotPath"], results["densityPlot"])

def storeModels(client, model, dammifModel, damaverModel, damminModel, results):
	client.service.storeAbInitioModelsByDataCollectionId(json.dumps([results["dataCollectionId"]]), json.dumps(model), json.dumps(damaverModel),
		json.dumps(dammifModel), json.dumps(dammifModel), results["nsdPlot"], "") #TODO replace last dammifModel with real damminModel

def runPipeline(filename, dataPath, threads, columns):
	os.system("module load edna/sas && run-sas-pipeline.py --rMaxStop 700 --rMaxIntervals 50 --data " + filename + " --threads "+ str(threads) + " --columns " + str(columns) + " --nxsQ " + dataPath+"q --nxsData " + dataPath + "data")

if __name__ == '__main__':

	import argparse
	parser = argparse.ArgumentParser()
	parser.add_argument("--filename", type=str, help="input filename after data reduction")
	parser.add_argument("--backgroundFilename", type=str, help="input filename of background after data reduction")
	parser.add_argument("--outputFolderName", type=str, help="output folder location")
	parser.add_argument("--detector", type=str, help="detector name")
	parser.add_argument("--dataCollectionId", type=int, help="dataCollectionId")
	parser.add_argument("--threads", type=int, help="number of threads to use")
	parser.add_argument("--fedid", type=str, help="fedid of user for output folder location, if folder is not specified (ignored if outputfolder name is used")
	parser.add_argument("--columns", type=int, help="number of columns to use from data file")
	args = parser.parse_args()

	if args.filename:
		filename = args.filename
	else:
		print "filename must be defined"
		sys.exit(1)
	if args.backgroundFilename:
		backgroundFilename = args.backgroundFilename
	else:
		backgroundFilename = None
	if args.outputFolderName:
		outputFolderName = args.outputFolderName
	else:
		if args.fedid:
			outputFolderName = "/dls/tmp/"+arg.fedid
		else:
			outputFolderName = "/dls/tmp/user"
	if args.detector:
		detector = args.detector
	else:
		defaultDetectorName = "detector"
		if filename.startswith("/dls/b21"):
			detector = "detector"
		elif filename.startswith("/dls/i22"):
			detector = "Pilatus2M"
		else:
			print "Unexpected data path, setting detector name to " + defaultDetectorName
			detector = defaultDetectorName
	if args.threads:
		threads = args.threads
	else:
		threads = 10
	if args.columns:
		columns = args.columns
	else:
		columns = 1
	if args.dataCollectionId:
		dataCollectionId = args.dataCollectionId
	else:
		print "cannot proceed without a data collection ID to attach the results to"
		sys.exit(1)

	originalDirectory = os.getcwd()
	os.chdir(outputFolderName)
	try:
		runPipeline(filename, "/entry1/"+detector+"_result/",threads, columns)

		results, folder = parseResults(outputFolderName, dataCollectionId)
		client = createWebService()
		(model, dammifModel, damaverModel, damminModel) = createModels(folder, results)
		storeAnalysis(client, filename, backgroundFilename, outputFolderName, detector, results)
		storeModels(client, model, dammifModel, damaverModel, damminModel, results)

		os.chdir(originalDirectory)
	except Exception as e:
		info = sys.exc_info()
		import traceback
		print "exception during the pipeline run or results insertion into database: ", e, info[0], info[1], traceback.print_exception(info[0], info[1], info[2])
