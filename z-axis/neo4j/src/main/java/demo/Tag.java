package demo;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class Tag {
    @GraphId
    Long id;

    @Indexed(unique = true)
    String tag;

    public Tag() {
    }

    public Tag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public String toString() {
        return "#" + tag;
    }
}