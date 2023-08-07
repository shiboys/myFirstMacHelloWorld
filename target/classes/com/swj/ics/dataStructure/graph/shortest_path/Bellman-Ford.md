### Bellman-Ford 最短路径算法

* 适用条件 & 范围
  * - 单源最短路径（从源点 S 到其他所有顶点 v）；
  * - 有向图&无向图（无向图可以看做（u,v）,(v,u)同属于边集 E 的有向图）
  * - 边的权重可正可负(如有负的权重回路输出错误提示);
  * - 查分约束系统

有关松弛操作 DJ 算法跟 B-F 算法的区别
* DJ 算法关注的是点
  DJ 是从某一个点出发，去修改到某个节点的最小距离
* B-F 算法关注的是边
  B-F 算法从边出发

#### 边的松弛

*  d[v]、d[u] 是目前 s 到 u、v 的最短距离
   - 关注边的 <u,v> 能否改善 d[v] 的值
   - if(d[u] + w < d[v]) 成立
   - 原有的 d[v] 线路将被 s->u->v 代替
   - 这就是边的松弛操作。

#### 边的松弛操作为什么是 V-1 次

* V 表示点的个数
* 松弛操作过程中的边的次数很重要
* 每一轮的松弛至少可以确定一个点的最优值

#### Bellman-Ford 算法伪代码

```java
bool Bellman_Ford() {
    for(int i=1;i<nodenum-1;++i) {
        for(int j=0;j<edgenum;++j) {
            relax(edge[j].u,edge[j].v,edje[j].weight);
        }
        boolean flag=true;
        // 判断是否有负的环路
        for(int i=1;i<=edgenum;i++) {
            if(dist[edge[i].v] > dist[edge[i].u] + edge[i].weight) {
                flag=false;
                break;
            }
        }
    }
    return flag;
}
```