package com.epam.rd.autocode.spring.project.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.Set;

@Entity
@Table(name = "categories")
@Data
@ToString(exclude = "books")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @ManyToMany(mappedBy = "categories")
    private Set<Book> books;
}
