package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import javax.annotation.PostConstruct;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.stream.Stream;
@Service
public class GraphQLService {
    private static Logger logger = LoggerFactory.getLogger(GraphQLService.class);
    private BookRepository bookRepository;
    private AllBooksDataFetcher allBooksDataFetcher;
    private BookDataFetcher bookDataFetcher;
    @Value("classpath:books.graphql")
    Resource resource;
    private GraphQL graphQL;
    @Autowired
    public GraphQLService(BookRepository bookRepository, AllBooksDataFetcher allBooksDataFetcher,
                          BookDataFetcher bookDataFetcher) {
        this.bookRepository=bookRepository;
        this.allBooksDataFetcher=allBooksDataFetcher;
        this.bookDataFetcher=bookDataFetcher;
    }
    @PostConstruct
    private void loadSchema() throws IOException {
        logger.info("Entering loadSchema@GraphQLService");
        loadDataIntoHSQL();
        //Get the graphql file
        String schema = "schema{\n" + 
        		" query:Query\n" + 
        		"}\n" + 
        		"type Query{\n" + 
        		"  allBooks: [Book]\n" + 
        		"  book(id: String): Book\n" + 
        		"}\n" + 
        		"type Book{\n" + 
        		"  isn:String\n" + 
        		"  title:String\n" + 
        		"  publisher:String\n" + 
        		"  author:[String]\n" + 
        		"  publishedDate:String\n" + 
        		"}";
        TypeDefinitionRegistry typeDefinitionRegistry = new SchemaParser().parse(schema);
        RuntimeWiring runtimeWiring = buildRuntimeWiring();
        GraphQLSchema graphQLSchema = new SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
        graphQL = GraphQL.newGraphQL(graphQLSchema).build();
    }
    private void loadDataIntoHSQL() {
        Stream.of(
                new Book("1001", "The C Programming Language", "PHI Learning", "1978",
                        new String[] {
                                "Brian W. Kernighan (Contributor)",
                                "Dennis M. Ritchie"
                }),
                new Book("1002","Your Guide To Scrivener", "MakeUseOf.com", " April 21st 2013",
                        new String[] {
                                "Nicole Dionisio (Goodreads Author)"
                        }),
                new Book("1003","Beyond the Inbox: The Power User Guide to Gmail", " Kindle Edition", "November 19th 2012",
                        new String[] {
                                "Shay Shaked"
                                ,  "Justin Pot"
                                , "Angela Randall (Goodreads Author)"
                        }),
                new Book("1004","Scratch 2.0 Programming", "Smashwords Edition", "February 5th 2015",
                        new String[] {
                                "Denis Golikov (Goodreads Author)"
                        }),
                new Book("1005","Pro Git", "by Apress (first published 2009)", "2014",
                        new String[] {
                                "Scott Chacon"
                        })
        ).forEach(book -> {
            bookRepository.save(book);
        });
    }
    private RuntimeWiring buildRuntimeWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .type("Query", typeWiring -> typeWiring
                .dataFetcher("allBooks", allBooksDataFetcher)
                .dataFetcher("book", bookDataFetcher)).
                 build();
    }
    public GraphQL getGraphQL(){
        return graphQL;
    }
}