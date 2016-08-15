#!/bin/bash

if [ "$1" = "stable" ]; then
  echo "Publish to STABLE repository."
  scp -r target/repository/* daybuild@www.rascal-mpl.org:/srv/www/vhosts/www.rascal-mpl.org/www/updates
elif [ "$1" = "unstable" ]; then
  echo "Publish to UNSTABLE repository."
  scp -r target/repository/* daybuild@www.rascal-mpl.org:/srv/www/vhosts/www.rascal-mpl.org/www/unstable-updates
else
  echo "Did not publish artifacts."
  echo "USAGE: publish.sh [stable | unstable]"
fi