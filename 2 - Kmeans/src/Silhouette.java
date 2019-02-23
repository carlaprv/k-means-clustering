import java.util.ArrayList;
import kmeans.Coordinate;

/**
 *
 * @author Carla Vieira
 */
public abstract class Silhouette {
    public static double calculoSilhouette(ArrayList<Coordinate> pontos, int tamCorpus, int numClusters, Kmeans kmeans) throws Exception {        
        
        System.out.println("####  Iniciando calculo silhouette  ####");
        
        //para cada ponto no conjunto de pontos    
        for(int p = 0; p < pontos.size(); p++) {
            double resultado = 0;

            //determina o INDICE do centro desse ponto
            int indice_centro = pontos.get(p).getCluster();
            //media do valor dentro do grupo
            double mediaGrupo = 0;

           //1 - media para cada elemento do mesmo grupo               
            int num_filhos = 0;

            for(int j = 0; j < tamCorpus; j++){
                //j não é o ponto p, e o centro do ponto é o mesmo centro do ponto p
                if(pontos.get(j).getCluster() == indice_centro && j != p){
                    //calcule as distancias entre o ponto escolhido e os pontos do cluster/grupo                       
                    mediaGrupo += kmeans.distancias(pontos.get(p), pontos.get(j)); 
                    num_filhos++;  
                }                    
            }	
            //media dos valores (tamanho das listas menos 1 que é o proprio ponto)
            
            if(num_filhos == 0){
                mediaGrupo = 0;    
                System.out.println("resolvendo divisao por 0");
            }
            else{
                mediaGrupo = mediaGrupo/(num_filhos);
            }
            
            
                
                
            //2 - media para elementos de centros diferentes do centro do ponto p
            double menorMediaCluster = Double.MAX_VALUE;
            double mediaDistanciasClusters = 0;
            for (int k = 0; k < numClusters; k++){
                int tamanho_cluster = 0;
                if(k != indice_centro){
                    for(int j = 0; j < tamCorpus; j++){                       
                        if(pontos.get(j).getCluster() != indice_centro && pontos.get(j).getCluster() == k){
                            //calcule as distancias entre o ponto escolhido e os pontos do cluster/grupo
                            mediaDistanciasClusters += kmeans.distancias(pontos.get(p), pontos.get(j)); 
                            tamanho_cluster++;
                        }                        
                    }
                    if(tamanho_cluster == 0){
                        mediaDistanciasClusters = 0;
                    }
                    else{
                        mediaDistanciasClusters = mediaDistanciasClusters/tamanho_cluster;
                    }     
                    menorMediaCluster = Math.min(menorMediaCluster, mediaDistanciasClusters);
                }  
            }			
            //3 - silhouette do ponto p
            resultado = (menorMediaCluster - mediaGrupo)/Math.max(mediaGrupo, menorMediaCluster);
            pontos.get(p).setSilhouette(resultado);

        }        
        
       
        double valorSil = 0;

        for(int p = 0; p < pontos.size(); p++){
            valorSil += pontos.get(p).getSilhouette();
        }
        
        double silhouetteMedia = 0;
        
        //4 - Silhouette media       
        
        if(pontos.size() == 0){
            System.out.println("sil_means:" + silhouetteMedia);
            return silhouetteMedia;
        }
       
        silhouetteMedia = valorSil/pontos.size();
        System.out.println("sil_means:" + silhouetteMedia);
        return silhouetteMedia;
    
        
        
    }
	
}