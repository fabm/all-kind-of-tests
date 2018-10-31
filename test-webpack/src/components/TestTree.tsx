import * as React from "react";

const TestTree = (props: any) => {
    return <div style={{marginLeft: props.lMargin}}>
        <div>main</div>
        <div className={"react-checkbox-tree"}>
            <ol>
                <li className={"rct-node rct-node-parent"}>
            <span className={"rct-text"}>
                <button aria-label="Toggle" className={"rct-collapse rct-collapse-btn"} title="Toggle" type="button">
                    <span className={"rct-icon rct-icon-expand-open"}></span>
                </button>
                <span className={"rct-title"}>app</span>
            </span>
                    <ol>
                        <li className={"rct-node rct-node-parent"}>
                    <span className={"rct-text"}>
                        <button aria-label="Toggle" className={"rct-collapse rct-collapse-btn"} title="Toggle"
                                type="button">
                            <span className={"rct-icon rct-icon-expand-close"}></span>
                        </button>
                            <span className={"rct-title"}>Http</span>
                    </span>
                        </li>
                        <li className={"rct-node rct-node-parent"}>
                    <span className={"rct-text"}>
                        <button aria-label="Toggle" className={"rct-collapse rct-collapse-btn"} title="Toggle"
                                type="button">
                            <span className={"rct-icon rct-icon-expand-close"}></span>
                        </button>
                        <label htmlFor="rct-DoPPd8Z-/app/Providers">
                            <input id="rct-DoPPd8Z-/app/Providers" type="checkbox"/>
                            <span className={"rct-title"}>Providers</span>
                        </label>
                    </span>
                        </li>
                    </ol>
                </li>
            </ol>
        </div>
        <div>transformed</div>
        <div className={"react-tree"}>
            <ol>
                <li className={"rct-node rct-node-parent"}>
            <span className={"rct-text"}>
                <span aria-label="Toggle" className={"rct-collapse rct-collapse-btn"} title="Toggle">
                    <span className={"rct-icon rct-icon-expand-open"}/>
                </span>
                <span className={"rct-title"}>app</span>
            </span>
                    <ol>
                        <li className={"rct-node rct-node-parent"}>
                    <span className={"rct-text node selectable"}>
                        <span aria-label="Toggle" className={"rct-collapse rct-collapse-btn"} title="Toggle">
                            <span className={"rct-icon rct-icon-expand-close"}/>
                        </span>
                            <span className={"rct-title"}>Http</span>
                    </span>
                        </li>
                        <li className={"rct-node rct-node-parent"}>
                    <span className={"rct-text"}>
                        <span aria-label="Toggle" className={"rct-collapse rct-collapse-btn"} title="Toggle">
                            <span className={"rct-icon rct-icon-expand-close"}/>
                        </span>
                        <label htmlFor="rct-DoPPd8Z-/app/Providers">
                            <input id="rct-DoPPd8Z-/app/Providers" type="checkbox"/>
                            <span className={"rct-title"}>Providers</span>
                        </label>
                    </span>
                        </li>
                    </ol>
                </li>
            </ol>
        </div>
    </div>
};

export default TestTree;