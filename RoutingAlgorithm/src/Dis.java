public class Dis {
    private String path; // 起点到该点的最短路径信息 
    private boolean isVisited; // 该点是否被访问过
    private int cost; // 起到到该点的权重和

    Dis(){
        this.path = "";
        this.isVisited = false;
        this.cost = 0;
    }

// ------------------------------------------
    public String getPath(){
        return this.path;
    }

    public boolean getIsVisited(){
        return this.isVisited;
    }

    public int getCost(){
        return this.cost;
    }

// ------------------------------------------
    public void setPath(String path){
        this.path = path;
    }

    public void setIsVisited(boolean isVisited){
        this.isVisited = isVisited;
    }

    public void setCost(int cost){
        this.cost = cost;
    }
}
