package com.example.map;

public class UserInfo {
    String name, address, phone, group, id;
    public UserInfo(String id, String name, String address, String phone, String group) {
        this.id = id;
        this.name = name;
        this.address= address;
        this.phone = phone;
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getGroup() {
        return group;
    }

    public String getId() {
        return id;
    }
}
