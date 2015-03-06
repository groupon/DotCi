import React from "react";
import IconLink from '../lib/IconLink.jsx';
import ButtonToolbar from 'react-bootstrap/lib/ButtonToolbar';
import ConfirmationModal from '../lib/ConfirmationModal.jsx';
import DropdownButton from 'react-bootstrap/lib/DropdownButton';
import MenuItem from 'react-bootstrap/lib/MenuItem';
require('./header.less');
export default React.createClass({
    render(){
        var settingsTitle = <span className="fa fa-cog">Settings</span>;
        return (
                <ButtonToolbar className="toolbar" justified>
                    <IconLink href={this.props.githubUrl} className="octicon octicon-mark-github">{this.props.fullName}</IconLink>
                    <IconLink href="build?delay=0sec" className="fa fa-rocket"> Build Now</IconLink>
                    <DropdownButton title={settingsTitle}   bsSize="small" className="settings-button" pullRight>
                        <MenuItem href="configure">Configure</MenuItem>
                        <ConfirmationModal onConfirm={this.deleteJob}>
                            <MenuItem>Delete</MenuItem>
                        </ConfirmationModal>
                    </DropdownButton>
                </ButtonToolbar>
        );
    },
    deleteJob(){
        let actions = this.props.flux.getActions('app');
        actions.deleteProject();
    }
});
