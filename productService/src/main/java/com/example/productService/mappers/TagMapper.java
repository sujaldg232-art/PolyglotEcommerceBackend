package com.example.productService.mappers;

import com.example.productService.dtos.Tag.TagDto;
import com.example.productService.dtos.Tag.TagRequestDto;
import com.example.productService.entities.Tag;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TagMapper {
    TagDto entityToDto(Tag tag);
    Tag requestToEntity(TagRequestDto tagRequestDto);
    List<Tag> requestToEntity(List<TagRequestDto> tagRequestDtoList);
}
