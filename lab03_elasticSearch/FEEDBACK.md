> Commentaires sur le report.md

```json
        "fielddata": true,   
        "index_options": "offsets"
      },
      "id"     : {"type": "keyword", "index": false}
```

From v8, doc says that `"index": false` is still queryable but slow.

```json
        "type"         : "text", 
        "fielddata"    : true,
        "index_options": "offsets",
        "term_vector"  : "yes"
```

Accept "yes", but impact would be greater if using "with_offsets".

```json
Les vecteurs de termes (term vectors) sont des structures qui contiennent des informtions sur les termes produits par l'analyse d'un champ. Ils contiennent:

- La liste des termes
- La position de chaque terme
- L'offset du caractère dans la string originale
- Un payload (si disponible)
```

Missing mention that the information provided by "term vector" is per document.

```json
      "filter": {
        "shingle_filter": {
          "type": "shingle"
        }
      },
```

Same as existing, no need to recreate it.

```json
          "filter": [
            "shingle_filter"
          ]
```

Missing "lowercase" filter.

```json
          "type": "custom",
          "tokenizer": "standard",
          "filter": [
            "shingle_filter"
```

Missing "lowercase" filter.

```json
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
```

Could directly use an analyzer of type "stop", instead of recreating it incorrectly.  
Native "stop" analyzer uses the "lowercase" tokenizer.

```
Nom index       |nb. doc. | nb. termes dans summary | taille sur disque | temps d'indexation
----------------|---------|-------------------------|-------------------|--------------------
cacm_whitespace |3202     |  90'426                 | 3.39 mb           |  431 ms
cacm_english    |3202     |  66'576                 | 2.63 mb           |  446 ms
cacm_shingle_2  |3202     | 228'039                 | 3.74 mb           | 1045 ms
cacm_shingle_3  |3202     | 232'671                 | 4.76 mb           |  679 ms
cacm_stop       |3202     |  61'663                 | 1.5  mb           |  557 ms
```

Should add the freq of the top 10 terms to have the number of terms

```json
    "query_string": {
      "query": "Information Retrieval"
    }
```

Missing escaped quote

```json
    "query_string": {
      "query": "(Retrieval) AND NOT (Database) OR (Information)"
    }
```

Use + and - notation.

```markdown
3. Sans l'application des "stop word", les termes les plus récurents ne change pas.

## Searching
### D.12
```

The queries should be on the summary field.
