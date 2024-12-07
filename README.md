# Load Balancer with RoundRobin and WeightedRoundRobin Algorithms

This project implements a high-performance **load balancer** using **RoundRobin** and **WeightedRoundRobin** load balancing algorithms. Built with **Java**, it leverages **NIO2 (Non-blocking I/O)** for scalability and efficiency in handling multiple concurrent connections. Additionally, the project demonstrates the use of **zero-copy techniques** using **ByteBuffer direct memory allocation** to optimize data transfer.

The load balancer supports **bi-directional asynchronous communication** to facilitate efficient, low-latency interactions between multiple clients and backend servers. For testing, the project includes Java-based stubs that simulate the behavior of clients and servers using **IO Multiplexing** techniques, which allow handling multiple I/O operations concurrently.

## Screenshot
![image](https://github.com/user-attachments/assets/157d5047-51e0-48da-bbfb-be1f1ad7b681)


## Key Features

- **RoundRobin Load Balancing**: Distributes incoming traffic across all backend servers in a circular manner, ensuring a simple and effective way to balance the load.

- **WeightedRoundRobin Load Balancing**: Distributes traffic according to predefined weights assigned to each server. Servers with higher weights receive a greater proportion of the traffic, making this approach suitable for environments with varying server capabilities.

- **Non-blocking Asynchronous I/O (NIO2)**: Uses Java's **NIO2 (New I/O)** framework for non-blocking asynchronous I/O operations. This enables the load balancer to handle thousands of concurrent connections efficiently without blocking threads.

- **Zero-copy Data Transfer**: By using **ByteBuffer** with **direct memory allocation**, the project minimizes memory copy operations, allowing data to be transferred directly from the network buffer to the destination buffer, improving performance.

- **Bi-directional Communication**: The load balancer supports both inbound and outbound asynchronous communication, allowing it to handle full-duplex communication with multiple clients and backend servers.

- **IO Multiplexing for Testing**: The load balancer's functionality is tested using IO multiplexing, where multiple connections are managed simultaneously. This approach allows simulating real-world scenarios where many clients connect concurrently to the system.

- **Iterator Design Pattern**: The project uses the **Iterator Design Pattern** to abstract the selection mechanism for load balancing algorithms. With this design, the load balancer dynamically chooses between **RoundRobin** or **WeightedRoundRobin** algorithms by implementing the `ServerIterator` interface.

## Technologies Used

- **Java** (with NIO2 for Asynchronous I/O)
- **ByteBuffer** (Direct Memory Allocation for Zero-copy)
- **RoundRobin** and **WeightedRoundRobin** Load Balancing Algorithms
- **IO Multiplexing** (to handle multiple connections)
- **Asynchronous Communication** (for bi-directional data transfer)
- **Iterator Design Pattern** (for dynamically switching between load balancing strategies)

## Iterator Design Pattern

To decouple the logic for the load balancing algorithms and make the system extensible, the **Iterator Design Pattern** is used to abstract the selection of the algorithm. This allows the load balancer to dynamically switch between the **RoundRobin** and **WeightedRoundRobin** load balancing algorithms using a common abstract class.

The `ServerIterator` abstract class defines the behavior for traversing through available backend servers and selecting the next one based on the current load balancing strategy. The two concrete iterators, `RoundRobinIterator` and `WeightedRoundRobinIterator`, extends this abstract class with their own algorithm-specific logic.

### ServerIterator abstract class

```java
public abstract class ServerIterator {
    protected List<Server> servers;

    public ServerIterator(List<Server> servers) {
        this.servers = servers;
    }

    public abstract Server getNextServer();
}
```

## Benefits

- **Scalability**: By using asynchronous I/O, the load balancer is capable of handling a large number of simultaneous connections efficiently, making it ideal for high-traffic environments.

- **Improved Performance**: Zero-copy techniques reduce memory overhead and improve throughput, making the system faster and more efficient.

- **Flexible Load Distribution**: With support for both RoundRobin and WeightedRoundRobin algorithms, the load balancer can adapt to various server configurations and traffic distribution needs.

- **Iterator Design Pattern**: The use of the Iterator design pattern allows for flexibility in choosing the load balancing strategy and makes it easier to add new algorithms in the future.

- **High Availability**: The load balancer distributes traffic evenly or weighted across multiple servers, reducing the risk of overloading a single server and ensuring higher availability.

## Example Workflow

1. **Client Request**: A client sends a request to the load balancer.
2. **Load Balancer Decision**: The load balancer applies the selected load balancing algorithm (RoundRobin or WeightedRoundRobin) to decide which backend server should handle the request.
3. **Forwarding the Request**: The request is forwarded to the selected backend server.
4. **Server Response**: The backend server processes the request and sends the response back to the load balancer.
5. **Sending Response to Client**: The load balancer forwards the server response to the client asynchronously.

## Configuration

- **Load Balancing Algorithm**: Choose between `RoundRobin` or `WeightedRoundRobin`.
- **Server Weights**: If using `WeightedRoundRobin`, assign weights to servers.
- **Backend Server Addresses**: Specify the IP addresses and ports of the backend servers.
- **Port**: Configure the listening port for the load balancer.

## Testing

Testing is a crucial part of the development process, and this project includes Java-based test stubs to simulate client-server interactions. The test stubs utilize **IO Multiplexing**, where multiple client connections can be handled in a non-blocking, asynchronous manner, simulating real-world traffic and load conditions.

### Load Testing: Simulating Multiple Concurrent Connections

To simulate a large number of concurrent connections and stress-test the load balancer, we use a bash script that opens multiple pipes, creates background processes using `nc` (Netcat), and sends bulk requests through the pipes to the load balancer. This allows us to verify how the load balancer handles many concurrent connections and ensures that traffic is distributed effectively across backend servers.

#### Steps for Load Testing

##### 1. Opening the Pipe and Initializing File Descriptors

```bash
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
done;
```

##### 2. Firing Bulk Messages to the Load Balancer
```bash
for ((i=10;i<510;i++)); \
do { \
  curr_fd=$i; \
  for ((j=0;j<100;j++)); \
  do \
    echo "Client $i, Message $j" >&$curr_fd; \
  done; \
} & \
done;
```

##### 3. Cleaning Up File Descriptors
```bash
for ((i=10;i<510;i++)); \
do \
  exec {i}>&-; \
done;
```

##### 4. Removing Temperory File and Directories
```bash
rm -r /tmp/load_balance;
```

##### 5. Killing Background `nc` Processes
```bash
kill `ps aux | awk '{ if ($11 == "nc") print $2 }'`
```

#### Monitoring During the Test
- **CPU and Memory Usage**: During the load test, monitor the system's resource consumption (CPU, memory, etc.) to ensure the load balancer and backend servers can handle the simulated traffic without issues.
- **Logs**: Review any logs generated by the load balancer for errors or warnings that may indicate issues under load.
- **Output Files**: Check the output files generated by each client to confirm that all responses are received correctly.

### Running Tests

1. Start the load balancer instance by running the compiled `Main` class.
2. Execute the test stubs to simulate multiple clients connecting to the load balancer.
3. Observe the load balancer's ability to distribute traffic based on the chosen algorithm and handle multiple simultaneous connections.

## Zero-Copy Illustration

The load balancer optimizes data transfer by utilizing **direct memory buffers** through **ByteBuffer**. This eliminates unnecessary copying between user space and kernel space. Here's a simple code example that demonstrates direct memory allocation:

```java
ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
// Now, this buffer can be used for network I/O operations without the need for intermediate memory copies.

