import re
import nltk

nltk.download('stopwords')

from nltk.corpus import reuters
from nltk.corpus import stopwords
from nltk import SnowballStemmer
from nltk.util import ngrams
from nltk.tokenize import word_tokenize
from nltk.tokenize import sent_tokenize

#Ainda verificando os pacotes
#Primeira versao do codigo feita anteriormente ainda nao incluida