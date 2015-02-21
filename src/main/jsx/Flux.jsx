'use strict';

import Flummox from 'flummox';
import AppActions from './actions/Actions';
import RecentProjectsStore from './stores/RecentProjectsStore';
import JobStore from './stores/JobStore';
 export default class Flux extends Flummox {

    constructor() {
        super();
        this.createActions('app', AppActions);
        this.createStore('recentProjects', RecentProjectsStore, this);
        this.createStore('job', JobStore, this);
    }

}
