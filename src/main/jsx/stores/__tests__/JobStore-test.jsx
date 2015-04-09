import React from 'react';
import  {assert} from 'chai';
import JobStore from '../JobStore.jsx';
import Flux from '../../Flux.jsx'
import {Flummox,Store} from 'flummox';

describe('JobStore', ()=>{
  let _getJobStore =function(){
    const flux = new Flux();
    return new JobStore(flux);
  }
  it('should remove tab from state when removed', ()=>{
    let jobStore = _getJobStore();
    jobStore.jobInfoChanged({buildHistoryTabs: ['1','2']})
    let {buildHistoryTabs} = jobStore._getState().toObject();

    //assert Initial state
    assert.equal(buildHistoryTabs.size,2)
    assert.equal(buildHistoryTabs.get(0),'1')
    assert.equal(buildHistoryTabs.get(1),'2')

    jobStore.tabRemoved('1')
    let {buildHistoryTabs:newBuildHistoryTabs} = jobStore._getState().toObject();
    assert.equal(newBuildHistoryTabs.size,1)
    assert.equal(newBuildHistoryTabs.get(0),'1')
  })

  it('should add tab from state when removed', ()=>{
    let jobStore = _getJobStore();
    jobStore.jobInfoChanged({buildHistoryTabs: ['1','2']})
    let {buildHistoryTabs} = jobStore._getState().toObject();


    jobStore.tabAdded('3')
    let {buildHistoryTabs:newBuildHistoryTabs} = jobStore._getState().toObject();
    assert.equal(newBuildHistoryTabs.size,3)
    assert.equal(newBuildHistoryTabs.get(2),'3')
  })
  it('should add change builds', ()=>{
    let jobStore = _getJobStore();
    jobStore.jobInfoChanged({builds: ['1','2']})
    let {builds} = jobStore._getState().toObject();


    jobStore.buildHistoryChanged(['3','4']);
    let {builds:newBuilds} = jobStore._getState().toObject();
    assert.equal(newBuilds.size,2)
    assert.equal(newBuilds.get(0),'3')
  })
})
