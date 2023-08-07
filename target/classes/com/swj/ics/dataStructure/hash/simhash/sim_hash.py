# simHash 的 python 实现
import jieba
import jieba.analyse
import numpy as np


class Simhash:
    def __init__(self, content) -> None:
        # 构造函数
        self.simhash = self.simhash(content)

    def __str__(self) -> str:
        return self.simhash

    def string_hash(self, source):
        # 自定义字符串的 hash 函数
        if source == "":
            return 0
        else:
            # ord 函数返回 参数的 ascii 码的整数形式。比如 'a' 返回 97。这个 根据 java 的hashcode
            # 应该是初始为 hash 种子。
            hash = ord(source[0]) << 7
            # m 为啥是 1000003 ，我查了很多资料，也没查出来，大家都是抄的，原始作者为啥要这么定义，
            # 目前不得而知。java 中这个数字是 31
            m = 1000003
            mask = 2**128 - 1
            for ch in source:
                hash = ((hash * m) ^ ord(ch)) & mask
            hash ^= len(source)

            if hash == -1:
                hash = -2
            # bin() 函数返回整数的二进制格式的字符串形式，但是 python 默认会带上 '0b', 因此这里需要 replace 掉，
            # zfill 函数返回指定宽度的的字符串，如果字符串本身的宽度不够则前面进行补齐 0 ，这是为了防止 bin() 函数返回的数字不够 64 位bit
            # [-64:] 是为了防止返回的字符串超过 64 位，则取最低的 64 位。
            hash = bin(hash).replace('0b', '').zfill(64)[-64:]
            # print("source:", source, ", hash:", hash)
            return str(hash)

    def simhash(self, source):
        # 使用 jieba 分词器获取分词，根据 jieba 文档，默认的模式为精确模式
        segment_list = jieba.cut(source)
        # 设置停词器
        jieba.analyse.set_stop_words('./simhash/cn_stopwords.txt')
        # 获取指定数量的带有权重的关键词
        # allowPOS: the allowed POS list eg. ['ns', 'n', 'vn', 'v','nr']. if the POS of w is not in this list,it will be filtered.
        # 这里的 extract_tags 调用了 jieba 的 tfidf.py 里面的 一个源码
        # 将 tags = sorted(freq.items(), key=itemgetter(1), reverse=True) 改为 tags = sorted(freq.items(), key=itemgetter(1,0), reverse=True)
        # 这样改的目的是先按照权重排序，再按照词排序
        #1、获取分词和权重，使用 jieba.analyse.extract_tags() 函数
        keyword_list = jieba.analyse.extract_tags(
            '|'.join(segment_list), topK=20, withWeight=True, allowPOS=())
        key_list = []
        for keyword, weight in keyword_list:
            weight = int(weight * 20)
            print(keyword, weight)
            temp_arr = []
            #2、hash 计算
            hash = self.string_hash(keyword)
            #3、加权
            for ch in hash:
                if ch == "1":
                    temp_arr.append(weight)
                else:
                    temp_arr.append(-weight)

            key_list.append(temp_arr)
        # axis 数组的层级，axis=0，表示a[0],a[1],a[2], axis=1 表示 a[0][0],a[0][1],a[0][2]
        # 这里如果 axis=1 是错误的，只能取到有跟列数 m 一样的集合大小，
        # 而整整的 simHash 是需要 m 个权重相加得到一个权重，最终是 64 个权重数据的集合，这样才能降维成 64 位 0 和 1 bit数据
        # 4，合并
        key_list = np.sum(np.array(key_list), axis=0)
        print("final key list after sum are ", key_list)
        if key_list == []:
            # 读取数据异常
            return '00'
        simhash_str = ""
        # 5、降维
        for num in key_list:
            if num > 0:
                simhash_str = simhash_str+'1'
            else:
                simhash_str = simhash_str+'0'

        return simhash_str

    def hamming_distance(self, another_simhash):
        h1 = '0b'+self.simhash
        h2 = '0b' + another_simhash.simhash
        # 经过异或运算，simhash 不同的位就会被计算出来
        diff = int(h1, 2) ^ int(h2, 2)
        n = 0
        # 二进制的1101 表示为13，
        # 第一次 按位与 & 运算 1101&1100=1100，  diff = 12
        # 第二次 按位与运算，1100&1011=1000，diff=8
        # 第三次 按位与运算 1000&0111，diff=0
        # 不知道这个计数法是谁发现的，反正感觉挺牛逼的。
        while diff:
            diff &= (diff-1)
            n = n+1

        return n


if __name__ == "__main__":
    sentence_A = """
    明朝军制建立在军户制度上，军户即为中国古代世代从军、充当军差的人户。
    东晋南北朝时，士兵及家属的户籍隶于军府称为军户。军户子弟世袭为兵未经准许不得脱离军籍。
    北魏军户亦有用俘虏充当的。元朝实行军户制度，军户必须出成年男子到军队服役，父死子替，兄亡弟代，世代相袭
    """
    sentence_B = """
    明朝的军制是在元朝基础上改进，而没有采用唐宋时期的募兵制。
    元朝的军制是建立在游牧民族制度上发展而来，游牧民族在战争是全民征兵，实际上是军户制度。
    建立元朝以后，蒙古族还是全部军户，对于占领区招降的军队，也实行军户制度。
    """

    sentence_C = "You know nothing Jon Snow!"
    sentence_D = "Jon Snow:I know nothing."

    simhash_A = Simhash(sentence_A)
    simhash_B = Simhash(sentence_B)
    simhash_C = Simhash(sentence_C)
    simhash_D = Simhash(sentence_D)

    print(simhash_A)
    print(simhash_B)
    print(simhash_C)
    print(simhash_D)

    print("sentenc_A's hamming distance with B is ",
          simhash_A.hamming_distance(simhash_B))
    print("sentenc_C's hamming distance with D is ",
          simhash_C.hamming_distance(simhash_D))

    '''
    Building prefix dict from the default dictionary ...
Loading model from cache /var/folders/f9/565gbx6d35370011nx_j7_800000gn/T/jieba.cache
Loading model cost 0.889 seconds.
Prefix dict has been built successfully.
source: 军户 , hash: 0010010100011100001011110001000111001000101011100110101000100100
source: 世代 , hash: 0010001110000010011101000011110000001101000000100011000000100011
source: 充当 , hash: 0010010011110101000100110101001000111010111101101010000011011110
source: 军差 , hash: 0010010100011100001011110001000111001000101011100101010111111101
source: 人户 , hash: 0010001111001101000010000111010110010010001000111100110110011011
source: 隶于 , hash: 0100010010001001001010101101110100110000010000111111111000101110
source: 军府 , hash: 0010010100011100001011110001000111001000101011100101011010001111
source: 为兵 , hash: 0010001110010010110100110011110100000100010000011001111101011001
source: 北魏军 , hash: 1000101010101101010001110000000111101111110101000101000001000110
source: 父死子 , hash: 0011010011111111011110011100000111000001111011101111101000011000
source: 兄亡 , hash: 0010010011110100100111101110000000110111111110111011000001101111
source: 弟代 , hash: 0010101101000001100111101000011011000100101001011001010001111100
source: 相袭 , hash: 0011011000011001110111100100100111011110001001110011111001000111
source: 制度 , hash: 0010010101100010101010111001001101001001101101111101110110000110
source: 军籍 , hash: 0010010100011100001011110001000111001000101011100111010001011110
source: 军制 , hash: 0010010100011100001011110001000111001000101011100101101000100101
source: 从军 , hash: 0010001111010110001000001100011111010110100001010001001101110011
source: 准许 , hash: 0010010100101111101111001111010101001001001101100110110001101000
source: 世袭 , hash: 0010001110000010011101000011110000001101000000101111011001101101
source: 户籍 , hash: 0010110010101001110010000000000110000101001100000011000110101010
final key list after sum are  [-250 -120    0    0  -16  -16  -16  -24  -24   32   32  -24   24    8
  -16  -32  -12    6  -24  -54]
/Volumes/home/github/myFirstMacHelloWorld/src/com/swj/ics/dataStructure/hash/simhash/sim_hash.py:67: DeprecationWarning: elementwise comparison failed; this will raise an error in the future.
  if key_list == []:
source: 军户 , hash: 0010010100011100001011110001000111001000101011100110101000100100
source: 元朝 , hash: 0010010011110100001010100111010110110111100111011001111100010110
source: 军制 , hash: 0010010100011100001011110001000111001000101011100101101000100101
source: 游牧民族 , hash: 0110010011110110110010010000010111001100000110110010100001111111
source: 制度 , hash: 0010010101100010101010111001001101001001101101111101110110000110
source: 募兵制 , hash: 1111010111001011010100110111101110100001101001100001111011001101
source: 占领区 , hash: 0001010111001011011101001010000011110110110110110011011001001011
source: 建立 , hash: 0010101100110000110010110010000101000010111010101100101010100111
source: 征兵 , hash: 0010101101101110001011110100010111000100111110110010110100110100
source: 招降 , hash: 0010110011110100010111000011101111101111001100111111110110011110
source: 全民 , hash: 0010010100000100111111011101111111111110000011100111111000101011
source: 唐宋 , hash: 0010011010101110101000110100010101010101110101101001000010111001
source: 蒙古族 , hash: 1110000000001010010000100110011000000011000101101001011011000001
source: 明朝 , hash: 0010111001101000110011001111000001100000101000100001111110110101
source: 改进 , hash: 0010111000000111111100000100111110100011010100010001110110110010
source: 军队 , hash: 0010010100011100001011110001000111001000101011101001111000001100
source: 实际上 , hash: 0100100010001001101110111010111110001110101000100111011100010100
source: 战争 , hash: 0010110010011011101011110001110000110110110101101100011011000011
source: 实行 , hash: 0010100110101001101101010101101110010011110001100100111100010100
source: 采用 , hash: 0100001001001010110100010110111110111001011001100000010010111111
final key list after sum are  [-180   78  -88    0   18   60   20  -30   20   80   48   -8  -72  -12
   -6  -24    0   12    0    0]
source: know , hash: 0001100100110101111010001111110010000110001001111111111001010101
source: nothing , hash: 0101010010111101011110011110001010111101101101110111001111011000
source: Jon , hash: 0111101110010111000101110101010101000100110110101000101010111110
source: Snow , hash: 1011000101110110001011101010101101011100000111001100010100001101
final key list after sum are  [354 826 354   0]
source: Jon , hash: 0111101110010111000101110101010101000100110110101000101010111110
source: Snow , hash: 1011000101110110001011101010101101011100000111001100010100001101
source: know , hash: 0001100100110101111010001111110010000110001001111111111001010101
source: nothing , hash: 0101010010111101011110011110001010111101101101110111001111011000
final key list after sum are  [354   0 354 826]
00000000011011000100
01001110111000000100
1110
1011
sentenc_A's hamming distance with B is  7
sentenc_C's hamming distance with D is  2
    '''
