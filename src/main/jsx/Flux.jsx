'use strict';

import Flummox from 'flummox';
import AppActions from './actions/Actions.jsx';
import BuildHistoryActions from './actions/BuildHistoryActions.jsx';
import RecentProjectsStore from './stores/RecentProjectsStore.jsx';
import JobStore from './stores/JobStore.jsx';
import BuildHistoryStore from './stores/BuildHistoryStore.jsx';
 export default class Flux extends Flummox {

    constructor() {
        super();
        this.createActions('app', AppActions);
        this.createStore('recentProjects', RecentProjectsStore, this);
        this.createStore('job', JobStore, this);


        this.createActions('buildHistory', BuildHistoryActions);
        this.createStore('buildHistory', BuildHistoryStore,this);
    }

}
