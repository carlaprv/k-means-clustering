import java.util.ArrayList;
import kmeans.Coordinate;

/**
 *
 * @author Carla Vieira
 */
public abstract class ErroQuadratico {
    public static double calculoErro(ArrayList <Coordinate> centroides, ArrayList<Coordinate> pontos, int tamCorpus, int numClusters, Kmeans kmeans) throws Exception {        
        
        System.out.println("####  Iniciando calculo erro quadratico  ####");

        double erro_quad_medio = 0;        
        double distPontoCentro = 0;
        double erro = 0;
        //para cada centro k
        for (int k = 0; k < numClusters; k++){    
            // olhar filhos do centro k
            for(int j = 0; j < tamCorpus; j++){                       
                if(pontos.get(j).getCluster() == k){
                    //calcule as distancias entre o ponto escolhido e o seu centro
                    distPontoCentro = (kmeans.distancias(pontos.get(j), centroides.get(k)));

                    erro += distPontoCentro * distPontoCentro; 
                }                        
            }    
         }  
        
        if(tamCorpus == 0){
            erro_quad_medio = 0;
        }
        else{
            erro_quad_medio = erro/tamCorpus;
        }
        
        System.out.println("erro_quad_medio " + erro_quad_medio);        
        return erro_quad_medio;                
    }
    
}
