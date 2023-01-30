package ir.ac.iust.comp.sa.repository.search;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import ir.ac.iust.comp.sa.domain.MyGameObject;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import reactor.core.publisher.Flux;

/**
 * Spring Data Elasticsearch repository for the {@link MyGameObject} entity.
 */
public interface MyGameObjectSearchRepository
    extends ReactiveElasticsearchRepository<MyGameObject, Long>, MyGameObjectSearchRepositoryInternal {}

interface MyGameObjectSearchRepositoryInternal {
    Flux<MyGameObject> search(String query);
}

class MyGameObjectSearchRepositoryInternalImpl implements MyGameObjectSearchRepositoryInternal {

    private final ReactiveElasticsearchTemplate reactiveElasticsearchTemplate;

    MyGameObjectSearchRepositoryInternalImpl(ReactiveElasticsearchTemplate reactiveElasticsearchTemplate) {
        this.reactiveElasticsearchTemplate = reactiveElasticsearchTemplate;
    }

    @Override
    public Flux<MyGameObject> search(String query) {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryStringQuery(query));
        return reactiveElasticsearchTemplate.search(nativeSearchQuery, MyGameObject.class).map(SearchHit::getContent);
    }
}
