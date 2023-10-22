package com.github.sudarshan.productdetails.services;

import static com.github.sudarshan.categoryManager.core.clientService.CategoryManagerClient.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sudarshan.categoryManager.core.clientService.CategoryManagerClient;
import com.github.sudarshan.categoryManager.core.pojo.Categories;
import com.github.sudarshan.categoryManager.core.pojo.CategoriesPaths;
import com.github.sudarshan.categoryManager.core.pojo.CoreConstants;
import com.github.sudarshan.categoryManager.core.sp.Node;
import com.github.sudarshan.categoryManager.core.spi.Export;
import com.github.sudarshan.categoryManager.core.spi.Import;
import com.github.sudarshan.categoryManager.core.spi.RealtimeOperation;
import com.github.sudarshan.productdetails.models.requests.CategoryRequest;
import com.github.sudarshan.productdetails.models.responses.CategoryResponse;
import com.github.sudarshan.productdetails.models.responses.GroupedCategoriesResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
public class CategoriesService {
    @Autowired
    private CategoryManagerClientBuilder cmcBuilder;
    @Autowired
    Connection connection;

    private CategoryManagerClient cmc;
    private RealtimeOperation<String, Node> rop;
    private Export<String, Node> restExport;
    private Import<String, Node> importOperation;
    private ObjectMapper mapper;

    @PostConstruct
    public void setup() {
        this.cmc = cmcBuilder.configureRestExport()
                .configureRop()
                .build();
        this.rop = this.cmc.getRealtimeOperation();
        this.restExport = this.cmc.getRestExport();
        this.mapper = new ObjectMapper();
        this.importOperation = this.cmc.getDbImport();
    }
    public boolean upsertCategory(CategoryRequest request) {
        boolean isValid = this.validateRequest(request);
        if(isValid) {
            if(request.getRequestType().equalsIgnoreCase("create")) {
                Node node = new Node();
                node.set_id(request.getCategoryId());
                node.setParents(new HashSet<>(){{ addAll(request.getParents()); }});
                node.setData(request.getData());
                this.rop.add(node);
                return true;
            } else if(request.getRequestType().equalsIgnoreCase("update")) {
                Node node = new Node();
                node.set_id(request.getCategoryId());
                node.setParents(new HashSet<>(){{ addAll(request.getParents()); }});
                node.setData(request.getData());
                this.rop.update(node);
                return true;
            }else{
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean validateRequest(CategoryRequest request) {
        if(Objects.isNull(request.getCategoryId()) || request.getCategoryId().isBlank()) {
            log.error("Invalid request, categoryId is missing");
            return false;
        } else if(Objects.isNull(request.getParents())) {
            log.error("Invalid request, parent is null");
            return false;
        } else if(Objects.isNull(request.getRequestType()) || request.getRequestType().isBlank()){
            log.error("Invalid request, request type cannot be null or blank");
            return false;
        }else {
            return true;
        }
    }

    public boolean deleteCategory(String categoryId) {
        if(Objects.isNull(categoryId) || categoryId.isBlank())
            return false;
        this.rop.delete(new HashSet<>(){{add(categoryId);}});
        return true;
    }

    public Categories getAllCategories(boolean isGrouped) {
        var allCategories = (Categories)this.restExport.exportAll();
        if(isGrouped) {
            var response = new GroupedCategoriesResponse();
            this.groupCategories(allCategories, response);
            return response;
        } else {
            return allCategories;
        }
    }

    private void groupCategories(Categories allCategories, GroupedCategoriesResponse response) {
        for (Node node : allCategories.getCategoryList()) {
            if(node.getParents().contains(CoreConstants.HEAD_NODE_ID)) {
                response.getRootCategories().add(node.get_id());
                response.getLinkedCategories().add(node.get_id());
            } else if(node.getParents().contains(CoreConstants.UNLINKED_NODE_ID)) {
                response.getUnlinkedCategories().add(node.get_id());
            } else {
                if(node.get_id().equalsIgnoreCase(CoreConstants.HEAD_NODE_ID))
                    response.getLinkedCategories().add(node.get_id());
                else if(node.get_id().equalsIgnoreCase(CoreConstants.UNLINKED_NODE_ID))
                    response.getUnlinkedCategories().add(node.get_id());
                else
                    response.getLinkedCategories().add(node.get_id());
            }
        }
    }

    public CategoryResponse getCategoryHierarchyById(String id, boolean descendants) {
        var paths = (CategoriesPaths)this.restExport.exportPathById(id);
        var ancestorPaths = paths.getCategoriesPaths().get(0).getAncestorPaths();
        var descendantPaths = paths.getCategoriesPaths().get(0).getDescendantPaths();
        var nodeMap = paths.getCategoriesPaths().get(0).getNodeMap();
        log.info("nodeMap = {}", nodeMap);
        return this.constructCategoryResponse((descendants)? descendantPaths: ancestorPaths, nodeMap);
    }
/*
algo:
    - use FIFO queue to store each path.
    - iterate until queue is empty.
    - at each level of the tree, check for the FIFO element. If present , great, update the local ref
    and pop the first element out and move further.
    else, add new child.
    - continue the same for all the paths.
*/
    private CategoryResponse constructCategoryResponse(List<String> ancestorPaths, Map<String, Node> nodeMap) {

        CategoryResponse response = new CategoryResponse();
        response.setId("dummy_".concat(CoreConstants.HEAD_NODE_ID));
        response.setChildren(new ArrayList<>());
        response.setData(null);

        CategoryResponse temp = response;
        List<Queue<String>> queues = new ArrayList<>();
        for (var path: ancestorPaths) {
            Queue<String> q = Arrays.stream(path.split("\\.")).collect(Collectors.toCollection(LinkedList::new));
            queues.add(q);
        }
        for (var fifo: queues) {
            temp = response;
            while(!fifo.isEmpty()) {
                var children = temp.getChildren().stream().map(CategoryResponse::getId).collect(Collectors.toList() );
                var categoryId = fifo.peek();
                int index = children.indexOf(categoryId);
                if(index > -1) {
                    temp = temp.getChildren().get(index);
                } else {
                    var newCategory = new CategoryResponse();
                    newCategory.setId(categoryId);
                    newCategory.setChildren(new ArrayList<>());
                    temp.getChildren().add(newCategory);
                    children.add(categoryId);
                    temp = temp.getChildren().get(children.size() -1);
                }
                fifo.poll();
            }
        }
        try {
            log.info("{}", mapper.writeValueAsString(response));
        } catch (JsonProcessingException e) {
            log.error("Error logging categoryResponse object. {} ", response);
        }
        return response;
    }

    public boolean importCategoriesFromDb() {
        this.importOperation.importData();
        return true;
    }

    public Object getCategoryDetailsById(String id) {
        return ((Categories)this.restExport.exportById(id)).getCategoryList().get(0);
    }
}
