

import kmeans.Coordinate;
import java.util.ArrayList;
import kmeans.Coordinate;

public interface InterfaceKmeans {	
    public void iniciarCentroides(ArrayList <Coordinate> centroides, int numClusters, int tamCorpus);
    public double distanciaEuclidiana (Coordinate p1,  Coordinate p2);
    public double similaridadeCoseno (Coordinate p1,  Coordinate p2) ;
    public void adicionaNoCluster(ArrayList <Coordinate> centroides, ArrayList <Coordinate> pontos, Coordinate [][] distancias);
    public ArrayList<Coordinate> novosCentros (ArrayList <Coordinate> centroides, Coordinate [][] distancias, ArrayList <Coordinate> novosCentroides);
    public void executarKmeans(int numClusters, int tamCorpus, int tipoDistancia, int epocas, double rigor);
    public boolean condicaoDeParada(ArrayList <Coordinate> centroides, ArrayList <Coordinate> novosCentroides);

}
