package ir.ac.iust.comp.sa.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import ir.ac.iust.comp.sa.IntegrationTest;
import ir.ac.iust.comp.sa.domain.Application;
import ir.ac.iust.comp.sa.repository.ApplicationRepository;
import ir.ac.iust.comp.sa.repository.search.ApplicationSearchRepository;
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
import org.springframework.util.Base64Utils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Integration tests for the {@link ApplicationResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient
@WithMockUser
class ApplicationResourceIT {

    private static final Float DEFAULT_WIDTH = 1F;
    private static final Float UPDATED_WIDTH = 2F;

    private static final Float DEFAULT_HEIGHT = 1F;
    private static final Float UPDATED_HEIGHT = 2F;

    private static final byte[] DEFAULT_SCREEN_BUFFER = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_SCREEN_BUFFER = TestUtil.createByteArray(1, "1");
    private static final String DEFAULT_SCREEN_BUFFER_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_SCREEN_BUFFER_CONTENT_TYPE = "image/png";

    private static final String ENTITY_API_URL = "/api/applications";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/applications";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ApplicationRepository applicationRepository;

    /**
     * This repository is mocked in the ir.ac.iust.comp.sa.repository.search test package.
     *
     * @see ir.ac.iust.comp.sa.repository.search.ApplicationSearchRepositoryMockConfiguration
     */
    @Autowired
    private ApplicationSearchRepository mockApplicationSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Application application;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Application createEntity(EntityManager em) {
        Application application = new Application()
            .width(DEFAULT_WIDTH)
            .height(DEFAULT_HEIGHT)
            .screenBuffer(DEFAULT_SCREEN_BUFFER)
            .screenBufferContentType(DEFAULT_SCREEN_BUFFER_CONTENT_TYPE);
        return application;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Application createUpdatedEntity(EntityManager em) {
        Application application = new Application()
            .width(UPDATED_WIDTH)
            .height(UPDATED_HEIGHT)
            .screenBuffer(UPDATED_SCREEN_BUFFER)
            .screenBufferContentType(UPDATED_SCREEN_BUFFER_CONTENT_TYPE);
        return application;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Application.class).block();
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
        application = createEntity(em);
    }

    @Test
    void createApplication() throws Exception {
        int databaseSizeBeforeCreate = applicationRepository.findAll().collectList().block().size();
        // Configure the mock search repository
        when(mockApplicationSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Create the Application
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(application))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the Application in the database
        List<Application> applicationList = applicationRepository.findAll().collectList().block();
        assertThat(applicationList).hasSize(databaseSizeBeforeCreate + 1);
        Application testApplication = applicationList.get(applicationList.size() - 1);
        assertThat(testApplication.getWidth()).isEqualTo(DEFAULT_WIDTH);
        assertThat(testApplication.getHeight()).isEqualTo(DEFAULT_HEIGHT);
        assertThat(testApplication.getScreenBuffer()).isEqualTo(DEFAULT_SCREEN_BUFFER);
        assertThat(testApplication.getScreenBufferContentType()).isEqualTo(DEFAULT_SCREEN_BUFFER_CONTENT_TYPE);

        // Validate the Application in Elasticsearch
        verify(mockApplicationSearchRepository, times(1)).save(testApplication);
    }

    @Test
    void createApplicationWithExistingId() throws Exception {
        // Create the Application with an existing ID
        application.setId(1L);

        int databaseSizeBeforeCreate = applicationRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(application))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Application in the database
        List<Application> applicationList = applicationRepository.findAll().collectList().block();
        assertThat(applicationList).hasSize(databaseSizeBeforeCreate);

        // Validate the Application in Elasticsearch
        verify(mockApplicationSearchRepository, times(0)).save(application);
    }

    @Test
    void getAllApplicationsAsStream() {
        // Initialize the database
        applicationRepository.save(application).block();

        List<Application> applicationList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(Application.class)
            .getResponseBody()
            .filter(application::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(applicationList).isNotNull();
        assertThat(applicationList).hasSize(1);
        Application testApplication = applicationList.get(0);
        assertThat(testApplication.getWidth()).isEqualTo(DEFAULT_WIDTH);
        assertThat(testApplication.getHeight()).isEqualTo(DEFAULT_HEIGHT);
        assertThat(testApplication.getScreenBuffer()).isEqualTo(DEFAULT_SCREEN_BUFFER);
        assertThat(testApplication.getScreenBufferContentType()).isEqualTo(DEFAULT_SCREEN_BUFFER_CONTENT_TYPE);
    }

    @Test
    void getAllApplications() {
        // Initialize the database
        applicationRepository.save(application).block();

        // Get all the applicationList
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
            .value(hasItem(application.getId().intValue()))
            .jsonPath("$.[*].width")
            .value(hasItem(DEFAULT_WIDTH.doubleValue()))
            .jsonPath("$.[*].height")
            .value(hasItem(DEFAULT_HEIGHT.doubleValue()))
            .jsonPath("$.[*].screenBufferContentType")
            .value(hasItem(DEFAULT_SCREEN_BUFFER_CONTENT_TYPE))
            .jsonPath("$.[*].screenBuffer")
            .value(hasItem(Base64Utils.encodeToString(DEFAULT_SCREEN_BUFFER)));
    }

    @Test
    void getApplication() {
        // Initialize the database
        applicationRepository.save(application).block();

        // Get the application
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, application.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(application.getId().intValue()))
            .jsonPath("$.width")
            .value(is(DEFAULT_WIDTH.doubleValue()))
            .jsonPath("$.height")
            .value(is(DEFAULT_HEIGHT.doubleValue()))
            .jsonPath("$.screenBufferContentType")
            .value(is(DEFAULT_SCREEN_BUFFER_CONTENT_TYPE))
            .jsonPath("$.screenBuffer")
            .value(is(Base64Utils.encodeToString(DEFAULT_SCREEN_BUFFER)));
    }

    @Test
    void getNonExistingApplication() {
        // Get the application
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewApplication() throws Exception {
        // Configure the mock search repository
        when(mockApplicationSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Initialize the database
        applicationRepository.save(application).block();

        int databaseSizeBeforeUpdate = applicationRepository.findAll().collectList().block().size();

        // Update the application
        Application updatedApplication = applicationRepository.findById(application.getId()).block();
        updatedApplication
            .width(UPDATED_WIDTH)
            .height(UPDATED_HEIGHT)
            .screenBuffer(UPDATED_SCREEN_BUFFER)
            .screenBufferContentType(UPDATED_SCREEN_BUFFER_CONTENT_TYPE);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedApplication.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedApplication))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Application in the database
        List<Application> applicationList = applicationRepository.findAll().collectList().block();
        assertThat(applicationList).hasSize(databaseSizeBeforeUpdate);
        Application testApplication = applicationList.get(applicationList.size() - 1);
        assertThat(testApplication.getWidth()).isEqualTo(UPDATED_WIDTH);
        assertThat(testApplication.getHeight()).isEqualTo(UPDATED_HEIGHT);
        assertThat(testApplication.getScreenBuffer()).isEqualTo(UPDATED_SCREEN_BUFFER);
        assertThat(testApplication.getScreenBufferContentType()).isEqualTo(UPDATED_SCREEN_BUFFER_CONTENT_TYPE);

        // Validate the Application in Elasticsearch
        verify(mockApplicationSearchRepository).save(testApplication);
    }

    @Test
    void putNonExistingApplication() throws Exception {
        int databaseSizeBeforeUpdate = applicationRepository.findAll().collectList().block().size();
        application.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, application.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(application))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Application in the database
        List<Application> applicationList = applicationRepository.findAll().collectList().block();
        assertThat(applicationList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Application in Elasticsearch
        verify(mockApplicationSearchRepository, times(0)).save(application);
    }

    @Test
    void putWithIdMismatchApplication() throws Exception {
        int databaseSizeBeforeUpdate = applicationRepository.findAll().collectList().block().size();
        application.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(application))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Application in the database
        List<Application> applicationList = applicationRepository.findAll().collectList().block();
        assertThat(applicationList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Application in Elasticsearch
        verify(mockApplicationSearchRepository, times(0)).save(application);
    }

    @Test
    void putWithMissingIdPathParamApplication() throws Exception {
        int databaseSizeBeforeUpdate = applicationRepository.findAll().collectList().block().size();
        application.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(application))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Application in the database
        List<Application> applicationList = applicationRepository.findAll().collectList().block();
        assertThat(applicationList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Application in Elasticsearch
        verify(mockApplicationSearchRepository, times(0)).save(application);
    }

    @Test
    void partialUpdateApplicationWithPatch() throws Exception {
        // Initialize the database
        applicationRepository.save(application).block();

        int databaseSizeBeforeUpdate = applicationRepository.findAll().collectList().block().size();

        // Update the application using partial update
        Application partialUpdatedApplication = new Application();
        partialUpdatedApplication.setId(application.getId());

        partialUpdatedApplication.height(UPDATED_HEIGHT);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedApplication.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedApplication))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Application in the database
        List<Application> applicationList = applicationRepository.findAll().collectList().block();
        assertThat(applicationList).hasSize(databaseSizeBeforeUpdate);
        Application testApplication = applicationList.get(applicationList.size() - 1);
        assertThat(testApplication.getWidth()).isEqualTo(DEFAULT_WIDTH);
        assertThat(testApplication.getHeight()).isEqualTo(UPDATED_HEIGHT);
        assertThat(testApplication.getScreenBuffer()).isEqualTo(DEFAULT_SCREEN_BUFFER);
        assertThat(testApplication.getScreenBufferContentType()).isEqualTo(DEFAULT_SCREEN_BUFFER_CONTENT_TYPE);
    }

    @Test
    void fullUpdateApplicationWithPatch() throws Exception {
        // Initialize the database
        applicationRepository.save(application).block();

        int databaseSizeBeforeUpdate = applicationRepository.findAll().collectList().block().size();

        // Update the application using partial update
        Application partialUpdatedApplication = new Application();
        partialUpdatedApplication.setId(application.getId());

        partialUpdatedApplication
            .width(UPDATED_WIDTH)
            .height(UPDATED_HEIGHT)
            .screenBuffer(UPDATED_SCREEN_BUFFER)
            .screenBufferContentType(UPDATED_SCREEN_BUFFER_CONTENT_TYPE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedApplication.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedApplication))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Application in the database
        List<Application> applicationList = applicationRepository.findAll().collectList().block();
        assertThat(applicationList).hasSize(databaseSizeBeforeUpdate);
        Application testApplication = applicationList.get(applicationList.size() - 1);
        assertThat(testApplication.getWidth()).isEqualTo(UPDATED_WIDTH);
        assertThat(testApplication.getHeight()).isEqualTo(UPDATED_HEIGHT);
        assertThat(testApplication.getScreenBuffer()).isEqualTo(UPDATED_SCREEN_BUFFER);
        assertThat(testApplication.getScreenBufferContentType()).isEqualTo(UPDATED_SCREEN_BUFFER_CONTENT_TYPE);
    }

    @Test
    void patchNonExistingApplication() throws Exception {
        int databaseSizeBeforeUpdate = applicationRepository.findAll().collectList().block().size();
        application.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, application.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(application))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Application in the database
        List<Application> applicationList = applicationRepository.findAll().collectList().block();
        assertThat(applicationList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Application in Elasticsearch
        verify(mockApplicationSearchRepository, times(0)).save(application);
    }

    @Test
    void patchWithIdMismatchApplication() throws Exception {
        int databaseSizeBeforeUpdate = applicationRepository.findAll().collectList().block().size();
        application.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(application))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Application in the database
        List<Application> applicationList = applicationRepository.findAll().collectList().block();
        assertThat(applicationList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Application in Elasticsearch
        verify(mockApplicationSearchRepository, times(0)).save(application);
    }

    @Test
    void patchWithMissingIdPathParamApplication() throws Exception {
        int databaseSizeBeforeUpdate = applicationRepository.findAll().collectList().block().size();
        application.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(application))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Application in the database
        List<Application> applicationList = applicationRepository.findAll().collectList().block();
        assertThat(applicationList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Application in Elasticsearch
        verify(mockApplicationSearchRepository, times(0)).save(application);
    }

    @Test
    void deleteApplication() {
        // Configure the mock search repository
        when(mockApplicationSearchRepository.deleteById(anyLong())).thenReturn(Mono.empty());
        // Initialize the database
        applicationRepository.save(application).block();

        int databaseSizeBeforeDelete = applicationRepository.findAll().collectList().block().size();

        // Delete the application
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, application.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<Application> applicationList = applicationRepository.findAll().collectList().block();
        assertThat(applicationList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Application in Elasticsearch
        verify(mockApplicationSearchRepository, times(1)).deleteById(application.getId());
    }

    @Test
    void searchApplication() {
        // Configure the mock search repository
        when(mockApplicationSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Initialize the database
        applicationRepository.save(application).block();
        when(mockApplicationSearchRepository.search("id:" + application.getId())).thenReturn(Flux.just(application));

        // Search the application
        webTestClient
            .get()
            .uri(ENTITY_SEARCH_API_URL + "?query=id:" + application.getId())
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(application.getId().intValue()))
            .jsonPath("$.[*].width")
            .value(hasItem(DEFAULT_WIDTH.doubleValue()))
            .jsonPath("$.[*].height")
            .value(hasItem(DEFAULT_HEIGHT.doubleValue()))
            .jsonPath("$.[*].screenBufferContentType")
            .value(hasItem(DEFAULT_SCREEN_BUFFER_CONTENT_TYPE))
            .jsonPath("$.[*].screenBuffer")
            .value(hasItem(Base64Utils.encodeToString(DEFAULT_SCREEN_BUFFER)));
    }
}
