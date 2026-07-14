package com.example.productService;

import com.example.productService.dtos.Tag.TagDto;
import com.example.productService.dtos.Tag.TagRequestDto;
import com.example.productService.entities.Tag;
import com.example.productService.mappers.TagMapper;
import com.example.productService.repo.TagRepo;
import com.example.productService.service.TagService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class TagServiceTest {

    @Mock
    TagRepo tagRepo;

    @Mock
    TagMapper tagMapper;

    @InjectMocks
    TagService  tagService;

    TagRequestDto tagRequestDto;
    TagDto tagDto;
    Tag tag;

    @BeforeEach
    public void init() {
        tagRequestDto = new TagRequestDto("SampleTag");
        tagDto = new TagDto(1L, "SampleTag");
        tag = Tag.builder()
                .id(1L)
                .name("SampleTag")
                .build();
    }

    @Nested
    class FindById {

        @Test
        void findByIdSuccess() {
            when(tagRepo.findById(1L)).thenReturn(Optional.of(tag));
            when(tagMapper.entityToDto(tag)).thenReturn(tagDto);

            TagDto res = tagService.findById(tag.getId());

            assertNotNull(res);
            assertEquals(tagDto.id(), res.id());
            assertEquals(tagDto.name(), res.name());
            verify(tagRepo).findById(1L);
            verify(tagMapper).entityToDto(tag);
        }

        @Test
        void findByIdFailure() {
            when(tagRepo.findById(1L)).thenReturn(Optional.empty());
            when(tagMapper.entityToDto(null)).thenReturn(null);

            TagDto res = tagService.findById(1L);

            assertNull(res);
            verify(tagRepo).findById(1L);
            verify(tagMapper).entityToDto(null);
        }
    }

    @Nested
    class SaveByRequest {

        @Test
        void saveByRequestSuccess() {
            when(tagMapper.requestToEntity(tagRequestDto)).thenReturn(tag);
            when(tagRepo.save(tag)).thenReturn(tag);
            when(tagMapper.entityToDto(tag)).thenReturn(tagDto);

            TagDto res = tagService.saveByRequest(tagRequestDto);

            assertNotNull(res);
            assertEquals(tagDto.id(), res.id());
            assertEquals(tagDto.name(), res.name());
            verify(tagMapper).requestToEntity(tagRequestDto);
            verify(tagRepo).save(tag);
            verify(tagMapper).entityToDto(tag);
        }
    }
}
