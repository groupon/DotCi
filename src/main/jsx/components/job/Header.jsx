import React from "react";
import IconLink from '../lib/IconLink.jsx';
require('./header.less');
export default React.createClass({
    render(){
        return(
          <div className="flex-row">
                    <IconLink href={this.props.githubUrl} icon="octicon octicon-mark-github">{this.props.fullName}</IconLink>
                    <IconLink href="build?delay=0sec" icon="fa fa-rocket"> Build Now</IconLink>
            </div>
        );
    },
    deleteJob(){
        let actions = this.props.flux.getActions('app');
        actions.deleteProject();
    }
});
