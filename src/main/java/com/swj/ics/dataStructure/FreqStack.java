package com.swj.ics.dataStructure;

/**
 * Created by swj on 2020/2/23.
 * 最大频率栈
 */

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * 实现 FreqStack，模拟类似栈的数据结构的操作的一个类。FreqStack 有两个函数： push(int x)，将整数 x 推入栈中。
 * pop()，它移除并返回栈中出现最频繁的元素。如果最频繁的元素不只一个，则移除并返回最接近栈顶的元素。
 * 示例： push [5,7,5,7,4,5] pop() -> 返回 5，因为 5 是出现频率最高的。 栈变成 [5,7,5,7,4]。
 * pop() -> 返回 7，因为 5 和 7 都是频率最高的，但 7 最接近栈 顶。
 *  栈变成 [5,7,5,4]。 pop() -> 返回 5 。 栈变成 [5,7,4]。 pop() -> 返回 4 。 栈变成 [5,7]。
 */
public class FreqStack {
    /**
     * 参考实现
     * 令 freq 作为 x 的出现次数的映射 Map。
     * <p>
     * 此外 maxfreq，即栈中任意元素的当前最大频率，因为我们必须弹出频率最高的元素。
     * <p>
     * 当前主要的问题就变成了：在具有相同的（最大）频率的元素中，怎么判断那个元素是最新的？我们可以使用栈来查询这一信息：靠近栈顶的元素总是相对更新一些。
     * <p>
     * 为此，我们令 group 作为从频率到具有该频率的元素的映射Map。到目前，我们已经实现了 FreqStack 的所有必要的组件。
     * <p>
     * 算法：
     * <p>
     * 实际上，作为实现层面上的一点细节，如果 x 的频率为 f，那么我们将获取在所有 group[i] (i <= f) 中的 x,而不仅仅是栈顶的那个。这是因为每个 group[i] 都会存储与第 i 个 x 副本相关的信息。
     * <p>
     * 最后，我们仅仅需要如上所述维持 freq，group，以及 maxfreq。
     */

    private Map<Integer, Integer> freqMap;
    private int maxFreq;
    private Map<Integer, Stack<Integer>> groupMap;

    public FreqStack() {
        freqMap = new HashMap<>();
        groupMap = new HashMap<>();
    }

    public void push(int i) {
        //先计算单个元素到次数的map映射
        int freq = freqMap.getOrDefault(i, 0) + 1;
        if (freq > maxFreq) {
            maxFreq = freq;
        }
        freqMap.put(i, freq);
        //再得出次数到当前等于该次数的所有元素的映射，因为相等次数的元素还要有FILO的栈的特性，因此代码如下
        groupMap.computeIfAbsent(freq, val -> new Stack<Integer>()).push(i);
    }

    public int pop() {
        /**
         * 弹出最大freq的元素
         */
        int result = groupMap.get(maxFreq).pop();
        freqMap.put(result, freqMap.get(result) - 1);
        //如果最大元素的映射的栈的元素个数已经为0，则表示当前最大元素已经没有了，需要将将maxFreq-1
        if (groupMap.get(maxFreq).size() < 1) {
            maxFreq = maxFreq - 1;
        }
        return result;
    }

    public static void main(String[] args) {
        int[] arr= new int[]{5,7,5,7,4,5};
        FreqStack instance = new FreqStack();
        for(int i=0,len = arr.length;i <len;i++) {
            instance.push(arr[i]);
        }

        System.out.println("出栈元素如下：");
        for(int i=0,len = arr.length;i <len;i++) {
            System.out.print(instance.pop()+"\t,");
        }
        System.out.println("\n*****************end**********");
    }
}
