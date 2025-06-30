package com.tastopia.tastopia.specification;

import com.tastopia.tastopia.entity.MenuItem;
import com.tastopia.tastopia.entity.MenuItemRating;
import com.tastopia.tastopia.entity.Restaurant;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class RestaurantSpecification {

    public static Specification<Restaurant> isVegOnly() {
        return (root, query, criteriaBuilder) -> {
            Subquery<MenuItem> subquery = query.subquery(MenuItem.class);
            Root<MenuItem> menuItem = subquery.from(MenuItem.class);
            subquery.where(criteriaBuilder.and(
                criteriaBuilder.equal(menuItem.get("restaurant"), root),
                criteriaBuilder.notEqual(menuItem.get("vegStatus"), "VEG")
            ));
            return criteriaBuilder.not(criteriaBuilder.exists(subquery));
        };
    }

    public static Specification<Restaurant> isFastDelivery() {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.lessThanOrEqualTo(root.get("avgDeliveryTimeInMinutes"), 30);
    }

    public static Specification<Restaurant> hasMinimumRating(Double minRating) {
        return (root, query, criteriaBuilder) -> {
            Subquery<Double> subquery = query.subquery(Double.class);
            Root<MenuItem> menuItem = subquery.from(MenuItem.class);
            Join<MenuItem, MenuItemRating> rating = menuItem.join("ratings");
            subquery.select(criteriaBuilder.avg(rating.get("rating")))
                    .where(criteriaBuilder.equal(menuItem.get("restaurant"), root));
            return criteriaBuilder.greaterThanOrEqualTo(subquery, minRating);
        };
    }
}