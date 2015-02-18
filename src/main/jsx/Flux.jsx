'use strict';

import Flummox from 'flummox';
import AppActions from './actions/Actions';
import RecentProjectsStore from './stores/RecentProjectsStore';
 export default class Flux extends Flummox {

    constructor() {
        super();
        this.createActions('app', AppActions);
        this.createStore('recentProjects', RecentProjectsStore, this);
    }

}
