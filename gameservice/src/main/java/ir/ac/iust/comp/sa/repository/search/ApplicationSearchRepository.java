package ir.ac.iust.comp.sa.repository.search;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import ir.ac.iust.comp.sa.domain.Application;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import reactor.core.publisher.Flux;

/**
 * Spring Data Elasticsearch repository for the {@link Application} entity.
 */
public interface ApplicationSearchRepository
    extends ReactiveElasticsearchRepository<Application, Long>, ApplicationSearchRepositoryInternal {}

interface ApplicationSearchRepositoryInternal {
    Flux<Application> search(String query);
}

class ApplicationSearchRepositoryInternalImpl implements ApplicationSearchRepositoryInternal {

    private final ReactiveElasticsearchTemplate reactiveElasticsearchTemplate;

    ApplicationSearchRepositoryInternalImpl(ReactiveElasticsearchTemplate reactiveElasticsearchTemplate) {
        this.reactiveElasticsearchTemplate = reactiveElasticsearchTemplate;
    }

    @Override
    public Flux<Application> search(String query) {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryStringQuery(query));
        return reactiveElasticsearchTemplate.search(nativeSearchQuery, Application.class).map(SearchHit::getContent);
    }
}
