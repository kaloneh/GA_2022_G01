import React from 'react';
import { Switch } from 'react-router-dom';

import ErrorBoundaryRoute from 'app/shared/error/error-boundary-route';

import GameObject from './game-object';
import GameObjectDetail from './game-object-detail';
import GameObjectUpdate from './game-object-update';
import GameObjectDeleteDialog from './game-object-delete-dialog';

const Routes = ({ match }) => (
  <>
    <Switch>
      <ErrorBoundaryRoute exact path={`${match.url}/new`} component={GameObjectUpdate} />
      <ErrorBoundaryRoute exact path={`${match.url}/:id/edit`} component={GameObjectUpdate} />
      <ErrorBoundaryRoute exact path={`${match.url}/:id`} component={GameObjectDetail} />
      <ErrorBoundaryRoute path={match.url} component={GameObject} />
    </Switch>
    <ErrorBoundaryRoute exact path={`${match.url}/:id/delete`} component={GameObjectDeleteDialog} />
  </>
);

export default Routes;
