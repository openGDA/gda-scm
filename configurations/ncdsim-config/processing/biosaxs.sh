#!/bin/sh
# Script to run a workflow in batch

##################################################################
# Please enter the paths required to run the workflow here

# Where your workspace is located (under the File-Switch Workspace menu in the UI)
# WORKSPACE=/scratch/ws/junit-workspace

# The full path to where the model is located
# MODEL=/scratch/ws/junit-workspace/workflows/ncd_model.moml

# This script assumes that 'HOSTTYPE' is set, change or set it here if not
# HOSTTYPE=x86_64

##################################################################
# Nothing to do here...

# /dls_sw/apps/DawnDiamond/master/builds-stable/stable-linux64/dawn -noSplash -application com.isencia.passerelle.workbench.model.launch -data $WORKSPACE -consolelog -os linux -ws gtk -arch $HOSTTYPE -vmargs -Dorg.dawb.workbench.jmx.headless=true -Dcom.isencia.jmx.service.terminate=false -Dmodel=$MODEL -Dxml.path=/scratch/ws/gda836_git/scisoft-ncd.git/uk.ac.diamond.scisoft.ncd.actors/test/uk/ac/diamond/scisoft/ncd/actors/test/ncd_configuration.xml -Draw.path=/scratch/ws/gda836_git/scisoft-ncd.git/uk.ac.diamond.scisoft.ncd.actors/test/uk/ac/diamond/scisoft/ncd/actors/test/i22-34820.nxs -Dpersistence.path=/scratch/ws/gda836_git/scisoft-ncd.git/uk.ac.diamond.scisoft.ncd.actors/test/uk/ac/diamond/scisoft/ncd/actors/test/persistence_file.nxs -Doutput.path=/scratch/ws/junit-workspace/workflows/output

echo "$0 running!" | wall
