package com.howbuy.jso.service.network;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.howbuy.jso.service.network.config.Config;
import com.howbuy.jso.service.network.host.Host;

/**
 * 主机选择器.
 * @author li.zhang
 * 2014-9-23
 *
 */
public class HostSelector
{
    /** INSTANCE. **/
    private static final HostSelector INSTANCE = new HostSelector();
    
    /** SETTLE_SERVER_LIST **/
    private static final String SETTLE_SERVER_LIST = "settle.servers.list";
    
    /** SELECTION_TIMEOUT **/
    private static final String SELECTION_TIMEOUT = "selection.timeout";
    
    /** 标识连接各个服务器的渠道初始化是否已经结束. **/
    private boolean channelInitEnd;
    
    /** 通道选择器. **/
    private Selector selector;
    
    /** 注册的主机集合，所有的主机. **/
    private Set<Host> hostset;
    
    /** 就绪选择, 选中的host主机集合列表. **/
    private Set<Host> selectedHosts;
    
    /** 断连主机集合列表. **/
    private Set<Host> cancelledHosts;
    
    /** 
     * key为hostname_port(服务端host和服务端port), value为channel和selector选择器的注册关系对象selectionKey, 
     * 
     * selectionKey为客户端连接的key. 这个registry的本意是方便后面根据服务器端的主机名和端口号，获得一个客户端
     * 
     * 的连接.所以才这样设计的. 我们的设计初衷是一个服务端始终只有一个客户端连接来维持对应的会话.所以才没有将registry
     * 
     * 的value设置成List<SelectionKey>类型，而是设置成了SelectionKey类型.
     */
    private Map<String, SelectionKey> registry = new HashMap<String, SelectionKey>();
    
    /** 标识当前轮询的可用主机集合的位置. 默认是第一个位置.**/
    private int currPollingPos = 1;
    
    /**
     * 私有构造方法
     */
    private HostSelector()
    {
        try
        {
            init();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * 获取实例.
     * @return
     */
    public static HostSelector getInstance()
    {
        return INSTANCE;
    }
    
    public void init() throws Exception
    {
        //1.初始化配置.
        initProperties();
        
        //2.初始化socket渠道列表.
        initNioChannel();
    }
    
    /**
     * 初始化配置.
     * @throws Exception
     */
    public void initProperties() throws Exception
    {
        Config config = Config.getInstance();
        Properties props = new Properties();
        props.load(getClass().getClassLoader().getResourceAsStream("config.properties"));
        
        //就绪选择超时时间
        String selectionTimeout = props.getProperty(SELECTION_TIMEOUT);
        config.setSelectionTimeout(Long.valueOf(selectionTimeout));
        
        String settleServerList = props.getProperty(SETTLE_SERVER_LIST);
        config.setSettleServersList(settleServerList);
        if (null == settleServerList)
        {
            return;
        }
        
        String[] hosts = settleServerList.split(",");
        if (null == hosts)
        {
            return;
        }
        
        Set<Host> hostset = new HashSet<Host>();
        for (int i = 0; i < hosts.length; i++)
        {
            String hostinfo = hosts[i];
            if (null == hostinfo)
            {
                continue;
            }
            
            String[] splits = hostinfo.split(":");
            if (null == splits || 2 != splits.length)
            {
                continue;
            }
            
            Host host = new Host();
            host.setIp(splits[0]);
            host.setPort(Integer.parseInt(splits[1]));
            
            hostset.add(host);
        }
        
        this.hostset = hostset;
    }
    
    /**
     * 初始化渠道.
     * @throws Exception 
     */
    private void initNioChannel() throws Exception
    {
        if (null == hostset || hostset.isEmpty())
        {
            return;
        }
        
        //1.打开多路通道选中器.
        selector = Selector.open();
        
        //2.启动渠道监听线程
        ChannelMonitor monitor = new ChannelMonitor(this);
        monitor.setDaemon(true);
        monitor.start();
        
        //3.绑定端口、渠道注册到选择器.
        Iterator<Host> iterator = hostset.iterator();
        while (iterator.hasNext())
        {
            Host host = iterator.next();
            SocketChannel channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(host.getIp(), host.getPort()));
            
            SelectionKey key = channel.register(selector, SelectionKey.OP_CONNECT);
            
            registry.put(host.getIp() + "_" + host.getPort(), key);
        }
        
        //4.主机重连任务.
        ChannelReconnectTask reconnectTask = new ChannelReconnectTask(this);
        Thread reconnect = new Thread(reconnectTask);
        reconnect.setDaemon(true);
        reconnect.start();
    }
    
    /**
     * 随机从就绪主机中选择一个可用的服务器.
     * @return
     */
    public SelectionKey select()
    {
        SelectionKey key = null;
        Host host = null;
        
        //准备管道, 没准备好会阻塞在这里.
        prepareChannels();
        
        if (null == selectedHosts || selectedHosts.isEmpty())
        {
            return key;
        }
        
        synchronized (this)
        {
            Host[] hosts = new Host[selectedHosts.size()];
            selectedHosts.toArray(hosts);
            if (currPollingPos > hosts.length)
            {
                currPollingPos = 1;
            }
            
            host = hosts[(currPollingPos++) - 1];
        }
        
        if (null != host)
        {
            key = registry.get(host.getIp() + "_" + host.getPort());
        }
        
        return key;
    }
    
    /**
     * 随机从就绪主机中选择一个可用的服务器.
     * @return
     */
    public SelectionKey select(long timeout, TimeUnit unit)
    {
        SelectionKey key = null;
        Host host = null;
        
        //准备管道, 没准备好会阻塞在这里,直到准备好,或者超时.
        prepareChannels(timeout, unit);
        
        if (null == selectedHosts || selectedHosts.isEmpty())
        {
            return key;
        }
        
        synchronized (this)
        {
            Host[] hosts = new Host[selectedHosts.size()];
            selectedHosts.toArray(hosts);
            if (currPollingPos > hosts.length)
            {
                currPollingPos = 1;
            }
            
            host = hosts[(currPollingPos++) - 1];
        }
        
        if (null != host)
        {
            key = registry.get(host.getIp() + "_" + host.getPort());
        }
        
        return key;
    }
    
    /**
     * 阻塞等待管道就绪.
     */
    public void prepareChannels()
    {
        //一直阻塞等待管道就绪.
        while (!channelInitEnd);
    }
    
    /**
     * 阻塞等待管道就绪.
     */
    public void prepareChannels(long timeout, TimeUnit unit)
    {
        long current = System.currentTimeMillis();
        long miseconds = unit.toMillis(timeout);
        
        //如果任务没有完成并且没有超时就一直阻塞在这里.
        while (true)
        {
            //如果管道准备好.
            if (isChannelInitEnd())
            {
                break;
            }
            
            //如果超时.
            if ((current + miseconds) < System.currentTimeMillis())
            {
                break;
            }
        }
    }

    public void addSelectedHost(Host hostinfo)
    {
        if (null == hostinfo)
        {
            return;
        }
        
        Set<Host> selected = new HashSet<Host>();
        if (null != this.selectedHosts)
        {
            selected.addAll(this.selectedHosts);
        }
        
        selected.add(hostinfo);
        
        this.selectedHosts = selected;
    }
    
    /**
     * 从已选择列表删除.
     * @param hostinfo
     */
    public void removeSelectedHost(Host hostinfo)
    {
        if (null == hostinfo)
        {
            return;
        }
        
        Set<Host> selected = new HashSet<Host>();
        if (null != selectedHosts && !selectedHosts.isEmpty())
        {
            selected.addAll(this.selectedHosts);
        }
        
        Iterator<Host> iterator = selected.iterator();
        while (iterator.hasNext())
        {
            Host host = iterator.next();
            if (host.getIp().equals(hostinfo.getIp())
                    && host.getPort() == hostinfo.getPort())
            {
                iterator.remove();
                break;
            }
        }
       
        this.selectedHosts = selected;
    }
    

    /**
     * 加入到撤销列表，即断链主机列表.
     * @param hostinfo
     */
    public void addCancelledHost(Host hostinfo)
    {
        if (null == hostinfo)
        {
            return;
        }
        
        Set<Host> cancelled = new HashSet<Host>();
        if (null != this.cancelledHosts && !cancelledHosts.isEmpty())
        {
            cancelled.addAll(this.cancelledHosts);
        }
       
        cancelled.add(hostinfo);
        
        this.cancelledHosts = cancelled;
    }
    
    /**
     * 加入到撤销列表，即断链主机列表.
     * @param hostinfo
     */
    public void addCancelledHost(Set<Host> hostinfos)
    {
        if (null == hostinfos)
        {
            return;
        }
        
        Set<Host> cancelled = new HashSet<Host>();
        if (null != this.cancelledHosts && !cancelledHosts.isEmpty())
        {
            cancelled.addAll(this.cancelledHosts);
        }
       
        cancelled.addAll(hostinfos);
        
        this.cancelledHosts = cancelled;
    }
    
    /**
     * 从撤销列表删除.
     * @param hostinfo
     */
    public void removeFromCancelledHost(Host hostinfo)
    {
        if (null == hostinfo)
        {
            return;
        }
        
        Set<Host> cancelled = new HashSet<Host>();
        if (null != cancelledHosts && !cancelledHosts.isEmpty())
        {
            cancelled.addAll(this.cancelledHosts);
        }
        
        Iterator<Host> iterator = cancelled.iterator();
        while (iterator.hasNext())
        {
            Host host = iterator.next();
            if (host.getIp().equals(hostinfo.getIp())
                    && host.getPort() == hostinfo.getPort())
            {
                iterator.remove();
                break;
            }
        }
       
        this.cancelledHosts = cancelled;
    }
    
    public Selector getSelector()
    {
        return selector;
    }

    public Map<String, SelectionKey> getRegistry()
    {
        return registry;
    }
    
    public int getHostCount()
    {
        int count = 0;
        if (null != hostset)
        {
            count = hostset.size();
        }
        
        return count;
    }
    
    public Set<Host> getHostset()
    {
        return hostset;
    }

    public Set<Host> getCancelledHosts()
    {
        return cancelledHosts;
    }

    public boolean isChannelInitEnd()
    {
        return channelInitEnd;
    }

    public void setChannelInitEnd(boolean channelInitEnd)
    {
        this.channelInitEnd = channelInitEnd;
    }
}
