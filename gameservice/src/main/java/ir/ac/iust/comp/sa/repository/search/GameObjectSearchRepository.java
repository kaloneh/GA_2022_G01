package ir.ac.iust.comp.sa.repository.search;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import ir.ac.iust.comp.sa.domain.GameObject;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import reactor.core.publisher.Flux;

/**
 * Spring Data Elasticsearch repository for the {@link GameObject} entity.
 */
public interface GameObjectSearchRepository extends ReactiveElasticsearchRepository<GameObject, Long>, GameObjectSearchRepositoryInternal {}

interface GameObjectSearchRepositoryInternal {
    Flux<GameObject> search(String query);
}

class GameObjectSearchRepositoryInternalImpl implements GameObjectSearchRepositoryInternal {

    private final ReactiveElasticsearchTemplate reactiveElasticsearchTemplate;

    GameObjectSearchRepositoryInternalImpl(ReactiveElasticsearchTemplate reactiveElasticsearchTemplate) {
        this.reactiveElasticsearchTemplate = reactiveElasticsearchTemplate;
    }

    @Override
    public Flux<GameObject> search(String query) {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryStringQuery(query));
        return reactiveElasticsearchTemplate.search(nativeSearchQuery, GameObject.class).map(SearchHit::getContent);
    }
}
