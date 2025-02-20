> Tous les commentaires sont sur Requests.java

```java
var result = ctx.query("""
 SELECT
 email,
 COUNT(_id) AS cnt
```

Mieux d'utiliser `COUNT(*)` plutot que `COUNT(_id)`

```sql
                        SELECT RAW email
                        FROM `mflix-sample`.`_default`.comments
                        GROUP BY email
                        HAVING Count(*) >= 300;
```

On demande plus que 300, pas 300 ou plus.

```sql
                        SELECT
                        imdb.id AS imdb_id,
                        imdb.rating AS rating,
                        `cast`
                        FROM `mflix-sample`.`_default`.movies
                        WHERE imdb.rating > 8 AND @actor IN `cast`
```

Manque vérification que `imdb.rating` est un nombre.

```sql
                            UPDATE `mflix-sample`.`_default`.theaters
                            SET theaters.schedule = ARRAY s FOR s IN theaters.schedule
                            WHEN s.hourBegin >= '18:00:00' END
                            WHERE ARRAY_LENGTH(theaters.schedule) > 0
                            AND ANY s IN theaters.schedule SATISFIES s.hourBegin < '18:00:00' END
```

On veut enlever les projection avant 18h d'un **film donné**, pas de tous les films.
