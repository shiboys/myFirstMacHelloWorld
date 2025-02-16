package com.swj.ics.dataStructure.hash;

/**
 * @author shiweijie
 * @version 1.0.0
 * @since 2023/02/07 20:55
 * 通用的使用一致性 hash 接口
 */
public interface LoadBalancer {
  /**
   * 添加节点名称到 一致性hash 环中
   *
   * @param serverName
   */
  void addServerNode(String serverName);

  void delServerNode(String serverNodeName);

  /**
   * 给请求的 url 分配 服务节点
   *
   * @param requestUrl
   * @return
   */
  String selectServerNode(String requestUrl);
}
