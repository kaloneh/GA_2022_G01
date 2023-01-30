package ir.ac.iust.comp.sa.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import ir.ac.iust.comp.sa.IntegrationTest;
import ir.ac.iust.comp.sa.domain.MyApplication;
import ir.ac.iust.comp.sa.repository.MyApplicationRepository;
import ir.ac.iust.comp.sa.repository.search.MyApplicationSearchRepository;
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
 * Integration tests for the {@link MyApplicationResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient
@WithMockUser
class MyApplicationResourceIT {

    private static final String ENTITY_API_URL = "/api/my-applications";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/my-applications";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private MyApplicationRepository myApplicationRepository;

    /**
     * This repository is mocked in the ir.ac.iust.comp.sa.repository.search test package.
     *
     * @see ir.ac.iust.comp.sa.repository.search.MyApplicationSearchRepositoryMockConfiguration
     */
    @Autowired
    private MyApplicationSearchRepository mockMyApplicationSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private MyApplication myApplication;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static MyApplication createEntity(EntityManager em) {
        MyApplication myApplication = new MyApplication();
        return myApplication;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static MyApplication createUpdatedEntity(EntityManager em) {
        MyApplication myApplication = new MyApplication();
        return myApplication;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(MyApplication.class).block();
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
        myApplication = createEntity(em);
    }

    @Test
    void createMyApplication() throws Exception {
        int databaseSizeBeforeCreate = myApplicationRepository.findAll().collectList().block().size();
        // Configure the mock search repository
        when(mockMyApplicationSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Create the MyApplication
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(myApplication))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the MyApplication in the database
        List<MyApplication> myApplicationList = myApplicationRepository.findAll().collectList().block();
        assertThat(myApplicationList).hasSize(databaseSizeBeforeCreate + 1);
        MyApplication testMyApplication = myApplicationList.get(myApplicationList.size() - 1);

        // Validate the MyApplication in Elasticsearch
        verify(mockMyApplicationSearchRepository, times(1)).save(testMyApplication);
    }

    @Test
    void createMyApplicationWithExistingId() throws Exception {
        // Create the MyApplication with an existing ID
        myApplication.setId(1L);

        int databaseSizeBeforeCreate = myApplicationRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(myApplication))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the MyApplication in the database
        List<MyApplication> myApplicationList = myApplicationRepository.findAll().collectList().block();
        assertThat(myApplicationList).hasSize(databaseSizeBeforeCreate);

        // Validate the MyApplication in Elasticsearch
        verify(mockMyApplicationSearchRepository, times(0)).save(myApplication);
    }

    @Test
    void getAllMyApplicationsAsStream() {
        // Initialize the database
        myApplicationRepository.save(myApplication).block();

        List<MyApplication> myApplicationList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(MyApplication.class)
            .getResponseBody()
            .filter(myApplication::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(myApplicationList).isNotNull();
        assertThat(myApplicationList).hasSize(1);
        MyApplication testMyApplication = myApplicationList.get(0);
    }

    @Test
    void getAllMyApplications() {
        // Initialize the database
        myApplicationRepository.save(myApplication).block();

        // Get all the myApplicationList
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
            .value(hasItem(myApplication.getId().intValue()));
    }

    @Test
    void getMyApplication() {
        // Initialize the database
        myApplicationRepository.save(myApplication).block();

        // Get the myApplication
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, myApplication.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(myApplication.getId().intValue()));
    }

    @Test
    void getNonExistingMyApplication() {
        // Get the myApplication
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewMyApplication() throws Exception {
        // Configure the mock search repository
        when(mockMyApplicationSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Initialize the database
        myApplicationRepository.save(myApplication).block();

        int databaseSizeBeforeUpdate = myApplicationRepository.findAll().collectList().block().size();

        // Update the myApplication
        MyApplication updatedMyApplication = myApplicationRepository.findById(myApplication.getId()).block();

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedMyApplication.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedMyApplication))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the MyApplication in the database
        List<MyApplication> myApplicationList = myApplicationRepository.findAll().collectList().block();
        assertThat(myApplicationList).hasSize(databaseSizeBeforeUpdate);
        MyApplication testMyApplication = myApplicationList.get(myApplicationList.size() - 1);

        // Validate the MyApplication in Elasticsearch
        verify(mockMyApplicationSearchRepository).save(testMyApplication);
    }

    @Test
    void putNonExistingMyApplication() throws Exception {
        int databaseSizeBeforeUpdate = myApplicationRepository.findAll().collectList().block().size();
        myApplication.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, myApplication.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(myApplication))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the MyApplication in the database
        List<MyApplication> myApplicationList = myApplicationRepository.findAll().collectList().block();
        assertThat(myApplicationList).hasSize(databaseSizeBeforeUpdate);

        // Validate the MyApplication in Elasticsearch
        verify(mockMyApplicationSearchRepository, times(0)).save(myApplication);
    }

    @Test
    void putWithIdMismatchMyApplication() throws Exception {
        int databaseSizeBeforeUpdate = myApplicationRepository.findAll().collectList().block().size();
        myApplication.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(myApplication))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the MyApplication in the database
        List<MyApplication> myApplicationList = myApplicationRepository.findAll().collectList().block();
        assertThat(myApplicationList).hasSize(databaseSizeBeforeUpdate);

        // Validate the MyApplication in Elasticsearch
        verify(mockMyApplicationSearchRepository, times(0)).save(myApplication);
    }

    @Test
    void putWithMissingIdPathParamMyApplication() throws Exception {
        int databaseSizeBeforeUpdate = myApplicationRepository.findAll().collectList().block().size();
        myApplication.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(myApplication))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the MyApplication in the database
        List<MyApplication> myApplicationList = myApplicationRepository.findAll().collectList().block();
        assertThat(myApplicationList).hasSize(databaseSizeBeforeUpdate);

        // Validate the MyApplication in Elasticsearch
        verify(mockMyApplicationSearchRepository, times(0)).save(myApplication);
    }

    @Test
    void partialUpdateMyApplicationWithPatch() throws Exception {
        // Initialize the database
        myApplicationRepository.save(myApplication).block();

        int databaseSizeBeforeUpdate = myApplicationRepository.findAll().collectList().block().size();

        // Update the myApplication using partial update
        MyApplication partialUpdatedMyApplication = new MyApplication();
        partialUpdatedMyApplication.setId(myApplication.getId());

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedMyApplication.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedMyApplication))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the MyApplication in the database
        List<MyApplication> myApplicationList = myApplicationRepository.findAll().collectList().block();
        assertThat(myApplicationList).hasSize(databaseSizeBeforeUpdate);
        MyApplication testMyApplication = myApplicationList.get(myApplicationList.size() - 1);
    }

    @Test
    void fullUpdateMyApplicationWithPatch() throws Exception {
        // Initialize the database
        myApplicationRepository.save(myApplication).block();

        int databaseSizeBeforeUpdate = myApplicationRepository.findAll().collectList().block().size();

        // Update the myApplication using partial update
        MyApplication partialUpdatedMyApplication = new MyApplication();
        partialUpdatedMyApplication.setId(myApplication.getId());

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedMyApplication.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedMyApplication))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the MyApplication in the database
        List<MyApplication> myApplicationList = myApplicationRepository.findAll().collectList().block();
        assertThat(myApplicationList).hasSize(databaseSizeBeforeUpdate);
        MyApplication testMyApplication = myApplicationList.get(myApplicationList.size() - 1);
    }

    @Test
    void patchNonExistingMyApplication() throws Exception {
        int databaseSizeBeforeUpdate = myApplicationRepository.findAll().collectList().block().size();
        myApplication.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, myApplication.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(myApplication))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the MyApplication in the database
        List<MyApplication> myApplicationList = myApplicationRepository.findAll().collectList().block();
        assertThat(myApplicationList).hasSize(databaseSizeBeforeUpdate);

        // Validate the MyApplication in Elasticsearch
        verify(mockMyApplicationSearchRepository, times(0)).save(myApplication);
    }

    @Test
    void patchWithIdMismatchMyApplication() throws Exception {
        int databaseSizeBeforeUpdate = myApplicationRepository.findAll().collectList().block().size();
        myApplication.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(myApplication))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the MyApplication in the database
        List<MyApplication> myApplicationList = myApplicationRepository.findAll().collectList().block();
        assertThat(myApplicationList).hasSize(databaseSizeBeforeUpdate);

        // Validate the MyApplication in Elasticsearch
        verify(mockMyApplicationSearchRepository, times(0)).save(myApplication);
    }

    @Test
    void patchWithMissingIdPathParamMyApplication() throws Exception {
        int databaseSizeBeforeUpdate = myApplicationRepository.findAll().collectList().block().size();
        myApplication.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(myApplication))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the MyApplication in the database
        List<MyApplication> myApplicationList = myApplicationRepository.findAll().collectList().block();
        assertThat(myApplicationList).hasSize(databaseSizeBeforeUpdate);

        // Validate the MyApplication in Elasticsearch
        verify(mockMyApplicationSearchRepository, times(0)).save(myApplication);
    }

    @Test
    void deleteMyApplication() {
        // Configure the mock search repository
        when(mockMyApplicationSearchRepository.deleteById(anyLong())).thenReturn(Mono.empty());
        // Initialize the database
        myApplicationRepository.save(myApplication).block();

        int databaseSizeBeforeDelete = myApplicationRepository.findAll().collectList().block().size();

        // Delete the myApplication
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, myApplication.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<MyApplication> myApplicationList = myApplicationRepository.findAll().collectList().block();
        assertThat(myApplicationList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the MyApplication in Elasticsearch
        verify(mockMyApplicationSearchRepository, times(1)).deleteById(myApplication.getId());
    }

    @Test
    void searchMyApplication() {
        // Configure the mock search repository
        when(mockMyApplicationSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Initialize the database
        myApplicationRepository.save(myApplication).block();
        when(mockMyApplicationSearchRepository.search("id:" + myApplication.getId())).thenReturn(Flux.just(myApplication));

        // Search the myApplication
        webTestClient
            .get()
            .uri(ENTITY_SEARCH_API_URL + "?query=id:" + myApplication.getId())
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(myApplication.getId().intValue()));
    }
}
