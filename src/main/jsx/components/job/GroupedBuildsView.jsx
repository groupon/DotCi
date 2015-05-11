import React from 'react';
import LinearBuildsView from './LinearBuildsView.jsx';
export default React.createClass({
  render(){
    const groupedBuilds = this.props.builds.groupBy(x => x.get('commit'));
    const buildGroups = groupedBuilds.map((builds,commit) => {
      return(<div key={commit} className="ui segment">
        <LinearBuildsView  builds={builds}/> 
      </div>);
    });
    return(<span>{buildGroups.toArray()}</span>);
  }
});
