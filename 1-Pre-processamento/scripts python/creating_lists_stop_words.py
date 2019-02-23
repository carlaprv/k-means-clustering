import os

def cria_lista_stop_words(path_in):
	lista = open(path_in, 'r')
	lista_saida = []
	for linha in lista.readlines():
		for palavra in linha.split():
			lista_saida.append(palavra)
	return lista_saida

def controle(): 
	path_in = "stop_words_ucd_english.txt"
	lista_saida = cria_lista_stop_words(path_in)
	lista_saida.sort()
	print(lista_saida)

controle()