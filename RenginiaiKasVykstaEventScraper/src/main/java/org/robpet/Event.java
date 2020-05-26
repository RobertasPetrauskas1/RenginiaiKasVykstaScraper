package org.robpet;

import java.time.LocalDate;

public class Event {

    private Integer id;
    private Integer fk_user_id;
    private String name;
    private String fk_event_type;
    private LocalDate date;
    private String time;
    private String duration;
    private String fk_city;
    private Integer fk_place;
    private String phone_number;
    private String website;
    private String facebook;
    private String description;
    private String tickets;
    private Integer fk_photo;

    public Event(){super();}

    public Event(Integer id, Integer fk_user_id, String name, String fk_event_type, LocalDate date, String time, String duration, String fk_city, Integer fk_place, String phone_number, String website, String facebook, String description, String tickets, Integer fk_photo) {
        this.id = id;
        this.fk_user_id = fk_user_id;
        this.name = name;
        this.fk_event_type = fk_event_type;
        this.date = date;
        this.time = time;
        this.duration = duration;
        this.fk_city = fk_city;
        this.fk_place = fk_place;
        this.phone_number = phone_number;
        this.website = website;
        this.facebook = facebook;
        this.description = description;
        this.tickets = tickets;
        this.fk_photo = fk_photo;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getFk_event_type() {
        return fk_event_type;
    }

    public void setFk_event_type(String fk_event_type) {
        this.fk_event_type = fk_event_type;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getFk_city() {
        return fk_city;
    }

    public void setFk_city(String fk_city) {
        this.fk_city = fk_city;
    }

    public Integer getFk_place() {
        return fk_place;
    }

    public void setFk_place(Integer fk_place) {
        this.fk_place = fk_place;
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

    public String getTickets() {
        return tickets;
    }

    public void setTickets(String tickets) {
        this.tickets = tickets;
    }

    public Integer getFk_photo() {
        return fk_photo;
    }

    public void setFk_photo(Integer fk_photo) {
        this.fk_photo = fk_photo;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", fk_user_id=" + fk_user_id +
                ", name='" + name + '\'' +
                ", fk_event_type='" + fk_event_type + '\'' +
                ", date=" + date +
                ", time='" + time + '\'' +
                ", duration='" + duration + '\'' +
                ", fk_city='" + fk_city + '\'' +
                ", fk_place=" + fk_place +
                ", phone_number='" + phone_number + '\'' +
                ", website='" + website + '\'' +
                ", facebook='" + facebook + '\'' +
                ", description='" + description + '\'' +
                ", tickets='" + tickets + '\'' +
                ", fk_photo=" + fk_photo +
                '}';
    }
}
