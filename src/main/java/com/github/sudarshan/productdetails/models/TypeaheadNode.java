package com.github.sudarshan.productdetails.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
public class TypeaheadNode {
    private Set<Integer> ordinals;
    private Character key;
    private List<TypeaheadNode> children;

    public TypeaheadNode(){
        ordinals = new HashSet<>(2);
        children = new ArrayList<>(10);
        key = Character.MIN_VALUE;
    }


}
