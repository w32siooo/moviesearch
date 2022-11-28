package cygni.denmark.moviesearchservice.persistence;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ComponentScan
@Configuration
@EnableJpaRepositories
public class MovieSearchPersistenceAutoconfig {}
