from mpl_toolkits.mplot3d import Axes3D
import numpy as np
from matplotlib import cm
import matplotlib
matplotlib.use('Agg')
import sys
import os

def create3Plots(reducedfile, output):
	if reducedfile == None:
		print "reducedfile must be defined"
		sys.exit(1)

	datapath="/entry1/detector_processing/Normalisation/data"
	errorpath="/entry1/detector_processing/Normalisation/errors"
	qpath = "/entry1/detector_result/q"
	qerrorpath="/entry1/detector_result/q_errors"

	#set up plot
	import matplotlib.pyplot as plt

	#set up data
	import h5py
	f=h5py.File(reducedfile, 'r')
	reducedDataArray = f[datapath]
	numberOfDatasets = len(reducedDataArray[0])
	errorDataArray = f[errorpath]
	qArray=f[qpath]
	qErrorArray=f[qerrorpath]
	filenamesToReturn = []
	for i in (0, numberOfDatasets/2, numberOfDatasets-1):
		fig = plt.figure(figsize=(6, 5))
		plt.title("Plot for curve " + str(i+1) + " logI vs. q")
		ax = fig.add_subplot(1,1,1)
		ax.semilogy()
		ax.errorbar(qArray, reducedDataArray[0][i], yerr=errorDataArray[0][i], )
		ax.set_xlabel(u"q (1/A)")
		ax.set_ylabel('log I')
		filename = os.path.join(output+ str(i)+ "plot.png")
		thumbnailFilename = os.path.join(output+ str(i)+ "plott.png")
		fig.savefig(filename)
		fig.set_figheight(2.28)
		fig.set_figwidth(1.79)
		fig.savefig(thumbnailFilename)
		fig.clf()
		filenamesToReturn.append(filename)
		import subprocess
		subprocess.call(["setfacl", "-m", "user:vxn01537:r-x", filename])
		subprocess.call(["setfacl", "-m", "user:vxn01537:r-x", thumbnailFilename])
	return filenamesToReturn
