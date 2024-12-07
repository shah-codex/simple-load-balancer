#!/usr/bin/bash

## Opening up the pipe and
## Initializing all the file descriptors
fd_cnt=10; \
total_fd=$((fd_cnt+500)); \
for ((i=$fd_cnt;i<$total_fd;i++)); \
do \
  base_dir="/tmp/load_balance/client$i"; \
  queue="$base_dir/fifo"; \
  output="$base_dir/output"; \
\
  mkdir -p $base_dir; \
  mkfifo $queue; \
  touch $output; \
\
	nc localhost 9000 > $output < $queue & \
	exec {fd_cnt}>"${queue}"; \
	((fd_cnt++));
done; \


## Testing the connections with the bulk messaging
## firing against the load balancer.
for ((i=10;i<100;i++)); \
do { \
	curr_fd=$i; \
	for ((j=0;j<100;j++)); \
	do \
		echo "Client $i, Message $j" >&$curr_fd; \
	done; \
} & \
done; \


## Remove all the file descriptor.
for ((i=10;i<100;i++)); \
do \
	exec {i}>&-; \
done; \


## Remove all the temp directory created.
rm -r /tmp/load_balance;

## To Delete all the nc process that are still
## alive in the background
kill `ps aux | awk '{ if ($11 == "nc") print $2 }'`