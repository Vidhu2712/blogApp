package io.blogapp.dao;

import io.blogapp.model.Tags;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TagRepository extends JpaRepository<Tags, UUID> {
    Tags findByName(String name);
}