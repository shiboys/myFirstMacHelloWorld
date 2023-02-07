package com.swj.ics.dataStructure.hash;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/07 15:20
 * 一致性 hash 的探讨。
 * 一致性 hash 通过构建环状的 Hash 空间代替线性的 Hash 空间的方法解决了这个问题，整个 Hash
 * 空间被构建成一个首尾相接的环。
 * 具体的构造过程为：
 * 1、先构建一个长度为 2^32 的一致性 Hash 环
 * 2、计算每个缓存服务的 Hash 值，并记录，这就是它们在 Hash 环上的位置。
 * 3、对于每一个请求，现根据 key 的 hashcode 得到它在环上的位置，然后在 Hash 环上顺时针查找距离这个 key 的 Hash
 * 最近的服务器节点，
 * 这个就是该请求的所提供的服务的节点
 */
public class ConsistentHash {

  public static void main(String[] args) {
    // runStringHashTest();
    int virtualNodeCount = 10;
    int serverNumber = 100;
    int requestRandomIpCount = 10000;
    String serverPrefix = "192.168.0.";
    LoadBalancerWithoutVirtualNode serverNoVirtualNode = new LoadBalancerWithoutVirtualNode();
    serverNoVirtualNode.buildConsistentHashLoadBalancer(new TreeMap<>());
    for (int i = 0; i < serverNumber; i++) {
      serverNoVirtualNode.addServerNode(serverPrefix + i);
    }
    String[] randomIpArray = IpGenerator.generateRandomIp(requestRandomIpCount);
    Map<String, String> requestAnalysisMap = new HashMap<>();
    for (String requestIp : randomIpArray) {
      String serverName = serverNoVirtualNode.selectServerNode(requestIp);
      requestAnalysisMap.put(requestIp, serverName);
    }
    double variance = Analysis.getVarianceOfRequestHitServer(requestAnalysisMap);
    System.out.println("真实 server 节点，100 个节点，10000 个请求，方差为：" + variance);

    LoadBalancerWithVirtualNode serverWithVirtualNode = new LoadBalancerWithVirtualNode();
    serverWithVirtualNode.buildConsistentHashContainer(virtualNodeCount);
    for (int i = 0; i < serverNumber; i++) {
      serverWithVirtualNode.addServerNode(serverPrefix + i);
    }
    requestAnalysisMap.clear();
    for (String requestIp : randomIpArray) {
      String serverName = serverWithVirtualNode.selectServerNode(requestIp);
      requestAnalysisMap.put(requestIp, serverName);
    }
    variance = Analysis.getVarianceOfRequestHitServer(requestAnalysisMap);
    System.out.println("虚拟节点，1000 个虚拟节点，10000 个请求，方差为：" + variance);
  }

  static void runStringHashTest() {
    String ipPrefix = "192.168.0.";
    String port = "1111";
    for (int i = 0; i < 5; i++) {
      String ip = ipPrefix + i + ":" + port;
      System.out.println(ip + " 的哈希值：" + Math.abs(ip.hashCode()));
    }
    // FNV1_32_HASH.getHashCode()
  }

}


class LoadBalancerWithoutVirtualNode implements LoadBalancer {

  private TreeMap<Integer, String> serverNodeMap;

  @Override
  public void addServerNode(String serverName) {
    int hash = FNV1_32_HASH.getHashCode(serverName);
    this.serverNodeMap.put(hash, serverName);
  }

  @Override
  public void delServerNode(String serverNodeName) {
    int hash = FNV1_32_HASH.getHashCode(serverNodeName);
    serverNodeMap.remove(hash);
  }

  @Override
  public String selectServerNode(String requestUrl) {
    int hash = FNV1_32_HASH.getHashCode(requestUrl);
    Map.Entry<Integer, String> nextEntry = serverNodeMap.ceilingEntry(hash);
    if (nextEntry == null) { // 到达了尾部，根据一致性 hash 环形映射的原则，返回第一个 server 节点
      nextEntry = serverNodeMap.firstEntry();
    }
    return nextEntry.getValue();
  }

  public void buildConsistentHashLoadBalancer(TreeMap<Integer, String> treeMap) {
    this.serverNodeMap = treeMap;
  }
}


class LoadBalancerWithVirtualNode implements LoadBalancer {

  private int virtualServerCount;

  private TreeMap<Integer, String> containerMap;

  @Override
  public void addServerNode(String serverName) {
    String virtualServerName;
    for (int i = 0; i < virtualServerCount; i++) {
      virtualServerName = serverName + "&&VN" + i;
      int hash = FNV1_32_HASH.getHashCode(virtualServerName);
      containerMap.put(hash, virtualServerName);
    }
  }

  @Override
  public void delServerNode(String serverNodeName) {
    String virtualServerName;
    for (int i = 0; i < virtualServerCount; i++) {
      virtualServerName = serverNodeName + "&&VN" + i;
      int hash = FNV1_32_HASH.getHashCode(virtualServerName);
      containerMap.remove(hash);
    }
  }

  @Override
  public String selectServerNode(String requestUrl) {
    int urlHash = FNV1_32_HASH.getHashCode(requestUrl);
    Map.Entry<Integer, String> virtualServerEntry = containerMap.ceilingEntry(urlHash);
    if (virtualServerEntry == null) {
      virtualServerEntry = containerMap.firstEntry();
    }
    if (virtualServerEntry == null) {
      return null;
    }
    String virtualServer = virtualServerEntry.getValue();
    return virtualServer.substring(0, virtualServer.lastIndexOf("&&"));
  }

  public void buildConsistentHashContainer(int virtualServerCount) {
    assert virtualServerCount > 0;
    this.virtualServerCount = virtualServerCount;
    this.containerMap = new TreeMap<>();
  }
}


class IpGenerator {
  public static String[] generateRandomIp(int ipCount) {
    String[] ipArray = new String[ipCount];
    Random random = new Random();
    int ipMax = 256;
    int portMax = 9999;
    for (int i = 0; i < ipCount; i++) {
      ipArray[i] = random.nextInt(ipMax) + "."
          + random.nextInt(ipMax) + "."
          + random.nextInt(ipMax) + "."
          + random.nextInt(ipMax) + ":"
          + random.nextInt(portMax);
    }
    return ipArray;
  }
}


class Analysis {
  public static double getVarianceOfRequestHitServer(Map<String /* requestURL*/,
      String /*serverName*/> requestHitServerMap) {
    Map<String, Integer> serverHitCountMap = new HashMap<>();

    requestHitServerMap.forEach((key, value) -> {
      if (serverHitCountMap.containsKey(value)) {
        serverHitCountMap.put(value, serverHitCountMap.get(value) + 1);
      } else {
        serverHitCountMap.put(value, 1);
      }
    });
    int sum = serverHitCountMap.values().stream().mapToInt(x -> x).sum();
    double avg = sum * 1.0 / serverHitCountMap.size();
    double varianceSum = 0;
    // 求方差
    for (Map.Entry<String, Integer> entry : serverHitCountMap.entrySet()) {
      varianceSum += (entry.getValue() - avg) * (entry.getValue() - avg);
    }
    return varianceSum / serverHitCountMap.size();
  }


}


class FNV1_32_HASH {
  /**
   * FNV1_32_HASH 的简介请查看考 markdown 文档中有关 FNV1_32_HASH 部分
   * 算法核心
   * hash = offset_basis
   * for each octet_of_data to be hashed
   * hash = hash * FNV_prime
   * hash = hash xor octet_of_data
   * return hash
   */
  private static final int FNV_Prime = 16777619;
  private static final long FNV_Basis = 2166136261L;

  static int getHashCode(String val) {
    int p = FNV_Prime;
    int basis = (int) FNV_Basis;
    if (val == null || val.isEmpty()) {
      return -1;
    }
    int hash = basis;
    for (int i = 0; i < val.length(); i++) {
      hash = hash * p;
      hash = hash ^ val.charAt(i);
    }
    // 获取到 hash 之后，需要进行必要的扰动
    hash += hash << 13;
    hash ^= hash >> 7;
    hash += hash << 3;
    hash ^= hash >> 17;
    hash += hash << 5;

    if (hash < 0) {
      if (hash == Integer.MIN_VALUE) {
        hash += 1;
      }
      hash = Math.abs(hash);
    }
    return hash;
  }
}
