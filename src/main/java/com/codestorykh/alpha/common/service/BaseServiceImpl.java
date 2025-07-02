package com.codestorykh.alpha.common.service;

import com.codestorykh.alpha.common.domain.BaseEntity;
import com.codestorykh.alpha.common.repository.BaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class BaseServiceImpl<T extends BaseEntity, ID> implements BaseService<T, ID> {

    protected final BaseRepository<T, ID> repository;

    @Override
    public T save(T entity) {
        return repository.save(entity);
    }

    @Override
    public List<T> saveAll(List<T> entities) {
        return repository.saveAll(entities);
    }

    @Override
    public Optional<T> findById(ID id) {
        return repository.findById(id);
    }

    @Override
    public List<T> findAll() {
        return repository.findAll();
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public boolean existsById(ID id) {
        return repository.existsById(id);
    }

    @Override
    public long count() {
        return repository.count();
    }

    @Override
    public void deleteById(ID id) {
        repository.deleteById(id);
    }

    @Override
    public void delete(T entity) {
        repository.delete(entity);
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }

    @Override
    public T activate(ID id) {
        return repository.findById(id)
                .map(entity -> {
                    entity.setActive(true);
                    return repository.save(entity);
                })
                .orElseThrow(() -> new RuntimeException("Entity not found with id: " + id));
    }

    @Override
    public T deactivate(ID id) {
        return repository.findById(id)
                .map(entity -> {
                    entity.setActive(false);
                    return repository.save(entity);
                })
                .orElseThrow(() -> new RuntimeException("Entity not found with id: " + id));
    }

    @Override
    public List<T> findAllActive() {
        return repository.findAllByActive(true);
    }

    @Override
    public Page<T> findAllActive(Pageable pageable) {
        return repository.findAllByActive(true, pageable);
    }

    @Override
    public Optional<T> findByIdAndActive(ID id, boolean active) {
        return repository.findByIdAndActive(id, active);
    }
} 