package com.example.demo;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
@Component
public class AllBooksDataFetcher implements DataFetcher<List<Book>> {
    private BookRepository bookRepository;
    @Autowired
    public AllBooksDataFetcher(BookRepository bookRepository) {
        this.bookRepository=bookRepository;
    }
    @Override
    public List<Book> get(DataFetchingEnvironment dataFetchingEnvironment) {
        return bookRepository.findAll();
    }
}