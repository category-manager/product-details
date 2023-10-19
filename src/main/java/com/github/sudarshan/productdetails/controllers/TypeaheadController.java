package com.github.sudarshan.productdetails.controllers;

import com.github.sudarshan.productdetails.services.TypeaheadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/suggest")
public class TypeaheadController {
    @Autowired
    TypeaheadService typeaheadService;
    @GetMapping
    @Cacheable(value = "suggestions", key = "#query")
    public ResponseEntity<?> getSuggestions(@RequestParam(value = "query", required = true) String query) {
        List<String> suggestions = this.typeaheadService.getSuggestions(query.toLowerCase());
        return new ResponseEntity<>(suggestions, HttpStatus.OK);
    }

    @GetMapping(value = "/ordinal-words")
    @Cacheable(value="ordinal-words")
    public ResponseEntity<?> getOrdinalToNamesMap() {
        Map<Integer, List<String>> map = this.typeaheadService.getOrdinalToNamesMap();
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    @GetMapping(value = "/word-ordinals")
    public ResponseEntity<?> getWordToOrdinalsMap() {
        Map<String, List<Integer>> map = this.typeaheadService.getWordToOrdinalMap();
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

}
