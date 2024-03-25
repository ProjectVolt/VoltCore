package io.github.capure.voltcore.dto;

import io.github.capure.voltcore.model.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetTagDto {
    private Long id;
    private String name;

    public GetTagDto(Tag tag) {
        id = tag.getId();
        name = tag.getName();
    }
}
