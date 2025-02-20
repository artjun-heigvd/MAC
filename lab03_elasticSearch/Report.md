# Rapport labo 3
> Arthur Junod et Guillaume Dunant

## Indexing

### D.1
#### Création index
```
PUT /cacm_standard
{
  "mappings": {
    "properties": {
      "author" : {"type": "keyword"},
      "title"  : {"type": "text", "fielddata": true},
      "date"   : {"type": "date"},
      "summary": {
        "type": "text", 
        "fielddata": true,   
        "index_options": "offsets"
      },
      "id"     : {"type": "keyword", "index": false}
    }
  }
}
```
#### Réindexage
```
POST _reindex
{
  "source": {
    "index": "cacm_dynamic"
  },
  "dest": {
    "index": "cacm_standard"
  }
}
```
### D.2
#### Création index
```
PUT /cacm_termvector
{
  "mappings": {
    "properties": {
      "author" : {"type": "keyword"},
      "title"  : {"type": "text", "fielddata": true},
      "date"   : {"type": "date"},
      "id"     : {"type": "keyword", "index": false},
      "summary": {
        "type"         : "text", 
        "fielddata"    : true,
        "index_options": "offsets",
        "term_vector"  : "yes"
      }
    }
  }
}
```
#### Réindexage
```
POST _reindex
{
  "source": {
    "index": "cacm_dynamic"
  },
  "dest": {
    "index": "cacm_termvector"
  }
}
```
### D.3
#### Requête
```
GET /cacm_termvector/_termvectors/0qJgh5MBVoxiPr0S6HsP
{
  "fields" : ["summary"],
  "offsets" : true,
  "payloads" : true,
  "positions" : true,
  "term_statistics" : true,
  "field_statistics" : true
}
```
#### Résultat
```
{
  "_index": "cacm_termvector",
  "_id": "0qJgh5MBVoxiPr0S6HsP",
  "_version": 1,
  "found": true,
  "took": 60,
  "term_vectors": {
    "summary": {
      "field_statistics": {
        "sum_doc_freq": 97730,
        "doc_count": 1585,
        "sum_ttf": 150220
      },
      "terms": {
        "a": {
          "doc_freq": 1426,
          "ttf": 4922,
          "term_freq": 3
        },

        ...
}
```

### D.4

Les vecteurs de termes (term vectors) sont des structures qui contiennent des informtions sur les termes produits par l'analyse d'un champ. Ils contiennent:

- La liste des termes
- La position de chaque terme
- L'offset du caractère dans la string originale
- Un payload (si disponible)

### D.5

L'index `cacm_standard` a une taille de **1.79 mb** et le `cacm_termvector` une taille de **2.28 mb**. Cela est du au différents éléments supplémentaires que le vecteur de termes contient, expliqué prédcédemment. En plus des termes, il y a leur position, l'offset, etc...

## Reading index

### D.6
#### Requête
```
GET /cacm_standard/_search
{
  "size": 0, 
  "aggs": {
    "authors": {
      "terms": {
        "field": "author"
      }
    }
  }
}
```

#### Résultat
```
...

"aggregations": {
    "authors": {
      "doc_count_error_upper_bound": 0,
      "sum_other_doc_count": 4138,
      "buckets": [
        {
          "key": "Thacher Jr., H. C.",
          "doc_count": 38
        },

        ...
```
### D.7

```
GET /cacm_standard/_search
{
  "size": 0, 
  "aggs": {
    "titles_term": {
      "terms": {
        "field": "title",
        "size": 10
      }
    }
  }
}
```

```
{
    "key": "of",
    "doc_count": 1138
},
{
    "key": "algorithm",
    "doc_count": 975
},
{
    "key": "a",
    "doc_count": 895
},
{
    "key": "for",
    "doc_count": 714
},
{
    "key": "the",
    "doc_count": 645
},
{
    "key": "and",
    "doc_count": 434
},
{
    "key": "in",
    "doc_count": 416
},
{
    "key": "on",
    "doc_count": 340
},
{
    "key": "an",
    "doc_count": 275
},
{
    "key": "computer",
    "doc_count": 275
}
```

## Analyser

### D.8

#### whitespace

```
PUT /cacm_whitespace
{
  "mappings": {
    "properties": {
      "author": {
        "type": "keyword"
      },
      "title": {
        "type": "text",
        "fielddata": true,
        "analyzer": "whitespace"
      },
      "date": {
        "type": "date"
      },
      "id": {
        "type": "keyword",
        "index": false
      },
      "summary": {
        "type": "text",
        "fielddata": true,
        "index_options": "offsets",
        "analyzer": "whitespace"
      }
    }
  }
}
```

#### english

```
PUT /cacm_english
{
  "mappings": {
    "properties": {
      "author": {
        "type": "keyword"
      },
      "title": {
        "type": "text",
        "fielddata": true,
        "analyzer": "english"
      },
      "date": {
        "type": "date"
      },
      "id": {
        "type": "keyword",
        "index": false
      },
      "summary": {
        "type": "text",
        "fielddata": true,
        "index_options": "offsets",
        "analyzer": "english"
      }
    }
  }
}
```

#### shingle 2

```
PUT /cacm_shingle_2
{
  "settings": {
    "analysis": {
      "filter": {
        "shingle_filter": {
          "type": "shingle"
        }
      },
      "analyzer": {
        "analyzer_shingle_2": {
          "type": "custom",
          "tokenizer": "standard",
          "filter": [
            "shingle_filter"
          ]
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "author": {
        "type": "keyword"
      },
      "title": {
        "type": "text",
        "fielddata": true,
        "analyzer": "analyzer_shingle_2"
      },
      "date": {
        "type": "date"
      },
      "id": {
        "type": "keyword",
        "index": false
      },
      "summary": {
        "type": "text",
        "fielddata": true,
        "index_options": "offsets",
        "analyzer": "analyzer_shingle_2"
      }
    }
  }
}

```

#### shingle 3

```
PUT /cacm_shingle_3
{
  "settings": {
    "analysis": {
      "filter": {
        "shingle_filter": {
          "type": "shingle",
          "min_shingle_size": 3,
          "max_shingle_size": 3
        }
      },
      "analyzer": {
        "analyzer_shingle_3": {
          "type": "custom",
          "tokenizer": "standard",
          "filter": [
            "shingle_filter"
          ]
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "author": {
        "type": "keyword"
      },
      "title": {
        "type": "text",
        "fielddata": true,
        "analyzer": "analyzer_shingle_3"
      },
      "date": {
        "type": "date"
      },
      "id": {
        "type": "keyword",
        "index": false
      },
      "summary": {
        "type": "text",
        "fielddata": true,
        "index_options": "offsets",
        "analyzer": "analyzer_shingle_3"
      }
    }
  }
}
```

#### stop

```
PUT /cacm_stop
{
  "settings": { 
    "analysis": { 
      "filter": { 
        "my_stop": { 
          "type": "stop", 
          "stopwords_path": "./data/common_words.txt" 
        } 
        
      }, 
      "analyzer": { 
        "stop_analyzer": { 
          "type": "custom", 
          "tokenizer": "standard", 
          "filter": [ 
            "my_stop" 
            ] 
        } 
      } 
    } 
  },
  "mappings": {
    "properties": {
      "author": {
        "type": "keyword"
      },
      "title": {
        "type": "text",
        "fielddata": true,
        "analyzer": "stop_analyzer"
      },
      "date": {
        "type": "date"
      },
      "id": {
        "type": "keyword",
        "index": false
      },
      "summary": {
        "type": "text",
        "fielddata": true,
        "index_options": "offsets",
        "analyzer": "stop_analyzer"
      }
    }
  }
}

```

### D.9

- `whitespace` C'est un tokenizer qui transforme un text en terme en utilisant les espaces comme séparateur.
- `english` C'est analyser spécifique pour l'anglais. Il applique différent filtre adapté à la langue comme la liste de stop word ou du stemming.
- `shingle 1 et 2` L'analyser shingle va créer des termes à partir d'un mot mais aussi de plusieurs mots ensemble, en l'occurence, 2 à la fois.
- `shingle 3` Même chose que le prédcédent mais avec des groupes de 3 mots.
- `stop` Supprime les termes courants (comme les déterminants ou les pépositions) contenu dans le fichier `common_words.txt` des termes retenus dans l'analyse.

### D.10

Pour obtenir les informations des index, nous avons utilisé les requêtes suivantes en remplaçant `<index_name>` par le nom du bonne index.

#### Nombre de doc. indexé et taille sur disque

```
GET /<index_name>/_stats
```

#### Nombre de terme indexé dans `summary` et top 10

```
GET /<index_name>/_search
{
  "size": 0,
  "aggs": {
    "terms_count": {
      "terms": {
        "field": "summary",
        "size": 10
      }
    }
  }
}
```

#### Temps d'indexage

```
POST _reindex
{
  "source": {
    "index": "<index_name>"
  },
  "dest": {
    "index": "cacm_stop"
  }
}
```

#### Résultats


Nom index       |nb. doc. | nb. termes dans summary | taille sur disque | temps d'indexation
----------------|---------|-------------------------|-------------------|--------------------
cacm_whitespace |3202     |  90'426                 | 3.39 mb           |  431 ms
cacm_english    |3202     |  66'576                 | 2.63 mb           |  446 ms
cacm_shingle_2  |3202     | 228'039                 | 3.74 mb           | 1045 ms
cacm_shingle_3  |3202     | 232'671                 | 4.76 mb           |  679 ms
cacm_stop       |3202     |  61'663                 | 1.5  mb           |  557 ms

n° |cacm_whitespace | cacm_english | cacm_shingle_2 | cacm_shingle_3 | cacm_stop
---|----------------|--------------|----------------|----------------|----------
1  | of             | which        | of             | of             | The
2  | the            | us           | the            | the            | A
3  | is             | comput       | is             | is             | This
4  | and            | program      | and            | and            | computer
5  | a              | system       | a              | a              | system
6  | to             | present      | to             | to             | paper
7  | in             | describ      | for            | for            | presented
8  | for            | paper        | in             | in             | time
9  | The            | can          | The            | The            | program
10 | are            | gener        | are            | are            | data

### D.11

1. Il faudrait ajouter la normalisation des entrées (comme suppression des majuscules) afin d'éviter les termes à double (the et The) ou indésirables et non détecté dans la liste des "stop words".
2. L'utilisation du `shingle` augmente pas mal le nombre de terme et la taille occupé sur le disque.
3. Sans l'application des "stop word", les termes les plus récurents ne change pas.

## Searching
### D.12
#### 1
```
GET cacm_english/_search
{
  "_source": false,
  "query": {
    "query_string": {
      "query": "Information Retrieval"
    }
  },
  "fields": [
    "id"
  ]
}
```
#### 2
```
GET cacm_english/_search
{
  "_source": false,
  "query": {
    "query_string": {
      "query": "(Information) AND (Retrieval)"
    }
  },
  "fields": [
    "id"
  ]
}
```
#### 3
```
GET cacm_english/_search
{
  "_source": false, 
  "query": {
    "query_string": {
      "query": "(Retrieval) AND NOT (Database) OR (Information)"
    }
  },
  "fields": [
    "id"
  ]
}
```
#### 4
```
GET cacm_english/_search
{
  "_source": false, 
  "query": {
    "query_string": {
      "query": "Info*"
    }
  },
  "fields": [
    "id"
  ]
}
```
#### 5
```
GET cacm_english/_search
{
  "_source": false, 
  "query": {
    "query_string": {
      "query": "\"Information Retrieval\"~5"
    }
  },
  "fields": [
    "id"
  ]
}
```
### D.13
#### 1
Nous avons 287 hits avec cette requête.
#### 2
Nous avons 48 hits avec cette requête.
#### 3
Nous avons 86 hits avec cette requête. 
#### 4
Nous avons 247 hits avec cette requête.
#### 5
Nous avons 41 hits avec cette requête.
## Tuning

### D.14
Si nous avons bien compris et qu'il faut rechercher les documents qui ont `compiler program` alors on a:
```
GET cacm_english/_search
{
  "query": {
    "function_score": {
      "query": {
        "query_string": {
          "query": "compiler program"
        }
      },
      "functions": [
        {
          "linear": {
            "date": {
              "origin": "1970-01",
              "scale": "90d",
              "decay": 0.5
            }
          }
        }
      ],
    }
  }
}
```
