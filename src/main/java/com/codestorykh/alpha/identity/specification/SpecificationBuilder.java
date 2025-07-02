package com.codestorykh.alpha.identity.specification;

import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Utility class for building dynamic specifications with chaining support.
 * This class provides a fluent API for creating complex specifications.
 */
public class SpecificationBuilder<T> {

    private final List<Specification<T>> specifications = new ArrayList<>();

    /**
     * Add a specification to the builder
     */
    public SpecificationBuilder<T> and(Specification<T> specification) {
        if (specification != null) {
            specifications.add(specification);
        }
        return this;
    }

    /**
     * Add a specification conditionally
     */
    public SpecificationBuilder<T> andIf(boolean condition, Specification<T> specification) {
        if (condition && specification != null) {
            specifications.add(specification);
        }
        return this;
    }

    /**
     * Add a specification conditionally based on a value
     */
    public <V> SpecificationBuilder<T> andIfNotNull(V value, Function<V, Specification<T>> specificationFunction) {
        if (value != null) {
            Specification<T> specification = specificationFunction.apply(value);
            if (specification != null) {
                specifications.add(specification);
            }
        }
        return this;
    }

    /**
     * Add a specification conditionally based on a string value
     */
    public SpecificationBuilder<T> andIfHasText(String value, Function<String, Specification<T>> specificationFunction) {
        if (value != null && !value.trim().isEmpty()) {
            Specification<T> specification = specificationFunction.apply(value);
            if (specification != null) {
                specifications.add(specification);
            }
        }
        return this;
    }

    /**
     * Add a specification conditionally based on a list value
     */
    public <V> SpecificationBuilder<T> andIfNotEmpty(List<V> values, Function<List<V>, Specification<T>> specificationFunction) {
        if (values != null && !values.isEmpty()) {
            Specification<T> specification = specificationFunction.apply(values);
            if (specification != null) {
                specifications.add(specification);
            }
        }
        return this;
    }

    /**
     * Build the final specification by combining all specifications with AND
     */
    public Specification<T> build() {
        if (specifications.isEmpty()) {
            return Specification.where(null);
        }
        
        if (specifications.size() == 1) {
            return specifications.get(0);
        }
        
        return specifications.stream()
            .reduce(Specification.where(null), Specification::and);
    }

    /**
     * Build the final specification by combining all specifications with OR
     */
    public Specification<T> buildOr() {
        if (specifications.isEmpty()) {
            return Specification.where(null);
        }
        
        if (specifications.size() == 1) {
            return specifications.get(0);
        }
        
        return specifications.stream()
            .reduce(Specification.where(null), Specification::or);
    }

    /**
     * Create a new builder instance
     */
    public static <T> SpecificationBuilder<T> create() {
        return new SpecificationBuilder<>();
    }

    /**
     * Create a builder with an initial specification
     */
    public static <T> SpecificationBuilder<T> create(Specification<T> initialSpecification) {
        SpecificationBuilder<T> builder = new SpecificationBuilder<>();
        if (initialSpecification != null) {
            builder.specifications.add(initialSpecification);
        }
        return builder;
    }
} 