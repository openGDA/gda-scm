#! /bin/bash

export BEAMLINE=b22
. /usr/share/Modules/init/bash
module load java/gda

mv -f nohup.out nohup.out.0 || true
touch nohup.out

( 
umask 2
/dls_sw/$BEAMLINE/software/gda/bin/gda --stop logserver || true
nohup /dls_sw/$BEAMLINE/software/gda/bin/gda nameserver 
nohup /dls_sw/$BEAMLINE/software/gda/bin/gda eventserver 
nohup /dls_sw/$BEAMLINE/software/gda/bin/gda logserver 
nohup /dls_sw/$BEAMLINE/software/gda/bin/gda --properties=/dls_sw/$BEAMLINE/software/gda/config/properties/java.properties.clientlogserver logserver 
JAVA_OPTS="-Xms1024m -Xmx8192m -XX:PermSize=256m -XX:MaxPermSize=512m" nohup /dls_sw/$BEAMLINE/software/gda/bin/gda --debug --verbose objectserver 
) &

cat >> /dls/$BEAMLINE/logs/gda_server.log <<EOF

gda server restart

EOF

## show log until 'Server initialisation complete' is seen
PIP=/tmp/`basename $0`-$$
mknod $PIP p
tail -n 1 -f /dls/$BEAMLINE/logs/gda_server.log >  $PIP &
awk '{
        if (!/DEBUG/) print ;
        if (/gda.util.ObjectServer - Server initialisation complete.*b22_server_beans/) {
                print "\nAll done, you can start the client now\n" ;
                exit ;
        }
}' < $PIP
rm $PIP

sleep 6
