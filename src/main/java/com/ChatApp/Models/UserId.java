package com.ChatApp.Models;

import java.security.Principal;

public class UserId implements Principal {
	private String id;
    private String name;

    public UserId(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String getName() {
        return this.id;   // MUST override
    }

    public String getId() {
        return this.id;     // your custom method
    }

}
