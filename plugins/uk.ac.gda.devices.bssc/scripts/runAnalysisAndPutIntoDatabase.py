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
	host = "cs04r-sc-vserv-49"
	URL = "http://"+host+":8080/ispyb-ejb3/ispybWS/ToolsForBiosaxsWebService?wsdl"
	client = Client(URL)
	return client

def getLastFolderCreated(outputFolderName):
	directoryList = os.listdir(outputFolderName)
	newestTime = 0
	newestFolder = ""
	for directory in directoryList:
		newDirectory = os.path.join(outputFolderName, directory)
		if os.path.isdir(newDirectory) and newDirectory.count("mostRecentEDNASasDirectory")==0:
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
			results["rg"] = splitLine[5]
			results["rgstdev"] = splitLine[7]
			results["i0"] = splitLine[10]
			results["i0stdev"] = splitLine[12]
		if line.count("Total  estimate : ")>0:
			results["quality"] = splitLine[4]
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
	results["gnomFile"] = ""
	results["rgGuinier"] = ""
	results["rgGnom"] = ""
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
	dammifResultsModel["pdbFile"] = os.path.join(outputFolderName,additionalPath, "Dammifv0_1","dammif-0.pdb")
	dammifResultsModel["fitFile"] = os.path.join(outputFolderName,additionalPath, "Dammifv0_1","dammif.fit")
	dammifResultsModel["chiSqrt"] = results["dammifchi"]
	dammifResultsModel["rfactor"] = results["dammifrfactor"]
	damminResultsModel = {} #our "default" empty model because we do not run Dammin in the automated method
	damminResultsModel["firFile"] = ""
	damminResultsModel["pdbFile"] = ""
	damminResultsModel["fitFile"] = ""
	return modelList, dammifResultsModel, damminResultsModel

def storeAnalysis(client, results):
	#client.service.storeDataAnalysisResultByMeasurementId(None, None, None, None, None, None, 0, 0, None, None, "", 0, None, None, None, None, "", None, 0, 0, "", 0, "", "", "", "", None)
	#client.service.storeDataAnalysisResultByMeasurementId(None, None, None, None, None, None, 0, 0, None, None, "", 0, None, None, None, None, "", None, 0, 0, "", 1, "", "", "", "", None)
	client.service.storeDataAnalysisResultByDataCollectionId(results["dataCollectionId"], results["filename"],
		results["rg"], results["rgstdev"], results["i0"], results["i0stdev"], 0, 0,
		results["quality"], results["isagregated"], "", 0, results["gnomFile"], results["rgGuinier"], results["rgGnom"], results["dmax"], "",
		results["volume"], 0, 0, "", 2, "", "", "", "", results["densityPlot"])

def storeModels(client, model, dammifModel, damminModel, results):
	damaverResults = []#assemble from results["dammaver"]
	client.service.storeAbInitioModelsByDataCollectionId(json.dumps([results["dataCollectionId"]]), json.dumps(model), json.dumps(damminModel), #TODO this should be damaver
		json.dumps(dammifModel), json.dumps(damminModel), results["nsdPlot"], "")

def runPipeline(filename, dataPath, threads, columns):
	os.system("module load edna/sas && run-sas-pipeline.py --rMaxStop 700 --rMaxIntervals 50 --data " + filename + " --threads "+ str(threads) + " --columns " + str(columns) + " --nxsQ " + dataPath+"q --nxsData " + dataPath + "data")

if __name__ == '__main__':

	import argparse
	parser = argparse.ArgumentParser()
	parser.add_argument("--filename", type=str, help="input filename after data reduction")
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
		(model, dammifModel, damminModel) = createModels(folder, results)
		storeAnalysis(client, results)
		storeModels(client, model, dammifModel, damminModel, results)

		os.chdir(originalDirectory)
	except Exception as e:
		print "exception during the pipeline run or results insertion into database: ", e
