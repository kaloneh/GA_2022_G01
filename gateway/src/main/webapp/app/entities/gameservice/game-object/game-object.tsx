import React, { useState, useEffect } from 'react';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Input, InputGroup, FormGroup, Form, Col, Row, Table } from 'reactstrap';
import { openFile, byteSize, Translate, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { searchEntities, getEntities } from './game-object.reducer';
import { IGameObject } from 'app/shared/model/gameservice/game-object.model';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

export const GameObject = (props: RouteComponentProps<{ url: string }>) => {
  const dispatch = useAppDispatch();

  const [search, setSearch] = useState('');

  const gameObjectList = useAppSelector(state => state.gameObject.entities);
  const loading = useAppSelector(state => state.gameObject.loading);

  useEffect(() => {
    dispatch(getEntities({}));
  }, []);

  const startSearching = e => {
    if (search) {
      dispatch(searchEntities({ query: search }));
    }
    e.preventDefault();
  };

  const clear = () => {
    setSearch('');
    dispatch(getEntities({}));
  };

  const handleSearch = event => setSearch(event.target.value);

  const handleSyncList = () => {
    dispatch(getEntities({}));
  };

  const { match } = props;

  return (
    <div>
      <h2 id="game-object-heading" data-cy="GameObjectHeading">
        <Translate contentKey="iustGatewayApp.gameserviceGameObject.home.title">Game Objects</Translate>
        <div className="d-flex justify-content-end">
          <Button className="mr-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} />{' '}
            <Translate contentKey="iustGatewayApp.gameserviceGameObject.home.refreshListLabel">Refresh List</Translate>
          </Button>
          <Link to={`${match.url}/new`} className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp;
            <Translate contentKey="iustGatewayApp.gameserviceGameObject.home.createLabel">Create new Game Object</Translate>
          </Link>
        </div>
      </h2>
      <Row>
        <Col sm="12">
          <Form onSubmit={startSearching}>
            <FormGroup>
              <InputGroup>
                <Input
                  type="text"
                  name="search"
                  defaultValue={search}
                  onChange={handleSearch}
                  placeholder={translate('iustGatewayApp.gameserviceGameObject.home.search')}
                />
                <Button className="input-group-addon">
                  <FontAwesomeIcon icon="search" />
                </Button>
                <Button type="reset" className="input-group-addon" onClick={clear}>
                  <FontAwesomeIcon icon="trash" />
                </Button>
              </InputGroup>
            </FormGroup>
          </Form>
        </Col>
      </Row>
      <div className="table-responsive">
        {gameObjectList && gameObjectList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th>
                  <Translate contentKey="iustGatewayApp.gameserviceGameObject.id">ID</Translate>
                </th>
                <th>
                  <Translate contentKey="iustGatewayApp.gameserviceGameObject.x">X</Translate>
                </th>
                <th>
                  <Translate contentKey="iustGatewayApp.gameserviceGameObject.y">Y</Translate>
                </th>
                <th>
                  <Translate contentKey="iustGatewayApp.gameserviceGameObject.bitmap">Bitmap</Translate>
                </th>
                <th>
                  <Translate contentKey="iustGatewayApp.gameserviceGameObject.isEnabled">Is Enabled</Translate>
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {gameObjectList.map((gameObject, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`${match.url}/${gameObject.id}`} color="link" size="sm">
                      {gameObject.id}
                    </Button>
                  </td>
                  <td>{gameObject.x}</td>
                  <td>{gameObject.y}</td>
                  <td>
                    {gameObject.bitmap ? (
                      <div>
                        {gameObject.bitmapContentType ? (
                          <a onClick={openFile(gameObject.bitmapContentType, gameObject.bitmap)}>
                            <Translate contentKey="entity.action.open">Open</Translate>
                            &nbsp;
                          </a>
                        ) : null}
                        <span>
                          {gameObject.bitmapContentType}, {byteSize(gameObject.bitmap)}
                        </span>
                      </div>
                    ) : null}
                  </td>
                  <td>{gameObject.isEnabled ? 'true' : 'false'}</td>
                  <td className="text-right">
                    <div className="btn-group flex-btn-group-container">
                      <Button tag={Link} to={`${match.url}/${gameObject.id}`} color="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.view">View</Translate>
                        </span>
                      </Button>
                      <Button tag={Link} to={`${match.url}/${gameObject.id}/edit`} color="primary" size="sm" data-cy="entityEditButton">
                        <FontAwesomeIcon icon="pencil-alt" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.edit">Edit</Translate>
                        </span>
                      </Button>
                      <Button tag={Link} to={`${match.url}/${gameObject.id}/delete`} color="danger" size="sm" data-cy="entityDeleteButton">
                        <FontAwesomeIcon icon="trash" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.delete">Delete</Translate>
                        </span>
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && (
            <div className="alert alert-warning">
              <Translate contentKey="iustGatewayApp.gameserviceGameObject.home.notFound">No Game Objects found</Translate>
            </div>
          )
        )}
      </div>
    </div>
  );
};

export default GameObject;
