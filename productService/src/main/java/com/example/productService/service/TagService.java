package com.example.productService.service;

import com.example.productService.dtos.Tag.TagDto;
import com.example.productService.dtos.Tag.TagRequestDto;
import com.example.productService.entities.Tag;
import com.example.productService.mappers.TagMapper;
import com.example.productService.repo.TagRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TagService {
    TagRepo tagRepo;
    TagMapper tagMapper;

    @Autowired
    public TagService(TagRepo tagRepo,TagMapper tagMapper){
        this.tagRepo = tagRepo;
        this.tagMapper = tagMapper;
    }

    public TagDto findById(Long id){
        Tag result = tagRepo.findById(id).orElse(null);
        TagDto resultDto = tagMapper.entityToDto(result);
        return resultDto;
    }

    public TagDto saveByRequest(TagRequestDto tagRequestDto){
        Tag tag = tagMapper.requestToEntity(tagRequestDto);
        Tag savedTag = tagRepo.save(tag);
        return tagMapper.entityToDto(savedTag);
    }
}
