> Tous les commentaires sont sur Requests.java

```sql
                match (p1:Person)-[v1:VISITS]->(pl:Place)<-[v2:VISITS]-(p2:Person) 
                where 
                    p1.name <> p2.name and 
                    p1.healthstatus = "Sick" and 
                    p2.healthstatus = "Healthy" and
                    p1.confirmedtime < v1.starttime and
                    p2.confirmedtime < v2.starttime and
                    v1.starttime < v2 .starttime
                return p1.name as sickName, count(distinct p2) as nbHealthy
```

Manque les personnes malades qui n'ont pas fait de visite.  
Il n'est pas nécessaire de vérifier que `p1` et `p2` sont différent car l'un est malade et l'autre en bonne santé.  
Le `name` n'est pas suffisant pour différencier 2 personnes (il peut même apporter des erreurs).

```sql
                MATCH (careless:Person {healthstatus: 'Sick'}) -[:VISITS]->(p:Place)
                WITH
                    careless.name AS sickName,
                    COUNT(DISTINCT p) AS nbPlaces
                ORDER BY nbPlaces DESC
                WHERE nbPlaces > 10
                RETURN sickName, nbPlaces
```

La visite de la personne malade doit commencer après la confirmation de son état de santé.  
Il faut grouper par le nœud et non pas par l'attribut `name`, car il pourrait y avoir 2 personnes avec le même nom.

```sql
                MATCH (prs:Person)-[v:VISITS]->(plc:Place)
                WITH *, max(v.starttime) AS lastVisit
                WHERE 
                    plc.type = "Bar" AND
                    prs.healthstatus = "Sick" AND
                    lastVisit < prs.confirmedtime
                    OR
                    NOT EXISTS {
                        MATCH (prs)-[:VISITS]->(plc2:Place)
                        WHERE plc2.type = "Bar"
                    }
                    
                RETURN DISTINCT prs.name AS sickName
```

Pourquoi avoir fait le `max` ? Pourquoi le `OR` dans le `WHERE` ?  
Requête très bizarre qui ne correspond pas à ce qui est demandé.

```sql
                MATCH (sick:Person {healthstatus: 'Sick'}) -[sickV:VISITS]->(:Place)<-[healthyV:VISITS]-(healthy:Person {healthstatus: 'Healthy'})
                WHERE
                    sick.confirmedtime < sickV.starttime AND
                    healthy.confirmedtime < healthyV.starttime
                WITH *,
                    duration.between(apoc.coll.max([sickV.starttime, healthyV.starttime]), apoc.coll.min([sickV.endtime, healthyV.endtime])).hours AS overlap
                WHERE overlap >= 2
                RETURN DISTINCT sick.name AS sickName, collect(DISTINCT healthy.name) AS peopleToInform
```

Le `DISTINCT` après `RETURN` n'est pas nécessaire.  
`duration.between().hour` ne fonctionne pas si les dates ont plus d'un jour de différence, car `.hour` ne prends pas en compte la composante `.day` de la durée. Mieux d'utiliser `duration.inSeconds().hour`.

```sql
                MATCH (given:Person {name: $name})-[:VISITS*..3]-(companion:Person {healthstatus: 'Healthy'})
                WHERE companion <> given
                RETURN DISTINCT companion.name AS healthyName
```

Il y a 2 relation `VISITS` entre 2 personne qui visite le même endroit. Il faut donc max 6.
