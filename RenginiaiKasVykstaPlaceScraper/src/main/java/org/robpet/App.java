package org.robpet;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        String url = "https://renginiai.kasvyksta.lt/vietos/";
        String urlForInnerPage = "https://renginiai.kasvyksta.lt";
        List<Place> scrapedPlaces = new ArrayList<>();
        File input = new File("C:\\Users\\robpet2\\Desktop\\scrape.html");
        Document page = Jsoup.parse(input, "UTF-8", url);
        //Document page = Jsoup.connect(url).userAgent("Mozilla/4.0").get();
        Elements places = page.getElementsByClass("block place-block");

        ArrayList<String> imgURLs = new ArrayList<>();
        String imgURL = "";

        for(Element place : places) {
            Place obj = new Place();

            //Get place type from div with class=block-head
            String type = place.getElementsByClass("type-label").text();
            obj.setFk_place_type(type);

            //Get place title in div with class=block-body from div class=title
            String title = place.getElementsByClass("title").text();
            obj.setName(title);

            //Get place title in div with class=block-body from div class=location
            String location = place.getElementsByClass("location").select("a").text();
            obj.setAddress(location);
            //Get place city in div itemprop=address from meta tag
            String city = place.select("[itemprop=addressLocality]").attr("content");
            obj.setFk_city(city);
            //Get place phone from meta tag and remove unnecessary substrings
            String phone = place.select("[itemprop=telephone]").attr("content")
                    .replace(" ", "")
                    .replace("~", "")
                    .replace("-", "")
                    .replace("(", "")
                    .replace(")", "")
                    .split(";")[0]
                    .split(",")[0];
            obj.setPhone_number(phone);
            //Inner place page for additional data
            String nextUrl = place.select("[itemprop=url]").attr("href");
            Document innerPage = Jsoup.connect(nextUrl).userAgent("Mozilla/4.0").get();

            imgURL = "https:" + innerPage.select("img#1").attr("src");
            imgURLs.add(imgURL);

            //Places webpage
            String webPage = innerPage.select("div.bold-item.info-item:nth-of-type(4) > span > a").html();
            obj.setWebsite(webPage);
            //Places facebook page
            String fbPage = innerPage.select("div.bold-item.info-item:nth-of-type(5) > span > a").html();
            obj.setFacebook(fbPage);
            //Places description
            String description = innerPage.select("div.text-inner > p").text();
            obj.setDescription(description);

            obj.setFk_user_id(1);
            obj.setFk_photo(0);
            scrapedPlaces.add(obj);
        }

        //Login and get access token
        String loginSite = "http://localhost:8081/access/login";
        Token token = login("admin", "admin", loginSite);

        //HttpPost method to post objects in json format to endpoint /place
        HttpClient client;
        String postSite = "http://localhost:8081/place"; //Post places to this site
        String newPhotoSite = "http://localhost:8081/media";

        String folder = "place_photos";

        int counter = 0;
        for(Place place : scrapedPlaces){
            if(place.getPhone_number().equals("") || place.getPhone_number().equals(null)) place.setPhone_number("------------");
            if(place.getDescription().equals("") || place.getDescription().equals(null)) place.setDescription("Aprašymas nepateiktas.");
            try{
                System.out.println(place.toString()); //Place information
                client = HttpClientBuilder.create().build();
                HttpPost post = new HttpPost(postSite);
                String json = objectMapper.writeValueAsString(place);
                StringEntity postString = new StringEntity(json, "UTF-8");
                post.setEntity(postString);
                post.setHeader("Content-type", "application/json;charset=UTF-8");
                post.setHeader("Authorization", "Bearer " + token.getToken());
                Integer nextPlaceId = getNextPlaceId("http://localhost:8081/place/nextPlaceId");
                HttpResponse response = client.execute(post);
                System.out.println(EntityUtils.toString(response.getEntity())); //Response text
                if(response.getStatusLine().getStatusCode() / 100 == 2){
                    if(!imgURL.equals("")) {
                        Integer nextPhotoId = getNextPhotoId("http://localhost:8081/utils/nextPhotoId");
                        boolean saved = saveImage(imgURLs.get(counter), nextPhotoId.toString() + ".jpg");
                        if(saved) {
                            postPhoto(folder, newPhotoSite, token.getToken());
                            changePlacePhotoId(nextPlaceId, "http://localhost:8081/place", "http://localhost:8081/place", nextPhotoId, token.getToken());
                        }
                    }
                }
                counter++;
            }
            catch (JsonMappingException e){
                e.printStackTrace();
            }
        }
    }
    public static boolean saveImage(String imageUrl, String imgName) throws IOException {
        URL url = new URL(imageUrl);
        if(url != null && (url.toString().startsWith("http://") || url.toString().startsWith("https://"))){
            String fileName = url.getFile();
            //String destName = "C:\\Users\\robpet2\\Desktop\\tempFoto\\" + fileName.substring(fileName.lastIndexOf("/") + 1);
            String destName = "C:\\Users\\robpet2\\Desktop\\SemestroProjektas\\EventSearchPlatform\\src\\main\\resources\\user_upload\\place_photos\\" + imgName;
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
    public static Token login(String username, String password, String loginSite) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost login = new HttpPost(loginSite);
        StringEntity loginString = new StringEntity(objectMapper.writeValueAsString(new Login(username, password)), "UTF-8");
        login.setEntity(loginString);
        login.setHeader("Content-type", "application/json");
        HttpResponse loginResponse = client.execute(login);
        String tokenJson = EntityUtils.toString(loginResponse.getEntity(), "UTF-8");
        return objectMapper.readValue(tokenJson, Token.class);
    }
    public static int getNextPhotoId(String site) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet(site);
        return Integer.parseInt(EntityUtils.toString(client.execute(get).getEntity()));
    }
    public static int getNextPlaceId(String site) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet(site);
        return Integer.parseInt(EntityUtils.toString(client.execute(get).getEntity()));
    }
    public static void postPhoto(String folder, String site, String token) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(site);
        StringEntity entity = new StringEntity(folder, "UTF-8");
        post.setEntity(entity);
        post.setHeader("Authorization", "Bearer " + token);
        client.execute(post);
    }
    public static void changePlacePhotoId(Integer placeId, String getSite, String updateSite, Integer photoId, String token) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet(getSite + "/" + placeId);
        Place place = objectMapper.readValue(EntityUtils.toString(client.execute(get).getEntity()), Place.class);
        place.setFk_photo(photoId);
        HttpPut put = new HttpPut(updateSite + "/" + placeId);
        StringEntity placeJson = new StringEntity(objectMapper.writeValueAsString(place), "UTF-8");
        put.setEntity(placeJson);
        put.setHeader("Content-type", "application/json");
        put.setHeader("Authorization", "Bearer " + token);
        HttpResponse response = client.execute(put);
        System.out.println(EntityUtils.toString(response.getEntity()));
    }
}
