package demo;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.social.twitter.api.TwitterProfile;

import java.util.Date;

@NodeEntity
public class User {
    @GraphId
    Long id;

    @Indexed(unique = true)
    long userId;

    @Indexed
    String user;

    @Indexed
    String name;

    Date createdDate;
    int followers;
    int friends;
    String url;
    String language;
    String image;

    public User(long userId) {
        this.userId = userId;
    }

    public User(TwitterProfile user) {
        this.userId = user.getId();
        this.user = user.getScreenName();
        this.name = user.getName();
        createdDate = user.getCreatedDate();
        followers = user.getFollowersCount();
        friends = user.getFriendsCount();
        url = user.getUrl();
        image = user.getProfileImageUrl();
        language = user.getLanguage();
    }

    public User(long id, String name, String screenName) {
        this.userId = id;
        this.name = name;
        this.user = screenName;
    }

    public Long getId() {
        return id;
    }

    public String getUser() {
        return user;
    }

    public long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public int getFollowers() {
        return followers;
    }

    public int getFriends() {
        return friends;
    }

    public String getUrl() {
        return url;
    }

    public String getImage() {
        return image;
    }

    public String getLanguage() {
        return language;
    }

    @Override
    public String toString() {
        return "@" + user;
    }

    public User() {
    }
}