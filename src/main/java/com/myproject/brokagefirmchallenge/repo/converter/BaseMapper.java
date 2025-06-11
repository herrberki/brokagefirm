package com.myproject.brokagefirmchallenge.repo.converter;

import org.mapstruct.MappingTarget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;

public interface BaseMapper<E, D, V> {

    E toEntity(D dto);

    V toVO(E entity);

    List<V> toVOList(List<E> entities);

    void updateEntityFromDto(D dto, @MappingTarget E entity);

    default Page<V> toVOPage(Page<E> entityPage) {
        List<V> voList = toVOList(entityPage.getContent());
        return new PageImpl<>(voList, entityPage.getPageable(), entityPage.getTotalElements());
    }
}