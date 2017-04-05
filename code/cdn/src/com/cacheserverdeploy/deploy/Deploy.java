package com.cacheserverdeploy.deploy;

import com.filetool.util.GeneticAlgorithm;

import java.util.ArrayList;
import java.util.List;

public class Deploy {
    /**
     * todo
     *
     * @return caseouput info
     * @see [huawei]
     */
    static class Edge {
        int from;
        int to;
        int cap;
        int cost;
        int next;
    }

    public static class evaluate {
        public double error;
        public int cost;
        public ArrayList<List> list;
    }

    static int E;  //边的个数
    public static int NN;
    static int N;  //节点加上超级原点，汇点。
    static int S;  //超级原点
    static int T;  //超级汇点
    static int costServer;  //服务器单价
    public static int numServer;
    static int lines; //文件行数
    static int num_T;
    static List<String> link = new ArrayList<String>();
    //    static Edge[] e;
    static List<Edge> edgeList = new ArrayList<Edge>();
    static int en = 0;
    static int enT;
    static int[] head;
    static int[] pre;
    static int[] dist;
    static boolean[] visited;
    public static ArrayList<Integer> T_list = new ArrayList<Integer>();
    public static ArrayList<Integer> T_cost = new ArrayList<Integer>();

    static public void add(int x, int y, int f, int c, List<Edge> edlist) {
        Edge edge = new Edge();
        edge.from = x;
        edge.to = y;
        edge.cap = f;
        edge.cost = c;
        edge.next = head[x];
        head[x] = en++;
        edlist.add(edge);
    }

    static public void addedge(int x, int y, int f, int c, List<Edge> edlist) {
        add(x, y, f, c, edlist);
        add(y, x, 0, -c, edlist);
    }

    static public boolean SPFA(int s, int t, int n, List<Edge> totalEdges) {
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < n; i++) {
            pre[i] = -1;
            visited[i] = false;
            dist[i] = Integer.MAX_VALUE;
        }
        visited[s] = true;
        dist[s] = 0;
        list.add(s);
        while (list.size() != 0) {
            int current = list.get(0);
            list.remove(0);
            visited[current] = false;
            for (int i = head[current]; i != -1; i = totalEdges.get(i).next) {
                if (totalEdges.get(i).cap != 0) {
                    int v = totalEdges.get(i).to;
                    if (dist[v] > dist[current] + totalEdges.get(i).cost) {
                        dist[v] = dist[current] + totalEdges.get(i).cost;
                        pre[v] = i;
                        if (!visited[v]) {
                            visited[v] = true;
                            list.add(v);
                        }
                    }
                }
            }
        }
        return dist[t] != Integer.MAX_VALUE;        //找不到一条到终点的路
    }

    static public evaluate MCMF(int s, int t, int n, List<Edge> totalEdges) {
//        System.out.println(totalEdges.size() + "   size  " + edgeList.size());
        int mincost = 0;  //最小费用
        int flow = 0;
        int[] output = new int[T_cost.size()]; //初始化为0,每个消费节点获得的流量总数
        for (int i = 0; i < T_cost.size(); i++) output[i] = 0;
        ArrayList<List> string = new ArrayList<List>();
        while (SPFA(s, t, n, totalEdges)) {
            int minflow = Integer.MAX_VALUE;         //路径最小流量
            ArrayList<Integer> path = new ArrayList<Integer>();
            for (int i = pre[t], k1 = totalEdges.get(i).from; i != -1; k1 = totalEdges.get(i).from, i = pre[k1]) {
                minflow = Math.min(minflow, totalEdges.get(i).cap);
                path.add(k1);
            }
            flow += minflow;
            for (int i = pre[t], k1 = totalEdges.get(i).from; i != -1; k1 = totalEdges.get(i).from, i = pre[k1]) {
                totalEdges.get(i).cap -= minflow;   //当前边减去最小流量
                totalEdges.get(i ^ 1).cap += minflow;  //反向边加上最小流量
            }
            if (minflow != Integer.MAX_VALUE) {

                List<Integer> line = new ArrayList<Integer>();

                for (int i = 1; i < path.size(); i++) {
                    line.add(path.get(i));
                }
                int ss = line.get(line.size() - 1);
                int index = T_list.indexOf(ss);

                line.add(index);
                line.add(minflow);

                string.add(line);
            }
            mincost += dist[t] * minflow;
            if (T_list.contains(path.get(0))) {
                int index = T_list.indexOf(path.get(0));
                output[index] += minflow;
            }
        }
        double sum_error = 0.0;
        for (int i = 0; i < T_cost.size(); i++) sum_error += (T_cost.get(i) - output[i]);
        evaluate out = new evaluate();
        out.cost = mincost + numServer * costServer;
        out.error = sum_error;
        out.list = string;

//        System.out.println(out.error + "============");


        return out;
    }

    private static void readData(String[] graphContent) {

        //文件设置
        lines = graphContent.length;
        String[] arr1 = graphContent[0].split("\\s");
        NN = Integer.valueOf(arr1[0]);  //节点的个数
        E = Integer.valueOf(arr1[1]);  //节点的个数
        num_T = Integer.valueOf(arr1[2]); //消费节点的个数
        costServer = Integer.valueOf(graphContent[2]);  //单位服务器价格
        T_list = new ArrayList<Integer>();
        T_cost = new ArrayList<Integer>();
        for (int i = 4; i < lines - num_T - 1; i++) {
            link.add(graphContent[i]);
        }
        for (int i = lines - num_T; i < lines; i++) {
            String[] s = graphContent[i].split("\\s");
            int a = Integer.valueOf(s[1]);
            int b = Integer.valueOf(s[2]);
            T_list.add(a);
            T_cost.add(b);
        }

        en = 0;
        N = NN + 2;
        head = new int[N];
        for (int i = 0; i < N; i++) {
            head[i] = -1;
        }

        pre = new int[N];
        dist = new int[N];
        visited = new boolean[N];


        for (String str : link) {
            String[] s = str.split("\\s");
            int a = Integer.valueOf(s[0]);
            int b = Integer.valueOf(s[1]);
            int c = Integer.valueOf(s[2]);
            int d = Integer.valueOf(s[3]);
            addedge(a, b, c, d, edgeList);
            addedge(b, a, c, d, edgeList);
        }
        for (int i = 0; i < T_list.size(); i++) {
            addedge(S, T_list.get(i), T_cost.get(i), 0, edgeList); //添加消费节点到超级汇点
        }

        enT = en;
    }

    public static evaluate calC(ArrayList<Integer> server) {
        //*************************************************//

        pre = new int[N];
        dist = new int[N];
        visited = new boolean[N];

        //********************************************//
        List<Edge> totalEdges = new ArrayList<Edge>();
        totalEdges.addAll(edgeList);
        en = enT;

        head[N-1] = -1;

        //添加超级原点,汇点
        S = NN + 0;   //超级原点
        T = NN + 1;  //超级汇点
        numServer = server.size();

        for (int i = 0; i < server.size(); i++) {
            addedge(server.get(i), T, Integer.MAX_VALUE, 0, totalEdges);
        }
        evaluate res = MCMF(S, T, N, totalEdges);
        return res;

    }

    private static String listToString(List list) {
        String res = list.get(0) + "";
        for (int i = 1; i < list.size(); i++) {
            res += " " + list.get(i);
        }
        return res;
    }

    private static String[] changeResult(List<List> resultgraph) {

        int len = resultgraph.size();

        String[] result = new String[len + 2];
        result[0] = len + "";
        result[1] = "";
        for (int i = 0; i < len; i++) {
            result[i + 2] = listToString(resultgraph.get(i));
        }

        return result;
    }


    public static String[] deployServer(String[] graphContent) {
        readData(graphContent);
        GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm(50, 20);
        evaluate calculate = geneticAlgorithm.calculate();

        ArrayList<List> resultgraph = calculate.list;

//        System.out.println(calculate.error);
//        System.out.println(calculate.cost);
//        System.out.println(calculate.list);
        return changeResult(resultgraph);
    }

}

