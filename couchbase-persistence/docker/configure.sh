#!/bin/bash

USERNAME=admin
PASSWORD=admin1
CLUSTER_NAME=akka
BUCKET=akka

# Enables job control
set -m

# Enables error propagation
set -e

# Run the server and send it to the background
/entrypoint.sh couchbase-server &

# Check if couchbase server is up
check_db() {
  curl --silent http://127.0.0.1:8091/pools > /dev/null
  echo $?
}

# Variable used in echo
i=1
# Echo with
log() {
  echo "[$i] [$(date +"%T")] $@"
  i=`expr $i + 1`
}

# Wait until it's ready
until [[ $(check_db) = 0 ]]; do
  >&2 log "Waiting for Couchbase Server to be available ..."
  sleep 1
done

# Setup index and memory quota
log "$(date +"%T") Init cluster ........."
couchbase-cli cluster-init -c 127.0.0.1 --cluster-username $USERNAME --cluster-password $PASSWORD \
  --cluster-name $CLUSTER_NAME --cluster-ramsize 256 --cluster-index-ramsize 256 --services data,index,query,fts \
  --index-storage-setting default

# Create the bucket
log "$(date +"%T") Create buckets ........."
couchbase-cli bucket-create -c 127.0.0.1 --username $USERNAME --password $PASSWORD --bucket-type couchbase \
  --bucket-ramsize 100 --bucket $BUCKET --enable-flush 1

# Need to wait until query service is ready to process N1QL queries
log "$(date +"%T") Waiting ........."
sleep 20 #TODO: how to check if it's ready to process N1QL queries

# Create indexes
log "Create bucket indices ........."
cbq -u $USERNAME -p $PASSWORD -s "CREATE INDEX \`persistence-ids\` on \`akka\` (\`persistence_id\`) WHERE \`type\` = \"journal_message\""
cbq -u $USERNAME -p $PASSWORD -s "CREATE INDEX \`sequence-nrs\` on \`akka\` (DISTINCT ARRAY m.sequence_nr FOR m in messages END) WHERE \`type\` = \"journal_message\""
cbq -u $USERNAME -p $PASSWORD -s "CREATE INDEX \`tags\` ON \`akka\`((ALL (ARRAY (ALL (ARRAY [\`t\`.\`tag\`, \`m\`.\`ordering\`] FOR \`t\` IN (\`m\`.\`tags\`) END)) FOR \`m\` IN \`messages\` END))) WHERE (\`type\` = \"journal_message\")"
cbq -u $USERNAME -p $PASSWORD -s "CREATE INDEX \`tag-seq-nrs\` ON \`akka\`((ALL (ARRAY (ALL (ARRAY [\`persistence_id\`, \`t\`.\`tag\`, \`t\`.\`seq_nr\`] FOR \`t\` IN \`m\`.\`tags\` END)) FOR \`m\` IN \`messages\` END))) WHERE (\`type\` = \"journal_message\")"
cbq -u $USERNAME -p $PASSWORD -s "CREATE INDEX \`snapshots\` ON \`akka\` (persistence_id, sequence_nr) WHERE akka.type = \"snapshot\""

fg 1