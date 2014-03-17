#! /bin/bash

. /usr/share/Modules/init/bash

module load global/cluster

# those two need to be in sync
#DAWN=/dls_sw/apps/DawnDiamond/1.4.1/builds-stable/stable-linux64/dawn
DAWN=/dls/b21/data/2014/cm4976-1/tmp/DawnDiamond-1.5.0.v20140314-1320-linux64/dawn
MOML=/home/zjt21856/biosaxsred/ncd_model.moml

# those would be found in the environment
# beamline staff specified
#PERSISTENCEFILE=/home/zjt21856/persistence_file.nxs
#NCDREDXML=/home/zjt21856/ncd_configuration.xml

# these should be on the command line
#DATAFILE=/home/zjt21856/i22-34820.nxs
#BACKGROUNDFILE=/home/zjt21856/i22-34820.nxs
DATAFILE="$1"
BACKGROUNDFILE="$2"
DATACOLLID="$3"

ISPYBUPDATE=$(dirname $0)/updateispyb.py

REDUCTIONOUTPUTFILE=

echo edited file
#is file in visit
read VISIT RESTOFPATH <<<$( echo $DATAFILE | sed 's,\(/dls/.../data/20../[-a-z0-9]*\)/\(.*\),\1 \2,')
RESTOFPATH=${RESTOFPATH%.*}
echo $RESTOFPATH
if test -d $VISIT ; then
	echo running reduction in visit based directory under $VISIT
	if test -w $VISIT ; then
		REDUCTIONOUTPUTFILE=${VISIT}/processed/${RESTOFPATH}.reduced.nxs
		mkdir -p $(dirname $REDUCTIONOUTPUTFILE)
	else 
		REDUCTIONOUTPUTFILE=${VISIT}/processing/${RESTOFPATH}.reduced.nxs
		mkdir -p $(dirname $REDUCTIONOUTPUTFILE)
		REDUCTIONOUTPUTFILE=
	fi
	TMPDIR=${VISIT}/tmp/${RESTOFPATH}.$$
else
	echo running reduction outside of a visit
	TMPDIR=${DATAFILE}.$$
fi

mkdir -p $TMPDIR
cd $TMPDIR
echo now in $TMPDIR

WORKSPACE=$TMPDIR/workspace
mkdir $WORKSPACE
#cd $WORKSPACE
#tar xvzf /home/zjt21856/ws836_git/gda-scm.git/configurations/ncdsim-config/processing/dw.tar.gz
#cd ..
OUTPUTDIR=$TMPDIR/output
mkdir $OUTPUTDIR

sed "s,bgFile>.*</bgFile,bgFile>${BACKGROUNDFILE}</bgFile," < $NCDREDXML > ncd_reduction.xml
NCDREDXML=${TMPDIR}/ncd_reduction.xml

mkdir ${WORKSPACE}/workflows/
WORKSPACEMOML=${WORKSPACE}/workflows/reduction.moml
ln -s $MOML $WORKSPACEMOML

# /dls_sw/apps/DawnDiamond/master/builds-stable/stable-linux64/dawn -noSplash -application com.isencia.passerelle.workbench.model.launch -data $WORKSPACE -consolelog -os linux -ws gtk -arch $HOSTTYPE -vmargs -Dorg.dawb.workbench.jmx.headless=true -Dcom.isencia.jmx.service.terminate=false -Dmodel=$MODEL -Dxml.path=/scratch/ws/gda836_git/scisoft-ncd.git/uk.ac.diamond.scisoft.ncd.actors/test/uk/ac/diamond/scisoft/ncd/actors/test/ncd_configuration.xml -Draw.path=/scratch/ws/gda836_git/scisoft-ncd.git/uk.ac.diamond.scisoft.ncd.actors/test/uk/ac/diamond/scisoft/ncd/actors/test/i22-34820.nxs -Dpersistence.path=/scratch/ws/gda836_git/scisoft-ncd.git/uk.ac.diamond.scisoft.ncd.actors/test/uk/ac/diamond/scisoft/ncd/actors/test/persistence_file.nxs -Doutput.path=/scratch/ws/junit-workspace/workflows/output
#-data /tmp/foo \

SCRIPT=$TMPDIR/qsub.script
cat >> $SCRIPT <<EOF
#! /bin/sh

## set data reduction to started
$ISPYBUPDATE reduction $DATACOLLID STARTED ""

$DAWN -noSplash -application com.isencia.passerelle.workbench.model.launch \
-data $WORKSPACE \
-consolelog -os linux -ws gtk -arch $(arch) -vmargs \
-Dorg.dawb.workbench.jmx.headless=true \
-Dcom.isencia.jmx.service.terminate=false \
-Dmodel=$WORKSPACEMOML \
-Dlog.folder=$TMPDIR \
-Dxml.path=$NCDREDXML \
-Draw.path=$DATAFILE \
-Dpersistence.path=$PERSISTENCEFILE \
-Doutput.path=$OUTPUTDIR

for i in $OUTPUTDIR/results*.nxs ; do
	GENERATEDFILE=\$i
	break;
done

if test -n "\$GENERATEDFILE" && test -r \$GENERATEDFILE ; then
 : # all fine, but tell ISPyB later
else 
	# raise ISPyB error and exit
	MESSAGE="ERROR cannot find generated reduction file for collection $DATACOLLID in $OUTPUTDIR"
	$ISPYBUPDATE reduction $DATACOLLID FAILED $MESSAGE 
	echo $MESSAGE  >&2
	echo ABORTING. >&2
	exit 1
fi

if test -n "$REDUCTIONOUTPUTFILE" ; then 
	ln \$GENERATEDFILE $REDUCTIONOUTPUTFILE
	REDUCEDFILE="$REDUCTIONOUTPUTFILE"
else
	REDUCEDFILE=\$GENERATEDFILE
fi

# tell ispyb reduction worked and result is in \$REDUCEDFILE
$ISPYBUPDATE reduction $DATACOLLID COMPLETE \$REDUCEDFILE

module load edna/sas-local  ## we are on the cluster

## set analysis status started 
$ISPYBUPDATE analysis $DATACOLLID STARTED ""
## run edna 
run-sas-pipeline.py --data \$REDUCEDFILE --nxsQ '/entry1/detector_result/q' --nxsData '/entry1/detector_result/data' --rMaxStart 50 --rMaxStop 600 --rMaxIntervals 25 --rMaxAbsTol 0.1 --mode fast --threads 10 --columns 10 --symmetry P6 --qmin 0.005 --qmax 0.3 --plotFit

## update ispyb
EOF

bash $SCRIPT
#qsub $SCRIPT
