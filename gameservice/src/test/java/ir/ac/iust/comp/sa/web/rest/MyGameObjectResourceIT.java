package ir.ac.iust.comp.sa.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import ir.ac.iust.comp.sa.IntegrationTest;
import ir.ac.iust.comp.sa.domain.MyGameObject;
import ir.ac.iust.comp.sa.repository.MyGameObjectRepository;
import ir.ac.iust.comp.sa.repository.search.MyGameObjectSearchRepository;
import ir.ac.iust.comp.sa.service.EntityManager;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Integration tests for the {@link MyGameObjectResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient
@WithMockUser
class MyGameObjectResourceIT {

    private static final String ENTITY_API_URL = "/api/my-game-objects";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/my-game-objects";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private MyGameObjectRepository myGameObjectRepository;

    /**
     * This repository is mocked in the ir.ac.iust.comp.sa.repository.search test package.
     *
     * @see ir.ac.iust.comp.sa.repository.search.MyGameObjectSearchRepositoryMockConfiguration
     */
    @Autowired
    private MyGameObjectSearchRepository mockMyGameObjectSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private MyGameObject myGameObject;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static MyGameObject createEntity(EntityManager em) {
        MyGameObject myGameObject = new MyGameObject();
        return myGameObject;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static MyGameObject createUpdatedEntity(EntityManager em) {
        MyGameObject myGameObject = new MyGameObject();
        return myGameObject;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(MyGameObject.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @AfterEach
    public void cleanup() {
        deleteEntities(em);
    }

    @BeforeEach
    public void initTest() {
        deleteEntities(em);
        myGameObject = createEntity(em);
    }

    @Test
    void createMyGameObject() throws Exception {
        int databaseSizeBeforeCreate = myGameObjectRepository.findAll().collectList().block().size();
        // Configure the mock search repository
        when(mockMyGameObjectSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Create the MyGameObject
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(myGameObject))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the MyGameObject in the database
        List<MyGameObject> myGameObjectList = myGameObjectRepository.findAll().collectList().block();
        assertThat(myGameObjectList).hasSize(databaseSizeBeforeCreate + 1);
        MyGameObject testMyGameObject = myGameObjectList.get(myGameObjectList.size() - 1);

        // Validate the MyGameObject in Elasticsearch
        verify(mockMyGameObjectSearchRepository, times(1)).save(testMyGameObject);
    }

    @Test
    void createMyGameObjectWithExistingId() throws Exception {
        // Create the MyGameObject with an existing ID
        myGameObject.setId(1L);

        int databaseSizeBeforeCreate = myGameObjectRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(myGameObject))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the MyGameObject in the database
        List<MyGameObject> myGameObjectList = myGameObjectRepository.findAll().collectList().block();
        assertThat(myGameObjectList).hasSize(databaseSizeBeforeCreate);

        // Validate the MyGameObject in Elasticsearch
        verify(mockMyGameObjectSearchRepository, times(0)).save(myGameObject);
    }

    @Test
    void getAllMyGameObjectsAsStream() {
        // Initialize the database
        myGameObjectRepository.save(myGameObject).block();

        List<MyGameObject> myGameObjectList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(MyGameObject.class)
            .getResponseBody()
            .filter(myGameObject::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(myGameObjectList).isNotNull();
        assertThat(myGameObjectList).hasSize(1);
        MyGameObject testMyGameObject = myGameObjectList.get(0);
    }

    @Test
    void getAllMyGameObjects() {
        // Initialize the database
        myGameObjectRepository.save(myGameObject).block();

        // Get all the myGameObjectList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(myGameObject.getId().intValue()));
    }

    @Test
    void getMyGameObject() {
        // Initialize the database
        myGameObjectRepository.save(myGameObject).block();

        // Get the myGameObject
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, myGameObject.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(myGameObject.getId().intValue()));
    }

    @Test
    void getNonExistingMyGameObject() {
        // Get the myGameObject
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewMyGameObject() throws Exception {
        // Configure the mock search repository
        when(mockMyGameObjectSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Initialize the database
        myGameObjectRepository.save(myGameObject).block();

        int databaseSizeBeforeUpdate = myGameObjectRepository.findAll().collectList().block().size();

        // Update the myGameObject
        MyGameObject updatedMyGameObject = myGameObjectRepository.findById(myGameObject.getId()).block();

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedMyGameObject.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedMyGameObject))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the MyGameObject in the database
        List<MyGameObject> myGameObjectList = myGameObjectRepository.findAll().collectList().block();
        assertThat(myGameObjectList).hasSize(databaseSizeBeforeUpdate);
        MyGameObject testMyGameObject = myGameObjectList.get(myGameObjectList.size() - 1);

        // Validate the MyGameObject in Elasticsearch
        verify(mockMyGameObjectSearchRepository).save(testMyGameObject);
    }

    @Test
    void putNonExistingMyGameObject() throws Exception {
        int databaseSizeBeforeUpdate = myGameObjectRepository.findAll().collectList().block().size();
        myGameObject.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, myGameObject.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(myGameObject))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the MyGameObject in the database
        List<MyGameObject> myGameObjectList = myGameObjectRepository.findAll().collectList().block();
        assertThat(myGameObjectList).hasSize(databaseSizeBeforeUpdate);

        // Validate the MyGameObject in Elasticsearch
        verify(mockMyGameObjectSearchRepository, times(0)).save(myGameObject);
    }

    @Test
    void putWithIdMismatchMyGameObject() throws Exception {
        int databaseSizeBeforeUpdate = myGameObjectRepository.findAll().collectList().block().size();
        myGameObject.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(myGameObject))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the MyGameObject in the database
        List<MyGameObject> myGameObjectList = myGameObjectRepository.findAll().collectList().block();
        assertThat(myGameObjectList).hasSize(databaseSizeBeforeUpdate);

        // Validate the MyGameObject in Elasticsearch
        verify(mockMyGameObjectSearchRepository, times(0)).save(myGameObject);
    }

    @Test
    void putWithMissingIdPathParamMyGameObject() throws Exception {
        int databaseSizeBeforeUpdate = myGameObjectRepository.findAll().collectList().block().size();
        myGameObject.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(myGameObject))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the MyGameObject in the database
        List<MyGameObject> myGameObjectList = myGameObjectRepository.findAll().collectList().block();
        assertThat(myGameObjectList).hasSize(databaseSizeBeforeUpdate);

        // Validate the MyGameObject in Elasticsearch
        verify(mockMyGameObjectSearchRepository, times(0)).save(myGameObject);
    }

    @Test
    void partialUpdateMyGameObjectWithPatch() throws Exception {
        // Initialize the database
        myGameObjectRepository.save(myGameObject).block();

        int databaseSizeBeforeUpdate = myGameObjectRepository.findAll().collectList().block().size();

        // Update the myGameObject using partial update
        MyGameObject partialUpdatedMyGameObject = new MyGameObject();
        partialUpdatedMyGameObject.setId(myGameObject.getId());

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedMyGameObject.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedMyGameObject))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the MyGameObject in the database
        List<MyGameObject> myGameObjectList = myGameObjectRepository.findAll().collectList().block();
        assertThat(myGameObjectList).hasSize(databaseSizeBeforeUpdate);
        MyGameObject testMyGameObject = myGameObjectList.get(myGameObjectList.size() - 1);
    }

    @Test
    void fullUpdateMyGameObjectWithPatch() throws Exception {
        // Initialize the database
        myGameObjectRepository.save(myGameObject).block();

        int databaseSizeBeforeUpdate = myGameObjectRepository.findAll().collectList().block().size();

        // Update the myGameObject using partial update
        MyGameObject partialUpdatedMyGameObject = new MyGameObject();
        partialUpdatedMyGameObject.setId(myGameObject.getId());

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedMyGameObject.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedMyGameObject))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the MyGameObject in the database
        List<MyGameObject> myGameObjectList = myGameObjectRepository.findAll().collectList().block();
        assertThat(myGameObjectList).hasSize(databaseSizeBeforeUpdate);
        MyGameObject testMyGameObject = myGameObjectList.get(myGameObjectList.size() - 1);
    }

    @Test
    void patchNonExistingMyGameObject() throws Exception {
        int databaseSizeBeforeUpdate = myGameObjectRepository.findAll().collectList().block().size();
        myGameObject.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, myGameObject.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(myGameObject))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the MyGameObject in the database
        List<MyGameObject> myGameObjectList = myGameObjectRepository.findAll().collectList().block();
        assertThat(myGameObjectList).hasSize(databaseSizeBeforeUpdate);

        // Validate the MyGameObject in Elasticsearch
        verify(mockMyGameObjectSearchRepository, times(0)).save(myGameObject);
    }

    @Test
    void patchWithIdMismatchMyGameObject() throws Exception {
        int databaseSizeBeforeUpdate = myGameObjectRepository.findAll().collectList().block().size();
        myGameObject.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(myGameObject))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the MyGameObject in the database
        List<MyGameObject> myGameObjectList = myGameObjectRepository.findAll().collectList().block();
        assertThat(myGameObjectList).hasSize(databaseSizeBeforeUpdate);

        // Validate the MyGameObject in Elasticsearch
        verify(mockMyGameObjectSearchRepository, times(0)).save(myGameObject);
    }

    @Test
    void patchWithMissingIdPathParamMyGameObject() throws Exception {
        int databaseSizeBeforeUpdate = myGameObjectRepository.findAll().collectList().block().size();
        myGameObject.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(myGameObject))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the MyGameObject in the database
        List<MyGameObject> myGameObjectList = myGameObjectRepository.findAll().collectList().block();
        assertThat(myGameObjectList).hasSize(databaseSizeBeforeUpdate);

        // Validate the MyGameObject in Elasticsearch
        verify(mockMyGameObjectSearchRepository, times(0)).save(myGameObject);
    }

    @Test
    void deleteMyGameObject() {
        // Configure the mock search repository
        when(mockMyGameObjectSearchRepository.deleteById(anyLong())).thenReturn(Mono.empty());
        // Initialize the database
        myGameObjectRepository.save(myGameObject).block();

        int databaseSizeBeforeDelete = myGameObjectRepository.findAll().collectList().block().size();

        // Delete the myGameObject
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, myGameObject.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<MyGameObject> myGameObjectList = myGameObjectRepository.findAll().collectList().block();
        assertThat(myGameObjectList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the MyGameObject in Elasticsearch
        verify(mockMyGameObjectSearchRepository, times(1)).deleteById(myGameObject.getId());
    }

    @Test
    void searchMyGameObject() {
        // Configure the mock search repository
        when(mockMyGameObjectSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Initialize the database
        myGameObjectRepository.save(myGameObject).block();
        when(mockMyGameObjectSearchRepository.search("id:" + myGameObject.getId())).thenReturn(Flux.just(myGameObject));

        // Search the myGameObject
        webTestClient
            .get()
            .uri(ENTITY_SEARCH_API_URL + "?query=id:" + myGameObject.getId())
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(myGameObject.getId().intValue()));
    }
}
