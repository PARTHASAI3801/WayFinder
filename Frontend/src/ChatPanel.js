import React from "react";

export default function ChatPanel({ open, onClose, width = 420, children }) {
  if (!open) return null;

  return (
    <div
      role="dialog"
      aria-label="Map Chat — Let’s plan your path."
      style={{
        position: "fixed",
        right: 16,
        top: 16,
        bottom: 16,
        width,
        zIndex: 2500,
        background: "#fff",
        border: "1px solid #e6e6ef",
        borderRadius: 12,
        boxShadow: "0 12px 32px rgba(0,0,0,.25)",
        display: "flex",
        flexDirection: "column",
        overflow: "hidden",
        maxHeight: "calc(100vh - 32px)",
      }}
    >
      {/* Header */}
      <div
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          padding: "10px 12px",
          borderBottom: "1px solid #eee",
          flexShrink: 0,
        }}
      >
        <div style={{ display: "flex", flexDirection: "column" }}>
          <span style={{ fontWeight: 700 }}>Map Chat</span>
          <span style={{ fontSize: 12, color: "#6b6b76", marginTop: 2 }}>
            Let’s plan your path.
          </span>
        </div>

        <button
          onClick={onClose}
          title="Close"
          aria-label="Close chat"
          style={{
            background: "transparent",
            border: "none",
            fontSize: 20,
            cursor: "pointer",
            lineHeight: 1,
          }}
        >
          ×
        </button>
      </div>

      {/* Body */}
      <div
        style={{
          display: "flex",
          flex: 1,
          flexDirection: "column",
          overflow: "hidden",
          minHeight: 0,
        }}
      >
        {children}
      </div>
    </div>
  );
}
