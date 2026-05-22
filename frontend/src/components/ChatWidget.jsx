import { useState, useRef, useEffect } from "react";

const SYSTEM_PROMPT = `You are a helpful assistant for FreelanceLK, a Sri Lankan freelancing platform.
Help users find freelancers, post jobs, understand how payments work, and navigate the platform.
Keep answers short (2-4 sentences). Be friendly and occasionally use Sri Lankan context.
If asked anything unrelated to freelancing or the platform, politely redirect.`;

const QUICK_CHIPS = [
  "How do I post a job?",
  "How do I find freelancers?",
  "How does payment work?",
];

export default function ChatWidget() {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState([
    {
      role: "bot",
      text: "Welcome! I'm your FreelanceLK assistant. How can I help you today?",
      showChips: true,
    },
  ]);
  const [input, setInput] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const messagesEndRef = useRef(null);
  const historyRef = useRef([]);
  const inputRef = useRef(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, isLoading]);

  useEffect(() => {
    if (isOpen) inputRef.current?.focus();
  }, [isOpen]);

  const sendMessage = async (overrideText) => {
    const text = (overrideText ?? input).trim();
    if (!text || isLoading) return;

    setInput("");
    setIsLoading(true);

    const userMsg = { role: "user", text };
    setMessages((prev) => [...prev, userMsg]);

    historyRef.current.push({ role: "user", parts: [{ text }] });

    try {
      const apiKey = import.meta.env.VITE_GEMINI_KEY;
      const res = await fetch(
        `https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=${apiKey}`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            system_instruction: { parts: [{ text: SYSTEM_PROMPT }] },
            contents: historyRef.current,
          }),
        }
      );
      const data = await res.json();
      const reply =
        data?.candidates?.[0]?.content?.parts?.[0]?.text ||
        "Sorry, I couldn't get a response. Please try again!";
      historyRef.current.push({ role: "model", parts: [{ text: reply }] });
      setMessages((prev) => [...prev, { role: "bot", text: reply }]);
    } catch (e) {
      setMessages((prev) => [
        ...prev,
        { role: "bot", text: "Oops, something went wrong. Please try again!" },
      ]);
    }

    setIsLoading(false);
  };

  const handleKey = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  return (
    <>
      {/* Floating Action Button */}
      <button
        onClick={() => setIsOpen((o) => !o)}
        aria-label="Open chat assistant"
        style={{
          position: "fixed",
          bottom: 24,
          right: 24,
          width: 56,
          height: 56,
          borderRadius: "50%",
          background: "#1a56db",
          border: "none",
          cursor: "pointer",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          boxShadow: "0 4px 16px rgba(26,86,219,0.35)",
          zIndex: 1000,
          transition: "transform 0.2s, background 0.2s",
        }}
        onMouseEnter={(e) => (e.currentTarget.style.background = "#1e429f")}
        onMouseLeave={(e) => (e.currentTarget.style.background = "#1a56db")}
      >
        {isOpen ? (
          <svg width="22" height="22" viewBox="0 0 24 24" fill="white">
            <line x1="18" y1="6" x2="6" y2="18" stroke="white" strokeWidth="2.5" strokeLinecap="round" />
            <line x1="6" y1="6" x2="18" y2="18" stroke="white" strokeWidth="2.5" strokeLinecap="round" />
          </svg>
        ) : (
          <svg width="26" height="26" viewBox="0 0 24 24" fill="white">
            <path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-2 12H6v-2h12v2zm0-3H6V9h12v2zm0-3H6V6h12v2z" />
          </svg>
        )}
      </button>

      {/* Chat Window */}
      <div
        style={{
          position: "fixed",
          bottom: 90,
          right: 24,
          width: 340,
          height: 480,
          background: "#fff",
          borderRadius: 16,
          border: "0.5px solid #e5e7eb",
          display: "flex",
          flexDirection: "column",
          overflow: "hidden",
          zIndex: 999,
          boxShadow: "0 8px 32px rgba(0,0,0,0.12)",
          transform: isOpen ? "scale(1) translateY(0)" : "scale(0.92) translateY(12px)",
          opacity: isOpen ? 1 : 0,
          pointerEvents: isOpen ? "all" : "none",
          transition: "transform 0.22s cubic-bezier(.4,0,.2,1), opacity 0.2s",
        }}
        role="dialog"
        aria-label="FreelanceLK chat assistant"
      >
        {/* Header */}
        <div
          style={{
            background: "#1a56db",
            padding: "14px 16px",
            display: "flex",
            alignItems: "center",
            gap: 10,
            flexShrink: 0,
          }}
        >
          <div
            style={{
              width: 34,
              height: 34,
              borderRadius: "50%",
              background: "rgba(255,255,255,0.2)",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              fontWeight: 700,
              fontSize: 13,
              color: "white",
              flexShrink: 0,
            }}
          >
            FL
          </div>
          <div style={{ flex: 1 }}>
            <div style={{ fontWeight: 600, fontSize: 14, color: "white" }}>
              FreelanceLK Assistant
            </div>
            <div
              style={{
                fontSize: 11,
                color: "rgba(255,255,255,0.75)",
                display: "flex",
                alignItems: "center",
                gap: 4,
              }}
            >
              <span
                style={{
                  width: 6,
                  height: 6,
                  borderRadius: "50%",
                  background: "#34d399",
                  display: "inline-block",
                }}
              />
              Powered by Gemini
            </div>
          </div>
        </div>

        {/* Messages */}
        <div
          style={{
            flex: 1,
            overflowY: "auto",
            padding: "16px 12px",
            display: "flex",
            flexDirection: "column",
            gap: 10,
            background: "#f9fafb",
          }}
        >
          {messages.map((msg, i) => (
            <div
              key={i}
              style={{
                display: "flex",
                flexDirection: msg.role === "user" ? "row-reverse" : "row",
                gap: 8,
                maxWidth: "100%",
              }}
            >
              {msg.role === "bot" && (
                <div
                  style={{
                    width: 26,
                    height: 26,
                    borderRadius: "50%",
                    background: "#dbeafe",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    fontSize: 11,
                    fontWeight: 700,
                    color: "#1a56db",
                    flexShrink: 0,
                    alignSelf: "flex-end",
                  }}
                >
                  FL
                </div>
              )}
              <div>
                <div
                  style={{
                    maxWidth: 220,
                    padding: "9px 13px",
                    borderRadius: 14,
                    fontSize: 13.5,
                    lineHeight: 1.5,
                    ...(msg.role === "bot"
                      ? {
                        background: "#fff",
                        color: "#111",
                        border: "0.5px solid #e5e7eb",
                        borderBottomLeftRadius: 4,
                      }
                      : {
                        background: "#1a56db",
                        color: "white",
                        borderBottomRightRadius: 4,
                      }),
                  }}
                >
                  {msg.text}
                </div>
                {msg.showChips && (
                  <div style={{ display: "flex", flexWrap: "wrap", gap: 6, marginTop: 6 }}>
                    {QUICK_CHIPS.map((chip) => (
                      <button
                        key={chip}
                        onClick={() => sendMessage(chip)}
                        style={{
                          padding: "5px 10px",
                          borderRadius: 20,
                          fontSize: 12,
                          background: "#dbeafe",
                          color: "#1a56db",
                          border: "none",
                          cursor: "pointer",
                          fontFamily: "inherit",
                        }}
                      >
                        {chip}
                      </button>
                    ))}
                  </div>
                )}
              </div>
            </div>
          ))}

          {/* Typing indicator */}
          {isLoading && (
            <div style={{ display: "flex", gap: 8, alignItems: "flex-end" }}>
              <div
                style={{
                  width: 26, height: 26, borderRadius: "50%",
                  background: "#dbeafe", display: "flex",
                  alignItems: "center", justifyContent: "center",
                  fontSize: 11, fontWeight: 700, color: "#1a56db", flexShrink: 0,
                }}
              >
                FL
              </div>
              <div
                style={{
                  background: "#fff",
                  border: "0.5px solid #e5e7eb",
                  borderRadius: 14,
                  borderBottomLeftRadius: 4,
                  padding: "12px 14px",
                  display: "flex",
                  gap: 4,
                }}
              >
                {[0, 0.2, 0.4].map((delay, i) => (
                  <span
                    key={i}
                    style={{
                      width: 7, height: 7, borderRadius: "50%",
                      background: "#9ca3af",
                      display: "inline-block",
                      animation: `bounce 1.2s ${delay}s infinite`,
                    }}
                  />
                ))}
              </div>
            </div>
          )}
          <div ref={messagesEndRef} />
        </div>

        {/* Input */}
        <div
          style={{
            padding: "10px 12px",
            borderTop: "0.5px solid #e5e7eb",
            display: "flex",
            gap: 8,
            background: "#fff",
            flexShrink: 0,
          }}
        >
          <input
            ref={inputRef}
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKey}
            placeholder="Ask me anything..."
            disabled={isLoading}
            style={{
              flex: 1,
              border: "0.5px solid #d1d5db",
              borderRadius: 10,
              padding: "9px 12px",
              fontSize: 13.5,
              background: "#f9fafb",
              color: "#111",
              height: 38,
              outline: "none",
              fontFamily: "inherit",
            }}
          />
          <button
            onClick={() => sendMessage()}
            disabled={isLoading || !input.trim()}
            aria-label="Send message"
            style={{
              width: 38, height: 38,
              borderRadius: 10,
              background: "#1a56db",
              border: "none",
              cursor: "pointer",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              flexShrink: 0,
              opacity: isLoading || !input.trim() ? 0.5 : 1,
              transition: "opacity 0.15s",
            }}
          >
            <svg width="17" height="17" viewBox="0 0 24 24" fill="white">
              <path d="M2 21l21-9L2 3v7l15 2-15 2v7z" />
            </svg>
          </button>
        </div>
      </div>

      <style>{`
        @keyframes bounce {
          0%, 60%, 100% { transform: translateY(0); }
          30% { transform: translateY(-5px); }
        }
      `}</style>
    </>
  );
}
