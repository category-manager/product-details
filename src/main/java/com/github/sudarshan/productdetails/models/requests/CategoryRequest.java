package com.github.sudarshan.productdetails.models.requests;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.Set;

@Data
public class CategoryRequest {
    private String requestType;
    private String categoryId;
    private Set<String> parents;
    private JsonNode data;
}
