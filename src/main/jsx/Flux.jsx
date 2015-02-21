'use strict';

import Flummox from 'flummox';
import AppActions from './actions/Actions.jsx';
import RecentProjectsStore from './stores/RecentProjectsStore.jsx';
import JobStore from './stores/JobStore.jsx';
 export default class Flux extends Flummox {

    constructor() {
        super();
        this.createActions('app', AppActions);
        this.createStore('recentProjects', RecentProjectsStore, this);
        this.createStore('job', JobStore, this);
    }

}
