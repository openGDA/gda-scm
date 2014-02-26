from runAnalysisAndPutIntoDatabase import *
outputFolderName = "/dls/tmp/rbv51579"
results, folder = parseResults(outputFolderName, 1294)
client = createWebService()
(model, dammifModel, damminModel) = createModels(folder, results)
storeAnalysis(client, results)
storeModels(client, model, dammifModel, damminModel, results)
