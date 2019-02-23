#Preprocessamento

import os
import re
import nltk
import json
import pandas
nltk.download('stopwords')
from nltk import regexp_tokenize
from nltk.corpus import stopwords
from nltk import SnowballStemmer
from math import log10

#Metodo que le os arquivos e cria uma string com a informacao dos textos que entraram e os que nao
def read_files(path, corpus_list_text, log):
	message = ''
	for filename in os.listdir(path):
		new_path = path + '/' + filename
		text = open(new_path, "r")
		text_content = text.read()
		if len(text_content) > 250:
			corpus_list_text.append(text_content)
			message = str("Texto" + filename + " lido com sucesso") 
		else:
			message = str("Texto" + filename + " nao utilizado por tamanho insuficiente")
		log.append(message)

#Metodo que cria o arquivo de log formato json.
def create_log(path, log, len_corpus, len_list_tokens, list_tokens):
	log_path = path + 'log_reuters.json'
	log_registration = open(log_path, "w")
	print("Registrando textos lidos e nao lidos")
	new_dict = dict(texts = log, corpus_lenght = len_corpus, tokens_list_lenght = len_list_tokens, terms_list = list_tokens)
	log_registration.write(json.dumps(new_dict))
	return 1

#Metodo que escreve qualquer arquivo no formato txt.
def write_corpus(text_path, corpus_list_text):
	corpus_text = open(text_path, "w")
	corpus_text.write(str(corpus_list_text))
	return 1

#Metodo que recebe um dataFrame Pandas e o transforma em um arquivo csv.
def write_rep(rep, path):
	new_path = path + '.csv'
	rep.to_csv(new_path, header=False, index=False)
	return 1

#Metodo que realiza o pre processamento do corpus propriamente: coordena a sequencia de execucao
#Foldcase, depois criacao dos tokens, remocao das stopwords e por fim stemmizacao
#Retirado do Relatorio Tecnico PPgSI-001/2018
def process_corpus(corpus, params):
    (param_foldCase, param_listOfStopWords, param_stemmer) = params
    newCorpus = []
    for document in corpus:
        document = document.rstrip("\n")
        document = foldCase(document, param_foldCase)
        listOfTokens = tokenize(document)
        listOfTokens = remove_stopwords(listOfTokens, param_listOfStopWords)
        listOfTokens = apply_stemming(listOfTokens, param_stemmer)
        newCorpus.append(listOfTokens)
    return newCorpus

#Metodo que realiza o foldcase (letras todas minusculas, nesse caso)
#Retirado do Relatorio Tecnico PPgSI-001/2018
def foldCase(sentence, parameter):
    if(parameter): sentence = sentence.lower()
    return sentence

#Metodo que realiza a tokenizacao, alterando algumas expressoes (US,-,_), tokenizando e eliminando unidades menores do que 3
#Inspirado no Relatorio Tecnico PPgSI-001/2018 (contem alteracoes)
def tokenize(sentence):
	sentence = sentence.replace("_"," ").replace("u.s.","usa").replace("-","")
	words = (regexp_tokenize(sentence, '[a-z]+', gaps=False))
	return (list(filter(lambda token: len(token) > 3, words)))

#Metodo que remove as stopwords (lista parametrizada) da lista de tokens (tambem parametrizada)
#Retirado do Relatorio Tecnico PPgSI-001/2018
def remove_stopwords(listOfTokens, listOfStopWords):
    return [token for token in listOfTokens if token not in listOfStopWords]

#Metodo que aplica a stemmizacao para a lista de tokens passada como parametro
#Retirado do Relatorio Tecnico PPgSI-001/2018
def apply_stemming(listOfTokens, stemmer):
    return [stemmer.stem(token) for token in listOfTokens]

#Metodo que cria a representacao tf a partir da comparacao do corpus (em forma de matriz) e da lista de termos
def tf(corpus, term_list):

	tf_matrix = []
	for text in range(len(corpus)):
		tf_matrix.append([])
		for term in range(len(term_list)):
			contador = 0
			for token in corpus[text]:
				if token == term_list[term]:
					contador += 1
			tf_matrix[text].append(contador)

	return tf_matrix

#Metodo que cria a representacao tf_idf a partir da comparacao do corpus (em forma de matriz) e da lista de termos
def tf_idf(corpus, term_list, binary_matrix):

	n = len(corpus)
	nt = []
	idf = []

	j = 0
	while j < len(binary_matrix[0]):
		cont = 0
		i = 0
		while i < len(binary_matrix):
			if binary_matrix[i][j] == 1:
				cont = cont + 1
			i += 1
		nt.append(cont)
		j += 1

	k = 0
	while k < len(nt):
		idf.append(log10(n/nt[k]))
		k += 1

	tf_idf_matrix = []
	for text in range(len(corpus)):
		tf_idf_matrix.append([])
		for term in range(len(term_list)):
			contador = 0
			for token in corpus[text]:
				if token == term_list[term]:
					contador += 1
			tf_idf_matrix[text].append(contador * idf[term])

	return tf_idf_matrix

#Metodo que cria a representacao binaria a partir da comparacao do corpus (em forma de matriz) e da lista de termos
def binary(corpus, terms):

	binary_matrix = []
	for text in range(len(corpus)):
		binary_matrix.append([])
		for term in range(len(terms)):
			if terms[term] in corpus[text]:
				binary_matrix[text].append(1)
			else:
				binary_matrix[text].append(0)

	return binary_matrix

#Metodo que realiza a limpeza da lista de termos e da representacao binara simultaneamente
#A limpeza eh realizada de acordo com os parametros minimo (inteiro) e maximo (porcentagem)
def cleaning_terms(df, term_list, minimo, maximo):
	
	i = len(term_list) - 1
	while (i >= 0):
		total = df[i].sum()
		if(total < minimo or total > round(len(df[0]) * maximo)):
			df = df.drop(i, axis = 1)
			del term_list[i]
		i -= 1
	return df

#Metodo que cria a lista de termos a partir do corpus
def create_term_list(corpus):

	#cada linha eh um texto e cada coluna eh um token
	term_list = []
	for text in corpus:
		for token in text:
			if token not in term_list:
				term_list.append(token)

	return term_list

def main():

	#Inserir o endereco de entrada, ou seja, onde estao os arquivos:
	path_in = "../reuters/nltk/reuters/"
	#Inserir o endereco de saida, onde devem ficar registradas as representacoes:
	path_out = "../corpora/"
	
	corpus_list_text = []
	log = []
	directories = ['test', 'training']
	for folder in directories:
	 	new_path = path_in + folder
	 	print("Iniciando leitura do arquivo: " + new_path)
	 	read_files(new_path, corpus_list_text, log)

	print("Iniciando escrita do corpus original")
	write_corpus_status = write_corpus(path_out + "corpus_reuters.txt", corpus_list_text)
	print("Status da escrita: " + str(write_corpus_status))

	print("Iniciando preprocessamento dos textos")
	#Utilizar fold case?
	param_foldCase = True
	#Qual o idioma dos textos?
	param_language = "english"
	
	#Lista de stopwords a ser utilizada como parametro
	##Usando NLTK:
	param_listOfStopWords = stopwords.words(param_language)
	####Usando a lista da UCD (University College Dublin)
	#param_listOfStopWords = ['NULL', 'a', 'about', 'above', 'according', 'across', 'actually', 'adj', 'after', 'afterwards', 'again', 'all', 'almost', 'along', 'already', 'also', 'although', 'always', 'am', 'among', 'amongst', 'an', 'and', 'another', 'any', 'anyhow', 'anyone', 'anything', 'anywhere', 'are', 'aren', "aren't", 'around', 'as', 'at', 'be', 'became', 'because', 'become', 'becomes', 'been', 'beforehand', 'begin', 'being', 'below', 'beside', 'besides', 'between', 'both', 'but', 'by', 'can', "can't", 'cannot', 'caption', 'co', 'come', 'could', 'couldn', "couldn't", 'did', 'didn', "didn't", 'do', 'does', 'doesn', "doesn't", 'don', "don't", 'down', 'during', 'each', 'early', 'eg', 'either', 'else', 'elsewhere', 'end', 'ending', 'enough', 'etc', 'even', 'ever', 'every', 'everywhere', 'except', 'few', 'for', 'found', 'from', 'further', 'had', 'has', 'hasn', "hasn't", 'have', 'haven', "haven't", 'he', 'hence', 'her', 'here', 'hereafter', 'hereby', 'herein', 'hereupon', 'hers', 'him', 'his', 'how', 'however', 'i.e.', 'ie', 'if', 'in', 'inc', 'inc.', 'indeed', 'instead', 'into', 'is', 'isn', "isn't", 'it', 'its', 'itself', 'last', 'late', 'later', 'less', 'let', 'like', 'likely', 'll', 'ltd', 'made', 'make', 'makes', 'many', 'may', 'maybe', 'me', 'meantime', 'meanwhile', 'might', 'miss', 'more', 'most', 'mostly', 'mr', 'mrs', 'much', 'must', 'my', 'myself', 'namely', 'near', 'neither', 'never', 'nevertheless', 'new', 'next', 'no', 'nobody', 'non', 'none', 'nonetheless', 'noone', 'nor', 'not', 'now', 'of', 'off', 'often', 'on', 'once', 'only', 'onto', 'or', 'other', 'others', 'otherwise', 'our', 'ours', 'ourselves', 'out', 'over', 'own', 'per', 'perhaps', 'rather', 're', 'said', 'same', 'say', 'seem', 'seemed', 'seeming', 'seems', 'several', 'she', 'should', 'shouldn', "shouldn't", 'since', 'so', 'some', 'still', 'stop', 'such', 'taking', 'ten', 'than', 'that', 'the', 'their', 'them', 'themselves', 'then', 'thence', 'there', 'thereafter', 'thereby', 'therefore', 'therein', 'thereupon', 'these', 'they', 'this', 'those', 'though', 'thousand', 'through', 'throughout', 'thru', 'thus', 'to', 'together', 'too', 'toward', 'towards', 'under', 'unless', 'unlike', 'unlikely', 'until', 'up', 'upon', 'us', 'use', 'used', 'using', 've', 'very', 'via', 'was', 'wasn', 'we', 'well', 'were', 'weren', "weren't", 'what', 'whatever', 'when', 'whence', 'whenever', 'where', 'whereafter', 'whereas', 'whereby', 'wherein', 'whereupon', 'wherever', 'whether', 'which', 'while', 'whither', 'who', 'whoever', 'whole', 'whom', 'whomever', 'whose', 'why', 'will', 'with', 'within', 'without', 'won', 'would', 'wouldn', "wouldn't", 'yes', 'yet', 'you', 'your', 'yours', 'yourself', 'yourselves']
	
	#Lista de radicais para o idioma escolhido
	param_stemmer = SnowballStemmer(param_language)
	params = (param_foldCase, param_listOfStopWords, param_stemmer)
	pcorpus = process_corpus(corpus_list_text, params)

	print("Iniciando escrita do corpus preprocessado")
	write_corpus_status = write_corpus(path_out + "corpus_processado_reuters.txt", pcorpus)
	print("Status da escrita: " + str(write_corpus_status))

	print("Inicio da criacao da lista de termos")
	term_list = create_term_list(pcorpus)
	print("Criacao da lista de termos concluida")

	#Cria primeira representacao binaria, com o corpus completo
	print("Inicio da criacao da representacao binaria")
	binary_matrix = binary(pcorpus, term_list)
	print("Criacao da representacao binaria concluida")
	#Transforma a matriz em df do Pandas
	binary_matrix_df = pandas.DataFrame(binary_matrix)

	#Filtra a lista de termos e a representacao binaria anteriormente gerada
	print("Iniciando limpeza da representacao binaria e da lista de termos")
	df_binary = cleaning_terms(binary_matrix_df, term_list, 5, 0.33)
	print("Limpeza da representacao binaria e da lista de termos concluida")

	print("Iniciando escrita do log")
	create_log_status = create_log(path_out, log, len(corpus_list_text), len(term_list), term_list)
	print("Status da escrita: " + str(create_log_status))
	
	print("Iniciando escrita da representacao binaria")
	write_bin_status = write_rep(df_binary, path_out + 'rep_binaria_reuters')
	print("Status da escrita: " + str(write_bin_status))

	print("Iniciando escrita da representacao tf")
	tf_matrix = tf(pcorpus, term_list)
	write_tf_matrix_status = write_corpus(path_out + "tf_reuters.txt", tf_matrix)
	print("Status da escrita: " + str(write_tf_matrix_status))
	
	print("Iniciando escrita da representacao tf_idf")
	tf_idf_matrix = tf_idf(pcorpus, term_list, binary_matrix)
	write_tf_idf_matrix_status = write_corpus(path_out + "tf_idf_reuters.txt", tf_matrix)
	print("Status da escrita: " + str(write_tf_matrix_status))

main()