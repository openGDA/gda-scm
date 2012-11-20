from gdascripts.analysis.datasetprocessor.oned.GaussianPeaksInHole import GaussianPeaksInHole


xds = DataSet.array([ 0, 0.0379747, 0.0759494, 0.113924, 0.151899, 0.189873, 0.227848, 0.265823, 0.303797, 0.341772, 0.379747, 0.417722, 0.455696, 0.493671, 0.531646, 0.56962, 0.607595, 0.64557, 0.683544, 0.721519, 0.759494, 0.797468, 0.835443, 0.873418, 0.911392, 0.949367, 0.987342, 1.02532, 1.06329, 1.10127, 1.13924, 1.17722, 1.21519, 1.25316, 1.29114, 1.32911, 1.36709, 1.40506, 1.44304, 1.48101, 1.51899, 1.55696, 1.59494, 1.63291, 1.67089, 1.70886, 1.74684, 1.78481, 1.82278, 1.86076, 1.89873, 1.93671, 1.97468, 2.01266, 2.05063, 2.08861, 2.12658, 2.16456, 2.20253, 2.24051, 2.27848, 2.31646, 2.35443, 2.39241, 2.43038, 2.46835, 2.50633, 2.5443, 2.58228, 2.62025, 2.65823, 2.6962, 2.73418, 2.77215, 2.81013, 2.8481, 2.88608, 2.92405, 2.96203, 3 ])
yds = DataSet.array([ 7.96904, 7.83857, 7.8081, 7.97763, 8.014716, 8.031291, 8.05157, 8.19022, 8.09005, 7.680938, 1.89341, 1.77094, 1.75247, 1.734, 1.74348, 1.76058, 1.77769, 1.85017, 1.94299, 1.98262, 1.9446, 3.45257, 3.75871, 2.40293, 1.99392, 1.97456, 1.9552, 2.01992, 2.74518, 2.18621, 3.28968, 3.49369, 2.81022, 2.18995, 1.78282, 1.82886, 1.8749, 3.0462, 2.87688, 2.36283, 2.04244, 1.89971, 1.95806, 2.0164, 2.08537, 2.90781, 2.86168, 2.65216, 2.26415, 2.02656, 1.88511, 1.81856, 1.75502, 1.72322, 2.1077, 2.87167, 2.79636, 2.4295, 2.10355, 1.99098, 1.92903, 1.87857, 1.84643, 1.82499, 1.8038, 1.80311, 5.95309, 8.08113, 8.12717, 8.14649, 8.10432, 8.06215, 8.01998, 7.97782, 7.93712, 7.91865, 7.90018, 7.8817, 7.86323, 7.84476 ])
p = GaussianPeaksInHole()
p.plotPanel = "Plot 1"

print p._process(xds,-1.0*yds)