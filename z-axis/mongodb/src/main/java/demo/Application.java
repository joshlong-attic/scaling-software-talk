package demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.Page;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Z-axis scaling: introduce data partitions by movint to NoSQL stores like MongoDB.
 * <a href="http://localhost:8080/places/near?placeId=166760993387028&distance=0.2"> This endpoint
 * finds all records near a certain place</a>.
 *
 * @author Josh Long
 */
@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Application {

    private Logger logger = LoggerFactory.getLogger(getClass());

     @Bean
    CommandLineRunner places(PlaceService placeService, PlaceRepository placeRepository) {
        return args -> {

            logger.info("loading places..");
            Arrays.asList("Walgreens", "Philz Coffee", "Starbucks").forEach(
                    q -> placeService.loadPlaces(q).forEach( placeId -> System.out.println(placeRepository.findOne(placeId))));

            Place place = placeRepository.findOne("175060592519769");
            logger.info("finding places near " + place.getStreet() + ", " + place.getCity() + ", "
                    + place.getState() + ", " + place.getZip());
            placeService.placesNear(place.getId(), 1).forEach(p -> System.out.println(p.toString()));
        };
    }

    @Bean
    Facebook facebook(@Value("${facebook.appId}") String appId,
                      @Value("${facebook.appSecret}") String appSecret) {
        return new FacebookTemplate(appId + '|' + appSecret);
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

interface PlaceRepository extends MongoRepository<Place, String> {
    List<Place> findByPositionNear(org.springframework.data.geo.Point p,
                                   org.springframework.data.geo.Distance d);
}

@RestController
@RequestMapping("/places")
class PlaceService {

    private final Facebook facebook;
    private final MongoTemplate mongoTemplate;
    private final PlaceRepository placeRepository;

    @Autowired
    public PlaceService(Facebook facebook,
                        MongoTemplate mongoTemplate,
                        PlaceRepository placeRepository) {
        this.facebook = facebook;
        this.placeRepository = placeRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<Place> places() {
        return this.mongoTemplate.findAll(Place.class);
    }

    public List<String> loadPlaces(String query) {
        return this.loadPlaces(query, -122.414166, 37.752494, 5280);
    }

    @RequestMapping("/loader")
    public List<String> loadPlaces(@RequestParam String query,
                                   @RequestParam(required = false, defaultValue = "-122.414166") Double longitude,
                                   @RequestParam(required = false, defaultValue = "37.752494") Double latitude,
                                   @RequestParam(required = false, defaultValue = "5280") Integer distance) {
        return facebook.placesOperations()
                .search(query, latitude, longitude, distance)
                .stream()
                .map(p -> this.placeRepository.save(new Place(p)))
                .map(Place::getId)
                .collect(Collectors.toList());
    }

    @RequestMapping("/near")
    public List<Place> placesNear(@RequestParam String placeId, @RequestParam(required = false, defaultValue = ".5") float distance) {
        Place place = this.placeRepository.findOne(placeId);
        Point point = new Point(place.getLongitude(), place.getLatitude());
        Distance dist = new Distance(distance, Metrics.MILES);
        return this.placeRepository.findByPositionNear(point, dist);
    }
}

@Document
class Place {

    @Id
    private String id;

    @GeoSpatialIndexed(name = "position")
    private double[] position;
    private String city;
    private String country;
    private String description;
    private double latitude;
    private double longitude;
    private String state;
    private String street;
    private String zip;
    private String name;
    private String affilitation;
    private String category;
    private String about;
    private Date insertionDate;


    public String getId() {
        return id;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getDescription() {
        return description;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getState() {
        return state;
    }

    public String getStreet() {
        return street;
    }

    public String getZip() {
        return zip;
    }

    public double[] getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }

    public String getAffilitation() {
        return affilitation;
    }

    public String getCategory() {
        return category;
    }

    public String getAbout() {
        return about;
    }

    public Date getInsertionDate() {
        return insertionDate;
    }

    public Place(Page p) {
        this.affilitation = p.getAffiliation();
        this.id = p.getId();
        this.name = p.getName();
        this.category = p.getCategory();
        this.description = p.getDescription();
        this.about = p.getAbout();
        this.insertionDate = new Date();
        this.installLocation(p.getLocation());
    }

    Place() {
    }

    public Place(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Place{");
        sb.append("id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", category='").append(category).append('\'');
        sb.append(", street='").append(street).append('\'');
        sb.append(", city='").append(city).append('\'');
        sb.append(", state='").append(state).append('\'');
        sb.append(", zip='").append(zip).append('\'');
        sb.append(", latitude=").append(latitude);
        sb.append(", longitude=").append(longitude);
        sb.append(", country='").append(country).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", insertionDate=").append(insertionDate);
        sb.append('}');
        return sb.toString();
    }

    private void installLocation(org.springframework.social.facebook.api.Location pageLocation) {
        this.installLocation(pageLocation.getCity(), pageLocation.getCountry(),
                pageLocation.getDescription(), pageLocation.getLatitude(),
                pageLocation.getLongitude(), pageLocation.getState(),
                pageLocation.getStreet(), pageLocation.getZip());
    }

    private void installLocation(String city, String country, String description, double latitude, double longitude, String state, String street, String zip) {
        this.city = city;
        this.country = country;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.state = state;
        this.street = street;
        this.zip = zip;
        this.position = new double[]{this.longitude, this.latitude};
    }
}
