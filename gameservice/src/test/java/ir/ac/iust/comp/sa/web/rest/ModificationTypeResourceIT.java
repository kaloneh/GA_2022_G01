package ir.ac.iust.comp.sa.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import ir.ac.iust.comp.sa.IntegrationTest;
import ir.ac.iust.comp.sa.domain.ModificationType;
import ir.ac.iust.comp.sa.domain.enumeration.EnumModType;
import ir.ac.iust.comp.sa.repository.ModificationTypeRepository;
import ir.ac.iust.comp.sa.repository.search.ModificationTypeSearchRepository;
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
 * Integration tests for the {@link ModificationTypeResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient
@WithMockUser
class ModificationTypeResourceIT {

    private static final EnumModType DEFAULT_TYPE = EnumModType.NONE;
    private static final EnumModType UPDATED_TYPE = EnumModType.TRANLATION;

    private static final String ENTITY_API_URL = "/api/modification-types";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/modification-types";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ModificationTypeRepository modificationTypeRepository;

    /**
     * This repository is mocked in the ir.ac.iust.comp.sa.repository.search test package.
     *
     * @see ir.ac.iust.comp.sa.repository.search.ModificationTypeSearchRepositoryMockConfiguration
     */
    @Autowired
    private ModificationTypeSearchRepository mockModificationTypeSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private ModificationType modificationType;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ModificationType createEntity(EntityManager em) {
        ModificationType modificationType = new ModificationType().type(DEFAULT_TYPE);
        return modificationType;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ModificationType createUpdatedEntity(EntityManager em) {
        ModificationType modificationType = new ModificationType().type(UPDATED_TYPE);
        return modificationType;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(ModificationType.class).block();
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
        modificationType = createEntity(em);
    }

    @Test
    void createModificationType() throws Exception {
        int databaseSizeBeforeCreate = modificationTypeRepository.findAll().collectList().block().size();
        // Configure the mock search repository
        when(mockModificationTypeSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Create the ModificationType
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(modificationType))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the ModificationType in the database
        List<ModificationType> modificationTypeList = modificationTypeRepository.findAll().collectList().block();
        assertThat(modificationTypeList).hasSize(databaseSizeBeforeCreate + 1);
        ModificationType testModificationType = modificationTypeList.get(modificationTypeList.size() - 1);
        assertThat(testModificationType.getType()).isEqualTo(DEFAULT_TYPE);

        // Validate the ModificationType in Elasticsearch
        verify(mockModificationTypeSearchRepository, times(1)).save(testModificationType);
    }

    @Test
    void createModificationTypeWithExistingId() throws Exception {
        // Create the ModificationType with an existing ID
        modificationType.setId(1L);

        int databaseSizeBeforeCreate = modificationTypeRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(modificationType))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ModificationType in the database
        List<ModificationType> modificationTypeList = modificationTypeRepository.findAll().collectList().block();
        assertThat(modificationTypeList).hasSize(databaseSizeBeforeCreate);

        // Validate the ModificationType in Elasticsearch
        verify(mockModificationTypeSearchRepository, times(0)).save(modificationType);
    }

    @Test
    void getAllModificationTypesAsStream() {
        // Initialize the database
        modificationTypeRepository.save(modificationType).block();

        List<ModificationType> modificationTypeList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(ModificationType.class)
            .getResponseBody()
            .filter(modificationType::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(modificationTypeList).isNotNull();
        assertThat(modificationTypeList).hasSize(1);
        ModificationType testModificationType = modificationTypeList.get(0);
        assertThat(testModificationType.getType()).isEqualTo(DEFAULT_TYPE);
    }

    @Test
    void getAllModificationTypes() {
        // Initialize the database
        modificationTypeRepository.save(modificationType).block();

        // Get all the modificationTypeList
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
            .value(hasItem(modificationType.getId().intValue()))
            .jsonPath("$.[*].type")
            .value(hasItem(DEFAULT_TYPE.toString()));
    }

    @Test
    void getModificationType() {
        // Initialize the database
        modificationTypeRepository.save(modificationType).block();

        // Get the modificationType
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, modificationType.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(modificationType.getId().intValue()))
            .jsonPath("$.type")
            .value(is(DEFAULT_TYPE.toString()));
    }

    @Test
    void getNonExistingModificationType() {
        // Get the modificationType
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewModificationType() throws Exception {
        // Configure the mock search repository
        when(mockModificationTypeSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Initialize the database
        modificationTypeRepository.save(modificationType).block();

        int databaseSizeBeforeUpdate = modificationTypeRepository.findAll().collectList().block().size();

        // Update the modificationType
        ModificationType updatedModificationType = modificationTypeRepository.findById(modificationType.getId()).block();
        updatedModificationType.type(UPDATED_TYPE);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedModificationType.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedModificationType))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the ModificationType in the database
        List<ModificationType> modificationTypeList = modificationTypeRepository.findAll().collectList().block();
        assertThat(modificationTypeList).hasSize(databaseSizeBeforeUpdate);
        ModificationType testModificationType = modificationTypeList.get(modificationTypeList.size() - 1);
        assertThat(testModificationType.getType()).isEqualTo(UPDATED_TYPE);

        // Validate the ModificationType in Elasticsearch
        verify(mockModificationTypeSearchRepository).save(testModificationType);
    }

    @Test
    void putNonExistingModificationType() throws Exception {
        int databaseSizeBeforeUpdate = modificationTypeRepository.findAll().collectList().block().size();
        modificationType.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, modificationType.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(modificationType))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ModificationType in the database
        List<ModificationType> modificationTypeList = modificationTypeRepository.findAll().collectList().block();
        assertThat(modificationTypeList).hasSize(databaseSizeBeforeUpdate);

        // Validate the ModificationType in Elasticsearch
        verify(mockModificationTypeSearchRepository, times(0)).save(modificationType);
    }

    @Test
    void putWithIdMismatchModificationType() throws Exception {
        int databaseSizeBeforeUpdate = modificationTypeRepository.findAll().collectList().block().size();
        modificationType.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(modificationType))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ModificationType in the database
        List<ModificationType> modificationTypeList = modificationTypeRepository.findAll().collectList().block();
        assertThat(modificationTypeList).hasSize(databaseSizeBeforeUpdate);

        // Validate the ModificationType in Elasticsearch
        verify(mockModificationTypeSearchRepository, times(0)).save(modificationType);
    }

    @Test
    void putWithMissingIdPathParamModificationType() throws Exception {
        int databaseSizeBeforeUpdate = modificationTypeRepository.findAll().collectList().block().size();
        modificationType.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(modificationType))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the ModificationType in the database
        List<ModificationType> modificationTypeList = modificationTypeRepository.findAll().collectList().block();
        assertThat(modificationTypeList).hasSize(databaseSizeBeforeUpdate);

        // Validate the ModificationType in Elasticsearch
        verify(mockModificationTypeSearchRepository, times(0)).save(modificationType);
    }

    @Test
    void partialUpdateModificationTypeWithPatch() throws Exception {
        // Initialize the database
        modificationTypeRepository.save(modificationType).block();

        int databaseSizeBeforeUpdate = modificationTypeRepository.findAll().collectList().block().size();

        // Update the modificationType using partial update
        ModificationType partialUpdatedModificationType = new ModificationType();
        partialUpdatedModificationType.setId(modificationType.getId());

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedModificationType.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedModificationType))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the ModificationType in the database
        List<ModificationType> modificationTypeList = modificationTypeRepository.findAll().collectList().block();
        assertThat(modificationTypeList).hasSize(databaseSizeBeforeUpdate);
        ModificationType testModificationType = modificationTypeList.get(modificationTypeList.size() - 1);
        assertThat(testModificationType.getType()).isEqualTo(DEFAULT_TYPE);
    }

    @Test
    void fullUpdateModificationTypeWithPatch() throws Exception {
        // Initialize the database
        modificationTypeRepository.save(modificationType).block();

        int databaseSizeBeforeUpdate = modificationTypeRepository.findAll().collectList().block().size();

        // Update the modificationType using partial update
        ModificationType partialUpdatedModificationType = new ModificationType();
        partialUpdatedModificationType.setId(modificationType.getId());

        partialUpdatedModificationType.type(UPDATED_TYPE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedModificationType.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedModificationType))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the ModificationType in the database
        List<ModificationType> modificationTypeList = modificationTypeRepository.findAll().collectList().block();
        assertThat(modificationTypeList).hasSize(databaseSizeBeforeUpdate);
        ModificationType testModificationType = modificationTypeList.get(modificationTypeList.size() - 1);
        assertThat(testModificationType.getType()).isEqualTo(UPDATED_TYPE);
    }

    @Test
    void patchNonExistingModificationType() throws Exception {
        int databaseSizeBeforeUpdate = modificationTypeRepository.findAll().collectList().block().size();
        modificationType.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, modificationType.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(modificationType))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ModificationType in the database
        List<ModificationType> modificationTypeList = modificationTypeRepository.findAll().collectList().block();
        assertThat(modificationTypeList).hasSize(databaseSizeBeforeUpdate);

        // Validate the ModificationType in Elasticsearch
        verify(mockModificationTypeSearchRepository, times(0)).save(modificationType);
    }

    @Test
    void patchWithIdMismatchModificationType() throws Exception {
        int databaseSizeBeforeUpdate = modificationTypeRepository.findAll().collectList().block().size();
        modificationType.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(modificationType))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the ModificationType in the database
        List<ModificationType> modificationTypeList = modificationTypeRepository.findAll().collectList().block();
        assertThat(modificationTypeList).hasSize(databaseSizeBeforeUpdate);

        // Validate the ModificationType in Elasticsearch
        verify(mockModificationTypeSearchRepository, times(0)).save(modificationType);
    }

    @Test
    void patchWithMissingIdPathParamModificationType() throws Exception {
        int databaseSizeBeforeUpdate = modificationTypeRepository.findAll().collectList().block().size();
        modificationType.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(modificationType))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the ModificationType in the database
        List<ModificationType> modificationTypeList = modificationTypeRepository.findAll().collectList().block();
        assertThat(modificationTypeList).hasSize(databaseSizeBeforeUpdate);

        // Validate the ModificationType in Elasticsearch
        verify(mockModificationTypeSearchRepository, times(0)).save(modificationType);
    }

    @Test
    void deleteModificationType() {
        // Configure the mock search repository
        when(mockModificationTypeSearchRepository.deleteById(anyLong())).thenReturn(Mono.empty());
        // Initialize the database
        modificationTypeRepository.save(modificationType).block();

        int databaseSizeBeforeDelete = modificationTypeRepository.findAll().collectList().block().size();

        // Delete the modificationType
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, modificationType.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<ModificationType> modificationTypeList = modificationTypeRepository.findAll().collectList().block();
        assertThat(modificationTypeList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the ModificationType in Elasticsearch
        verify(mockModificationTypeSearchRepository, times(1)).deleteById(modificationType.getId());
    }

    @Test
    void searchModificationType() {
        // Configure the mock search repository
        when(mockModificationTypeSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Initialize the database
        modificationTypeRepository.save(modificationType).block();
        when(mockModificationTypeSearchRepository.search("id:" + modificationType.getId())).thenReturn(Flux.just(modificationType));

        // Search the modificationType
        webTestClient
            .get()
            .uri(ENTITY_SEARCH_API_URL + "?query=id:" + modificationType.getId())
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(modificationType.getId().intValue()))
            .jsonPath("$.[*].type")
            .value(hasItem(DEFAULT_TYPE.toString()));
    }
}
