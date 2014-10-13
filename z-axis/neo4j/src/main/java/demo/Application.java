package demo;

import org.neo4j.graphdb.GraphDatabaseService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.neo4j.rest.SpringRestGraphDatabase;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.data.repository.query.Param;
import org.springframework.social.twitter.api.HashTagEntity;
import org.springframework.social.twitter.api.MentionEntity;
import org.springframework.social.twitter.api.SearchResults;
import org.springframework.social.twitter.api.impl.TwitterTemplate;

import java.util.Collection;
import java.util.Set;

/*
 see for TWITTER_BEARER token: https://dev.twitter.com/docs/auth/application-only-auth
// curl -XPOST -u customer:secret 'https://api.twitter.com/oauth2/token?grant_type=client_credentials'
// returns {"token_type":"bearer","access_token":"....bearer token...."}
 */

/**
 * Simple Neo4j example.
 *
 * @author Michael Hunger
 * @author Josh Long
 */
@ComponentScan
@Configuration
@EnableAutoConfiguration
@EnableNeo4jRepositories
public class Application extends Neo4jConfiguration {

    public Application() {
        this.setBasePackage(Application.class.getPackage().getName());
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    GraphDatabaseService graphDatabaseService(@Value("${neo4j.url}") String neo4jUrl) {
        return new SpringRestGraphDatabase(neo4jUrl);
    }

    @Bean
    TwitterTemplate twitterTemplate(@Value("${twitter.bearerToken}") String bearerToken) {
        return new TwitterTemplate(bearerToken);
    }


    @Bean
    CommandLineRunner importer(UserRepository userRepository,
                               TweetRepository tweetRepository,
                               TagRepository tagRepository,
                               Neo4jTemplate neo4jTemplate,
                               TwitterTemplate twitterTemplate) {
        return args -> {

            // reset db
            neo4jTemplate.query("MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE n,r", null);

            // search for tweets matching query ('@SpringBoot')
            SearchResults results = twitterTemplate.searchOperations().search("@SpringBoot", 200);

            results.getTweets().stream().map(source -> {

                User user = userRepository.save(new User(source.getUser()));

                Tweet tweet = new Tweet(source.getId(), user, source.getText());

                source.getEntities().getMentions().forEach((MentionEntity m) -> tweet.addMention(
                        userRepository.save(new User(m.getId(), m.getName(), m.getScreenName()))));

                source.getEntities().getHashTags().forEach((HashTagEntity t) -> tweet.addTag(tagRepository.save(new Tag(t.getText()))));

                return tweetRepository.save(tweet);

            }).forEach(System.out::println);

            userRepository.suggestFriends("starbuxman").forEach(System.out::println);
        };
    }
}

interface UserRepository extends GraphRepository<User> {

    @Query("MATCH (me:User {user:{name}})-[:POSTED]->" +
            "(tweet)-[:MENTIONS]->(user)" +
            " WHERE me <> user " +
            " RETURN distinct user")
    Set<User> suggestFriends(@Param("name") String user);

    User findByUser(@Param("0") String user);

}

interface TweetRepository extends GraphRepository<Tweet> {
    Tweet findByTweetId(Long id);

    Collection<Tweet> findByTagsTag(String tag);
}

interface TagRepository extends GraphRepository<Tag> {
}
