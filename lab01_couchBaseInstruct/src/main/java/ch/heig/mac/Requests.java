package ch.heig.mac;

import java.util.List;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryOptions;


public class Requests {
    private final Cluster ctx;

    public Requests(Cluster cluster) {
        this.ctx = cluster;
    }

    public List<String> getCollectionNames() {
        var result = ctx.query("""
                        SELECT RAW r.name
                        FROM system:keyspaces r
                        WHERE r.`bucket` = "mflix-sample";
                        """
        );
        return result.rowsAs(String.class);
    }

    public List<JsonObject> inconsistentRating() {

        var result = ctx.query("""
                    SELECT
                    imdb.id AS imdb_id,
                    tomatoes.viewer.rating AS tomatoes_rating,
                    imdb.rating AS imdb_rating
                    FROM `mflix-sample`.`_default`.movies
                    WHERE tomatoes.viewer.rating != 0 AND ABS(imdb.rating - tomatoes.viewer.rating) > 7
                    """
        );

        return result.rowsAsObject();
    }

    public List<JsonObject> hiddenGem() {
        var result = ctx.query("""
                        SELECT title 
                        FROM `mflix-sample`.`_default`.movies
                        WHERE tomatoes.viewer.rating IS MISSING
                            AND tomatoes.critic.rating = 10;
                        """
        );
        return result.rowsAsObject();
    }

    public List<JsonObject> topReviewers() {

        var result = ctx.query("""
                    SELECT
                    email,
                    COUNT(_id) AS cnt
                    FROM `mflix-sample`.`_default`.comments
                    GROUP BY email
                    ORDER BY cnt DESC
                    LIMIT 10
                    """);


        return result.rowsAsObject();
    }

    public List<String> greatReviewers() {
        var result = ctx.query("""
                        SELECT RAW email
                        FROM `mflix-sample`.`_default`.comments
                        GROUP BY email
                        HAVING Count(*) >= 300;
                        """
        );

        return result.rowsAs(String.class);
    }

    public List<JsonObject> bestMoviesOfActor(String actor) {
        var result = ctx.query("""
                        SELECT
                        imdb.id AS imdb_id,
                        imdb.rating AS rating,
                        `cast`
                        FROM `mflix-sample`.`_default`.movies
                        WHERE imdb.rating > 8 AND @actor IN `cast`
                        """,
                        QueryOptions.queryOptions().parameters(JsonObject.create().put("actor", actor)));
        
        return result.rowsAsObject();
    }

    public List<JsonObject> plentifulDirectors() {
        var result = ctx.query("""
            SELECT d AS director_name, COUNT(*) count_film
            FROM `mflix-sample`.`_default`.movies m
            UNNEST m.directors d
            GROUP BY d
            HAVING COUNT(*) > 30;
            """
        );
        return result.rowsAsObject();
    }

    public List<JsonObject> confusingMovies() {
        var result = ctx.query("""
                        SELECT
                        _id AS movie_id,
                        title
                        FROM `mflix-sample`.`_default`.movies
                        WHERE ARRAY_COUNT(directors) > 20
                        """);
        
        return result.rowsAsObject();
    }

    public List<JsonObject> commentsOfDirector1(String director) {
        var result = ctx.query("""
            SELECT c.movie_id, c.text
            FROM `mflix-sample`.`_default`.comments c
                INNER JOIN `mflix-sample`.`_default`.movies m ON c.movie_id = m._id
            WHERE @director IN m.directors
            """,
            QueryOptions.queryOptions().parameters(JsonObject.create().put("director", director)));

        return result.rowsAsObject();
    }

    public List<JsonObject> commentsOfDirector2(String director) {
        var result = ctx.query("""
            SELECT movie_id, text
            FROM `mflix-sample`.`_default`.comments
            WHERE movie_id WITHIN (
                SELECT _id
                FROM `mflix-sample`.`_default`.movies
                WHERE @director IN directors);
                        """,
            QueryOptions.queryOptions().parameters(JsonObject.create().put("director", director)));

        return result.rowsAsObject();
    }

    // Returns the number of documents updated.
    public long removeEarlyProjection(String movieId) {
        var result = ctx.query("""
                            UPDATE `mflix-sample`.`_default`.theaters
                            SET theaters.schedule = ARRAY s FOR s IN theaters.schedule
                            WHEN s.hourBegin >= '18:00:00' END
                            WHERE ARRAY_LENGTH(theaters.schedule) > 0
                            AND ANY s IN theaters.schedule SATISFIES s.hourBegin < '18:00:00' END
                            """,
                            QueryOptions.queryOptions().metrics(true));

        return result.metaData().metrics().get().mutationCount();
    }

    public List<JsonObject> nightMovies() {
        throw new UnsupportedOperationException("Not implemented, yet");
    }


}
