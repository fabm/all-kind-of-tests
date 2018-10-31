import * as React from "react";
import { KeyboardEventHandler, ReactElement, Ref, RefObject } from "react";
import  classNames from "classnames";

export interface Model {
    name: string;
    link: string;
    expanded: boolean;
    selectable: boolean;
    children: Model[];
}

export interface TNodeData {
    selectionPath: number[];
    navigationPath: number[];
    value: Model[];
}

export interface TNodeProps extends TNodeData {
    onExpand: (idx: number[]) => void;
    onSelect: (idx: number[]) => void;
    onKeyboardDown: KeyboardEventHandler<any>;
    tabIndex: number;
}

interface TNodeParams extends TNodeProps {
    path: number[];
}

function arraysEqual(array1: number[], array2: number[]) {
    if (array1.length !== array2.length) {
        return false;
    }

    for (let i in array1) {
        if (array1[i] !== array2[i]) {
            return false;
        }
    }
    return true;
}

function createNodes(params: TNodeParams): ReactElement<HTMLElement>[] | undefined {
    if (params.value.length === 0) {
        return undefined;
    }
    const path = params.path;
    const onExpand = params.onExpand;
    const onSelect = params.onSelect;
    let styleNavigate = {
        borderStyle: 'solid',
        borderColor: '#ccc',
        borderRadius: '5px',
        paddingLeft: '5px'
    };
    return params.value.map((value, idx): ReactElement<HTMLElement> => {
        const selected = arraysEqual(params.selectionPath, [...params.path, idx]);
        const navigate = arraysEqual(params.navigationPath, [...params.path, idx]);

        let classes = {
            'rct-selected':selected,
            'rct-text':true,
            'node':true,
            'selectable':value.selectable
        };
        function singleNode(style: any): ReactElement<HTMLElement> {
            return <span
                    className={classNames(classes)}
                    onClick={(evt) => {
                        evt.stopPropagation();
                        onSelect([...path, idx]);
                    }}
                    style={style}
                >{value.name}</span>;                
        }

        function withChildren(style:any,childParams:TNodeParams): ReactElement<HTMLElement>{
            let nodeClasses = {
                'rt-icon': true,
                'rct-icon-expand-open': value.expanded,
                'rct-icon-expand-close': !value.expanded
            };

            return <div>
                        <ol>
                            <li className={"rct-node rct-node-parent"}>
                                <span
                                    className={classNames(classes)}
                                    onClick={(evt) => {
                                        evt.stopPropagation();
                                        onSelect(childParams.path);
                                    }}
                                    style={style}
                                >
                                    <span className="rct-collapse rct-collapse-btn">
                                        <span
                                            className={classNames(nodeClasses)}
                                            onClick={(evt) => {
                                                evt.stopPropagation();
                                                onExpand(childParams.path);
                                            }}
                                        />
                                    </span>
                                    <span className="rct-title">{value.name}</span>
                                </span>
                            </li>
                        </ol>
                        <div style={{ marginLeft: '20px' }}>{value.expanded && createNodes(childParams)}</div>
                    </div>;
        }

        if (!(value.children.length > 0)) {
            let style: any = { marginLeft: '9px' };
            if (navigate) {
                style = { style, ...styleNavigate };
            }
            return singleNode(style);
        } else {
            const childParams: TNodeParams = {
                value: value.children,
                selectionPath: params.selectionPath,
                navigationPath: params.navigationPath,
                onKeyboardDown: params.onKeyboardDown,
                tabIndex: params.tabIndex,
                onSelect,
                onExpand,
                path: [...path, idx]
            };
            let style: any;
            if (navigate) {
                style = styleNavigate;
            }

            return withChildren(style,childParams);
        }
    });
}


class TNode extends React.Component<TNodeProps, any> {
    constructor(props: TNodeProps) {
        super(props);
        this.rootNode = React.createRef();
    }

    rootNode: RefObject<HTMLDivElement>;

    componentDidMount() {
        this.rootNode.current.focus();
    }

    public render() {
        let params: TNodeParams = {
            onKeyboardDown: this.props.onKeyboardDown,
            navigationPath: this.props.navigationPath,
            selectionPath: this.props.selectionPath,
            value: this.props.value,
            onExpand: this.props.onExpand,
            onSelect: this.props.onSelect,
            tabIndex: this.props.tabIndex,
            path: []
        };
        return (
            <div ref={this.rootNode}
                onKeyDown={this.props.onKeyboardDown}
                tabIndex={this.props.tabIndex}
                className={'react-tree'}
            >{createNodes(params)}</div>
        );
    }
}

export default TNode