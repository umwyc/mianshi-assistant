import React from "react";
import "./index.css";

/**
 * 全局底部栏组件
 * @constructor
 */
export default function GlobalFooter() {
  const currentYear = new Date().getFullYear();

  return (
    <div className="global-footer">
      <div>© {currentYear} 面试助手</div>
      <div>
        <a href="https://www.code-nav.cn" target="_blank">
          created by orange
        </a>
      </div>
    </div>
  );
}
