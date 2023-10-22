package com.github.sudarshan.productdetails.controllers;

import com.github.sudarshan.productdetails.models.requests.CategoryRequest;
import com.github.sudarshan.productdetails.services.CategoriesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(value = {"*"})
@RequestMapping(value = "/api/categories")
public class CategoriesController {
    @Autowired
    private CategoriesService categoriesService;

    @RequestMapping(value = "/import", method = RequestMethod.GET)
    public ResponseEntity<?> importCategoriesFromDb () {
        boolean isSuccess = this.categoriesService.importCategoriesFromDb();
        return new ResponseEntity<>(isSuccess, HttpStatus.OK);
    }
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResponseEntity<?> createNewCategory(@RequestBody CategoryRequest request) {
        boolean isSuccess = categoriesService.upsertCategory(request);
        return new ResponseEntity<>(isSuccess, HttpStatus.OK);
    }
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public ResponseEntity<?> updateCategoryDetails(@RequestBody CategoryRequest request) {
        boolean isSuccess = categoriesService.upsertCategory(request);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteCategory(@RequestParam(value = "id", required = true) String categoryId) {
        boolean isSuccess = categoriesService.deleteCategory(categoryId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public ResponseEntity<?> getAllCategories(@RequestParam(value = "grouped", required = false, defaultValue = "true") boolean isGrouped) {
        var categoriesResponse = this.categoriesService.getAllCategories(isGrouped);
        return new ResponseEntity<>(categoriesResponse, HttpStatus.OK);
    }
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> getCategoryDetailsByIdInHierarchicalForm(@PathVariable("id") String id) {

        var categoryDetails = this.categoriesService.getCategoryDetailsById(id);
        return new ResponseEntity<>(categoryDetails, HttpStatus.OK);
    }
    @RequestMapping(value = "/hierarchy/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> getCategoryHierarchyByIdInHierarchicalForm(@PathVariable("id") String id,
                                                    @RequestParam(value = "ancestors", required = false, defaultValue = "false") boolean ancestors,
                                                    @RequestParam(value = "descendants", required = false, defaultValue = "false") boolean descendants,
                                                    @RequestParam(value = "height", required = false, defaultValue = "2") int height) {
        // TODO: use height to generate descendant-paths for
        //  the `id` of depth `height`.

        var categoryInHierarchicalForm = this.categoriesService.getCategoryHierarchyById(id, descendants);
        return new ResponseEntity<>(categoryInHierarchicalForm, HttpStatus.OK);
    }

}
