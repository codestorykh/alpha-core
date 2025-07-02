package com.codestorykh.alpha.common.service;

import com.codestorykh.alpha.common.domain.BaseEntity;
import com.codestorykh.alpha.common.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface BaseService<T extends BaseEntity, ID> {

    T save(T entity);

    List<T> saveAll(List<T> entities);

    Optional<T> findById(ID id);

    List<T> findAll();

    Page<T> findAll(Pageable pageable);

    boolean existsById(ID id);

    long count();

    void deleteById(ID id);

    void delete(T entity);

    void deleteAll();

    T activate(ID id);

    T deactivate(ID id);

    List<T> findAllActive();

    Page<T> findAllActive(Pageable pageable);

    Optional<T> findByIdAndActive(ID id, boolean active);
} 