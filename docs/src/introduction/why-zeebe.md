# Why Zeebe?

There are many different worflow systems out there, why should you spend time learning about Zeebe?

**TL;DR** - Zeebe is worthwhile checking out if you need to process large volumes of transactions in near real time. It is super fast (more than 100K ops/s on a single machine) and it can scale horizontally to many machines. It replicates data for fault tolerance and high availability. It uses pub/sub to scale orchestration of hundreds of (micro) services in a resilient way. Traditional workflow systems do not provide such features at all yet they are often much harder to use suffering from feature-bloat in other areas. By comparison, Zeebe is super simple, focusing 100% on workflow and workflow only.

## Increasing Transaction Volumes

Workflow engines reliably coordinate multi-step transactions within a single application or across different applications and services. Over the last years, transaction volumes have increased exponentially with no limit in sight. There are different drivers behind this: the digitalization of more and more business processes and aspects of our lives, the globalization of economies, mobile applications, IoT and device-to-device communication to just name a few. It is clear that the same forces driving _Big Data_ are pushing _Big Transaction_.

Zeebe offers both excellent throughput on a single machine and scales horizontally to many machines. On a single server, Zeebe performs more than 100 times better (ops/s) than traditional workflow engine technology. When running on multiple machines, Zeebe functions as a peer-to-peer network, distributing data storage and computation evenly across the cluster. In a Zeebe cluster, all nodes are equal: there is no special "master" node, no single point of failure and no central choking point.

## Distribution and Microservices  

Often, Workflow engines drive transactions by orchestrating services. Lately, in the effort to scale the engineering efforts required to build ever more sophisticated systems, large monolithic applications are increasingly being broken down into distributed microservices communicating over the network. Driving many thousands of transactions per second through a network of distributed microservices poses new challenges to the resilience and efficiency of communication and interactions between the workflow engine and the services.

Zeebe applies publish-subscribe as interaction model for orchestration. A service capable of performing a certain task or step in a workflow subscribes to this task and is notified via a message when a task is available. Publish-subscribe gives a lot of control to the service: the service decides to which tasks to subscribe, when to subscribe and can even control processing rates. These properties make the overall system highly resilient, scalable and _reactive_.

## Fault Tolerance redefined

When processing critical business transactions, both the system's availability but also it's capability to prevent data loss are crucial. In the past, it was often adequate for workflow engines to store their state into databases which are  regularly backed up and are running on redundant server hardware. In the rare case of a hardware failure, a temporary downtime could be accommodated and the system could be manually restored. Today, requirements have changed. In the cloud, we do not own nor control the hardware our systems run on. Also, the more machines the system runs on, the more likely hardware failures become an the less desirable and practicable downtimes and manual restores become.

Zeebe replicates data across multiple machines to ensure availability and prevent data loss. If a machine fails or gets otherwise disconnected from the cluster, another machine which has a copy of the same data automatically takes over, ensuring that the system as a whole remains available without requiring manual action.

## Devops, SRE and the Automation of Operations

Running large distributed systems makes it impossible to administer each server manually. Over the years, new practices and approaches in operations have been established, mostly around resource abstraction, containers, container management and automation.

Zeebe is designed with these practices in mind. First, it does not need a database nor any other external system to function. It is completely self-contained and self sufficient. Second, since all nodes in the cluster are equal, it is comparatively simple to scale. This makes it  play well together with modern datacenter and cloud management systems such as [Docker](https://www.docker.com/), [Kubernetes](https://kubernetes.io/) or [DC/OS](https://dcos.io/).

What's more, the CLI (Command Line Interface) allows you to script and automate management and operations tasks.

## The new Simple

Most existing workflow systems provide many more features than Zeebe. While having many features at our disposition is generally positive, they also come at a cost. Having many features often results in higher complexity, causing poor performance and make the system hard to understand and use.

Zeebe focuses 100% on providing a small, robust and well scalable solution to workflow. Rather than covering a broad scope of features, it's goal is to be excellent within the limited scope it covers. In addition, it composes well with other systems. For example: Zeebe, provides a simple event stream API which makes it easy to stream all internal data into another system like elastic search for indexing and querying.

## Zeebe may not be right for you

Maybe your applications does not need the kind of scalability and fault tolerance provided by Zeebe. Or, you may require a large set of features around BPM (Business Process Management) which Zeebe does not offer.

In such scenarios, a traditional workflow system such as [Camunda BPM](https://camunda.org) is a much better choice.
