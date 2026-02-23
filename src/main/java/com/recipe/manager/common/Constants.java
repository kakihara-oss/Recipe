package com.recipe.manager.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {

    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    // Recipe
    public static final int MAX_RECIPE_TITLE_LENGTH = 200;
    public static final int MAX_RECIPE_DESCRIPTION_LENGTH = 2000;

    // Recipe Status
    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_PUBLISHED = "PUBLISHED";
    public static final String STATUS_ARCHIVED = "ARCHIVED";
    public static final String STATUS_DELETED = "DELETED";

    // Roles
    public static final String ROLE_CHEF = "CHEF";
    public static final String ROLE_SERVICE = "SERVICE";
    public static final String ROLE_PURCHASER = "PURCHASER";
    public static final String ROLE_PRODUCER = "PRODUCER";

    // Ingredient Supply Status
    public static final String SUPPLY_AVAILABLE = "AVAILABLE";
    public static final String SUPPLY_LIMITED = "LIMITED";
    public static final String SUPPLY_UNAVAILABLE = "UNAVAILABLE";
    public static final String SUPPLY_SEASONAL = "SEASONAL";
}
