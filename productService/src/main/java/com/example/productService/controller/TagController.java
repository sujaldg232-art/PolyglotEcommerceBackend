package com.example.productService.controller;

import com.example.productService.dtos.Tag.TagDto;
import com.example.productService.dtos.Tag.TagRequestDto;
import com.example.productService.service.TagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/Tag")
public class TagController {

    TagService tagService;

    public TagController(TagService tagService){
        this.tagService = tagService;
    }

    @GetMapping("/findById")
    public ResponseEntity<TagDto> findById(@RequestParam Long id){
        return ResponseEntity.ok(tagService.findById(id));
    }

    @PostMapping("/Create")
    public ResponseEntity<TagDto> findByTagDto(@RequestBody TagRequestDto tagRequestDto){
        return ResponseEntity.ok(tagService.saveByRequest(tagRequestDto));
    }
}

