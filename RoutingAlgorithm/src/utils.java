public class utils {
    
    public static void Dijkstra(Graph g, int startId) {
        int vexnum = g.getVexnum();
        Dis[] disList = g.getDis();
        int[][] matrix = g.getMatrix();
        // 初始化dis
        for (int i = 0; i < vexnum; i++) {
            disList[i] = new Dis();
            disList[i].setPath("v" + String.valueOf(startId) + "--->" + "v" + String.valueOf(i+1));
            disList[i].setCost(matrix[startId - 1][i]);
        }

        // 初始化设置起点到起点的cost为0, 将起点加入集合T(标记为被访问)
        disList[startId-1].setCost(0);
        disList[startId-1].setIsVisited(true);

        int count = 1; // 记录当前被访问过的点的个数
        while (count <= vexnum) {
            // 找到当前距离起点start代价最小的且未被访问过的节点
            int min_id = 0;
            int min_cost = Integer.MAX_VALUE;
            for (int i = 0; i < vexnum; i++) {
                if (disList[i].getIsVisited() == false && disList[i].getCost() <= min_cost) { // 该节点没有被访问过,且cost小于当前最小的cost
                    min_cost = disList[i].getCost();
                    min_id = i;
                }
            }
            // 遍历节点min_id的所有出度节点
            disList[min_id].setIsVisited(true);
            for (int j = 0; j < vexnum; j++) {
                if (disList[j].getIsVisited() == false && matrix[min_id][j] != Integer.MAX_VALUE) { //遍历所有未被访问过的出边节点
                    if (disList[j].getCost() > disList[min_id].getCost() + matrix[min_id][j]) { // 看是否min_id 可以改善起点到目标出边节点的距离.
                        disList[j].setCost(disList[min_id].getCost() + matrix[min_id][j]);
                        disList[j].setPath(disList[min_id].getPath() + "--->" + "v" + String.valueOf(j+1));
                    }
                }
            }
            count++;
        }
    }

    public static void Floyd(Graph g){
        int vexnum = g.getVexnum();
        int[][] matrix = g.getMatrix();
        int[][] path = g.getPath();
        int[][] distance = g.getDistance();

        // 初始化
        for(int i = 0 ; i < vexnum ; i ++){
            for(int j = 0 ; j < vexnum ;j ++){
                path[i][j] = j;
                distance[i][j] = matrix[i][j];
            }
        }

        int select = 0;
        for(int k = 0 ; k < vexnum ; k ++){ // 尝试所有节点作为中介
            for(int i = 0 ; i < vexnum ; i++){
                for(int j = 0 ; j < vexnum ; j++){
                    select = distance[i][k] == Integer.MAX_VALUE || distance[k][j] == Integer.MAX_VALUE ? Integer.MAX_VALUE : (distance[i][k] + distance[k][j]); // 防止溢出
                    if(select < distance[i][j]){
                        distance[i][j] = select;
                        path[i][j] = path[i][k];
                    }
                }
            }
        }
    }

}
