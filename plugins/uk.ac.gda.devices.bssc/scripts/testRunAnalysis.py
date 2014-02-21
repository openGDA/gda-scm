from runAnalysisAndPutIntoDatabase import *

results = parseResults("/dls/tmp/rbv51579", 1)
client = createWebService()
storeAnalysis(client, results)
