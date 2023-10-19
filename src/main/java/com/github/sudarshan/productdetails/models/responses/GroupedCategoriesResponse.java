package com.github.sudarshan.productdetails.models.responses;

import com.github.sudarshan.categoryManager.core.pojo.Categories;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
public class GroupedCategoriesResponse extends Categories {
    Set<String> linkedCategories;
    Set<String> rootCategories;
    Set<String> unlinkedCategories;
    public GroupedCategoriesResponse() {
        linkedCategories = new HashSet<>();
        unlinkedCategories = new HashSet<>();
        rootCategories = new HashSet<>();
    }
}
