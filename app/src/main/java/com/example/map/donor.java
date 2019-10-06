package com.example.map;

public class donor {
    String id;
    String name;
    String group;
    String address;
    String phone;
    double latitude, longitude;
    public donor(){

    }
    public donor(String id, String name, String group, String phone, double latitude, double longitude){
        this.name = name;
        this.id = id;
        this.group = group;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phone = phone;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() { return latitude; }

    public double getLongitude() { return longitude; }

    public String getGroup() {
        return group;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }
}
