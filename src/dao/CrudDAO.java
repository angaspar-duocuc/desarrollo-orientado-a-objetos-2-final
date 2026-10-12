package dao;

import java.util.List;
import java.util.Optional;

public interface CrudDAO<T> {
    int create(T value);
    Optional<T> findById(int id);
    List<T> findAll();
    void update(T value);
    void delete(int id);
}