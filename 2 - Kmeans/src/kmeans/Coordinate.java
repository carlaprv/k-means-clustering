package kmeans;
public class Coordinate implements Comparable<Coordinate>{
    //coordenados do ponto
    private double [] coordenadas;
        
    // sil do ponto
    private double silhouette; 
    
    // id do documento
    private int id;
    
    // cluster atual desse ponto
    private int cluster;
    
    
    public void setSilhouette(double silhouette){
        this.silhouette = silhouette;
    }   
    
    public double getSilhouette(){
        return silhouette;
    }
    
    public void setId(int id){
        this.id = id;
    }   
    
    public int getId(){
        return id;
    }
    
    @Override
    public int compareTo(Coordinate outroPonto) {
        //ordem crescente para clusters
        if(outroPonto.getCluster() != this.cluster){
            return Integer.compare(this.cluster,outroPonto.getCluster());
        }
        
        //ordem descrecente para silhouette
        return Double.compare(outroPonto.getSilhouette(), this.silhouette);
        
    }
   
    public void setClusters(int cluster){
        this.cluster = cluster;
    }   
    
    public int getCluster(){
        return cluster;
    }
    
    public Coordinate() {
    }
    

    public Coordinate(double [] coordenadas) {
            setCoordenadas(coordenadas);
            //setY(y);
    }

    public double [] getCoordenadas() {
            return coordenadas;
    }

    public void setCoordenadas(double [] coordenadas) {
            this.coordenadas = coordenadas;
    }
    
    public String toString(){
        String s = "";
        for(double d : coordenadas){
            s += d + ",";
        }
        
        return s;       
    }

  
}
