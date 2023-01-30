import React, { useState, useEffect } from 'react';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col, FormText } from 'reactstrap';
import { isNumber, Translate, translate, ValidatedField, ValidatedForm, ValidatedBlobField } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { getEntity, updateEntity, createEntity, reset } from './game-object.reducer';
import { IGameObject } from 'app/shared/model/gameservice/game-object.model';
import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

export const GameObjectUpdate = (props: RouteComponentProps<{ id: string }>) => {
  const dispatch = useAppDispatch();

  const [isNew] = useState(!props.match.params || !props.match.params.id);

  const gameObjectEntity = useAppSelector(state => state.gameObject.entity);
  const loading = useAppSelector(state => state.gameObject.loading);
  const updating = useAppSelector(state => state.gameObject.updating);
  const updateSuccess = useAppSelector(state => state.gameObject.updateSuccess);

  const handleClose = () => {
    props.history.push('/game-object');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(props.match.params.id));
    }
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    const entity = {
      ...gameObjectEntity,
      ...values,
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {}
      : {
          ...gameObjectEntity,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="iustGatewayApp.gameserviceGameObject.home.createOrEditLabel" data-cy="GameObjectCreateUpdateHeading">
            <Translate contentKey="iustGatewayApp.gameserviceGameObject.home.createOrEditLabel">Create or edit a GameObject</Translate>
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew ? (
                <ValidatedField
                  name="id"
                  required
                  readOnly
                  id="game-object-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('iustGatewayApp.gameserviceGameObject.x')}
                id="game-object-x"
                name="x"
                data-cy="x"
                type="text"
              />
              <ValidatedField
                label={translate('iustGatewayApp.gameserviceGameObject.y')}
                id="game-object-y"
                name="y"
                data-cy="y"
                type="text"
              />
              <ValidatedBlobField
                label={translate('iustGatewayApp.gameserviceGameObject.bitmap')}
                id="game-object-bitmap"
                name="bitmap"
                data-cy="bitmap"
                openActionLabel={translate('entity.action.open')}
              />
              <ValidatedField
                label={translate('iustGatewayApp.gameserviceGameObject.isEnabled')}
                id="game-object-isEnabled"
                name="isEnabled"
                data-cy="isEnabled"
                check
                type="checkbox"
              />
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/game-object" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">
                  <Translate contentKey="entity.action.back">Back</Translate>
                </span>
              </Button>
              &nbsp;
              <Button color="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp;
                <Translate contentKey="entity.action.save">Save</Translate>
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default GameObjectUpdate;
