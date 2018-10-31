import {ChildNode} from "./ChildNode"
import * as React from "react";
import {ReactNode} from "react";
import * as classNames from "classnames";

module TreeNode {
    export interface Props {
        disabled: boolean,
        expandDisabled: boolean,
        expanded: boolean,
        label: HTMLElement,
        optimisticToggle: boolean,
        showNodeIcon: boolean,
        treeId: string,
        value: string,
        onCheck: () => void,
        onExpand: (evt: {
            value: string,
            expanded: boolean
        }) => void,

        children?: HTMLElement,
        className?: string,
        expandOnClick?: boolean,
        icon?: HTMLElement,
        rawChildren?: any[],
        onClick?: (evt: {
            value: string,
            children: any[]
        }) => void,

    }
}



class TreeNode extends React.Component<TreeNode.Props, any> {

    static defaultProps: {
        onClick: () => {}
    };

    constructor(props: TreeNode.Props) {
        super(props);

        this.onClick = this.onClick.bind(this);
        this.onExpand = this.onExpand.bind(this);
    }


    onClick() {
        // Auto expand if enabled
        if (this.hasChildren() && this.props.expandOnClick) {
            this.onExpand();
        }

        this.props.onClick({
            value: this.props.value,
            children: this.props.rawChildren,
        });
    }

    onExpand() {
        this.props.onExpand({
            value: this.props.value,
            expanded: !this.props.expanded,
        });
    }

    hasChildren() {
        return this.props.rawChildren !== null;
    }

    renderCollapseButton() {
        const {expandDisabled} = this.props;

        if (!this.hasChildren()) {
            return (
                <span className="rct-collapse">
                    <span className="rct-icon"/>
                </span>
            );
        }

        return (
            <button
                aria-label="Toggle"
                className="rct-collapse rct-collapse-btn"
                disabled={expandDisabled}
                title="Toggle"
                type="button"
                onClick={this.onExpand}
            >
                {this.renderCollapseIcon()}
            </button>
        );
    }

    renderCollapseIcon() {
        if (!this.props.expanded) {
            return <span className="rct-icon rct-icon-expand-close"/>;
        }

        return <span className="rct-icon rct-icon-expand-open"/>;
    }


    renderNodeIcon() {
        if (this.props.icon !== null) {
            return this.props.icon;
        }
        if (!this.hasChildren()) {
            return <span className="rct-icon rct-icon-leaf"/>;
        }

        if (!this.props.expanded) {
            return <span className="rct-icon rct-icon-parent-close"/>;
        }

        return <span className="rct-icon rct-icon-parent-open"/>;
    }

    renderBareLabel(...children: ReactNode[]) {
        const {onClick} = this.props;

        const clickable = onClick.toString() !== TreeNode.defaultProps.onClick.toString();

        return (
            <span className="rct-bare-label">
                {clickable ? (
                    <span
                        className="rct-node-clickable"
                        onClick={this.onClick}
                        onKeyPress={this.onClick}
                        role="button"
                        tabIndex={0}
                    >
                        {children}
                    </span>
                ) : children}
            </span>
        );
    }

    renderCheckboxLabel(...children: ReactNode[]) {
        const {
            disabled,
            label,
            treeId,
            value,
            onClick,
        } = this.props;

        const clickable = onClick.toString() !== TreeNode.defaultProps.onClick.toString();
        const inputId = `${treeId}-${String(value).split(' ').join('_')}`;

        const render = [(
            <label key={0} htmlFor={inputId}>
                {!clickable ? children : null}
            </label>
        )];

        if (clickable) {
            render.push((
                <span
                    key={1}
                    className="rct-node-clickable"
                    onClick={this.onClick}
                    onKeyPress={this.onClick}
                    role="link"
                    tabIndex={0}
                >
                    {children}
                </span>
            ));
        }

        return render;
    }

    renderLabel() {
        const {label, showNodeIcon} = this.props;

        const labelChildren = [
            showNodeIcon ? (
                <span key={0} className="rct-node-icon">
                    {this.renderNodeIcon()}
                </span>
            ) : null,
            <span key={1} className="rct-title">
                {label}
            </span>,
        ];
        return this.renderBareLabel(labelChildren);

    }

    renderChildren() {
        if (!this.props.expanded) {
            return null;
        }

        return this.props.children;
    }

    render() {
        const {className, disabled} = this.props;
        const nodeClass = classNames({
            'rct-node': true,
            'rct-node-parent': this.hasChildren(),
            'rct-node-leaf': !this.hasChildren(),
            'rct-disabled': disabled,
        }, className);

        return (
            <li className={nodeClass}>
                <span className="rct-text">
                    {this.renderCollapseButton()}
                    {this.renderLabel()}
                </span>
                {this.renderChildren()}
            </li>
        );
    }
}

export default TreeNode;