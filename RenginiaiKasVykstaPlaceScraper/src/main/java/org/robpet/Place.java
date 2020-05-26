package org.robpet;

public class Place {
    private Integer id;
    private Integer fk_user_id;
    private String name;
    private String fk_place_type;
    private String address;
    private String fk_city;
    private String phone_number;
    private String website;
    private String facebook;
    private String description;
    private String working_hours;
    private Integer fk_photo;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Place(Integer fk_user_id, String name, String fk_place_type, String address, String fk_city, String phone_number, String website, String facebook, String description, String working_hours, Integer fk_photo) {
        this.fk_user_id = fk_user_id;
        this.name = name;
        this.fk_place_type = fk_place_type;
        this.address = address;
        this.fk_city = fk_city;
        this.phone_number = phone_number;
        this.website = website;
        this.facebook = facebook;
        this.description = description;
        this.working_hours = working_hours;
        this.fk_photo = fk_photo;
    }
    public Place(){
        super();
    }

    @Override
    public String toString() {
        return "Place{" +
                "fk_user_id=" + fk_user_id +
                ", name='" + name + '\'' +
                ", fk_place_type='" + fk_place_type + '\'' +
                ", address='" + address + '\'' +
                ", fk_city='" + fk_city + '\'' +
                ", phone_number='" + phone_number + '\'' +
                ", website='" + website + '\'' +
                ", facebook='" + facebook + '\'' +
                ", description='" + description + '\'' +
                ", working_hours='" + working_hours + '\'' +
                ", fk_photo=" + fk_photo +
                '}';
    }

    public Integer getFk_user_id() {
        return fk_user_id;
    }

    public void setFk_user_id(Integer fk_user_id) {
        this.fk_user_id = fk_user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFk_place_type() {
        return fk_place_type;
    }

    public void setFk_place_type(String fk_place_type) {
        this.fk_place_type = fk_place_type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getFk_city() {
        return fk_city;
    }

    public void setFk_city(String fk_city) {
        this.fk_city = fk_city;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getFacebook() {
        return facebook;
    }

    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWorking_hours() {
        return working_hours;
    }

    public void setWorking_hours(String working_hours) {
        this.working_hours = working_hours;
    }

    public Integer getFk_photo() {
        return fk_photo;
    }

    public void setFk_photo(Integer fk_photo) {
        this.fk_photo = fk_photo;
    }

}
