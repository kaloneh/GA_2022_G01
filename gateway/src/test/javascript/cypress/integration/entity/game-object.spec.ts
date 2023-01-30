import { entityItemSelector } from '../../support/commands';
import {
  entityTableSelector,
  entityDetailsButtonSelector,
  entityDetailsBackButtonSelector,
  entityCreateButtonSelector,
  entityCreateSaveButtonSelector,
  entityCreateCancelButtonSelector,
  entityEditButtonSelector,
  entityDeleteButtonSelector,
  entityConfirmDeleteButtonSelector,
} from '../../support/entity';

describe('GameObject e2e test', () => {
  const gameObjectPageUrl = '/game-object';
  const gameObjectPageUrlPattern = new RegExp('/game-object(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'admin';
  const password = Cypress.env('E2E_PASSWORD') ?? 'admin';

  before(() => {
    cy.window().then(win => {
      win.sessionStorage.clear();
    });
    cy.visit('');
    cy.login(username, password);
    cy.get(entityItemSelector).should('exist');
  });

  beforeEach(() => {
    cy.intercept('GET', '/services/gameservice/api/game-objects+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/services/gameservice/api/game-objects').as('postEntityRequest');
    cy.intercept('DELETE', '/services/gameservice/api/game-objects/*').as('deleteEntityRequest');
  });

  it('should load GameObjects', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('game-object');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('GameObject').should('exist');
    cy.url().should('match', gameObjectPageUrlPattern);
  });

  it('should load details GameObject page', function () {
    cy.visit(gameObjectPageUrl);
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response.body.length === 0) {
        this.skip();
      }
    });
    cy.get(entityDetailsButtonSelector).first().click({ force: true });
    cy.getEntityDetailsHeading('gameObject');
    cy.get(entityDetailsBackButtonSelector).click({ force: true });
    cy.wait('@entitiesRequest').then(({ response }) => {
      expect(response.statusCode).to.equal(200);
    });
    cy.url().should('match', gameObjectPageUrlPattern);
  });

  it('should load create GameObject page', () => {
    cy.visit(gameObjectPageUrl);
    cy.wait('@entitiesRequest');
    cy.get(entityCreateButtonSelector).click({ force: true });
    cy.getEntityCreateUpdateHeading('GameObject');
    cy.get(entityCreateSaveButtonSelector).should('exist');
    cy.get(entityCreateCancelButtonSelector).click({ force: true });
    cy.wait('@entitiesRequest').then(({ response }) => {
      expect(response.statusCode).to.equal(200);
    });
    cy.url().should('match', gameObjectPageUrlPattern);
  });

  it('should load edit GameObject page', function () {
    cy.visit(gameObjectPageUrl);
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response.body.length === 0) {
        this.skip();
      }
    });
    cy.get(entityEditButtonSelector).first().click({ force: true });
    cy.getEntityCreateUpdateHeading('GameObject');
    cy.get(entityCreateSaveButtonSelector).should('exist');
    cy.get(entityCreateCancelButtonSelector).click({ force: true });
    cy.wait('@entitiesRequest').then(({ response }) => {
      expect(response.statusCode).to.equal(200);
    });
    cy.url().should('match', gameObjectPageUrlPattern);
  });

  it('should create an instance of GameObject', () => {
    cy.visit(gameObjectPageUrl);
    cy.get(entityCreateButtonSelector).click({ force: true });
    cy.getEntityCreateUpdateHeading('GameObject');

    cy.get(`[data-cy="x"]`).type('96424').should('have.value', '96424');

    cy.get(`[data-cy="y"]`).type('71823').should('have.value', '71823');

    cy.setFieldImageAsBytesOfEntity('bitmap', 'integration-test.png', 'image/png');

    cy.get(`[data-cy="isEnabled"]`).should('not.be.checked');
    cy.get(`[data-cy="isEnabled"]`).click().should('be.checked');

    // since cypress clicks submit too fast before the blob fields are validated
    cy.wait(200); // eslint-disable-line cypress/no-unnecessary-waiting
    cy.get(entityCreateSaveButtonSelector).click({ force: true });
    cy.scrollTo('top', { ensureScrollable: false });
    cy.get(entityCreateSaveButtonSelector).should('not.exist');
    cy.wait('@postEntityRequest').then(({ response }) => {
      expect(response.statusCode).to.equal(201);
    });
    cy.wait('@entitiesRequest').then(({ response }) => {
      expect(response.statusCode).to.equal(200);
    });
    cy.url().should('match', gameObjectPageUrlPattern);
  });

  it('should delete last instance of GameObject', function () {
    cy.intercept('GET', '/services/gameservice/api/game-objects/*').as('dialogDeleteRequest');
    cy.visit(gameObjectPageUrl);
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response.body.length > 0) {
        cy.get(entityTableSelector).should('have.lengthOf', response.body.length);
        cy.get(entityDeleteButtonSelector).last().click({ force: true });
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('gameObject').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click({ force: true });
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response.statusCode).to.equal(200);
        });
        cy.url().should('match', gameObjectPageUrlPattern);
      } else {
        this.skip();
      }
    });
  });
});
