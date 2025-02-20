package ch.heig.mac;

import java.util.List;
import java.util.Map;

import org.neo4j.driver.*;
import org.neo4j.driver.Record;

public class Requests {
    private final Driver driver;

    public Requests(Driver driver) {
        this.driver = driver;
    }

    public List<String> getDbLabels() {
        var dbVisualizationQuery = """
                CALL db.labels
                """;

        try (var session = driver.session()) {
            var result = session.run(dbVisualizationQuery);
            return result.list(t -> t.get("label").asString());
        }
    }

    public List<Record> possibleSpreaders() {
        var query = """
                MATCH (sick:Person {healthstatus: 'Sick'})-[sickVisit:VISITS]->(:Place)<-[healthyVisit:VISITS]-(healthy:Person {healthstatus: 'Healthy'})
                WHERE
                    sickVisit.starttime > sick.confirmedtime AND
                    sickVisit.starttime < healthyVisit.starttime AND
                    healthy.confirmedtime < healthyVisit.starttime
                RETURN DISTINCT sick.name AS sickName
                """;

        try (var session = driver.session()) {
            var result = session.run(query);
            return result.list();
        }
    }

    public List<Record> possibleSpreadCounts() {
        var query = """
                match (p1:Person)-[v1:VISITS]->(pl:Place)<-[v2:VISITS]-(p2:Person) 
                where 
                    p1.name <> p2.name and 
                    p1.healthstatus = "Sick" and 
                    p2.healthstatus = "Healthy" and
                    p1.confirmedtime < v1.starttime and
                    p2.confirmedtime < v2.starttime and
                    v1.starttime < v2 .starttime
                return p1.name as sickName, count(distinct p2) as nbHealthy
                """;
            try (var session = driver.session()) {
                var result = session.run(query);
                return result.list();
            }
    }

    public List<Record> carelessPeople() {
        var query = """
                MATCH (careless:Person {healthstatus: 'Sick'}) -[:VISITS]->(p:Place)
                WITH
                    careless.name AS sickName,
                    COUNT(DISTINCT p) AS nbPlaces
                ORDER BY nbPlaces DESC
                WHERE nbPlaces > 10
                RETURN sickName, nbPlaces
                """;

        try (var session = driver.session()) {
            var result = session.run(query);
            return result.list();
        }
    }

    public List<Record> sociallyCareful() {
        var query = """
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
                """;
            try (var session = driver.session()) {
                var result = session.run(query);
                return result.list();
            }
    }

    public List<Record> peopleToInform() {
        var query = """
                MATCH (sick:Person {healthstatus: 'Sick'}) -[sickV:VISITS]->(:Place)<-[healthyV:VISITS]-(healthy:Person {healthstatus: 'Healthy'})
                WHERE
                    sick.confirmedtime < sickV.starttime AND
                    healthy.confirmedtime < healthyV.starttime
                WITH *,
                    duration.between(apoc.coll.max([sickV.starttime, healthyV.starttime]), apoc.coll.min([sickV.endtime, healthyV.endtime])).hours AS overlap
                WHERE overlap >= 2
                RETURN DISTINCT sick.name AS sickName, collect(DISTINCT healthy.name) AS peopleToInform
                """;

        try (var session = driver.session()) {
            var result = session.run(query);
            return result.list();
        }
    }

    public List<Record> setHighRisk() {
        var query = """
                MATCH (sick:Person {healthstatus: 'Sick'}) -[sickV:VISITS]->(:Place)<-[healthyV:VISITS]-(healthy:Person {healthstatus: 'Healthy'})
                WHERE
                    sick.confirmedtime < sickV.starttime AND
                    healthy.confirmedtime < healthyV.starttime
                WITH *,
                    duration.between(apoc.coll.max([sickV.starttime, healthyV.starttime]), apoc.coll.min([sickV.endtime, healthyV.endtime])).hours AS overlap
                WHERE overlap >= 2
                SET healthy.risk = "high"
                RETURN DISTINCT healthy.id AS highRiskId, healthy.name AS highRiskName
                """;
        try (var session = driver.session()) {
            var result = session.run(query);
            return result.list();
        }
    }

    public List<Record> healthyCompanionsOf(String name) {
        var query = """
                MATCH (given:Person {name: $name})-[:VISITS*..3]-(companion:Person {healthstatus: 'Healthy'})
                WHERE companion <> given
                RETURN DISTINCT companion.name AS healthyName
                """;
        try (var session = driver.session()) {
            var result = session.run(query, Map.of("name", name));
            return result.list();
        }
    }

    public Record topSickSite() {
        var query = """
                MATCH (place:Place)<-[v:VISITS]-(person:Person {healthstatus: "Sick"})
                WHERE person.confirmedtime < v.starttime
                RETURN place.type AS placeType, COUNT(*) AS nbOfSickVisits
                ORDER BY nbOfSickVisits DESC
                LIMIT 1
                """;
        try (var session = driver.session()) {
            var result = session.run(query);
            return result.single();
        }
    }

    public List<Record> sickFrom(List<String> names) {
        var query = """
                MATCH (sick:Person {healthstatus: 'Sick'})
                WHERE sick.name IN $names
                RETURN sick.name AS sickName
                """;
        try (var session = driver.session()) {
            var result = session.run(query, Map.of("names", names));
            return result.list();
        }
    }
}
