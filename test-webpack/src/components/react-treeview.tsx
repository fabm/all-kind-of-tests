import * as React from "react";
import {ReactElement} from "react";
import * as classNames from "classnames";


class TreeView extends React.PureComponent<any, any> {

    constructor(props: any) {
        super(props);

        this.state = {
            collapsed: props.defaultCollapsed,
            selected: props.selected
        };
        this.handleExpandClick = this.handleExpandClick.bind(this);

    }

    handleExpandClick(...args: any[]) {
        this.setState({collapsed: !this.state.collapsed});
        if (this.props.onClick) {
            this.props.onClick(...args);
        }
    }

    handleSelectClick(...args: any[]) {
        if (!this.props.nodeSelected(this)) {
            return;
        }
        this.setState({selected: !this.state.selected});
        if (this.props.onClick) {
            this.props.onClick(...args);
        }
    }

    render() {
        let collapsed = this.state.collapsed;
        let nodeLabel = this.props.nodeLabel;
        let children = this.props.children;

        const classNamesInNode: string = classNames({
            'rct-selected': this.state.selected,
            'rct-text': true,
            'node': true,
            'selectable': this.props.selectable
        });

        const createIcon = (isCollapsed: boolean): string => 'rct-icon-expand-' + (isCollapsed ? 'close' : 'open');

        const createNode = (title: string) => (
            <ol>
                <li className={'rct-node rct-node-parent'}>
                    <span className={classNamesInNode}>
                        <span className={'rct-collapse rct-collapse-btn'} onClick={this.handleExpandClick.bind(this)}>
                            <span className={'rct-icon ' + createIcon(collapsed)}/>
                        </span>
                        <span onClick={this.props.selectable ? this.handleSelectClick.bind(this) : null}
                              className={'rct-title'}
                        >{title}</span>
                    </span>
                </li>
            </ol>
        );

        const createLeaf = (title: string) => (
            <div
                style={{marginLeft: '9px'}}
                className={classNamesInNode}
                onClick={this.props.selectable ? this.handleSelectClick.bind(this) : null}
            >{title}</div>
        )


        return (
            <div>
                <div>
                    {children == null ? createLeaf(nodeLabel) : createNode(nodeLabel)}
                </div>
                <div style={{marginLeft: '20px'}}>
                    {collapsed ? null : children}
                </div>
            </div>
        );
    }
}

export default TreeView;