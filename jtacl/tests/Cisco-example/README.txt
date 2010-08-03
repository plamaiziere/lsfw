A test and demonstration example using one PIX525 firewall and
one router 7204.

See network.dia for a drawing of the network (Dia format).

Quick Howto:

The configurations



Launch jtacl using the jar with depencies:
(you can define a shell alias to save time)

java -jar /path-to-jar/jtacl-VERSION-jar-with-dependencies.jar -f jtacl.xml

$ cd tests/Cisco-example
$ java -jar /path-to-jar/jtacl-VERSION-jar-with-dependencies.jar -f jtacl.xml
> topology
topology
10.0.1.0/24 {R1(interfaceFastEthernet1/1 - 10.0.1.254)}
10.0.2.0/24 {R1(interfaceFastEthernet1/2 - 10.0.2.254)}
10.0.3.0/24 {R2(FastEthernet1/0 - 10.0.3.254), R1(FastEthernet1/0 - 10.0.3.254)}
192.168.0.0/24 {R2(interfaceFastEthernet1/1 - 192.168.0.254)}
192.168.1.0/24 {R2(interfaceFastEthernet1/2 - 192.168.1.254)}
192.168.2.0/24 {R2(interfaceFastEthernet1/3 - 192.168.2.254)}

The topology describes the links beetween network equipments (here R1 and R2).
The first column is the network address of the link, after come the equipments
linked by the link.

By example:
10.0.1.0/24 {R1(interfaceFastEthernet1/1 - 10.0.1.254)}

