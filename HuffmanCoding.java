package tree.haffumanCoding;

import java.io.*;
import java.util.*;

public class HuffmanCoding {
    private static HashMap<Character, String> codeTable = new HashMap<>();//用于保存哈夫曼编码
    private static short mantissa;//用于记录byte数组中最后一个数转换为二进制数后应该有几位

    public static void main(String[] args) {
        //哈夫曼编码(最佳编码)
        //先统计一串字符串中每个字符出现的次数，然后将他们重新编码(需要是前缀编码)，出现频率越高的字符，给予比较短的编码
        //要通过哈夫曼树来寻找到最佳编码，其中，每个字符出现的次数作为value
        //构建完哈夫曼树之后，我们规定通往左子树的路径为0,通往右子树的路径为1。这样从root到每个字符对应的节点，就可以找到一个编码,而且这个编码是前缀编码

        //压缩文件
        String srcPath1 = "F:/EclipseFileCreate/Huffman/src/src.txt";
        String destPath1 = "F:/EclipseFileCreate/Huffman/src/srcZip.zip";
        zipFile(srcPath1, destPath1);

        //解压文件
        String srcPath2 = "F:/EclipseFileCreate/Huffman/src/srcZip.zip";
        String destPath2 = "F:/EclipseFileCreate/Huffman/dest/dest.txt";
        unZipFile(srcPath2,destPath2);

    }

    /**
     * 将srcPath的文件压缩后输出到destPath
     *
     * @param srcPath  源路径
     * @param destPath 目标(.zip)路径
     */
    public static void zipFile(String srcPath, String destPath) {
        BufferedInputStream bufferedInputStream = null;
        ObjectOutputStream objectOutputStream = null;
        byte[] bytes = null;
        //读取需要进行压缩的文件中的内容
        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(srcPath));
            bytes = new byte[bufferedInputStream.available()];//available()方法查询当前文件中可读取的字符
            bufferedInputStream.read(bytes);

        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                bufferedInputStream.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        String content = new String(bytes);

        //进行压缩
        byte[] zipCodes = zipper(content);

        //输出
        try {
            //这里我们使用对象流，有利于后续文件的恢复
            objectOutputStream = new ObjectOutputStream(new FileOutputStream(destPath));
            //写出压缩完后的二进制编码
            objectOutputStream.writeObject(zipCodes);
            //写出哈夫曼编码表
            objectOutputStream.writeObject(codeTable);
            //写出尾数个数
            objectOutputStream.writeObject(mantissa);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                objectOutputStream.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        System.out.println("压缩完毕");
    }

    /**
     * 用于将我自己的压缩文件解压
     *
     * @param srcPath  .zip文件的路径
     * @param destPath 解压完毕的文件的路径
     */
    public static void unZipFile(String srcPath, String destPath) {
        //由于压缩的时候我们是以对象流的方式输出的，现在也可以以对象流的方式输入
        ObjectInputStream objectInputStream = null;
        byte[] codes = null;
        try {
            objectInputStream = new ObjectInputStream(new FileInputStream(srcPath));
            //读取哈夫曼编码
            codes = (byte[]) objectInputStream.readObject();
            //读取哈夫曼编码表
            HashMap<Character, String> fileCodeTable = (HashMap<Character, String>) objectInputStream.readObject();
            codeTable = fileCodeTable;
            //读取尾数
            short fileMantissa = (short) objectInputStream.readObject();
            mantissa = fileMantissa;
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                objectInputStream.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        //进行解码
        String decode = uncompress(codes);

        //写入目标文件
        BufferedOutputStream bufferedOutputStream = null;
        try {
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(destPath));
            bufferedOutputStream.write(decode.getBytes());
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                bufferedOutputStream.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        System.out.println("解压完毕");
    }

    /**
     * 整个压缩方法的封装
     *
     * @param content 要进行哈夫曼编码的原始字符串
     * @return 返回原始的字符串对应的经过哈夫曼编码的byte数组
     */
    public static byte[] zipper(String content) {
        //2.将字符串中每个字符转换Node节点，并将这些节点转换为Node节点的ArrayList
        ArrayList<Node> nodes = getNodes(content);

        //3.将Node节点构建成哈夫曼树
        Node node = buildHuffmanTree(nodes);

        //4.获取哈夫曼编码表
        buildCode(node);

        //5.将字符串根据哈夫曼编码表重新编码，并且返回
        return coding(content);
    }

    /**
     * 将字符串根据哈夫曼编码表重新编码
     *
     * @param content 要进行哈夫曼编码的字符串
     * @return 返回装有每个字符哈夫曼编码的byte数组。生成的哈夫曼编码，每八位为byte数组的一个元素
     */
    public static byte[] coding(String content) {
        //1.先将content转为byte数组
        char[] chars = content.toCharArray();

        //2.先将这些char数组进行哈夫曼编码，转换成一个字符串
        StringBuilder huffmanCodes = new StringBuilder();
        for (char c : chars) {
            String s = codeTable.get(c);
            huffmanCodes.append(s);
        }
        mantissa = (short) (huffmanCodes.length() % 8);

        //3.将huffmanCodes中的数据八个为一组，封装到byte[]数组中
        int length = (huffmanCodes.length() + 7) / 8;
        byte[] zipCodes = new byte[length];
        for (int i = 0; i < huffmanCodes.length(); i = i + 8) {
            if (i + 8 < huffmanCodes.length()) {
                zipCodes[i / 8] = (byte) Integer.parseInt(huffmanCodes.substring(i, i + 8), 2);
            } else {
                zipCodes[i / 8] = (byte) Integer.parseInt(huffmanCodes.substring(i), 2);
            }
        }
        return zipCodes;
    }

    /**
     * 用于生成哈夫曼编码表，生成的结果保存在一个hashMap中
     *
     * @param node  从node节点开始生成哈夫曼编码
     * @param path  判断是哪条路径，左子树是0，右子树是1
     * @param codes 当前的哈夫曼编码，不断连接字符串直到叶子节点
     */
    public static void buildCode(Node node, String path, String codes) {
        codes = codes + path;//要想考虑效率问题可以使用stringBuilder
        if (node == null) {
            return;
        }
        if (node.getData() == '\u0000') {//非叶子节点
            buildCode(node.getLeft(), "0", codes);
            buildCode(node.getRight(), "1", codes);
        } else {//非叶子节点
            codeTable.put(node.getData(), codes);
        }
    }

    /**
     * 用于在外面直接调用
     *
     * @param node 哈夫曼树的根节点
     */
    public static void buildCode(Node node) {
        buildCode(node, "", "");
    }

    public static Node buildHuffmanTree(ArrayList<Node> nodes) {
        while (nodes.size() > 1) {//当集合中只有一个节点的时候，此时这个节点就是root节点，直接返回
            Collections.sort(nodes);
            Node left = nodes.get(0);//value最小
            Node right = nodes.get(1);//value次小
            nodes.remove(0);
            nodes.remove(0);
            Node root = new Node((char) 0, left.getWeight() + right.getWeight());
            root.setLeft(left);
            root.setRight(right);
            nodes.add(root);
        }
        //5.返回节点中剩余的那个节点
        return nodes.get(0);
    }

    /**
     * 将字符串转换为哈夫曼树的一个个节点
     *
     * @param s 要转换的字符串
     * @return 返回由哈夫曼树的节点构成的ArrayList
     */
    private static ArrayList<Node> getNodes(String s) {
        //1.先将String转换为char[]
        char[] chars = s.toCharArray();

        //2.遍历char[]，通过hashMap来保存具体值data以及出现次数weight
        HashMap<Character, Integer> hashMap = new HashMap<>();
        for (char c : chars) {
            if (hashMap.containsKey(c)) {//查看当前hashMap中是否已存在这个值
                hashMap.replace(c, hashMap.get(c) + 1);
            } else {
                hashMap.put(c, 1);
            }
        }

        //3.创建Node的ArrayList
        ArrayList<Node> nodes = new ArrayList<>();
        Set<Map.Entry<Character, Integer>> entries = hashMap.entrySet();
        for (Map.Entry<Character, Integer> entry : entries) {
            nodes.add(new Node(entry.getKey(), entry.getValue()));
        }

        //4.返回
        return nodes;
    }

    /**
     * 整个解压方法的封装
     *
     * @param zipCodes 要进行解压的byte数组
     * @return 返回解压完毕的字符串
     */
    public static String uncompress(byte[] zipCodes) {
        //1.先将byte数组中的数据重新转为二进制字符串
        String s = byteToString(zipCodes);

        //2.根据哈夫曼编码表，进行解码,并返回

        return decode(s);
    }

    /**
     * 将二进制字符串通过反向的哈夫曼编码表解码得到真实字符串
     *
     * @param s 待解码的二进制字符串
     * @return 返回真实数据
     */
    public static String decode(String s) {
        //1.先获得反向哈夫曼编码表(将key和value进行调换)
        Set<Map.Entry<Character, String>> entries = codeTable.entrySet();
        HashMap<String, Character> codeTableReverse = new HashMap<>();
        for (Map.Entry<Character, String> entry : entries) {
            codeTableReverse.put(entry.getValue(), entry.getKey());
        }

        //2.根据反向哈夫曼编码表和待解码的二进制字符串获得真实字符串
        //双指针匹配字符串
        StringBuilder stringBuilder = new StringBuilder();
        int slow = 0;
        int fast = 0;
        while (fast <= s.length()) {
            String temp = s.substring(slow, fast);
            Character c = codeTableReverse.get(temp);
            if (c == null) {//说明这次slow指针和fast指针之间的二进制编码不是一个有效的哈夫曼编码
                fast++;
            } else {//匹配到有效的哈夫曼编码了
                stringBuilder.append(c);
                slow = fast;
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 将byte数组转换为二进制字符串
     *
     * @param zipCodes 要解压的byte数组
     * @return 返回二进制字符串
     */
    public static String byteToString(byte[] zipCodes) {
        //1.先获得原来的二进制字符串
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < zipCodes.length - 1; i++) {
            String s = byteToStringCore(true, zipCodes[i]);
            stringBuilder.append(s);
        }
        String s = byteToStringCore(false, zipCodes[zipCodes.length - 1]);
        stringBuilder.append(s);

        return stringBuilder.toString();
    }

    /**
     * 将byte数组中某一个元素转换为对应的二进制字符串
     *
     * @param flag 用于标记当前元素是否是原byte数组中的最后一位，是最后一位为false，不是最后一位为true
     * @param b    要转换的byte
     * @return 返回对应的二进制字符串
     */
    public static String byteToStringCore(boolean flag, byte b) {
        //下面与数据结构无关，而且有关二进制，有点难懂，我懒得搞懂了，直接抄
        //主要是要考虑到，正数的二进制会自动将前面的0全部省略，这样就会导致位数不对
        //但是byte数组中最后一位又不一定会满8位位数
        int temp = b;
        if (flag) {
            //处理其他位
            temp = temp | 256;//按位与,256其实是0B100000000
            String str = Integer.toBinaryString(temp);
            return str.substring(str.length() - 8);
        } else {
            //处理最后一位
            //最后一位数处理起来非常麻烦，究其原因是因为如:1,01,001,0001……这样的数经过转换后都会变成1存储在byte数组中
            //导致我们在解码的时候不知道应该保留几位，因此，我们需要一个额外变量来记录最后一位的数字实际表示几位二进制数
            temp = temp | 256;
            String str = Integer.toBinaryString(temp);
            if (mantissa == 0) {//说明8位全要
                return str.substring(str.length() - 8);
            } else {
                return str.substring(str.length() - mantissa);//保留mantissa位尾数
            }
        }
    }
}