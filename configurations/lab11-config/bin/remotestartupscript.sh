#! /bin/bash

export BEAMLINE=i22
. /usr/share/Modules/init/bash
module load java/gda

mv -f nohup.out nohup.out.0 || true
touch nohup.out

( 
umask 2
/scratch/gda/bin/gda --config=/scratch/gda/config --stop logserver || true
nohup /scratch/gda/bin/gda --config=/scratch/gda/config nameserver 
nohup /scratch/gda/bin/gda --config=/scratch/gda/config eventserver 
nohup /scratch/gda/bin/gda --config=/scratch/gda/config logserver 
nohup /scratch/gda/bin/gda --config=/scratch/gda/config --properties=/scratch/gda/config/properties/java.properties.clientlogserver logserver 
JAVA_OPTS="-Xms1024m -Xmx8192m -XX:PermSize=256m -XX:MaxPermSize=512m" nohup /scratch/gda/bin/gda --config=/scratch/gda/config --debug --verbose objectserver 
) &

cat >> /scratch/gda/logs/gda_server.log <<EOF

gda server restart

EOF

## show log until 'Server initialisation complete' is seen
PIP=/tmp/`basename $0`-$$
mknod $PIP p
tail -n 1 -f /scratch/gda/logs/gda_server.log >  $PIP &
awk '{
        if (!/DEBUG/) print ;
        if (/gda.util.ObjectServer.* Server initialisation complete./) {
                print "\nAll done, you can start the client now\n" ;
                exit ;
        }
}' < $PIP
rm $PIP

sleep 6
