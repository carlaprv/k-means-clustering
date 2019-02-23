
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import kmeans.Coordinate;

public class Kmeans implements InterfaceKmeans{	
    protected int numClusters;                  // numero inicial de clusters
    protected int tamCorpus;                    // numero de elementos do corpus - quantidade de textos
    protected int epocas;                       // numero de epocas
    protected double rigor;                     // rigor da condicao de parada
    protected int tDistancia;                   // tipo da distancia
    protected double erro_quad_medio;          // variavel para armazenar erro quadratico medio da execucao
    protected double silhouetteMedia;          // variavel para armazenar silhouette medio da execucao
    protected ArrayList <Coordinate> centroides = new ArrayList <Coordinate>(); //vetor com centroides
    protected ArrayList <Coordinate> pontos = new ArrayList <Coordinate>(); //vetor com os pontos da matriz do corpus
    protected Coordinate [][] grupos = null; //matriz com coordenadas de cada elemento de cada cluster
    ArrayList <Coordinate> novosCentroides = null; //vetor para calculo dos novos centroides
    protected int dimensao; //dimensoes dos dados - quantidade de palavras
    protected double [] maiorValor;
    protected String [] palavras_corpus; // armazena as palavras do corpus - usado para pos processamento
    protected int quantidade_palavras;  // dimensao dos dados
    protected static String diretorio_resultados;
    


    Random random = new Random();
    
    // argumento 0 vai receber o caminho do arquivo a ser lido
    // argumento 1 vai ser a quantidade K de clusters
    // argumento 2 vai ser o tamanho do corpus = quantidade de textos
    // argumento 3 vai determinar as distancias (0 para cosseno e 1 para euclidiana)
    // argumento 4 vai determinar o rigor da condicao de parada
    // argumento 5 vai determinar o caminho do arquivo de palavras a ser lido
    public static void main(String[]args){  
        // caminho onde serao armazenados os prints do programa - usado como log da execucao
        String dir = System.getProperty("user.dir");
        diretorio_resultados = dir.substring(0,dir.length()-4) + "\\resultados";
        
        File f = new File(diretorio_resultados, "log_execucao.txt");
        try {
            f.createNewFile();
            System.setOut(new PrintStream(f));
        } catch (IOException ex) {
            Logger.getLogger(Kmeans.class.getName()).log(Level.SEVERE, null, ex);
        }         
        
        //recebimento dos parametros pela linha de comando
        if(args.length != 6){
            System.out.println("QUANTIDADE DE ARGUMENTOS INVALIDA");
            System.out.println("Kmeans: uso \narg 0 = caminho do arquivo; \narg 1 = quantidade de clusters; \narg 2 = tamcorpus; \narg 3 = tipo distancia, 0 para cosseno e 1 para euclidiana; \narg 4 = rigor do algoritmo; \n args 5 = caminho para arquivo de leitura das palavras do corpus");
            System.exit(0);
        }
        
        Kmeans agrupamento = new Kmeans();        
        agrupamento.lercsv(args[0]);  
        
        int NumClusters = Integer.parseInt(args[1]);
        int tamanhoCorpus = Integer.parseInt(args[2]);  
        int tipoDistancia = Integer.parseInt(args[3]);
        int epocas = 30;
        double criterio =  Double.parseDouble(args[4]);
        
        //leitura do arquivo de palavras do corpus
        agrupamento.lerPalavrasCorpus(args[5]);          
        
        //chamada de execucao do kmeans
        agrupamento.executarKmeans(NumClusters, tamanhoCorpus, tipoDistancia, epocas, criterio);
        
    }
    
    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////
    //////  EXECUTAR KMEANS - CHAMADAS PARA OUTRAS FUNÇÕES  ///////
    ///////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////
    
     public void executarKmeans(int numClusters, int tamCorpus, int tDistancia, int epocas, double rigor) {
        this.numClusters = numClusters;
        this.tamCorpus = tamCorpus;
        this.tDistancia = tDistancia;
        this.epocas = epocas;
              
        grupos = new Coordinate [numClusters] [tamCorpus];
        
        // iniciacao aleatoria dos centroides
        iniciarCentroides(centroides,  numClusters,  tamCorpus);
        
        do {
            // limpar as variaveis que armazenam os grupos
            limparGrupos(grupos);
            
            //determinar o centroide de cada elemento
            adicionaNoCluster(centroides, pontos, grupos);            
            
            novosCentroides = new ArrayList <Coordinate>();
            
            //calculo dos novos centroides
            novosCentroides = novosCentros (centroides, grupos, novosCentroides);  
            epocas --;
        } while(condicaoDeParada (centroides,novosCentroides) && epocas > 0);
        
        ///Calculo silhouette
        try {            
            this.silhouetteMedia = Silhouette.calculoSilhouette(pontos, tamCorpus, numClusters,this);
        } catch (Exception ex) {
            Logger.getLogger(Kmeans.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        ///Calculo erro quadratico medio
        try {
            this.erro_quad_medio = ErroQuadratico.calculoErro(centroides, pontos, tamCorpus, numClusters, this);
        } catch (Exception ex) {
            Logger.getLogger(Kmeans.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //arquivo com dados da silhouette e grupo para cada documento/texto lido     
        gerarArquivoCsv(diretorio_resultados);
        
        //arquivo amostrado com dados da silhouette e grupo para cada documento/texto lido  
        gerarArquivoCsvGrafico(diretorio_resultados);
        
        //arquivo com dados da silhouette, EQM e outros dados da execucao
        gerarLogFinal(diretorio_resultados);
        
        
        /// arquivo gerado para cada K - representacao em nuvem de palavras de cada cluster     
        for(int k = 0; k < numClusters; k++){
            String caminho = diretorio_resultados + "\\nuvem_cluster_" + k + ".txt";
            gerarArquivoNuvemPalavras(caminho, k);
        }

        

    }
    

    //inicializando o vetor de centroides aleatorios
    public void iniciarCentroides(ArrayList <Coordinate> centroides, int numClusters, int tamCorpus) { 
        
        for(int i = 0; i < numClusters; i++) {
            Coordinate centro = new Coordinate();
            double [] coordenadas = new double[dimensao];

            //definindo aleatoriamente as coordenadas de cada centro
            for(int j = 0; j < dimensao; j++){
                coordenadas[j] = random.nextInt((int)(maiorValor[j]+1));                 
            }          
            
            centro.setCoordenadas(coordenadas);
            
            //adicionando a lista de centroids, as coordenadas criadas
            centroides.add(i, centro);
        }
    }

    //distancia euclidiana
    public double distanciaEuclidiana (Coordinate p1,  Coordinate p2) {
        double resp = 0;
        
        for(int j = 0; j < dimensao; j++){
            resp += Math.pow(Math.abs(p1.getCoordenadas()[j] - p2.getCoordenadas()[j]), 2);            
        }     
        return Math.sqrt(resp);
    }
    
    //calculo da similaridade coseno
    public double similaridadeCoseno (Coordinate p1,  Coordinate p2) {
        double somaNumerador = 0;
        double somaDenominador1 = 0;
        double somaDenominador2 = 0;
        double denominador = 0;
        double temp = 0;
        double dist = 0;
        
        for(int j = 0; j < dimensao; j++){           
            somaNumerador += Math.abs(p1.getCoordenadas()[j] * p2.getCoordenadas()[j]);              
            somaDenominador1 += Math.abs(p1.getCoordenadas()[j] * p1.getCoordenadas()[j]);            
            somaDenominador2 += Math.abs(p2.getCoordenadas()[j] * p2.getCoordenadas()[j]);            
        }        
       
        denominador = Math.sqrt(somaDenominador1) * Math.sqrt(somaDenominador2);
        
        temp = somaNumerador/denominador;
        
        dist = 1 - temp;
        
        return dist;
    }
    
    public double distancias(Coordinate p1,  Coordinate p2){
        //distanciaCosseno
        if(tDistancia == 0){
            return similaridadeCoseno(p1,p2);            
        }
        //distancia euclidiana
        else{
            return distanciaEuclidiana(p1,p2);
        }          
    }
         

    //leitura do arquivo csv de dados - importacao da representacao
    public void lercsv (String caminho){

    	try{
            File f = new File(caminho);
            Scanner sc = new Scanner(f);
            int linha = 0;
            
            while(sc.hasNextLine()){
                String [] line = sc.nextLine().split(",");
                Coordinate c = new Coordinate();
                
                double [] dados = new double[line.length];
                dimensao = line.length;
                
                //primeira iteracao - inicializa vetor de maiores valores
                if(maiorValor == null){
                    maiorValor = new double[dimensao];
                }
                
                //para cada linha, armazena os valores de cada dimensao
                for(int i = 0; i < line.length; i++){
                    dados[i] = Double.parseDouble(line[i]);                    
                    //guarda maior valor de cada dimensao
                    if(dados[i] >= maiorValor[i]){
                        maiorValor[i] = dados[i];
                    }
                }
                c.setCoordenadas(dados);
                
                /// adicionado id daquele ponto em relacao a ordem original de leitura
                c.setId(linha);
                
                // adicionando texto c ao vetor de pontos
                pontos.add(c);                
                linha++;
            }  
    	}
    	catch(IOException e){

    	}
    }
    
    
    //leitura do arquivo de palavras do corpus
    public void lerPalavrasCorpus (String caminho){
    	try{
            File f = new File(caminho);
            Scanner sc = new Scanner(f);
            int linha = 0;            
            while(sc.hasNextLine()){
                
                String [] line = sc.nextLine().split(",");   
                quantidade_palavras = line.length;  
                palavras_corpus = new String[line.length];
                int qtd_palavras = line.length; 
                for(int i = 0; i < line.length; i++){
                    palavras_corpus[i] = (line[i]); 
                }
                linha++;
            }  
    	}
    	catch(IOException e){
            System.out.println("erro leitura corpus palavras");

    	}
    }
    
    
    public void adicionaNoCluster(ArrayList <Coordinate> centroides, ArrayList <Coordinate> pontos, Coordinate [][] grupos) {
        double menorDist;
        int cluster;
        
        //percorre o array de pontos
        for(int p = 0; p < pontos.size(); p++) {	
            //maior distancia possivel
            menorDist = Double.MAX_VALUE;
            cluster = 0;

            //para cada ponto P, percorre todos os centroides até encontrar o centroide mais proximo
            for(int c = 0; c < centroides.size(); c++) {
                double distAtual = distancias(pontos.get(p), centroides.get(c));
                
                    if(distAtual < menorDist) {
                        menorDist = distAtual;
                        //guarda o valor do centroide (ou cluster) mais proximo
                        cluster = c;
                        pontos.get(p).setClusters(cluster);
                   }
               
            }
            //dentro do vetor de grupos
            //adiciona esse ponto no cluster mais proximo
            
            for(int i = 0; i < pontos.size(); i++) {
                if(grupos[cluster][i] == null) {
                    grupos[cluster][i] = pontos.get(p);       
                    break;
                }
            }

        }//fim do for       

        /////////////////////////////////////////////////////////
        /////////////verificar distribuicao dos grupos  /////////
        /////////////////////////////////////////////////////////
        int cont = 0;
        for(int c = 0; c < centroides.size(); c++) {
            for(cont = 0;cont < tamCorpus && grupos[c][cont] != null;cont++){
                
            }
            System.out.println("grupo " + c + " - filhos: " + cont);
        }
        System.out.println("");
           
        
    }

public ArrayList<Coordinate> novosCentros (ArrayList <Coordinate> centroides, Coordinate [][] grupos, 
    ArrayList <Coordinate> novosCentroides){
    int c;
    //percorrendo os centros
    for(int k = 0; k < centroides.size(); k++) {
        double [] novaCoordenada = new double[dimensao];
        
        //preenche o vetor novaCoordenada com as coordenadas no grupo K
        for(int j = 0; j < dimensao; j ++){
            Coordinate centro = centroides.get(k);                       
            novaCoordenada[j] = centro.getCoordenadas()[j];
        }
        
        //altera o valor de novacoordenada como media de cada atributo de todos os elementos do cluster k
        for(int i = 0; i < dimensao; i ++){            
            //olha cada elemento pertencente ao cluster k
            for(c = 0; c < tamCorpus && grupos[k][c] != null; c++) {            
                novaCoordenada[i] = Math.abs(novaCoordenada[i] + grupos[k][c].getCoordenadas()[i]);                
            }               
            //c + 1 = quantidade de elementos do cluster k
            novaCoordenada[i] = novaCoordenada[i]/(c+1);
        }  
        
        Coordinate novoCentro = new Coordinate(novaCoordenada);               
        novosCentroides.add(k, novoCentro);
        
    } 
   
    return novosCentroides;
    }


    //checar distancia entre os centroides
    // chamar distancia

    public boolean condicaoDeParada(ArrayList <Coordinate> centroides, ArrayList <Coordinate> novosCentroides){
        //menor media - menor deslocamento de centroides, cluster quase não mudaram
        double distancia = 0;
        for(int i = 0; i < centroides.size(); i++){
            distancia += distancias(centroides.get(i), novosCentroides.get(i));    
        }

        double mediaDistancias = (distancia/centroides.size());
        
        // se os centroides deslocaram muito pouco, interromper as iteracoes
        if(mediaDistancias < rigor){
            return false;
        } 
        
        //se a mudanca foi significativa, continua a iteracao e atualizamos os valores de centroides
        else{
           for(int i = 0; i < centroides.size(); i++){
               
               this.centroides.set(i,novosCentroides.get(i));  
           }             
           return true;    
        }       
              
    }
    
    
    public void limparGrupos (Coordinate [][] grupos){
         ///limpar distribuicao dos grupos para armazenar novos valores para a proxima iteracao
        int filho = 0;
        for(int c = 0; c < centroides.size(); c++) {
            for(filho = 0; filho < tamCorpus && grupos[c][filho] != null;filho++){
                grupos[c][filho] = null;
            }
        }      
    }
    

     //////////////////////////////////////////////////////////
    ////////////////////////ARQUIVO 1 /////////////////////////
    ////////////////////////////////////////////////////////////
    
    // Informacoes de cada documento
    
    public void gerarArquivoCsvGrafico(String sFileName)   {
        try{
            File f = new File(sFileName, "resultado_agrupamento_amostrado.csv");
            f.createNewFile();
            
            FileWriter writer = new FileWriter(f);           

            writer.append("cluster");
            writer.append(',');
            writer.append("id_texto");
            writer.append(',');
            writer.append("sil_texto");
            writer.append('\n');
            
            //ordem do Sil - MAIOR para MENOR para plotar o grafico do cotovelo
            Collections.sort(pontos);
            
            for(int k = 0; k < numClusters;k++){
                for(int i = 0; i < tamCorpus; i++){
                    //olhar filhos do grupo k apenas
                    if(pontos.get(i).getCluster() == k){                        
                        int sorteio = random.nextInt((int)(10));                         
                        if(sorteio < 2){
                            writer.append(pontos.get(i).getCluster() + "");
                            writer.append(',');
                            writer.append(pontos.get(i).getId() + "");
                            writer.append(',');
                            writer.append(pontos.get(i).getSilhouette() + "");
                            writer.append('\n');                              
                        }                     
                    }                
                }                
            }            
            writer.flush();
            writer.close();
        }
        catch(IOException e){
             e.printStackTrace();
        } 
    }
    
    public void gerarArquivoCsv(String sFileName)   {
        try{
            File f = new File(sFileName, "resultado_agrupamento.csv");
            f.createNewFile();
            
            FileWriter writer = new FileWriter(f);           

            writer.append("cluster");
            writer.append(',');
            writer.append("id_texto");
            writer.append(',');
            writer.append("sil_texto");
            writer.append('\n');
            
            //ordem do Sil - MAIOR para MENOR
            Collections.sort(pontos);
            
            for(int k = 0; k < numClusters;k++){
                for(int i = 0; i < tamCorpus; i++){
                    //olhar filhos do grupo k apenas
                    if(pontos.get(i).getCluster() == k){                       
                        writer.append(pontos.get(i).getCluster() + "");
                        writer.append(',');
                        writer.append(pontos.get(i).getId() + "");
                        writer.append(',');
                        writer.append(pontos.get(i).getSilhouette() + "");
                        writer.append('\n');    
                    }                
                }                
            }            
            writer.flush();
            writer.close();
        }
        catch(IOException e){
             e.printStackTrace();
        } 
    }
    
    //////////////////////////////////////////////////////////
    ////////////////////////ARQUIVO 2 /////////////////////////
    ////////////////////////////////////////////////////////////
    
    //// gerar arquivo para nuvems de palavras   
    
    public void gerarArquivoNuvemPalavras(String sFileName, int k)   {
        try{
            File f = new File(sFileName);
            f.createNewFile();
            
            FileWriter writer = new FileWriter(f);          

            int [] freq_pal = new int[quantidade_palavras];
            
            //para cada palavra
            for(int w = 0; w < quantidade_palavras; w++){
                for(int i = 0; i < tamCorpus; i++){
                    //olhar filhos do grupo k apenas
                    if(pontos.get(i).getCluster() == k){                        
                        freq_pal[w] += (int)pontos.get(i).getCoordenadas()[w];                                       
                    }                
                }
                
                if(freq_pal[w] > 100){
                    writer.append(palavras_corpus[w]);
                    writer.append('\t');
                    writer.append(freq_pal[w] + "");
                    writer.append('\n');
                }
                
            }
            writer.flush();
            writer.close();
        }
        catch(IOException e){
             e.printStackTrace();
        } 
    }
    
    
    //////////////////////////////////////////////////////////
    ////////////////////////ARQUIVO 3 /////////////////////////
    ////////////////////////////////////////////////////////////
    
    /// dados gerais da execucao    
   
    public void gerarLogFinal(String sFileName)   {
        try{
            File f = new File(sFileName, "resultado_execucao.txt");
            f.createNewFile();
            
            FileWriter writer = new FileWriter(f);           

            writer.append("quant_clusters");
            writer.append(',');
            writer.append("rigor");
            writer.append(',');
            writer.append("distancia");
            writer.append(',');
            writer.append("erro_quad_medio");
            writer.append(',');   
            writer.append("sil_medio");
            writer.append('\n');
            
            ////////////////////////////////////////////
            ////    ESCREVENDO DADOS DA EXECUCAO   /////
            ////////////////////////////////////////////
            writer.append(numClusters + "");
            writer.append(',');
            writer.append(rigor + "");
            writer.append(',');
            if(tDistancia == 0){
                writer.append("cosseno");
                writer.append(',');
            }else{
                writer.append("euclidiana");
                writer.append(',');
            }
            writer.append(erro_quad_medio + "");
            writer.append(',');  
            writer.append(silhouetteMedia + "");
            writer.append('\n');
            
            writer.flush();
            writer.close();
        }
        catch(IOException e){
             e.printStackTrace();
        } 
    }
    
    
        
}