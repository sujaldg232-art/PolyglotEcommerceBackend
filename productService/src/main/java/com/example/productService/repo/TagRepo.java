package com.example.productService.repo;

import com.example.productService.entities.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepo extends JpaRepository<Tag,Long> {

    Optional<Tag> findByName(String name);
    List<Tag> findByNameIn(List<String> names);
}
