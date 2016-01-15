package controllers;

import securesocial.core.java.Authorization;
import services.DemoUser;

/**
 * A sample authorization implementation that lets you filter requests based
 * on the provider that authenticated the user
 */
public class WithProvider implements Authorization<DemoUser> {
    public boolean isAuthorized(DemoUser user, String params[]) {
        return user.main.providerId().equals(params[0]);
    }
}