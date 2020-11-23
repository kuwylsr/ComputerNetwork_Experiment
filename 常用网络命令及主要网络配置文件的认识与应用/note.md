# 常用网络命令及网络配置文件的认识与应用

## 常用网络命令

### Ping (Packet Inter-Net Groper) 分组网间探测

- **作用**
校验与远程计算机或本地计算机的连接.

- **工作原理**
发送方向接收方发送"互联网控制报文协议(ICMP)" 回显(echo) 请求消息, 接收方将对该回显请求进行自动回显应答来验证两台计算机之间的IP层连接.

- **Ping 命令格式**

    ~~~shell
    Ping [-t] [-a] [-n count] [-l size] [-f] [-i TTL] [-v TOS] [-r count] [-s count] [[-j host-list] | [-k host-list]] [-w timeout] target_name
    ~~~

- **命令参数详解**
    - **-t** 校验与指定计算机的连接, 知道用户中断
    - **-a** 地址解析为计算机名
    - **-n** count 发送由count指定数量的 ECHO 报文. 默认值为4
    - **-l** size 发送包含由size指定大小的 ECHO 数据包.
    - **-f** 指定发送的回显请求消息带有"不分段"标志. 这样数据包就不会被路由器上的网关分段.
    - **-i** TTL 将"生存时间"字段设置为TTL指定的数值.
    - **-v** TOS 将"服务类型" 字段设置为 TOS 指定的数值.
    - **-r** count 在"记录路由" 字段中记录发出报文和返回报文的路由.
    - **-s** count 指定由 count 指定的转发次数的时间戳.
    - **-w** timeout 指定超时间隔 (ms)
    - **TargetName** 指定要校验连接的远程计算机.

- **常见出错信息**
    - Unknown host
    远程主机的名字不能被命名服务器转换成IP地址
    原因: 命名服务器故障; 名字错误; 通信线路故障.

    - Network unreachable 网络不可达
    原因: 本地主机没有到达远程主机的路由

    - No answer 无响应
    说明本地主机有一条到达远程主机的路由但接收不到该远程主机返回的任何报文.
    原因: 远程主机不工作; 本地或远程主机配置错误; 线路故障; 本地或远程路由器没工作等.

    - Timed out 超时
    原因: 路由器不能通过; 链接有问题; 远程主机关闭

### Tracert (TraceRoute)

- **作用**
TCP/IP 网络中的一个路由跟踪程序, 用来显示数据包到达目标主机所经过的路径, 并显示到达每个节点的时间.

- **原理**
通过向目标主机发送不同IP生存时间的ICMP回应数据包来确定到达目标主机所采取的路由.

- exp
源主机发送的第一个数据包的TTL 设为1, 第二个为2, 第三个为3,等等. 每当路由器收到一个数据包, 都会将其TTL值减1. 这样一来, 当第n个数据包到达了第n个路由器时, 第n个路由器发现该数据包的TTL已经过期了. 根据IP协议的规则, 路由器将该数据包丢弃并将一个 "ICMP已超时"送回源主机.

- 命令格式

    ~~~shell
    Tracert [-d] [-h maxumum_hops] [-j host-list] [-w timeout] target_name
    ~~~

- 命令参数详解

    - **-d** 防止将中间路由器IP地址解析为计算机名

    - **-h** maximum_hops 指定在搜索目标的路径中跃点的最大数

    - **-w** timeout 指定应答需要等待的时间, 即制定等待 "ICMP已超时" 或 "回显回答" 消息的时间

    - **Target_name** 目标主机名称或IP地址.


### netstat

- **作用**
显示网络连接, 路由表和网络接口信息, 让用户了解当前状态下有哪些网络连接正常运作.

- **命令格式**
    ~~~shell
    Netstat [-a] [-e] [-n] [-o] [-p protocol] [-r] [-s] [interval]
    ~~~

- **命令参数详解**
    - **-a** 显示所有有限连接信息列表(ESTABLISHED, 监听连接请求的那些连接LISTENING, 计算机侦听的TCP和UDP端口)

    - **-e** 显示以太网统计数据(数据报数量, 总字节数, 错误数, 广播的数量等)

    - **-n** 显示已建立的有效TCP连接, 以数字形式显示地址和端口信息.

    - **-o** 显示与每个连接相关的所属进行ID

    - **-p** protocol 显示由 protocol (tcp,udp,icmp,ip)所指定的协议的连接情况, 若与-s一起使用, 额按协议显示统计信息.

    - **-r** 显示路由表信息

    - **-s** 按照不同协议显示其统计的信息

    - **-v** 显示正在进行的工作

    - Interval 每隔 interval 秒重新显示一次选定的信息.

### ARP(Address Resolution Protocol) 地址转换协议

- **作用**
TCP/IP 网络中用于将IP地址映射为网卡物理地址(MAC地址)的一个协议

- **命令格式**
    ~~~shell
    ARP –s inet_addr eth_addr[if_addr]
    ARP –d inet_addr [if_addr]
    ARP –a [inet_addr] [-N if_addr]
    ~~~

- **命令参数详解**

    - **–s inet_addr eth_addr[if_addr]**
    向ARP缓存添加可以将IP地址inet_addr解析为物理地址eth_addr的静态表项

    - **–d inet_addr [if_addr]**
    删除指定的IP地址项，此处inet_addr代表IP地址，对于指定的端口要删除表中的某项，使用if_addr参数

    - **–a [inet_addr] [-N if_addr]**
    显示所有端口的当前ARP缓存表。

### ipconfig

- **作用**
显示本机所有当前的 TCP/IP 网络配置值, 刷新动态主机配置协议(DHCP) 和 域名系统(DNS)设置. 使用不带参数的ipconfig 可以显示所有适配器的 IP 地址, 子网掩码, 默认网关.

- **命令格式**
    ~~~shell
    Ipconfig [/all | /renew [adapter] | /release [adapter] | /flushdns | /displaydns | /registerdns| /showclassid Adapter| /setclassid Adapter]
    ~~~

- **命令参数详解**
    - **/all** 显示所有适配器的完整TCP/IP配置信息，若无此参数则只显示IP地址，子网掩码和各个适配器默认网关值。
    - **/renew** [adapter] 更新所有适配器DHCP配置参数

    - **release** [adapter] 发送Dhcprelease消息到DHCP服务器，以释放当前DHCP配置并丢弃IP地址配置。

    - **/flushdns** 清理并重设DNS客户解析器缓存内容

    - **/displaydns** 显示DNS客户解析器缓存内容，包括从本地主机文件预装载的记录以及由域名解析获得的任何资源记录。

    - **/registerdns** 初始化计算机上配置的DNS名称和IP地址的手工动态注册
    - **/showclassid** Adapter 显示指定适配器的DHCP类别ID
    - **/setclassid** Adapter 配置特定适配器的DHCP类别ID

## 网络配置文件

### HOSTS 文件

- **作用**
实现域名本地解析. 我们上网时会首选搜索Hosts这个文件是否有相关的解析, 如果没有才会通过网络服务的服务器进行解析.

- **工作原理**
在进行DNS请求之前, Windows系统回显检查自己的Hosts文件中是否有这个地址的映射关系,如果有, 泽调用这个IP地址映射, 如果没有再向已知的DNS服务器提出域名解析.

- 存放地址
C:\WINDOWS\system32\drivers\etc

- **作用**
    - 加快域名解析
    - 方便局域网用户
    - 屏蔽网站
    - 顺利连接系统

### LMHOSTS 文件

- **作用**
进行 NETBIOS 名静态解析的. 将 NETBIOS名和IP地址对应起来, 功能类似于 DNS, 只不过DNS是将 域名/主机名和IP对应. 解决广播方式无法跨越路由器的局限的, LMHOSTS文件与广播方式相辅相成实现网段内外的全通信.

- LMHOSTS 文件的部分内容
    ~~~vim
    #   
    # The following example illustrates all of these extensions:
    #
    # 102.54.94.97 rhino #PRE #DOM:networking #net group's DC
    # 102.54.94.102 "appname \0x14" #special app server
    # 102.54.94.123 popular #PRE #source server
    # 102.54.94.117 localsrv #PRE #needed for the include
    #
    # #BEGIN_ALTERNATE
    # #INCLUDE \\localsrv\public\lmhosts
    # #INCLUDE \\rhino\public\lmhosts
    # #END_ALTERNATE
    #
    # end of this file.
    ~~~

    - 与HOSTS的不同点: LMHOSTS文件中可以指定执行某种特殊功能的特定的命令.
    - #PRE: 这个命令放在IP地址和名称后面, 表示当系统启动时, 先将这个地址预先载入(Preload)到Cache(内存高速缓存)中.
    - #DOM: domain_name 这个命令放在计算机 NetBIOS名字之后, 表明它是一个域控制器(Domain Controller)
    - #INCLUDE<Filename> 这个命令表明将 filename 所指向的另一个LMHOSTS文件加入本文件中, 系统从改文件中读取IP地址和NetBIOS名字.
    - #BEGIN_ALTERNATE 和 #END_ALTERNATE 命令 必须配套使用, 它们的作用是可以在一个LMHOSTS文件中指定多个 #INCLUDE 命令.


- **工作过程**
    - 系统启动时将LMHOSTS文件中被设为 #PRE 的地址和及其名预载到内存中.
    - 客户端发出查询请求时, 先在Cache中检查是否已存在目的IP地址.
    - 如果在 Cache 中没有找到, 系统改用广播的方式再次查找.
    - 如果广播查询还是没有找到,就得用到LIMHOSTS文件了.
    - 如果找到了,则把它也添加到Cache中保存起来,如果还是没有找到,发送错误信息到客户端