package com.github.sudarshan.productdetails.models.responses;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.List;

@Data
public class CategoryResponse {
    private JsonNode data;
    private String id;
    private List<CategoryResponse> children;
}
