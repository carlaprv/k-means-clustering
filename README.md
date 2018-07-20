# K-means-clustering
Implementação do algoritmo de agrupamento K-Means em Java. Este projeto tem como objetivo ser utilizado como trabalho na disciplina de Inteligência Artificial e ser utilizado futuramente para fins didáticos.


## Descrição
A idéia do algoritmo K-Means (também chamado de K-Médias) é fornecer uma classificação de informações de acordo com os próprios dados. Esta classificação, como será vista a seguir, é baseada em análise e comparações entre os valores numéricos dos dados. Desta maneira, o algoritmo automaticamente vai fornecer uma classificação sem a necessidade de nenhuma supervisão humana, ou seja, sem nenhuma pré-classificação existente. Por causa desta característica, o K-Means é considerado como um algoritmo de mineração de dados não supervisionado.

# Sobre o projeto
O algoritmo foi implementado para realizar análise dos textos da BBC (http://mlg.ucd.ie/howmanytopics/index.html). Para realizar análise dos textos, os mesmos foram pré-processados em python.


### Parâmetros
O único parâmetro para o K-Means é o número de Ks, que na verdade é o número de grupos esperado. No entanto, no código apresentado outros parâmetros foram utilizados:

* parâmetro 0 - caminho do arquivo de dados (em .csv);
* parâmetro 1 - quantidade de clusters; 
* parâmetro 2 - quantidade de elementos/dados; 
* parâmetro 3 - tipo distância, 0 para similaridade do cosseno e 1 para distância euclidiana;
* parâmetro 4 - rigor da condição de parada;
* parâmetro 5 - caminho do arquivo de palavras do corpus (para pós-processamento do agrupamentos).


### Execução do código
O comando abaixo deve ser utilizado para executar o código

```
java Kmeans <caminho da matriz de dados> <numero de clusters> <tamCorpus> <rigor> <caminho arquivo de palavras corpus>
```
