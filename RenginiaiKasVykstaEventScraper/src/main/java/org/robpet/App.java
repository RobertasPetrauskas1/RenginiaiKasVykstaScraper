package org.robpet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App 
{
    static ObjectMapper objectMapper = new ObjectMapper();

    public static void main( String[] args ) throws IOException {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String url = "https://renginiai.kasvyksta.lt";
        String urlForLogin = "http://localhost:8081/access/login";
        String urlForPlaceId = "http://localhost:8081/admin/getPlaceId";
        String urlForEventPost = "http://localhost:8081/event";
        String urlForEventGet = "http://localhost:8081/event";
        String urlForNextPhoto = "http://localhost:8081/utils/nextPhotoId";
        String urlForNewPhoto = "http://localhost:8081/media";
        String urlForNextEventId = "http://localhost:8081/event/nextEventId";

        Token token = login("admin", "admin", urlForLogin);

        List<Event> scrapedEvents = new ArrayList<>();
        List<String> imgURLs = new ArrayList<>();

        File input = new File("C:\\Users\\robpet2\\Desktop\\scrape.html");
        Document page = Jsoup.parse(input, "UTF-8", url);
        //Document page = Jsoup.connect(url).userAgent("Mozilla/4.0").get();
        Elements events = page.getElementsByClass("block event-block");

        String imgURL = "";

        for(Element event : events){
            Event obj = new Event();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String dateString = event.select("[itemprop=startDate]").attr("content").split(" ")[0];
            LocalDate date = LocalDate.parse(dateString, formatter);
            obj.setDate(date);

            String city = event.select("[itemprop=addressLocality]").attr("content");
            obj.setFk_city(city);

            //Inner event page containing the data
            String nextUrl = event.select("[itemprop=url]").attr("href");
            Document innerPage = Jsoup.connect(nextUrl).userAgent("Mozilla/4.0").get();

            String name = innerPage.select("[itemprop=name]").text();
            obj.setName(name);

            String typeHref = innerPage.select("div.with-icon.type > a").attr("href");
            int lastIndexOfTypeHref = typeHref.lastIndexOf("/") + 1;
            String type = typeHref.substring(lastIndexOfTypeHref);
            obj.setFk_event_type(type);

            String place = innerPage.select("div.with-icon.place > a").text();

            Integer placeId = getPlaceId(place, urlForPlaceId, token);
            obj.setFk_place(placeId);

            String phone = innerPage.select("div.with-icon.phone > a").text()
                    .replace(" ", "")
                    .replace("~", "")
                    .replace("-", "")
                    .replace("(", "")
                    .replace(")", "")
                    .split(";")[0]
                    .split(",")[0];
            obj.setPhone_number(phone);

            String website = innerPage.select("div.with-icon.webpage > a").text();
            obj.setWebsite(website);

            String facebook = innerPage.select("div.with-icon.facebook > a").text();
            obj.setFacebook(facebook);

            String time = innerPage.select("div.date-time-wrap > span.time-h").text();
            obj.setTime(time);

            String duration = innerPage.select("div.duration").text();
            obj.setDuration(duration);

            String ticketLinkTemp = innerPage.select("a.ticket-url").attr("onclick");
            int startIndex = ticketLinkTemp.indexOf("'");
            int endIndex = ticketLinkTemp.indexOf(",");
            String ticketLink = null;
            if(!ticketLinkTemp.equals(null) && !ticketLinkTemp.equals("")) {
                ticketLink = ticketLinkTemp.substring(startIndex + 1, endIndex - 1);
            }
            obj.setTickets(ticketLink);

            String description = innerPage.select("div.text-inner > p").text();
            obj.setDescription(description);

            obj.setFk_photo(0);
            obj.setFk_user_id(1);

            imgURL = "https:" + innerPage.select("div.featured.gallery-open-btn > img").attr("src");

            imgURLs.add(imgURL);
            scrapedEvents.add(obj);
        }
        int counter = 0;
        for(Event event : scrapedEvents){
            if(event.getPhone_number().equals("") || event.getPhone_number().equals(null)) event.setPhone_number("------------");
            if(event.getDescription().equals("") || event.getDescription().equals(null)) event.setDescription("Aprašymas nepateiktas.");
            if(event.getFk_place() != -1){
                System.out.println(event);
                Integer nextEventId = getNextEventId(urlForNextEventId);
                Integer status = postEvent(event, urlForEventPost, token);
                if((status / 100 == 2) && !imgURL.equals("")){
                    Integer nextPhotoId = getNextPhotoId(urlForNextPhoto);
                    boolean saved = saveImage(imgURLs.get(counter), nextPhotoId + ".jpg");
                    if(saved){
                        postPhoto("event_photos", urlForNewPhoto, token);
                        changeEventPhotoId(nextEventId, urlForEventGet, urlForEventPost, nextPhotoId, token);
                    }
                }
            }
            counter++;
        }
    }
    private static Integer getPlaceId(String placeName, String site, Token token) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(site);
        StringEntity postString = new StringEntity(placeName, "UTF-8");
        post.setEntity(postString);
        post.setHeader("Authorization", "Bearer " + token.getToken());
        HttpResponse response = client.execute(post);
        return Integer.parseInt(EntityUtils.toString(response.getEntity()));
    }
    private static Token login(String username, String password, String loginSite) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost login = new HttpPost(loginSite);
        StringEntity loginString = new StringEntity(objectMapper.writeValueAsString(new Login(username, password)), "UTF-8");
        login.setEntity(loginString);
        login.setHeader("Content-type", "application/json");
        HttpResponse loginResponse = client.execute(login);
        String tokenJson = EntityUtils.toString(loginResponse.getEntity(), "UTF-8");
        return objectMapper.readValue(tokenJson, Token.class);
    }
    private static Integer postEvent(Event event, String site, Token token) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(site);
        String json = objectMapper.writeValueAsString(event);
        StringEntity postString = new StringEntity(json, "UTF-8");
        post.setHeader("Content-type", "application/json;charset=UTF-8");
        post.setHeader("Authorization", "Bearer " + token.getToken());
        post.setEntity(postString);
        HttpResponse response = client.execute(post);
        System.out.println(EntityUtils.toString(response.getEntity()));
        return response.getStatusLine().getStatusCode();
    }
    public static int getNextPhotoId(String site) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet(site);
        return Integer.parseInt(EntityUtils.toString(client.execute(get).getEntity()));
    }
    public static void postPhoto(String folder, String site, Token token) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(site);
        StringEntity entity = new StringEntity(folder, "UTF-8");
        post.setEntity(entity);
        post.setHeader("Authorization", "Bearer " + token.getToken());
        client.execute(post);
    }
    public static void changeEventPhotoId(Integer placeId, String getSite, String updateSite, Integer photoId, Token token) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet(getSite + "/" + placeId);
        Event place = objectMapper.readValue(EntityUtils.toString(client.execute(get).getEntity()), Event.class);
        place.setFk_photo(photoId);
        HttpPut put = new HttpPut(updateSite + "/" + placeId);
        StringEntity placeJson = new StringEntity(objectMapper.writeValueAsString(place), "UTF-8");
        put.setEntity(placeJson);
        put.setHeader("Content-type", "application/json");
        put.setHeader("Authorization", "Bearer " + token.getToken());
        HttpResponse response = client.execute(put);
        System.out.println(EntityUtils.toString(response.getEntity()));
    }
    public static boolean saveImage(String imageUrl, String imgName) throws IOException {
        URL url = new URL(imageUrl);
        if(url != null && (url.toString().startsWith("http://") || url.toString().startsWith("https://"))){
            String fileName = url.getFile();
            //String destName = "C:\\Users\\robpet2\\Desktop\\tempFoto\\" + fileName.substring(fileName.lastIndexOf("/") + 1);
            String destName = "C:\\Users\\robpet2\\Desktop\\SemestroProjektas\\EventSearchPlatform\\src\\main\\resources\\user_upload\\event_photos\\" + imgName;
            System.out.println(destName);

            InputStream is = url.openStream();
            OutputStream os = new FileOutputStream(destName);

            byte[] b = new byte[2048];
            int length;

            while ((length = is.read(b)) != -1) {
                os.write(b, 0, length);
            }

            is.close();
            os.close();
            return true;
        }
        return false;
    }
    public static int getNextEventId(String site) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet(site);
        return Integer.parseInt(EntityUtils.toString(client.execute(get).getEntity()));
    }
}
