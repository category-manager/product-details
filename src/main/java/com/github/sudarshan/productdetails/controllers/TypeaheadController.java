package com.github.sudarshan.productdetails.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/typeahead")
public class TypeaheadController {
    @GetMapping
    public ResponseEntity<?> getSuggestions(@RequestParam("query")String query) {

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
