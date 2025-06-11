package com.myproject.brokagefirmchallenge.repo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

    Optional<T> findByIdAndDeletedFalse(ID id);

    default T getByIdOrThrow(ID id) {
        return findById(id).orElseThrow(() ->
                new RuntimeException("Entity not found with id: " + id));
    }

    default T getByIdAndNotDeletedOrThrow(ID id) {
        return findByIdAndDeletedFalse(id).orElseThrow(() ->
                new RuntimeException("Entity not found or deleted with id: " + id));
    }
}
