#input: datacollectionid, folder name, reduced results filename, detector name 

#run the data analysis pipeline that Irakli set up
#parameters:  --rMaxStop 700 --rMaxIntervals 50 --threads 10 --nxsQ /entry/detector_result/q --nxsData /entry1/detector_result/data
# **** requires module load edna/sas to have been done
#remove the existing subtraction(s) - consider merging subtraction in the web service stage
#find the log file and file locations where the analysis stored stuff
#call ISPyB web service and store the results
import os

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
		newTime = os.stat(newDirectory).st_mtime
		if newestTime < newTime:
			newestTime = newTime
			newestFolder = newDirectory
	return newDirectory

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
		line = f.readline()

def parseResults(outputFolderName, measurementId):
	results = {}
	results["measurementId"] = measurementId
	folder = getLastFolderCreated(outputFolderName)
	#get filenames from last folder created
	results['firFile'] = os.path.join(folder, "Dammifv0_1", "dammif.fir")
	results['fitFile'] = os.path.join(folder, "Dammifv0_1", "dammif.fit")
	results['nsdPlot'] = os.path.join(folder, "dammifNSDResults.png")
	results['densityPlot'] = os.path.join(folder, "distributionPR.png")
	results['scatteringFilePath'] = os.path.join(folder, "gnomFittingResults.png")
	#get last log file created - then get some information from it
	lastLogName = folder + ".log"
	parseLogFile(lastLogName, results)
	
	#TODO fix these up later - not sure exactly how I can get these values
	results["gnomFile"] = ""
	results["rgGuinier"] = ""
	results["rgGnom"] = ""
	results["volume"] = "0"
	results["filename"] = ""
	results["dmax"] = "0"
	return results

def storeAnalysis(client, results):
	client.service.storeDataAnalysisResultByMeasurementId(results["measurementId"], results["filename"],
		results["rg"], results["rgstdev"], results["i0"], results["i0stdev"], 0, 0,
		results["quality"], results["isagregated"], "", 0, results["gnomFile"], results["rgGuinier"], results["rgGnom"], results["dmax"], "",
		results["volume"], 0, 0, [], 2, "", "", "", "", results["densityPlot"])
def storeModels(client, results):
	damaverResults = []#assemble from results["dammaver"]
	dammifResults = [] #assemble from results["dammif"]
	client.service.storeAbInitioModels(results["measurementId"], [], damaverResults,
	dammifResults, [], results["nsdPlot"], None)
def runPipeline(filename, outputFolderName, datapath, threads, columns):
	os.system("module load edna/sas && run-sas-pipeline.py --rMaxStop 700 --rMaxIntervals 50 --data " + filename + " --threads "+ threads + " --columns " + columns + " --nxsQ " + dataPath+"q --nxsData " + dataPath + "data")

if __name__ == '__main__':

	import argparse
	parser = argparse.ArgumentParser()
	parser.add_argument("--n", "--filename", type=str, help="input filename after data reduction")
	parser.add_argument("--o", "--outputFolderName", type=str, help="output folder location")
	parser.add_argument("--d", "--detector", type=str, help="detector name")
	parser.add_argument("--m", "--measurementId", type=int, help="measurementId")
	parser.add_argument("--t", "--threads", type=int, help="number of threads to use")
	parser.add_argument("--f", "--fedid", type=str, help="fedid of user for output folder location, if folder is not specified (ignored if outputfolder name is used")
	parser.add_argument("--c", "--columns", type=int, help="number of columns to use from data file")
	args = parser.parse_args()

	if args.filename:
		filename = args.filename
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
		detector = "detector"
	if args.threads:
		threads = args.threads
	else:
		threads = 10
	if args.columns:
		columns = args.columns

	runPipeline(filename, outputFolderName, "/entry1/"+detector+"_result/",threads, columns)

	results = parseResults(outputFolderName, measurementId)
	client = createWebService()
	storeAnalysis(client, results)

