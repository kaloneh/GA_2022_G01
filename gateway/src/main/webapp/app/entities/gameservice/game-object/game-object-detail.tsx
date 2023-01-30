import React, { useEffect } from 'react';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate, openFile, byteSize } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { getEntity } from './game-object.reducer';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

export const GameObjectDetail = (props: RouteComponentProps<{ id: string }>) => {
  const dispatch = useAppDispatch();

  useEffect(() => {
    dispatch(getEntity(props.match.params.id));
  }, []);

  const gameObjectEntity = useAppSelector(state => state.gameObject.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="gameObjectDetailsHeading">
          <Translate contentKey="iustGatewayApp.gameserviceGameObject.detail.title">GameObject</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{gameObjectEntity.id}</dd>
          <dt>
            <span id="x">
              <Translate contentKey="iustGatewayApp.gameserviceGameObject.x">X</Translate>
            </span>
          </dt>
          <dd>{gameObjectEntity.x}</dd>
          <dt>
            <span id="y">
              <Translate contentKey="iustGatewayApp.gameserviceGameObject.y">Y</Translate>
            </span>
          </dt>
          <dd>{gameObjectEntity.y}</dd>
          <dt>
            <span id="bitmap">
              <Translate contentKey="iustGatewayApp.gameserviceGameObject.bitmap">Bitmap</Translate>
            </span>
          </dt>
          <dd>
            {gameObjectEntity.bitmap ? (
              <div>
                {gameObjectEntity.bitmapContentType ? (
                  <a onClick={openFile(gameObjectEntity.bitmapContentType, gameObjectEntity.bitmap)}>
                    <Translate contentKey="entity.action.open">Open</Translate>&nbsp;
                  </a>
                ) : null}
                <span>
                  {gameObjectEntity.bitmapContentType}, {byteSize(gameObjectEntity.bitmap)}
                </span>
              </div>
            ) : null}
          </dd>
          <dt>
            <span id="isEnabled">
              <Translate contentKey="iustGatewayApp.gameserviceGameObject.isEnabled">Is Enabled</Translate>
            </span>
          </dt>
          <dd>{gameObjectEntity.isEnabled ? 'true' : 'false'}</dd>
        </dl>
        <Button tag={Link} to="/game-object" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/game-object/${gameObjectEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default GameObjectDetail;
