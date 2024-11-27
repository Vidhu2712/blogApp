package io.blogapp.service;

import io.blogapp.dao.TagRepository;
import io.blogapp.model.Tags;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TagService {
    @Autowired
    private TagRepository tagRepository;

    public Tags getTagByName(String tagName) {
        return tagRepository.findByName(tagName);
    }

    public Tags getOrCreateTag(String tagName) {
        Tags tag = getTagByName(tagName);
        if (tag == null) {
            tag = new Tags();
            tag.setName(tagName);
            tag = tagRepository.save(tag);
        }
        return tag;
    }
}