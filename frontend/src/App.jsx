import { useState, useEffect, useRef } from "react";
import axios from "axios";

// ─── API layer (Axios) ────────────────────────────────────────────────────────
const API_BASE = "/api/v1";

// Token helpers — JWT persisted in localStorage so session survives page refresh
const getToken = () => localStorage.getItem("flk_token");
const setToken = (t) => localStorage.setItem("flk_token", t);
const clearToken = () => localStorage.removeItem("flk_token");
const getRefresh = () => localStorage.getItem("flk_refresh");
const setRefresh = (t) => localStorage.setItem("flk_refresh", t);
const clearRefresh = () => localStorage.removeItem("flk_refresh");

// Axios instance — shared config for every request
const api = axios.create({
  baseURL: API_BASE,
  headers: { "Content-Type": "application/json" },
  timeout: 15000, // 15 s — fail fast rather than hang forever
});

// Request interceptor — attach Bearer token automatically
api.interceptors.request.use(config => {
  const token = getToken();
  if (token) config.headers["Authorization"] = `Bearer ${token}`;
  return config;
});

// Response interceptor — unwrap ApiResponse wrapper + auto-refresh JWT on 401
// Backend always returns: { success, message, data, timestamp }
// We unwrap to response.data.data so every .then(res => ...) receives the actual payload
let _refreshPromise = null; // shared across concurrent requests
api.interceptors.response.use(
  response => response.data?.data !== undefined ? response.data.data : response.data,
  async error => {
    const original = error.config;
    // Auto-refresh on 401 (access token expired) — but not for auth endpoints or missing routes
    const status = error.response?.status;
    if (status === 401 && !original._retry && !original.url?.includes("/auth/")) {
      original._retry = true;
      try {
        if (!_refreshPromise) {
          const rt = getRefresh();
          if (!rt) throw new Error("No refresh token");
          _refreshPromise = axios.post(`${API_BASE}/auth/refresh`, { refreshToken: rt })
            .then(r => {
              const newToken = r.data?.data?.accessToken || r.data?.accessToken;
              if (!newToken) throw new Error("Bad refresh response");
              setToken(newToken);
              api.defaults.headers["Authorization"] = `Bearer ${newToken}`;
              return newToken;
            })
            .finally(() => { _refreshPromise = null; });
        }
        const newToken = await _refreshPromise;
        original.headers["Authorization"] = `Bearer ${newToken}`;
        return api(original).then(r => r.data?.data !== undefined ? r.data.data : r.data);
      } catch (_) {
        clearToken(); clearRefresh();
        window.dispatchEvent(new Event("flk:session-expired"));
        return Promise.reject(new Error("Session expired. Please log in again."));
      }
    }
    const msg =
      error.response?.data?.message   // backend ApiResponse message
      || error.response?.data?.error
      || error.message
      || "Network error";
    return Promise.reject(new Error(msg));
  }
);

// Convenience alias used throughout the app
const apiFetch = (path, options = {}) => {
  const { method = "GET", body, ...rest } = options;
  return api.request({
    url: path,
    method: method.toLowerCase(),
    data: body ? JSON.parse(body) : undefined,  // body was JSON.stringify'd — undo that for axios
    ...rest,
  });
};

// ─── Design tokens ────────────────────────────────────────────────────────────

// ─── Design tokens (Upgraded for Dark Mode) ──────────────────────────────────
const C = {
  blue: "var(--brand)",
  blueDark: "var(--brand-dark)",
  blueLight: "var(--brand-light)",
  green: "var(--green)",
  greenDark: "var(--green-dark)",
  greenLight: "var(--green-light)",
  teal: "#0EA5E9", // Kept static as it's an accent
  tealLight: "var(--teal-light)",
  navy: "var(--navy)",
  navyDark: "#151F4D",
  gray: "var(--text-muted)",
  grayLight: "var(--bg-subtle)",
  border: "var(--border)",
  text: "var(--text)",
  textMuted: "var(--text-muted)",
};

// ─── Static data ──────────────────────────────────────────────────────────────
const GIG_IMAGES = [
  "https://images.unsplash.com/photo-1561070791-2526d30994b5?w=400&h=220&fit=crop",
  "https://images.unsplash.com/photo-1486312338219-ce68d2c6f44d?w=400&h=220&fit=crop",
  "https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=400&h=220&fit=crop",
  "https://images.unsplash.com/photo-1611162617474-5b21e879e113?w=400&h=220&fit=crop",
  "https://images.unsplash.com/photo-1542744173-8e7e53415bb0?w=400&h=220&fit=crop",
  "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=400&h=220&fit=crop",
];

const SAMPLE_GIGS = [
  { id: 1, img: GIG_IMAGES[0], category: "Graphic Design", seller: "Kamal Silva", initials: "K", rating: 5.0, reviews: 127, price: 5000, delivery: 3, title: "I will design a modern logo for your business" },
  { id: 2, img: GIG_IMAGES[1], category: "Content Writing", seller: "Nimesha Fernando", initials: "N", rating: 5.0, reviews: 89, price: 3500, delivery: 2, title: "I will write SEO-optimized content in English & Sinhala" },
  { id: 3, img: GIG_IMAGES[2], category: "Web Development", seller: "Ravindu Perera", initials: "R", rating: 5.0, reviews: 203, price: 25000, delivery: 7, title: "I will develop a responsive WordPress website" },
  { id: 4, img: GIG_IMAGES[3], category: "Social Media", seller: "Thilini Jayasinghe", initials: "T", rating: 4.9, reviews: 64, price: 4500, delivery: 3, title: "I will manage your social media accounts professionally" },
  { id: 5, img: GIG_IMAGES[4], category: "Data Entry", seller: "Sachini Wijeratne", initials: "S", rating: 4.8, reviews: 41, price: 1200, delivery: 1, title: "I will do accurate data entry and Excel work" },
  { id: 6, img: GIG_IMAGES[5], category: "Marketing", seller: "Hasitha Bandara", initials: "H", rating: 4.9, reviews: 78, price: 6000, delivery: 5, title: "I will create a complete digital marketing strategy" },
];

const SAMPLE_JOBS = [
  { id: 1, badge: "Part-Time", time: "2 hours ago", title: "Cashier - Part Time", company: "Keells Super", desc: "Looking for a friendly cashier for weekend shifts", location: "Nugegoda", wage: "LKR 2,000", wageType: "Daily Wage" },
  { id: 2, badge: "Part-Time", time: "5 hours ago", title: "English Tutor", company: "Home Tuition Network", desc: "Need an experienced tutor for O/L students", location: "Colombo 07", wage: "LKR 3,000", wageType: "Daily Wage" },
  { id: 3, badge: "Part-Time", time: "1 day ago", title: "Data Entry Operator", company: "Tech Solutions Lanka", desc: "Remote work available. Flexible hours.", location: "Maharagama", wage: "LKR 1,500", wageType: "Daily Wage" },
  { id: 4, badge: "Part-Time", time: "1 day ago", title: "Waiter / Waitress", company: "Cafe Mocha", desc: "Evening shifts, friendly environment.", location: "Mount Lavinia", wage: "LKR 1,800", wageType: "Daily Wage" },
  { id: 5, badge: "Remote", time: "2 days ago", title: "Social Media Assistant", company: "Digital Agency LK", desc: "Create and schedule posts for multiple clients.", location: "Remote", wage: "LKR 2,500", wageType: "Daily Wage" },
  { id: 6, badge: "Part-Time", time: "3 days ago", title: "Event Support Staff", company: "TechSL Events", desc: "IT exhibition support roles available June 14-15.", location: "Colombo 03", wage: "LKR 2,200", wageType: "Daily Wage" },
];

const GIG_CATEGORIES = [
  { emoji: "🎨", name: "Graphic Design", count: 142 },
  { emoji: "💻", name: "Web Development", count: 98 },
  { emoji: "✍️", name: "Content Writing", count: 76 },
  { emoji: "📱", name: "Social Media", count: 54 },
  { emoji: "🎥", name: "Video Editing", count: 43 },
  { emoji: "📊", name: "Data Entry", count: 67 },
  { emoji: "🌐", name: "Translation", count: 38 },
  { emoji: "🎓", name: "Tutoring", count: 91 },
];

const AVATAR_COLORS = ["#2B3FBF", "#0EA5E9", "#16A34A", "#7C3AED", "#EA580C", "#0EA5E9"];

const DASH_TABS = ["Overview", "My Gigs", "Orders", "Applications", "Wallet", "Messages", "Settings"];
const DASH_ICONS = { Overview: "📊", "My Gigs": "🎯", Orders: "📋", Applications: "💼", Wallet: "💰", Messages: "💬", Settings: "⚙️" };
const ADMIN_TABS = ["Verifications", "Users", "Gigs", "Jobs", "Disputes"];
const ADMIN_ICONS = { Verifications: "🪪", Users: "👥", Gigs: "🎯", Jobs: "💼", Disputes: "⚖️" };

// ─── Global CSS ───────────────────────────────────────────────────────────────
const css = `
  @import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&display=swap');

  /* ── Modern Animations ── */
  @keyframes fadeUp {
    0% { opacity: 0; transform: translateY(20px); }
    100% { opacity: 1; transform: translateY(0); }
  }
  
  /* ── Layout Grids (Dashboard & Admin) ── */
  .dash-grid, .admin-grid {
    display: grid;
    grid-template-columns: 260px 1fr;
    gap: 32px;
    max-width: 1240px;
    margin: 0 auto;
    align-items: start;
  }
  
  .admin-sidebar {
    background: var(--surface); border-radius: 20px; border: 1px solid var(--border);
    padding: 24px; height: fit-content;
    box-shadow: var(--shadow-card);
    transition: var(--transition-theme);
  }
  
  .admin-nav-item {
    display: flex; align-items: center; gap: 12px; padding: 12px 16px;
    border-radius: 12px; font-size: 15px; font-weight: 500; color: var(--text-secondary);
    cursor: pointer; margin-bottom: 4px;
    transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  }
  .admin-nav-item:hover { background: var(--bg-muted); transform: translateX(4px); color: var(--text); }
  .admin-nav-item.active { background: var(--sidebar-active-bg); color: var(--sidebar-active-color); font-weight: 600; }

  /* ── Stat Cards ── */
  .stat-cards { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 16px; margin-bottom: 32px; }
  .stat-card { background: var(--surface); border: 1px solid var(--border); border-radius: 14px; padding: 20px; transition: var(--transition-theme); box-shadow: var(--shadow-card); }
  .stat-card-icon { font-size: 24px; margin-bottom: 8px; }
  .stat-card-label { font-size: 13px; color: var(--text-muted); margin-bottom: 4px; }
  .stat-card-val { font-size: 24px; font-weight: 800; color: var(--text); }

  /* ── Footer ── */
  .footer { 
    background: var(--footer-bg); color: var(--footer-text); 
    padding: 64px 2rem 24px; margin-top: 60px; 
    transition: var(--transition-theme); 
  }
  .footer-grid { 
    max-width: 1240px; margin: 0 auto; display: grid; 
    grid-template-columns: 2fr 1fr 1fr 1fr; gap: 40px; margin-bottom: 48px; 
  }
  .footer-brand-desc { font-size: 14px; line-height: 1.6; margin: 16px 0; max-width: 300px; color: var(--footer-text); }
  .footer-socials { display: flex; gap: 12px; }
  .footer-social { 
    width: 36px; height: 36px; border-radius: 50%; border: 1px solid var(--footer-border); 
    background: transparent; color: var(--footer-text); cursor: pointer; 
    display: flex; align-items: center; justify-content: center; transition: all 0.3s ease; 
  }
  .footer-social:hover { background: var(--brand); color: white; border-color: transparent; transform: translateY(-2px); }
  .footer-col-title { font-size: 16px; font-weight: 700; color: var(--footer-text-hover); margin-bottom: 20px; }
  .footer-link { font-size: 14px; margin-bottom: 12px; color: var(--footer-text); transition: color 0.2s ease; display: block; }
  .footer-link:hover { color: var(--footer-text-hover); text-decoration: underline; }
  .footer-contact-item { font-size: 14px; margin-bottom: 12px; display: flex; gap: 8px; color: var(--footer-text); }
  .footer-bottom { 
    max-width: 1240px; margin: 0 auto; padding-top: 24px; 
    border-top: 1px solid var(--footer-border); display: flex; justify-content: space-between; 
    align-items: center; font-size: 13px; color: var(--footer-text);
  }
  .footer-bottom-links { display: flex; gap: 24px; }
  .footer-bottom-link { cursor: pointer; transition: color 0.2s ease; }
  .footer-bottom-link:hover { color: var(--footer-text-hover); }

  /* Generic Button Fixes */
  .action-btn, .view-all-btn { transition: all 0.2s ease; cursor: pointer; border-radius: 8px; font-family: inherit; border: none; padding: 10px 20px; font-weight: 600;}
  .action-btn:hover, .view-all-btn:hover { transform: translateY(-2px); filter: brightness(0.95); }
  .action-btn:active, .view-all-btn:active { transform: scale(0.95); }

  @keyframes pulseGlow {
    0% { box-shadow: 0 0 0 0 rgba(43,63,191,0.4); }
    70% { box-shadow: 0 0 0 10px rgba(43,63,191,0); }
    100% { box-shadow: 0 0 0 0 rgba(43,63,191,0); }
  }

  /* ── Design tokens: Light ── */
  :root {
    --bg:          #ffffff;
    --bg-subtle:   #F9FAFB;
    --bg-muted:    #F3F4F6;
    --surface:     #ffffff;
    --surface-raised: #ffffff;
    --border:      #F3F4F6; /* Softer borders */
    --border-focus: #2B3FBF;
    --text:        #09090B; /* Deeper modern black */
    --text-secondary: #3F3F46;
    --text-muted:  #71717A;
    --text-placeholder: #A1A1AA;
    --brand:       #2B3FBF;
    --brand-dark:  #1E2E9E;
    --brand-light: #EEF1FF;
    --green:       #16A34A;
    --green-dark:  #15803D;
    --green-light: #DCFCE7;
    --teal-light:  #E0F2FE;
    --navy:        #1E2D6B;
    --hero-bg:     linear-gradient(145deg, #EEF1FF 0%, #F8FAFC 50%, #E0F2FE 100%);
    --hero-badge-bg: rgba(255,255,255,0.85);
    --search-bg:   #ffffff;
    --tab-active-bg: #ffffff;
    --tab-rail-bg: rgba(243,244,246,0.8);
    --notif-unread: #EEF1FF;
    --notif-hover: #F9FAFB;
    
    /* Modern, diffused shadows */
    --shadow-card: 0 4px 20px rgba(0, 0, 0, 0.03), 0 1px 3px rgba(0,0,0,0.02);
    --shadow-card-hover: 0 12px 32px rgba(43, 63, 191, 0.08), 0 4px 12px rgba(0,0,0,0.04);
    --shadow-search: 0 8px 30px rgba(0, 0, 0, 0.06);
    --shadow-panel: 0 16px 40px rgba(0,0,0,0.1);
    
    --nav-bg:      rgba(255,255,255,0.85);
    --footer-bg:   #1E2D6B;
    --footer-text: rgba(255,255,255,0.65);
    --footer-text-hover: rgba(255,255,255,1);
    --footer-border: rgba(255,255,255,0.15);
    --stats-bg:    #2B3FBF;
    --trust-bg:    #FAFAFA;
    --modal-bg:    #ffffff;
    --modal-overlay: rgba(0,0,0,0.3);
    --sidebar-active-bg: #EEF1FF;
    --sidebar-active-color: #2B3FBF;
    --upload-bg:   #F9FAFB;
    --upload-border: #E5E7EB;
    --pipeline-bg-from: #1E2D6B;
    --pipeline-bg-to:   #151F4D;
    --jobs-section-bg: linear-gradient(135deg, #DCFCE7 0%, #E0F2FE 100%);
    --client-hero-bg:  linear-gradient(135deg, #EEF1FF 0%, #E0F2FE 70%);
    
    /* Ultra-smooth theme transition */
    --transition-theme: background-color 0.5s cubic-bezier(0.22, 1, 0.36, 1), 
                        color 0.5s cubic-bezier(0.22, 1, 0.36, 1), 
                        border-color 0.5s ease, 
                        box-shadow 0.5s ease;
  }

  /* ── Design tokens: Dark ── */
  [data-theme="dark"] {
    --bg:          #09090B; /* Zinc-950 for a sleeker dark mode */
    --bg-subtle:   #18181B;
    --bg-muted:    #27272A;
    --surface:     #121214;
    --surface-raised: #18181B;
    --border:      #27272A;
    --border-focus: #5B73E8;
    --text:        #FAFAFA;
    --text-secondary: #D4D4D8;
    --text-muted:  #A1A1AA;
    --text-placeholder: #71717A;
    --brand:       #5B73E8;
    --brand-dark:  #818CF8;
    --brand-light: rgba(91, 115, 232, 0.15);
    --green:       #22C55E;
    --green-dark:  #4ADE80;
    --green-light: rgba(34, 197, 94, 0.15);
    --teal-light:  rgba(20, 184, 166, 0.15);
    --navy:        #E0E7FF;
    --hero-bg:     linear-gradient(145deg, #09090B 0%, #111827 50%, #0F172A 100%);
    --hero-badge-bg: rgba(39, 39, 42, 0.6);
    --search-bg:   #18181B;
    --tab-active-bg: #27272A;
    --tab-rail-bg: rgba(24, 24, 27, 0.8);
    --notif-unread: rgba(91, 115, 232, 0.15);
    --notif-hover: #27272A;
    
    --shadow-card: 0 4px 20px rgba(0, 0, 0, 0.4);
    --shadow-card-hover: 0 12px 32px rgba(0, 0, 0, 0.6), 0 0 0 1px rgba(91, 115, 232, 0.3);
    --shadow-search: 0 8px 30px rgba(0, 0, 0, 0.5);
    --shadow-panel: 0 16px 50px rgba(0, 0, 0, 0.8);
    
    --nav-bg:      rgba(9, 9, 11, 0.85);
    --footer-bg:   #050505;
    --stats-bg:    #050505;
    --trust-bg:    #09090B;
    --modal-bg:    #18181B;
    --modal-overlay: rgba(0,0,0,0.7);
    --sidebar-active-bg: rgba(91, 115, 232, 0.15);
    --sidebar-active-color: #818CF8;
    --upload-bg:   #18181B;
    --upload-border: #3F3F46;
    --pipeline-bg-from: #0D1117;
    --pipeline-bg-to:   #161B22;
    --jobs-section-bg: linear-gradient(135deg, rgba(34,197,94,0.08) 0%, rgba(14,165,233,0.08) 100%);
    --client-hero-bg:  linear-gradient(135deg, rgba(91,115,232,0.12) 0%, rgba(14,165,233,0.08) 70%);
  }

  * { box-sizing: border-box; margin: 0; padding: 0; }
  body {
    font-family: 'Inter', sans-serif;
    background: var(--bg);
    color: var(--text);
    transition: var(--transition-theme);
    overflow-x: hidden;
  }

  /* Entrance animations for main elements */
  .section, .hero, .gig-card, .job-card, .cat-card, .trust-card, .stat-card {
    animation: fadeUp 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards;
  }

  /* ── Nav ── */
  .nav {
    position: sticky; top: 0; z-index: 100;
    background: var(--nav-bg);
    border-bottom: 1px solid var(--border);
    padding: 0 2rem;
    display: flex; align-items: center; justify-content: space-between;
    height: 68px;
    backdrop-filter: blur(16px);
    -webkit-backdrop-filter: blur(16px);
    transition: var(--transition-theme);
  }
  .nav-left { display: flex; align-items: center; gap: 2rem; }
  .logo-badge {
    width: 38px; height: 38px; border-radius: 12px;
    background: linear-gradient(135deg, #4F46E5 0%, #2B3FBF 100%);
    display: flex; align-items: center; justify-content: center;
    color: white; font-weight: 700; font-size: 13px; flex-shrink: 0;
    box-shadow: 0 4px 14px rgba(43,63,191,0.4);
    transition: transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1), box-shadow 0.3s ease;
  }
  .logo-badge:hover { transform: scale(1.1) rotate(-5deg); box-shadow: 0 6px 20px rgba(43,63,191,0.6); }
  .logo-text { font-size: 18px; font-weight: 800; color: var(--text); letter-spacing: -0.5px; transition: var(--transition-theme); }
  .nav-links { display: flex; gap: 4px; }
  .nav-link {
    padding: 8px 16px; border-radius: 10px; font-size: 14px; font-weight: 500;
    color: var(--text-secondary); cursor: pointer; border: none; background: transparent;
    font-family: inherit;
    transition: background 0.3s ease, color 0.3s ease, transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  }
  .nav-link:hover { background: var(--bg-muted); color: var(--text); transform: translateY(-2px); }
  
  .nav-right { display: flex; gap: 12px; align-items: center; }
  .btn-signin {
    padding: 9px 18px; border-radius: 10px; font-size: 14px; font-weight: 600;
    color: var(--text); cursor: pointer; border: none; background: transparent; font-family: inherit;
    transition: background 0.3s ease, transform 0.2s ease;
  }
  .btn-signin:hover { background: var(--bg-muted); transform: translateY(-1px); }
  
  /* Modern Button styling with spring active state */
  .btn-join {
    padding: 10px 22px; border-radius: 12px; font-size: 14px; font-weight: 600;
    color: white; background: var(--brand); cursor: pointer; border: none; font-family: inherit;
    box-shadow: 0 4px 14px rgba(43,63,191,0.3);
    transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  }
  .btn-join:hover { background: var(--brand-dark); transform: translateY(-2px); box-shadow: 0 8px 20px rgba(43,63,191,0.4); }
  .btn-join:active { transform: scale(0.95); }

  /* ── Theme toggle ── */
  .theme-toggle {
    width: 38px; height: 38px; border-radius: 12px; border: 1.5px solid var(--border);
    background: var(--bg-subtle); cursor: pointer; font-size: 16px;
    display: flex; align-items: center; justify-content: center;
    transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  }
  .theme-toggle:hover { background: var(--bg-muted); transform: rotate(15deg) scale(1.1); box-shadow: 0 4px 12px rgba(0,0,0,0.05); }

  /* ── Hero ── */
  .hero {
    background: var(--hero-bg);
    padding: 80px 2rem 72px; text-align: center;
    transition: var(--transition-theme);
  }
  .hero-badge {
    display: inline-flex; align-items: center; gap: 8px;
    background: var(--hero-badge-bg);
    border: 1px solid var(--border);
    border-radius: 100px; padding: 8px 20px; font-size: 13px;
    color: var(--text-secondary); font-weight: 600; margin-bottom: 28px;
    backdrop-filter: blur(10px);
    transition: var(--transition-theme), transform 0.3s ease;
    box-shadow: 0 2px 10px rgba(0,0,0,0.02);
  }
  .hero-badge:hover { transform: translateY(-2px); }
  
  .hero-title {
    font-size: 56px; font-weight: 800; color: var(--text);
    line-height: 1.1; letter-spacing: -2px; margin-bottom: 24px;
    transition: var(--transition-theme);
  }
  .hero-sub {
    font-size: 18px; color: var(--text-muted); font-weight: 400;
    line-height: 1.6; margin-bottom: 40px; max-width: 600px;
    margin-left: auto; margin-right: auto;
    transition: var(--transition-theme);
  }
  .hero-btns { display: flex; gap: 16px; justify-content: center; margin-bottom: 40px; }
  .hero-btn {
    display: inline-flex; align-items: center; gap: 8px; padding: 14px 32px;
    border-radius: 14px; font-size: 15px; font-weight: 600; cursor: pointer; border: none;
    font-family: inherit;
    transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  }
  .hero-btn:hover { transform: translateY(-3px); box-shadow: 0 8px 20px rgba(0,0,0,0.08); }
  .hero-btn:active { transform: scale(0.96); }

  .search-wrap {
    max-width: 720px; margin: 0 auto; background: var(--search-bg);
    border-radius: 18px; box-shadow: var(--shadow-search); padding: 10px;
    display: flex; align-items: center; gap: 12px;
    border: 1px solid var(--border);
    transition: var(--transition-theme), transform 0.3s ease;
  }
  .search-wrap:focus-within { transform: translateY(-2px); box-shadow: var(--shadow-panel); border-color: var(--brand); }
  .search-input {
    flex: 1; border: none; outline: none; font-size: 16px; font-family: inherit;
    color: var(--text); background: transparent; padding-left: 8px;
    transition: var(--transition-theme);
  }
  .search-input::placeholder { color: var(--text-placeholder); }
  
  .search-btn {
    padding: 12px 28px; border-radius: 12px; font-size: 15px; font-weight: 600;
    color: white; cursor: pointer; border: none; font-family: inherit;
    background: var(--brand);
    transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  }
  .search-btn:hover { background: var(--brand-dark); transform: scale(1.02); box-shadow: 0 6px 20px rgba(43,63,191,0.4); }
  .search-btn:active { transform: scale(0.95); }

  /* ── Sections ── */
  .section { max-width: 1240px; margin: 0 auto; padding: 72px 2rem; }
  .section-head { margin-bottom: 40px; }
  .section-title { font-size: 32px; font-weight: 800; color: var(--text); margin-bottom: 8px; letter-spacing: -1px; transition: var(--transition-theme); }
  .section-sub { font-size: 16px; color: var(--text-muted); transition: var(--transition-theme); }
  .section-row { display: flex; align-items: flex-end; justify-content: space-between; margin-bottom: 40px; }
  
  .nav-arrows { display: flex; gap: 12px; }
  .arr-btn {
    width: 44px; height: 44px; border-radius: 50%;
    border: 1px solid var(--border); background: var(--surface);
    cursor: pointer; font-size: 18px; display: flex; align-items: center; justify-content: center;
    color: var(--text-secondary);
    transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  }
  .arr-btn:hover { background: var(--brand-light); border-color: transparent; color: var(--brand); transform: scale(1.1); box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
  .arr-btn:active { transform: scale(0.9); }

  /* ── Category cards ── */
  .categories-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); gap: 16px; }
  .cat-card {
    background: var(--surface); border: 1px solid var(--border);
    border-radius: 16px; padding: 24px 16px; cursor: pointer;
    text-align: center;
    transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
    box-shadow: var(--shadow-card);
  }
  .cat-card:hover {
    border-color: transparent; background: var(--surface-raised);
    transform: translateY(-6px) scale(1.02);
    box-shadow: var(--shadow-card-hover);
  }
  .cat-card:active { transform: scale(0.96); }
  .cat-emoji { font-size: 32px; margin-bottom: 12px; transition: transform 0.3s ease; }
  .cat-card:hover .cat-emoji { transform: scale(1.15); }
  .cat-name { font-size: 14px; font-weight: 600; color: var(--text); margin-bottom: 4px; transition: var(--transition-theme); }
  .cat-count { font-size: 13px; color: var(--text-muted); transition: var(--transition-theme); }

  /* ── Gig cards ── */
  .gig-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 24px; }
  .gig-card {
    background: var(--surface); border-radius: 20px;
    border: 1px solid var(--border); overflow: hidden; cursor: pointer;
    transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
    box-shadow: var(--shadow-card);
    display: flex; flex-direction: column;
  }
  .gig-card:hover {
    transform: translateY(-6px);
    box-shadow: var(--shadow-card-hover);
    border-color: transparent;
  }
  .gig-img-wrap { position: relative; overflow: hidden; padding-bottom: 60%; }
  .gig-img { position: absolute; top: 0; left: 0; width: 100%; height: 100%; object-fit: cover; transition: transform 0.6s cubic-bezier(0.22, 1, 0.36, 1); }
  .gig-card:hover .gig-img { transform: scale(1.08); }
  
  .gig-cat-badge {
    position: absolute; bottom: 12px; left: 12px;
    background: rgba(255, 255, 255, 0.9); color: #09090b; font-size: 11px; font-weight: 700;
    padding: 6px 12px; border-radius: 10px;
    backdrop-filter: blur(8px);
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  }
  [data-theme="dark"] .gig-cat-badge { background: rgba(0,0,0,0.8); color: white; }
  
  .gig-fav {
    position: absolute; top: 12px; right: 12px;
    width: 36px; height: 36px; border-radius: 50%;
    background: rgba(255, 255, 255, 0.9); display: flex; align-items: center; justify-content: center;
    font-size: 16px; color: var(--text-muted); cursor: pointer; border: none;
    box-shadow: 0 4px 12px rgba(0,0,0,0.1); backdrop-filter: blur(8px);
    transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  }
  [data-theme="dark"] .gig-fav { background: rgba(0,0,0,0.7); }
  .gig-fav:hover { color: #EF4444; transform: scale(1.15); }
  .gig-fav:active { transform: scale(0.9); }
  
  .gig-body { padding: 20px; background: var(--surface); transition: var(--transition-theme); flex: 1; display: flex; flex-direction: column; }
  .gig-seller { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
  .avatar {
    width: 32px; height: 32px; border-radius: 50%; color: white;
    font-size: 13px; font-weight: 700; display: flex; align-items: center; justify-content: center;
    flex-shrink: 0; box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  }
  .seller-name { font-size: 14px; font-weight: 600; color: var(--text); transition: var(--transition-theme); }
  .verified-icon { color: var(--brand); font-size: 14px; }
  
  .gig-title { font-size: 16px; font-weight: 600; color: var(--text); line-height: 1.5; margin-bottom: 16px; transition: var(--transition-theme); flex: 1; }
  .gig-footer { display: flex; align-items: flex-end; justify-content: space-between; margin-top: auto; padding-top: 16px; border-top: 1px solid var(--border); transition: border-color 0.3s ease;}
  .gig-rating { font-size: 14px; color: var(--text-secondary); display: flex; align-items: center; gap: 6px; transition: var(--transition-theme); font-weight: 500; }
  .star { color: #FBBF24; font-size: 16px; }
  .gig-price-label { font-size: 12px; color: var(--text-muted); text-align: right; transition: var(--transition-theme); margin-bottom: 2px;}
  .gig-price { font-size: 18px; font-weight: 800; color: var(--brand); transition: var(--transition-theme); }

  /* ── Job cards ── */
  .job-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 20px; }
  .job-card {
    background: var(--surface); border: 1px solid var(--border);
    border-radius: 20px; padding: 24px; cursor: pointer;
    transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1); display: flex; flex-direction: column;
    box-shadow: var(--shadow-card);
  }
  .job-card:hover {
    transform: translateY(-6px);
    box-shadow: var(--shadow-card-hover);
    border-color: transparent;
  }
  .job-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
  .job-badge {
    font-size: 12px; font-weight: 700; padding: 6px 12px; border-radius: 8px;
    background: var(--green-light); color: var(--green);
    transition: var(--transition-theme);
  }
  .job-badge.remote { background: var(--brand-light); color: var(--brand); }
  .job-time { font-size: 13px; color: var(--text-muted); display: flex; align-items: center; gap: 6px; transition: var(--transition-theme); font-weight: 500;}
  .job-bookmark { color: var(--border); font-size: 18px; cursor: pointer; border: none; background: none; transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1); }
  .job-bookmark:hover { transform: scale(1.2); color: var(--brand); }
  
  .job-title { font-size: 18px; font-weight: 700; color: var(--text); margin-bottom: 6px; transition: var(--transition-theme); letter-spacing: -0.5px;}
  .job-company { font-size: 14px; color: var(--text-muted); display: flex; align-items: center; gap: 6px; margin-bottom: 12px; transition: var(--transition-theme); font-weight: 500;}
  .job-desc { font-size: 14px; color: var(--text-secondary); line-height: 1.6; margin-bottom: 20px; flex: 1; transition: var(--transition-theme); }
  
  .job-meta-row { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; padding-top: 16px; border-top: 1px solid var(--border); }
  .job-loc { font-size: 14px; color: var(--text-muted); display: flex; align-items: center; gap: 6px; transition: var(--transition-theme); font-weight: 500;}
  .job-wage { font-size: 18px; font-weight: 800; color: var(--green); transition: var(--transition-theme); }
  
  .btn-apply {
    width: 100%; padding: 12px; border-radius: 12px; font-size: 15px; font-weight: 600;
    color: white; background: var(--green); cursor: pointer; border: none; font-family: inherit;
    margin-top: 12px;
    box-shadow: 0 4px 14px rgba(22,163,74,0.2);
    transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  }
  .btn-apply:hover { background: var(--green-dark); transform: translateY(-2px); box-shadow: 0 6px 20px rgba(22,163,74,0.3); }
  .btn-apply:active { transform: scale(0.96); }

  /* ── Modals / Inputs ── */
  .modal-overlay {
    position: fixed; inset: 0; background: var(--modal-overlay); z-index: 200;
    display: flex; align-items: center; justify-content: center; padding: 20px;
    transition: opacity 0.4s ease;
    backdrop-filter: blur(8px);
    -webkit-backdrop-filter: blur(8px);
  }
  .modal {
    background: var(--modal-bg); border-radius: 24px; padding: 40px;
    width: 100%; max-width: 440px; position: relative;
    border: 1px solid var(--border);
    box-shadow: var(--shadow-panel);
    transition: var(--transition-theme);
    animation: fadeUp 0.4s cubic-bezier(0.16, 1, 0.3, 1) forwards;
  }
  .form-input, .form-select {
    width: 100%; padding: 12px 16px; border-radius: 12px;
    border: 1.5px solid var(--border); font-size: 15px; font-family: inherit; outline: none;
    background: var(--bg-subtle); color: var(--text);
    transition: all 0.3s cubic-bezier(0.22, 1, 0.36, 1);
  }
  .form-input:focus, .form-select:focus { 
    border-color: var(--brand); 
    box-shadow: 0 0 0 4px var(--brand-light); 
    background: var(--surface);
  }

  /* ── Missing Modal Inner Styles ── */
  .modal-close {
    position: absolute; top: 20px; right: 20px;
    background: none; border: none; font-size: 20px; color: var(--text-muted);
    cursor: pointer; transition: color 0.2s;
  }
  .modal-close:hover { color: var(--text); }
  
  .modal-title { 
    font-size: 24px; font-weight: 800; color: var(--text); 
    margin-bottom: 8px; letter-spacing: -0.5px; 
  }
  
  .modal-sub { 
    font-size: 14px; color: var(--text-muted); 
    margin-bottom: 24px; line-height: 1.5; 
  }
  
  .form-group { 
    margin-bottom: 16px; text-align: left; width: 100%;
  }
  
  .form-label { 
    display: block; font-size: 13px; font-weight: 600; 
    color: var(--text); margin-bottom: 8px; 
  }
  
  .modal-btn {
    width: 100%; padding: 12px; border-radius: 12px; font-size: 15px; font-weight: 600;
    color: white; background: var(--brand); cursor: pointer; border: none; font-family: inherit;
    margin-top: 8px; box-shadow: 0 4px 14px rgba(43,63,191,0.3);
    transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  }
  
  .modal-btn:hover { 
    background: var(--brand-dark); transform: translateY(-2px); 
    box-shadow: 0 6px 20px rgba(43,63,191,0.4); 
  }
  
  .modal-btn:active { transform: scale(0.96); }
  
  .modal-footer-text { 
    margin-top: 24px; font-size: 14px; color: var(--text-muted); text-align: center; 
  }
  
  .modal-link { 
    color: var(--brand); font-weight: 600; cursor: pointer; transition: color 0.2s; 
  }
  
  .modal-link:hover { color: var(--brand-dark); text-decoration: underline; }
  
  /* ── Sidebar & Dashboard ── */
  .sidebar {
    background: var(--surface); border-radius: 20px; border: 1px solid var(--border);
    padding: 24px; height: fit-content;
    box-shadow: var(--shadow-card);
    transition: var(--transition-theme);
  }
  .sidebar-nav-item {
    display: flex; align-items: center; gap: 12px; padding: 12px 16px;
    border-radius: 12px; font-size: 15px; font-weight: 500; color: var(--text-secondary);
    cursor: pointer; margin-bottom: 4px;
    transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  }
  .sidebar-nav-item:hover { background: var(--bg-muted); transform: translateX(4px); color: var(--text); }
  .sidebar-nav-item.active { background: var(--sidebar-active-bg); color: var(--sidebar-active-color); font-weight: 600; }

  /* Toggle Transition Fix */
  .theme-toggle * { pointer-events: none; }

  /* ── Stats bar ── */
  .stats-bar {
    background: var(--stats-bg);
    padding: 48px 2rem;
    transition: var(--transition-theme);
  }
  .stats-inner {
    max-width: 1240px; margin: 0 auto;
    display: grid; grid-template-columns: repeat(4, 1fr);
    gap: 32px; text-align: center;
  }
  .stat-num {
    font-size: 36px; font-weight: 800; color: #ffffff;
    letter-spacing: -1px; margin-bottom: 6px;
  }
  .stat-label {
    font-size: 14px; color: rgba(255,255,255,0.55); font-weight: 500;
  }

  /* ── Trust section ── */
  .trust-section {
    background: var(--trust-bg);
    padding: 72px 2rem;
    transition: var(--transition-theme);
  }
  .trust-inner {
    max-width: 1000px; margin: 0 auto; text-align: center;
  }
  .trust-title {
    font-size: 30px; font-weight: 800; color: var(--text);
    letter-spacing: -0.5px; margin-bottom: 10px;
    transition: var(--transition-theme);
  }
  .trust-sub {
    font-size: 16px; color: var(--text-muted); margin-bottom: 48px;
    transition: var(--transition-theme);
  }
  .trust-cards {
    display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 24px;
  }
  .trust-icon { font-size: 32px; margin-bottom: 14px; }
  .trust-card-title {
    font-size: 16px; font-weight: 700; color: var(--text);
    margin-bottom: 6px; transition: var(--transition-theme);
  }
  .trust-card-sub {
    font-size: 14px; color: var(--text-muted); line-height: 1.5;
    transition: var(--transition-theme);
  }

  /* ── Popular tags (hero) ── */
  .popular {
    margin-top: 20px; display: flex; align-items: center;
    justify-content: center; gap: 6px; flex-wrap: wrap;
    font-size: 14px; color: var(--text-muted); font-weight: 500;
  }
  .popular-tag {
    cursor: pointer; font-weight: 600;
    transition: opacity 0.2s;
  }
  .popular-tag:hover { opacity: 0.75; text-decoration: underline; }

  /* ── Notification bell & panel ── */
  .notif-bell {
    position: relative; background: none; border: none;
    font-size: 20px; cursor: pointer; padding: 6px;
    border-radius: 10px; line-height: 1;
    transition: background 0.2s;
  }
  .notif-bell:hover { background: var(--bg-muted); }
  .notif-dot {
    position: absolute; top: 4px; right: 4px;
    width: 8px; height: 8px; border-radius: 50%;
    background: #EF4444; border: 2px solid var(--nav-bg);
  }
  .notif-panel {
    position: absolute; top: calc(100% + 10px); right: 0;
    width: 320px; background: var(--surface);
    border: 1px solid var(--border); border-radius: 16px;
    box-shadow: var(--shadow-panel); overflow: hidden; z-index: 200;
    transition: var(--transition-theme);
  }
  .notif-header {
    display: flex; align-items: center; justify-content: space-between;
    padding: 14px 16px; border-bottom: 1px solid var(--border);
  }
  .notif-item {
    display: flex; align-items: flex-start; gap: 12px;
    padding: 12px 16px; cursor: pointer;
    transition: background 0.15s;
    border-bottom: 1px solid var(--border);
  }
  .notif-item:last-child { border-bottom: none; }
  .notif-item:hover { background: var(--notif-hover); }
  .notif-item.unread { background: var(--notif-unread); }

  /* ── Sidebar avatar & user info ── */
  .sidebar-avatar {
    width: 56px; height: 56px; border-radius: 50%;
    display: flex; align-items: center; justify-content: center;
    font-size: 22px; font-weight: 700; color: white;
    margin: 0 auto 10px; flex-shrink: 0;
  }
  .sidebar-user {
    text-align: center; padding-bottom: 20px;
    border-bottom: 1px solid var(--border); margin-bottom: 16px;
  }

  /* ── Skill tags ── */
  .skill-tag {
    display: inline-flex; align-items: center; gap: 6px;
    padding: 5px 12px; border-radius: 100px;
    border: 1px solid var(--border); background: var(--bg-subtle);
    font-size: 13px; font-weight: 500; color: var(--text-secondary);
    transition: var(--transition-theme);
  }

  /* ── Upload zone ── */
  .upload-zone {
    border: 2px dashed var(--upload-border);
    border-radius: 14px; padding: 32px;
    text-align: center; cursor: pointer;
    background: var(--upload-bg);
    transition: border-color 0.2s, background 0.2s;
  }
  .upload-zone:hover { border-color: var(--brand); background: var(--brand-light); }

  /* ── Review stars ── */
  .review-stars {
    display: flex; gap: 3px; font-size: 16px; color: #F59E0B;
  }

  /* ── Page Header (Browse, PostJob, CreateGig, About) ── */
  .page-header {
    background: var(--bg-subtle);
    border-bottom: 1px solid var(--border);
    padding: 28px 2rem 0;
    transition: var(--transition-theme);
  }
  .page-header-inner {
    max-width: 1240px;
    margin: 0 auto;
  }
  .page-header-title {
    font-size: 26px; font-weight: 800; color: var(--text);
    letter-spacing: -0.5px; margin-bottom: 4px;
    transition: var(--transition-theme);
  }
  .page-header-sub {
    font-size: 14px; color: var(--text-muted);
    margin-bottom: 20px;
    transition: var(--transition-theme);
  }

  /* ── Tab switcher (Services / Part-Time Jobs) ── */
  .tabs {
    display: inline-flex;
    background: var(--tab-rail-bg);
    border-radius: 12px;
    padding: 4px;
    gap: 2px;
    margin-bottom: 16px;
    border: 1px solid var(--border);
    backdrop-filter: blur(8px);
  }
  .tab {
    padding: 8px 20px; border-radius: 9px; border: none;
    font-size: 14px; font-weight: 600; cursor: pointer;
    font-family: inherit; color: var(--text-muted);
    background: transparent;
    transition: all 0.25s cubic-bezier(0.22, 1, 0.36, 1);
  }
  .tab:hover { color: var(--text); background: var(--bg-muted); }
  .tab.active {
    background: var(--tab-active-bg);
    color: var(--brand);
    box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  }

  /* ── Filter bar ── */
  .filter-bar {
    display: flex; flex-wrap: wrap; gap: 10px;
    margin-bottom: 20px; align-items: center;
  }
  .filter-select {
    padding: 8px 14px; border-radius: 10px;
    border: 1.5px solid var(--border);
    background: var(--surface); color: var(--text);
    font-size: 14px; font-family: inherit; font-weight: 500;
    cursor: pointer; outline: none;
    transition: all 0.2s ease;
    appearance: auto;
  }
  .filter-select:focus { border-color: var(--brand); box-shadow: 0 0 0 3px var(--brand-light); }
  
  @media (max-width: 768px) {
    .hero-title { font-size: 40px; letter-spacing: -1px; }
    .nav-links { display: none; }
    .trust-cards, .stats-inner { grid-template-columns: 1fr 1fr; }
    .footer-grid { grid-template-columns: 1fr 1fr; }
    .dash-grid { grid-template-columns: 1fr; }
    .stat-cards { grid-template-columns: 1fr 1fr; }
  }
`;

// ─── Shared leaf components (already top-level, no change needed) ─────────────
const GigCard = ({ gig, onCardClick, onSellerClick, saved, onSave }) => (
  <div className="gig-card" onClick={() => onCardClick(gig.id)}>
    <div className="gig-img-wrap">
      <img className="gig-img" src={gig.img} alt={gig.title} />
      <span className="gig-cat-badge">{gig.category}</span>
      <button
        className="gig-fav"
        title={saved ? "Remove from saved" : "Save gig"}
        onClick={e => { e.stopPropagation(); onSave && onSave(gig.id); }}
        style={{ color: saved ? "#EF4444" : "#9CA3AF" }}
      >{saved ? "♥" : "♡"}</button>
    </div>
    <div className="gig-body">
      <div className="gig-seller">
        <div className="avatar" style={{ background: AVATAR_COLORS[gig.id % AVATAR_COLORS.length] }}>{gig.initials}</div>
        <span className="seller-name" onClick={e => { e.stopPropagation(); onSellerClick(gig.id); }} style={{ cursor: "pointer" }}>{gig.seller}</span>
        <span className="verified-icon">✔</span>
      </div>
      <div className="gig-title">{gig.title}</div>
      <div className="gig-footer">
        <div className="gig-rating"><span className="star">★</span> <strong>{gig.rating}</strong> <span style={{ color: "#6B7280" }}>({gig.reviews})</span></div>
        <div><div className="gig-price-label">Starting at</div><div className="gig-price">LKR {gig.price.toLocaleString()}</div></div>
      </div>
    </div>
  </div>
);

const JobCard = ({ job, onApply, onView, saved, onSave }) => (
  <div className="job-card" onClick={onView} style={{ cursor: "pointer" }}>
    <div className="job-top">
      <span className={`job-badge ${job.badge === "Remote" ? "remote" : ""}`}>{job.badge}</span>
      <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
        <span className="job-time">🕐 {job.time}</span>
        <button className="job-bookmark" title={saved ? "Unsave job" : "Save job"} onClick={e => { e.stopPropagation(); onSave && onSave(job.id); }} style={{ color: saved ? C.blue : "#D1D5DB" }}>🔖</button>
      </div>
    </div>
    <div className="job-title">{job.title}</div>
    <div className="job-company">{job.company} <span className="verified-icon">✔</span></div>
    <div className="job-desc">{job.desc}</div>
    <div className="job-meta-row">
      <span className="job-loc">📍 {job.location}</span>
      <span className="job-wage">{job.wage}</span>
    </div>
    <div className="job-meta-row" style={{ marginBottom: 14 }}>
      <span className="job-loc">📅 {job.wageType}</span>
    </div>
    <button className="btn-apply" onClick={e => { e.stopPropagation(); onApply(); }}>Apply Now</button>
  </div>
);

// ─── Nav ──────────────────────────────────────────────────────────────────────
const Nav = ({ user, setPage, setTab, setAuthMode, openAuthModal, setAuthModal, setUser, setForm, notifications, setNotifications, showNotifPanel, setShowNotifPanel, darkMode, setDarkMode, loadNotifications }) => (
  <nav className="nav">
    <div className="nav-left">
      <div style={{ display: "flex", alignItems: "center", gap: 10, cursor: "pointer" }} onClick={() => setPage("home")}>
        <div className="logo-badge">FL</div>
        <span className="logo-text">Freelancelk</span>
      </div>
      <div className="nav-links">
        {!user && (
          /* Logged-out: show general nav */
          <>
            <button className="nav-link" onClick={() => { setPage("browse"); setTab("gigs"); }}>Find Services</button>
            <button className="nav-link" onClick={() => { setPage("browse"); setTab("jobs"); }}>Find Jobs</button>
            <button className="nav-link" onClick={() => setPage("about")}>About</button>
          </>
        )}
        {user && (
          <>
            {/* FREELANCER */}
            {user.role === "FREELANCER" && (
              <>
                <button className="nav-link" onClick={() => { setPage("browse"); setTab("jobs"); }}>Find Jobs</button>
                <button className="nav-link" onClick={() => setPage("about")}>About</button>
                <button className="nav-link" style={{ color: C.blue, fontWeight: 600 }} onClick={() => setPage("create-gig")}>+ Create Gig</button>
              </>
            )}
            {/* CLIENT */}
            {user.role === "CLIENT" && (
              <>
                <button className="nav-link" onClick={() => { setPage("browse"); setTab("gigs"); }}>Find Services</button>
                <button className="nav-link" onClick={() => setPage("about")}>About</button>
                <button className="nav-link" style={{ color: C.green, fontWeight: 600 }} onClick={() => setPage("post-job")}>Post a Job</button>
              </>
            )}
            {/* ADMIN */}
            {user.role === "ADMIN" && (
              <>
                <button className="nav-link" onClick={() => { setPage("browse"); setTab("gigs"); }}>Find Services</button>
                <button className="nav-link" onClick={() => { setPage("browse"); setTab("jobs"); }}>Find Jobs</button>
                <button className="nav-link" onClick={() => setPage("about")}>About</button>
                <button className="nav-link" style={{ color: "#EF4444", fontWeight: 600 }} onClick={() => setPage("admin")}>Admin ⚙️</button>
              </>
            )}
          </>
        )}
      </div>
    </div>
    <div className="nav-right">
      <button className="theme-toggle" onClick={() => setDarkMode(d => !d)} title={darkMode ? "Switch to light mode" : "Switch to dark mode"}>
        {darkMode ? "☀️" : "🌙"}
      </button>
      {user ? (
        <>
          <div style={{ position: "relative" }}>
            <button className="notif-bell" onClick={e => { e.stopPropagation(); setShowNotifPanel(p => !p); }} title="Notifications">
              🔔
              {notifications.some(n => !n.read) && <span className="notif-dot" />}
            </button>
            {showNotifPanel && (
              <div className="notif-panel" onClick={e => e.stopPropagation()}>
                <div className="notif-header">
                  <span style={{ fontWeight: 700, fontSize: 14 }}>Notifications</span>
                  <button style={{ fontSize: 12, color: C.blue, background: "none", border: "none", cursor: "pointer", fontFamily: "inherit", fontWeight: 600 }}
                    onClick={async () => { setNotifications(ns => ns.map(n => ({ ...n, read: true }))); try { await apiFetch("/notifications/read-all", { method: "PATCH" }); loadNotifications && loadNotifications(); } catch (_) { } }}>Mark all read</button>
                </div>
                {notifications.length === 0
                  ? <div style={{ padding: "20px", textAlign: "center", color: C.gray, fontSize: 13 }}>No notifications</div>
                  : notifications.map(n => (
                    <div key={n.id} className={`notif-item ${!n.read ? "unread" : ""}`}
                      onClick={async () => { setNotifications(ns => ns.map(x => x.id === n.id ? { ...x, read: true } : x)); if (!n.read && n.id) { try { await apiFetch(`/notifications/${n.id}/read`, { method: "PATCH" }); } catch (_) { try { await apiFetch(`/notifications/${n.id}/mark-read`, { method: "POST" }); } catch (__) { } } } }}>
                      <span style={{ fontSize: 20, flexShrink: 0 }}>{n.icon}</span>
                      <div style={{ flex: 1 }}>
                        <div style={{ fontSize: 13, fontWeight: 500, marginBottom: 2 }}>{n.text}</div>
                        <div style={{ fontSize: 11, color: C.gray }}>{n.time}</div>
                      </div>
                      {!n.read && <span style={{ width: 8, height: 8, background: C.blue, borderRadius: "50%", flexShrink: 0, marginTop: 4 }} />}
                    </div>
                  ))}
              </div>
            )}
          </div>
          <div style={{ display: "flex", alignItems: "center", gap: 8, cursor: "pointer" }} onClick={() => setPage("dashboard")}>
            <div className="avatar" style={{ background: C.blue, width: 36, height: 36 }}>{user.initials}</div>
            <span style={{ fontSize: 14, fontWeight: 600, color: C.text }}>{user.name.split(" ")[0]}</span>
          </div>
          <button className="btn-signin" onClick={async () => {
            try { const rt = getRefresh(); if (rt) await apiFetch("/auth/logout", { method: "POST", body: JSON.stringify({ refreshToken: rt }) }); } catch (_) { }
            clearToken(); clearRefresh(); setUser(null); setPage("home");
            setForm({ email: "", password: "", name: "", role: "FREELANCER" });
          }}>Logout</button>
        </>
      ) : (
        <>
          <button className="btn-signin" onClick={() => openAuthModal("login")}>Sign In</button>
          <button className="btn-join" onClick={() => openAuthModal("register")}>Join Now</button>
        </>
      )}
    </div>
  </nav>
);

// ─── Footer ───────────────────────────────────────────────────────────────────
const Footer = ({ user, setPage, setTab, setSearchQ, setAuthMode, setAuthModal, openAuthModal }) => (
  <footer className="footer">
    <div className="footer-grid">
      <div>
        <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
          <div className="logo-badge">FL</div>
          <span style={{ fontSize: 18, fontWeight: 700 }}>Freelancelk</span>
        </div>
        <div className="footer-brand-desc">Sri Lanka's first hybrid marketplace connecting freelancers and part-time job seekers with verified opportunities.</div>
        <div className="footer-socials">
          {["f", "𝕏", "ig", "in"].map(s => <button key={s} className="footer-social">{s}</button>)}
        </div>
      </div>
      <div>
        <div className="footer-col-title">For Freelancers</div>
        {[
          ["Find Work", () => { setSearchQ(""); setTab("jobs"); setPage("browse"); }],
          ["Create Gigs", () => user ? setPage("create-gig") : openAuthModal("register")],
          ["Pricing Plans", null],
          ["Success Stories", null],
          ["Become Verified", () => user ? setPage("dashboard") : openAuthModal("register")],
        ].map(([l, fn]) => (
          <button key={l} className="footer-link" onClick={fn || undefined} style={{ display: "block", textAlign: "left", background: "none", border: "none", cursor: fn ? "pointer" : "default", fontFamily: "inherit", padding: 0, width: "100%" }}>{l}</button>
        ))}
      </div>
      <div>
        <div className="footer-col-title">For Employers</div>
        {[
          ["Post a Job", () => user ? setPage("post-job") : openAuthModal("login")],
          ["Browse Talent", () => { setSearchQ(""); setTab("gigs"); setPage("browse"); }],
          ["Hire Freelancers", () => { setSearchQ(""); setTab("gigs"); setPage("browse"); }],
          ["Enterprise Solutions", null],
          ["Employer Resources", null],
        ].map(([l, fn]) => (
          <button key={l} className="footer-link" onClick={fn || undefined} style={{ display: "block", textAlign: "left", background: "none", border: "none", cursor: fn ? "pointer" : "default", fontFamily: "inherit", padding: 0, width: "100%" }}>{l}</button>
        ))}
      </div>
      <div>
        <div className="footer-col-title">Contact Us</div>
        <div className="footer-contact-item">📍 <span>123 Galle Road, Colombo 03, Sri Lanka</span></div>
        <div className="footer-contact-item">📞 <span>+94 11 234 5678</span></div>
        <div className="footer-contact-item">✉️ <span>hello@freelancelk.com</span></div>
      </div>
    </div>
    <div className="footer-bottom">
      <span>© 2026 Freelancelk. All rights reserved.</span>
      <div className="footer-bottom-links">
        {["Privacy Policy", "Terms of Service", "Cookie Policy"].map(l => <span key={l} className="footer-bottom-link">{l}</span>)}
      </div>
    </div>
  </footer>
);

// ─── AuthModal ────────────────────────────────────────────────────────────────
const AuthModal = ({ authMode, setAuthMode, form, setForm, formError, setFormError, handleLogin, setAuthModal, setPage, authLoading }) => (
  <div className="modal-overlay" onClick={() => setAuthModal(false)}>
    <div className="modal" onClick={e => e.stopPropagation()}>
      <button className="modal-close" onClick={() => setAuthModal(false)}>✕</button>
      <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 20 }}>
        <div className="logo-badge">FL</div>
        <span style={{ fontSize: 16, fontWeight: 700 }}>Freelancelk</span>
      </div>
      {authMode === "forgot" ? (
        <>
          <div className="modal-title">Reset Password</div>
          <div className="modal-sub">Enter your email and we'll send a reset link</div>
          <div className="form-group">
            <label className="form-label" htmlFor="flk-forgot-email">Email Address</label>
            <input className="form-input" type="email" placeholder="you@sjp.ac.lk" value={form.email} id="flk-forgot-email" onChange={e => setForm({ ...form, email: e.target.value })} />
          </div>
          {formError && <div style={{ fontSize: 13, color: "#EF4444", background: "#FEF2F2", padding: "8px 12px", borderRadius: 8, marginBottom: 8 }}>⚠️ {formError}</div>}
          <button className="modal-btn" onClick={async () => {
            if (!form.email.trim()) { setFormError("Email is required."); return; }
            try {
              await apiFetch(`/auth/forgot-password?email=${encodeURIComponent(form.email)}`, { method: "POST" });
              setAuthModal(false); setFormError("");
              alert("✅ If that email exists, a reset link has been sent.");
            } catch (e) { setFormError(e.message); }
          }}>Send Reset Link</button>
          <div className="modal-footer-text">
            <span className="modal-link" onClick={() => { setAuthMode("login"); setFormError(""); }}>← Back to Sign In</span>
          </div>
        </>
      ) : authMode === "verify-email" ? (
        <>
          <div style={{ textAlign: "center", padding: "12px 0" }}>
            <div style={{ fontSize: 48, marginBottom: 16 }}>📧</div>
            <div className="modal-title">Check Your Email</div>
            <div className="modal-sub">We've sent a verification link to <strong>{form.email || "your email"}</strong>. Click the link to activate your account.</div>
            <div style={{ background: "#F0FDF4", border: "1px solid #BBF7D0", borderRadius: 10, padding: 14, fontSize: 13, color: "#15803D", margin: "16px 0" }}>
              ✅ Didn't receive it? Check your spam folder or resend below.
            </div>
            <button className="modal-btn" onClick={async () => {
              try {
                await apiFetch(`/auth/resend-verification?email=${encodeURIComponent(form.email)}`, { method: "POST" });
                alert("✅ Verification email resent!");
              } catch (e) { alert("Error: " + e.message); }
            }}>Resend Verification Email</button>
            <div className="modal-footer-text">
              <span className="modal-link" onClick={() => { setAuthMode("login"); setFormError(""); }}>Back to Sign In</span>
            </div>
          </div>
        </>
      ) : (
        <>
          <div className="modal-title">{authMode === "login" ? "Welcome back" : "Create your account"}</div>
          <div className="modal-sub">{authMode === "login" ? "Sign in to continue" : "Join 2,400+ verified students"}</div>
          {authMode === "register" && (
            <div className="form-group">
              <label className="form-label" htmlFor="flk-full-name">Full Name</label>
              <input className="form-input" placeholder="e.g. Nuwan Perera" value={form.name} id="flk-full-name" onChange={e => setForm({ ...form, name: e.target.value })} />
            </div>
          )}
          <div className="form-group">
            <label className="form-label" htmlFor="flk-email-address">Email Address</label>
            <input className="form-input" type="email" placeholder="you@sjp.ac.lk" value={form.email} id="flk-email-address" onChange={e => setForm({ ...form, email: e.target.value })} />
          </div>
          <div className="form-group">
            <label className="form-label" htmlFor="flk-password">Password</label>
            <input className="form-input" type="password" placeholder="••••••••" value={form.password} id="flk-password" onChange={e => setForm({ ...form, password: e.target.value })} />
          </div>
          {authMode === "register" && (
            <div className="form-group">
              <label className="form-label" htmlFor="flk-role">I am a</label>
              <select id="flk-role" className="form-select" value={form.role} onChange={e => setForm({ ...form, role: e.target.value })}>
                <option value="FREELANCER">Student / Freelancer</option>
                <option value="CLIENT">Client / Employer</option>
              </select>
            </div>
          )}
          {formError && <div style={{ fontSize: 13, color: "#EF4444", background: "#FEF2F2", padding: "8px 12px", borderRadius: 8, marginBottom: 8 }}>⚠️ {formError}</div>}
          <button className="modal-btn" onClick={handleLogin} disabled={authLoading} style={{ opacity: authLoading ? 0.7 : 1 }}>{authLoading ? "Please wait…" : authMode === "login" ? "Sign In" : "Create Account"}</button>
          {authMode === "login" && (
            <div style={{ textAlign: "center", marginTop: 10 }}>
              <span className="modal-link" style={{ fontSize: 13 }} onClick={() => { setAuthMode("forgot"); setFormError(""); }}>Forgot password?</span>
            </div>
          )}
          <div className="modal-footer-text">
            {authMode === "login"
              ? <span>Don't have an account? <span className="modal-link" onClick={() => { setAuthMode("register"); setFormError(""); }}>Sign up free</span></span>
              : <span>Already have an account? <span className="modal-link" onClick={() => { setAuthMode("login"); setFormError(""); }}>Sign in</span></span>
            }
          </div>
        </>
      )}
    </div>
  </div>
);

// ─── Home ─────────────────────────────────────────────────────────────────────
// ─── Home (Role Router) ───────────────────────────────────────────────────────
// Drop-in replacement for the existing Home component in App.jsx.
//
// Routing logic:
//   user === null          → <LandingHome>   (conversion / SEO)
//   user.role === 'CLIENT' → <ClientHome>    (discovery feed)
//   user.role === 'FREELANCER' → <FreelancerHome> (opportunity feed)
//   user.role === 'ADMIN'  → immediate redirect to page 'admin'
//
// Usage in App.jsx — no prop changes needed on the call-site:
//   {page === "home" && (
//     <Home
//       heroMode={heroMode} setHeroMode={setHeroMode}
//       searchQ={searchQ} setSearchQ={setSearchQ}
//       setTab={setTab} setPage={setPage}
//       setSelectedGigId={setSelectedGigId} setSelectedJobId={setSelectedJobId}
//       {...authProps}           ← still works: authProps = { user, setAuthMode, setAuthModal }
//     />
//   )}
// ─────────────────────────────────────────────────────────────────────────────

// ─── Home (Role Router) ───────────────────────────────────────────────────────
// Drop-in replacement for the existing Home component in App.jsx.
//
// Routing logic:
//   user === null          → <LandingHome>   (conversion / SEO)
//   user.role === 'CLIENT' → <ClientHome>    (discovery feed)
//   user.role === 'FREELANCER' → <FreelancerHome> (opportunity feed)
//   user.role === 'ADMIN'  → immediate redirect to page 'admin'
//
// Usage in App.jsx — no prop changes needed on the call-site:
//   {page === "home" && (
//     <Home
//       heroMode={heroMode} setHeroMode={setHeroMode}
//       searchQ={searchQ} setSearchQ={setSearchQ}
//       setTab={setTab} setPage={setPage}
//       setSelectedGigId={setSelectedGigId} setSelectedJobId={setSelectedJobId}
//       {...authProps}           ← still works: authProps = { user, setAuthMode, setAuthModal }
//     />
//   )}
// ─────────────────────────────────────────────────────────────────────────────

const Home = (props) => {
  const { user, setPage } = props;

  // Admin bypass — redirect immediately, render nothing
  if (user?.role === "ADMIN") {
    // useEffect-free instant redirect: set state during render is intentional
    // for a redirect-only path with no visible flash.
    setTimeout(() => setPage("admin"), 0);
    return null;
  }

  if (!user) return <LandingHome {...props} />;
  if (user.role === "CLIENT") return <ClientHome {...props} />;
  if (user.role === "FREELANCER") return <FreelancerHome {...props} />;

  // Fallback — unknown role sees the landing page
  return <LandingHome {...props} />;
};


// ─────────────────────────────────────────────────────────────────────────────
// 1. LANDING HOME  (logged-out — conversion engine)
//    This is your existing Home component, renamed. Zero logic changes.
// ─────────────────────────────────────────────────────────────────────────────
const LandingHome = ({ heroMode, setHeroMode, searchQ, setSearchQ, setTab, setPage, setAuthMode, setAuthModal, setSelectedGigId, setSelectedJobId }) => {
  const [overviewGigs, setOverviewGigs] = useState([]);
  const [overviewJobs, setOverviewJobs] = useState([]);

  useEffect(() => {
    apiFetch("/listings/gigs?page=0&size=6").then(res => {
      const items = res?.content ?? (Array.isArray(res) ? res : []);
      setOverviewGigs(items.map(g => {
        const sellerName = g.seller?.displayName ||
          (g.seller?.firstName ? `${g.seller.firstName} ${g.seller.lastName || ""}`.trim() : null) ||
          "Freelancer";
        return {
          id: g.gigId || g.listingId,
          img: g.thumbnailUrl || g.imageUrl || GIG_IMAGES[0],
          category: g.category || "General",
          seller: sellerName,
          initials: sellerName[0]?.toUpperCase() || "F",
          rating: g.seller?.averageRating || g.averageRating || 0,
          reviews: g.seller?.totalReviews || g.totalReviews || 0,
          price: g.basePrice || g.startingPrice || 0,
          delivery: g.deliveryDays || 3,
          title: g.title,
        };
      }));
    }).catch(() => { });

    apiFetch("/listings/jobs?page=0&size=4").then(res => {
      const items = res?.content ?? (Array.isArray(res) ? res : []);
      setOverviewJobs(items.map(j => ({
        id: j.jobId || j.listingId,
        badge: j.employmentType || "Part-Time",
        time: j.createdAt ? new Date(j.createdAt).toLocaleDateString() : "Recently",
        title: j.title,
        company: j.companyName || "Employer",
        desc: j.description || "",
        location: j.location || "Sri Lanka",
        wage: `LKR ${(j.hourlyRate || j.wageAmount || 0).toLocaleString()}`,
        wageType: j.rateType || "Daily Wage",
      })));
    }).catch(() => { });
  }, []);

  return (
    <>
      <div className="hero">
        <div className="hero-badge">✦ Sri Lanka's First Hybrid Marketplace</div>
        <h1 className="hero-title">Hire Freelancers &<br />Find Part-Time Jobs</h1>
        <p className="hero-sub">Connect with verified students and professionals across Sri Lanka. Post gigs or find flexible work opportunities.</p>
        <div className="hero-btns">
          <button className="hero-btn" style={{ background: heroMode === "gigs" ? C.blue : "transparent", color: heroMode === "gigs" ? "white" : C.text }} onClick={() => setHeroMode("gigs")}>🔍 Find Services</button>
          <button className="hero-btn" style={{ background: heroMode === "jobs" ? C.green : "transparent", color: heroMode === "jobs" ? "white" : C.text }} onClick={() => setHeroMode("jobs")}>🗂️ Find Part-Time Jobs</button>
        </div>
        <div className="search-wrap">
          <span style={{ color: "#9CA3AF", fontSize: 18, padding: "0 8px" }}>🔍</span>
          <input
            className="search-input"
            placeholder={heroMode === "gigs" ? `Search for services... (e.g., "Logo Design", "Web Development")` : `Search for jobs... (e.g., "Cashier", "Tutor")`}
            value={searchQ}
            onChange={e => setSearchQ(e.target.value)}
            onKeyDown={e => { if (e.key === "Enter") { setTab(heroMode); setPage("browse"); } }}
          />
          <button className="search-btn" style={{ background: heroMode === "gigs" ? C.blue : C.green }} onClick={() => { setTab(heroMode); setPage("browse"); }}>Search</button>
        </div>
        <div className="popular">
          <span>Popular:</span>
          {(heroMode === "gigs" ? ["Logo Design", "WordPress", "Content Writing", "Social Media"] : ["Colombo", "Cashier", "Tutor", "Part-time"]).map((tag, i, arr) => (
            <span key={tag}>
              <span className="popular-tag" style={{ color: heroMode === "gigs" ? C.blue : C.green }} onClick={() => { setSearchQ(tag); setTab(heroMode); setPage("browse"); }}>{tag}</span>
              {i < arr.length - 1 && <span style={{ color: "#D1D5DB" }}> •</span>}
            </span>
          ))}
        </div>
      </div>

      <div className="section">
        <div className="section-head">
          <div className="section-title">Browse by Category</div>
          <div className="section-sub">Find services across all popular categories</div>
        </div>
        <div className="categories-grid">
          {GIG_CATEGORIES.map(cat => (
            <div className="cat-card" key={cat.name} role="button" tabIndex={0}
              onClick={() => { setSearchQ(cat.name); setTab("gigs"); setPage("browse"); }}
              onKeyDown={e => { if (e.key === "Enter") { setSearchQ(cat.name); setTab("gigs"); setPage("browse"); } }}
            >
              <div className="cat-emoji">{cat.emoji}</div>
              <div className="cat-name">{cat.name}</div>
              <div className="cat-count">{cat.count} gigs</div>
            </div>
          ))}
        </div>
      </div>

      <div style={{ background: "var(--bg-subtle)" }}>
        <div className="section">
          <div className="section-row">
            <div><div className="section-title">Popular Gigs</div><div className="section-sub">Top-rated services from verified freelancers</div></div>
            <div className="nav-arrows"><button className="arr-btn">‹</button><button className="arr-btn">›</button></div>
          </div>
          <div className="gig-grid">
            {overviewGigs.map(gig => (
              <GigCard key={gig.id} gig={gig}
                onCardClick={id => { setSelectedGigId(id); setPage("gig-detail"); }}
                onSellerClick={id => { setSelectedGigId(id); setPage("seller-profile"); }}
              />
            ))}
          </div>
          <div style={{ textAlign: "center" }}>
            <button className="view-all-btn" style={{ background: C.blue, color: "white" }} onClick={() => { setSearchQ(""); setTab("gigs"); setPage("browse"); }}>View All Gigs →</button>
          </div>
        </div>
      </div>

      <div style={{ background: "linear-gradient(135deg, #DCFCE7 0%, #E0F2FE 100%)" }}>
        <div className="section">
          <div className="section-row">
            <div><div className="section-title">Latest Part-Time Jobs</div><div className="section-sub">Fresh opportunities posted by verified employers</div></div>
            <div className="nav-arrows"><button className="arr-btn">‹</button><button className="arr-btn">›</button></div>
          </div>
          <div className="job-grid">
            {overviewJobs.map(job => (
              <JobCard key={job.id} job={job}
                onApply={() => { setAuthMode("login"); setAuthModal(true); }}
                onView={() => { setSelectedJobId(job.id); setPage("job-detail"); }}
              />
            ))}
          </div>
          <div style={{ textAlign: "center" }}>
            <button className="view-all-btn" style={{ background: C.green, color: "white" }} onClick={() => { setSearchQ(""); setTab("jobs"); setPage("browse"); }}>View All Jobs →</button>
          </div>
        </div>
      </div>

      <div className="stats-bar">
        <div className="stats-inner">
          {[["2,400+", "Verified Freelancers"], ["850+", "Registered Employers"], ["5,200+", "Completed Gigs"], ["98%", "Satisfaction Rate"]].map(([n, l]) => (
            <div key={l}><div className="stat-num">{n}</div><div className="stat-label">{l}</div></div>
          ))}
        </div>
      </div>

      <div className="trust-section">
        <div className="trust-inner">
          <div className="trust-title">Why Freelancelk? The Trust Advantage.</div>
          <div className="trust-sub">Every user goes through verification — giving you peace of mind on every transaction.</div>
          <div className="trust-cards">
            {[
              { icon: "🪪", title: "ID Verified", sub: "NIC & University ID checked for all students" },
              { icon: "⭐", title: "Trust Score", sub: "Smart algorithm rating across gigs & jobs" },
              { icon: "🛡️", title: "Escrow Wallet", sub: "Funds held safely until work is approved" },
              { icon: "💬", title: "Dispute Support", sub: "Admin-managed resolution for every order" },
            ].map(t => (
              <div className="trust-card" key={t.title}>
                <div className="trust-icon">{t.icon}</div>
                <div className="trust-card-title">{t.title}</div>
                <div className="trust-card-sub">{t.sub}</div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </>
  );
};


// ─────────────────────────────────────────────────────────────────────────────
// 2. CLIENT HOME  (logged-in as CLIENT — discovery feed)
// ─────────────────────────────────────────────────────────────────────────────
const ClientHome = ({ user, searchQ, setSearchQ, setTab, setPage, setSelectedGigId, setSelectedJobId }) => {
  const [recommendedGigs, setRecommendedGigs] = useState([]);
  const [recentJobs, setRecentJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const firstName = user?.name?.split(" ")[0] || "there";

  useEffect(() => {
    setLoading(true);
    Promise.all([
      apiFetch("/listings/gigs?page=0&size=8").catch(() => null),
      apiFetch("/listings/jobs?page=0&size=4").catch(() => null),
    ]).then(([gigsRes, jobsRes]) => {
      if (gigsRes) {
        const items = gigsRes?.content ?? (Array.isArray(gigsRes) ? gigsRes : []);
        setRecommendedGigs(items.map(g => {
          const sellerName = g.seller?.displayName ||
            (g.seller?.firstName ? `${g.seller.firstName} ${g.seller.lastName || ""}`.trim() : null) ||
            "Freelancer";
          return {
            id: g.gigId || g.listingId,
            img: g.thumbnailUrl || g.imageUrl || GIG_IMAGES[0],
            category: g.category || "General",
            seller: sellerName,
            initials: sellerName[0]?.toUpperCase() || "F",
            rating: g.seller?.averageRating || g.averageRating || 0,
            reviews: g.seller?.totalReviews || g.totalReviews || 0,
            price: g.basePrice || g.startingPrice || 0,
            delivery: g.deliveryDays || 3,
            title: g.title,
          };
        }));
      }
      if (jobsRes) {
        const items = jobsRes?.content ?? (Array.isArray(jobsRes) ? jobsRes : []);
        setRecentJobs(items.map(j => ({
          id: j.jobId || j.listingId,
          badge: j.employmentType || "Part-Time",
          time: j.createdAt ? new Date(j.createdAt).toLocaleDateString() : "Recently",
          title: j.title,
          company: j.companyName || "Employer",
          desc: j.description || "",
          location: j.location || "Sri Lanka",
          wage: `LKR ${(j.hourlyRate || j.wageAmount || 0).toLocaleString()}`,
          wageType: j.rateType || "Daily Wage",
        })));
      }
    }).finally(() => setLoading(false));
  }, []);

  return (
    <>
      {/* ── Personalised greeting hero ── */}
      <div style={{
        background: "var(--client-hero-bg)",
        padding: "40px 2rem 36px",
        borderBottom: `1px solid var(--border)`,
      }}>
        <div style={{ maxWidth: 1200, margin: "0 auto" }}>
          <div style={{ marginBottom: 6, fontSize: 14, color: "var(--text-muted)", fontWeight: 500 }}>
            👋 Welcome back, {firstName}
          </div>
          <h1 style={{ fontSize: 28, fontWeight: 800, color: "var(--text)", marginBottom: 20, letterSpacing: "-0.5px" }}>
            What service are you looking for today?
          </h1>

          {/* Gig search bar */}
          <div style={{
            maxWidth: 680,
            background: "var(--surface)",
            borderRadius: 14,
            boxShadow: "0 2px 20px rgba(0,0,0,0.08)",
            padding: 8,
            display: "flex",
            alignItems: "center",
            gap: 8,
            border: "1px solid var(--border)",
          }}>
            <span style={{ color: "#9CA3AF", fontSize: 18, padding: "0 8px" }}>🔍</span>
            <input
              className="search-input"
              placeholder='Try "Logo Design", "WordPress", "Social Media"…'
              value={searchQ}
              onChange={e => setSearchQ(e.target.value)}
              onKeyDown={e => { if (e.key === "Enter") { setTab("gigs"); setPage("browse"); } }}
            />
            <button
              className="search-btn"
              style={{ background: C.blue }}
              onClick={() => { setTab("gigs"); setPage("browse"); }}
            >Search Gigs</button>
          </div>

          {/* Quick-filter tags */}
          <div style={{ display: "flex", gap: 8, marginTop: 14, flexWrap: "wrap" }}>
            {["Logo Design", "Web Development", "Content Writing", "Social Media", "Video Editing"].map(tag => (
              <button key={tag} onClick={() => { setSearchQ(tag); setTab("gigs"); setPage("browse"); }}
                style={{
                  padding: "6px 14px", borderRadius: 100, border: `1px solid var(--border)`,
                  background: "var(--surface)", fontSize: 13, fontWeight: 500, color: "var(--text)",
                  cursor: "pointer", fontFamily: "inherit", transition: "all 0.15s",
                }}
                onMouseOver={e => { e.currentTarget.style.borderColor = C.blue; e.currentTarget.style.color = C.blue; }}
                onMouseOut={e => { e.currentTarget.style.borderColor = "var(--border)"; e.currentTarget.style.color = "var(--text)"; }}
              >{tag}</button>
            ))}
          </div>
        </div>
      </div>

      {/* ── Quick actions row ── */}
      <div style={{ background: "var(--surface)", borderBottom: `1px solid var(--border)` }}>
        <div style={{ maxWidth: 1200, margin: "0 auto", padding: "20px 2rem", display: "flex", gap: 12, flexWrap: "wrap" }}>
          {[
            { icon: "📋", label: "Post a New Job", desc: "Find the right talent fast", action: () => setPage("post-job"), primary: true },
            { icon: "🔍", label: "Browse Freelancers", desc: "Explore all services", action: () => { setTab("gigs"); setPage("browse"); } },
            { icon: "💼", label: "Browse Jobs", desc: "Part-time opportunities", action: () => { setTab("jobs"); setPage("browse"); } },
            { icon: "📊", label: "My Dashboard", desc: "Orders & wallet", action: () => setPage("dashboard") },
          ].map(({ icon, label, desc, action, primary }) => (
            <button key={label} onClick={action} style={{
              display: "flex", alignItems: "center", gap: 12,
              padding: "14px 20px", borderRadius: 12, border: `1.5px solid ${primary ? C.blue : "var(--border)"}`,
              background: primary ? C.blue : "var(--surface)", cursor: "pointer", fontFamily: "inherit",
              flex: "1 1 200px", transition: "all 0.15s", textAlign: "left",
            }}
              onMouseOver={e => { e.currentTarget.style.transform = "translateY(-2px)"; e.currentTarget.style.boxShadow = "0 6px 20px rgba(0,0,0,0.08)"; }}
              onMouseOut={e => { e.currentTarget.style.transform = ""; e.currentTarget.style.boxShadow = ""; }}
            >
              <span style={{ fontSize: 22 }}>{icon}</span>
              <div>
                <div style={{ fontSize: 14, fontWeight: 700, color: primary ? "white" : "var(--text)" }}>{label}</div>
                <div style={{ fontSize: 12, color: primary ? "rgba(255,255,255,0.75)" : "var(--text-muted)" }}>{desc}</div>
              </div>
            </button>
          ))}
        </div>
      </div>

      {/* ── Recommended Gigs ── */}
      <div style={{ background: "var(--bg-subtle)" }}>
        <div className="section">
          <div className="section-row">
            <div>
              <div className="section-title">Recommended for You</div>
              <div className="section-sub">Services matching your activity on Freelancelk</div>
            </div>
            <button className="view-all-btn" style={{ background: C.blue, color: "white" }}
              onClick={() => { setSearchQ(""); setTab("gigs"); setPage("browse"); }}>
              View All →
            </button>
          </div>
          {loading ? (
            <div style={{ textAlign: "center", padding: "48px 0", color: C.textMuted, fontSize: 14 }}>
              Loading gigs…
            </div>
          ) : (
            <div className="gig-grid">
              {recommendedGigs.map(gig => (
                <GigCard key={gig.id} gig={gig}
                  onCardClick={id => { setSelectedGigId(id); setPage("gig-detail"); }}
                  onSellerClick={id => { setSelectedGigId(id); setPage("seller-profile"); }}
                />
              ))}
            </div>
          )}
        </div>
      </div>

      {/* ── Category shortcuts ── */}
      <div className="section" style={{ paddingTop: 0 }}>
        <div className="section-head">
          <div className="section-title">Browse by Category</div>
          <div className="section-sub">Hire across all popular service categories</div>
        </div>
        <div className="categories-grid">
          {GIG_CATEGORIES.map(cat => (
            <div className="cat-card" key={cat.name} role="button" tabIndex={0}
              onClick={() => { setSearchQ(cat.name); setTab("gigs"); setPage("browse"); }}
              onKeyDown={e => { if (e.key === "Enter") { setSearchQ(cat.name); setTab("gigs"); setPage("browse"); } }}
            >
              <div className="cat-emoji">{cat.emoji}</div>
              <div className="cat-name">{cat.name}</div>
              <div className="cat-count">{cat.count} gigs</div>
            </div>
          ))}
        </div>
      </div>

      {/* ── Latest Jobs (clients might also want part-time staff) ── */}
      {recentJobs.length > 0 && (
        <div style={{ background: "var(--jobs-section-bg)" }}>
          <div className="section">
            <div className="section-row">
              <div>
                <div className="section-title">Newly Posted Jobs</div>
                <div className="section-sub">Fresh part-time openings from verified employers</div>
              </div>
              <button className="view-all-btn" style={{ background: C.green, color: "white" }}
                onClick={() => { setSearchQ(""); setTab("jobs"); setPage("browse"); }}>
                View All →
              </button>
            </div>
            <div className="job-grid">
              {recentJobs.map(job => (
                <JobCard key={job.id} job={job}
                  onApply={() => { setSelectedJobId(job.id); setPage("job-detail"); }}
                  onView={() => { setSelectedJobId(job.id); setPage("job-detail"); }}
                />
              ))}
            </div>
          </div>
        </div>
      )}
    </>
  );
};


// ─────────────────────────────────────────────────────────────────────────────
// 3. FREELANCER HOME  (logged-in as FREELANCER — opportunity feed)
// ─────────────────────────────────────────────────────────────────────────────
const FreelancerHome = ({ user, searchQ, setSearchQ, setTab, setPage, setSelectedJobId }) => {
  const [recommendedJobs, setRecommendedJobs] = useState([]);
  const [dashStats, setDashStats] = useState(null);   // { activeOrders, pendingEarnings, totalEarnings }
  const [gigPerf, setGigPerf] = useState([]);          // [{ title, impressions }]
  const [loading, setLoading] = useState(true);
  const firstName = user?.name?.split(" ")[0] || "there";

  useEffect(() => {
    if (!user?.userId) return;
    setLoading(true);

    // Stats — GET /api/v1/users/{userId}/stats
    apiFetch(`/users/${user.userId}/stats`)
      .then(res => {
        setDashStats({
          activeOrders: res.activeOrders ?? 0,
          pendingEarnings: res.pendingEarnings ?? 0,
          totalEarnings: res.totalEarned ?? 0,
          completedOrders: res.completedOrders ?? 0,
        });
      })
      .catch(() => { }); // stats failing should never log the user out

    // Jobs feed — separate call so a stats failure never blocks the feed
    apiFetch("/listings/jobs?page=0&size=6")
      .then(res => {
        const items = res?.content ?? (Array.isArray(res) ? res : []);
        setRecommendedJobs(items.map(j => ({
          id: j.jobId || j.listingId,
          badge: j.employmentType || "Part-Time",
          time: j.createdAt ? new Date(j.createdAt).toLocaleDateString() : "Recently",
          title: j.title,
          company: j.companyName || "Employer",
          desc: j.description || "",
          location: j.location || "Sri Lanka",
          wage: `LKR ${(j.hourlyRate || j.wageAmount || 0).toLocaleString()}`,
          wageType: j.rateType || "Daily Wage",
        })));
      })
      .catch(() => { })
      .finally(() => setLoading(false));
  }, [user?.userId]);

  const fmtLKR = n => `LKR ${Number(n || 0).toLocaleString()}`;

  return (
    <>
      {/* ── Mini-dashboard strip ── */}
      <div style={{
        background: "linear-gradient(135deg, var(--pipeline-bg-from) 0%, var(--pipeline-bg-to) 100%)",
        padding: "36px 2rem",
        color: "white",
      }}>
        <div style={{ maxWidth: 1200, margin: "0 auto" }}>
          <div style={{ marginBottom: 4, fontSize: 13, color: "rgba(255,255,255,0.6)", fontWeight: 500 }}>
            👋 Good to see you,
          </div>
          <h1 style={{ fontSize: 26, fontWeight: 800, marginBottom: 28, letterSpacing: "-0.5px" }}>
            {firstName} — Here's your pipeline
          </h1>

          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(180px, 1fr))", gap: 16 }}>
            {[
              { label: "Active Orders", value: dashStats?.activeOrders ?? "—", icon: "📋", accent: C.teal },
              { label: "Pending Earnings", value: dashStats ? fmtLKR(dashStats.pendingEarnings) : "—", icon: "💰", accent: C.green },
              { label: "Total Earned", value: dashStats ? fmtLKR(dashStats.totalEarnings) : "—", icon: "🏦", accent: "#A78BFA" },
              { label: "Completed Orders", value: dashStats?.completedOrders ?? "—", icon: "✅", accent: "#FB923C" },
            ].map(({ label, value, icon, accent }) => (
              <div key={label} style={{
                background: "rgba(255,255,255,0.08)", borderRadius: 12,
                padding: "18px 20px", border: "1px solid rgba(255,255,255,0.1)",
                cursor: "pointer", transition: "background 0.15s",
              }}
                onClick={() => setPage("dashboard")}
                onMouseOver={e => e.currentTarget.style.background = "rgba(255,255,255,0.14)"}
                onMouseOut={e => e.currentTarget.style.background = "rgba(255,255,255,0.08)"}
              >
                <div style={{ fontSize: 22, marginBottom: 8 }}>{icon}</div>
                <div style={{ fontSize: 22, fontWeight: 800, color: accent, marginBottom: 2 }}>{loading ? "…" : value}</div>
                <div style={{ fontSize: 12, color: "rgba(255,255,255,0.55)" }}>{label}</div>
              </div>
            ))}
          </div>

          <div style={{ display: "flex", gap: 10, marginTop: 20 }}>
            <button onClick={() => setPage("dashboard")}
              style={{ padding: "10px 20px", borderRadius: 9, border: "1.5px solid rgba(255,255,255,0.3)", background: "rgba(255,255,255,0.1)", color: "white", fontSize: 13, fontWeight: 600, cursor: "pointer", fontFamily: "inherit" }}>
              📊 Full Dashboard →
            </button>
            <button onClick={() => setPage("create-gig")}
              style={{ padding: "10px 20px", borderRadius: 9, border: "none", background: C.teal, color: "white", fontSize: 13, fontWeight: 600, cursor: "pointer", fontFamily: "inherit" }}>
              ➕ Create New Gig
            </button>
          </div>
        </div>
      </div>

      {/* ── Gig Performance Nudge ── */}
      {gigPerf.length > 0 && (
        <div style={{ background: C.blueLight, borderBottom: `1px solid #C7D2FE` }}>
          <div style={{ maxWidth: 1200, margin: "0 auto", padding: "16px 2rem" }}>
            <div style={{ fontSize: 13, fontWeight: 600, color: C.blue, marginBottom: 10 }}>
              💡 Gig Performance Snapshot
            </div>
            <div style={{ display: "flex", gap: 12, flexWrap: "wrap" }}>
              {gigPerf.map(g => (
                <div key={g.id} style={{
                  background: "var(--surface)", border: `1px solid #C7D2FE`, borderRadius: 10,
                  padding: "12px 16px", flex: "1 1 260px", display: "flex", justifyContent: "space-between", alignItems: "center",
                }}>
                  <div>
                    <div style={{ fontSize: 13, fontWeight: 600, color: C.text, marginBottom: 2 }}>{g.title}</div>
                    <div style={{ fontSize: 12, color: C.textMuted }}>
                      {g.impressions} impressions this week · {g.orders} orders
                    </div>
                  </div>
                  {g.impressions < 20 && (
                    <span style={{
                      fontSize: 11, fontWeight: 600, background: "#FEF3C7", color: "#92400E",
                      borderRadius: 100, padding: "4px 10px", whiteSpace: "nowrap", marginLeft: 12,
                    }}>Try updating tags</span>
                  )}
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* ── Recommended Jobs Feed ── */}
      <div style={{ background: "var(--jobs-section-bg)" }}>
        <div className="section">
          <div className="section-row">
            <div>
              <div className="section-title">Jobs Recommended for You</div>
              <div className="section-sub">Matched to your skills and category</div>
            </div>
            <button className="view-all-btn" style={{ background: C.green, color: "white" }}
              onClick={() => { setSearchQ(""); setTab("jobs"); setPage("browse"); }}>
              Browse All Jobs →
            </button>
          </div>

          {/* Job search bar */}
          <div style={{
            background: "var(--surface)", borderRadius: 12, boxShadow: "0 2px 12px rgba(0,0,0,0.06)",
            padding: "8px 12px", display: "flex", alignItems: "center", gap: 8, marginBottom: 24,
            border: "1px solid var(--border)",
          }}>
            <span style={{ color: "#9CA3AF", fontSize: 16 }}>🔍</span>
            <input
              className="search-input"
              placeholder='Search for jobs… (e.g., "Tutor", "Data Entry")'
              value={searchQ}
              onChange={e => setSearchQ(e.target.value)}
              onKeyDown={e => { if (e.key === "Enter") { setTab("jobs"); setPage("browse"); } }}
            />
            <button className="search-btn" style={{ background: C.green }}
              onClick={() => { setTab("jobs"); setPage("browse"); }}>Search</button>
          </div>

          {loading ? (
            <div style={{ textAlign: "center", padding: "48px 0", color: C.textMuted, fontSize: 14 }}>
              Loading opportunities…
            </div>
          ) : (
            <div className="job-grid">
              {recommendedJobs.map(job => (
                <JobCard key={job.id} job={job}
                  onApply={() => { setSelectedJobId(job.id); setPage("job-detail"); }}
                  onView={() => { setSelectedJobId(job.id); setPage("job-detail"); }}
                />
              ))}
            </div>
          )}
        </div>
      </div>

      {/* ── Category shortcuts for browsing gigs (so freelancers can benchmark) ── */}
      <div className="section">
        <div className="section-head">
          <div className="section-title">Explore the Marketplace</div>
          <div className="section-sub">See what services others are offering — benchmark & get ideas</div>
        </div>
        <div className="categories-grid">
          {GIG_CATEGORIES.map(cat => (
            <div className="cat-card" key={cat.name} role="button" tabIndex={0}
              onClick={() => { setSearchQ(cat.name); setTab("gigs"); setPage("browse"); }}
              onKeyDown={e => { if (e.key === "Enter") { setSearchQ(cat.name); setTab("gigs"); setPage("browse"); } }}
            >
              <div className="cat-emoji">{cat.emoji}</div>
              <div className="cat-name">{cat.name}</div>
              <div className="cat-count">{cat.count} gigs</div>
            </div>
          ))}
        </div>
      </div>
    </>
  );
};


// ─── Browse ───────────────────────────────────────────────────────────────────
const Browse = ({
  tab, setTab, searchQ, setSearchQ,
  filterCat, setFilterCat, filterSort, setFilterSort,
  filterPrice, setFilterPrice, filterDelivery, setFilterDelivery,
  filterJobType, setFilterJobType, filterJobLoc, setFilterJobLoc, filterJobPay, setFilterJobPay,
  savedGigs, setSavedGigs, savedJobs, setSavedJobs,
  user, setAuthMode, setAuthModal,
  setSelectedGigId, setSelectedJobId, setPage, showToast,
}) => {
  const switchTab = newTab => { setTab(newTab); setSearchQ(""); setFilterCat(""); setFilterJobType(""); setFilterJobLoc(""); setFilterJobPay(""); };

  // ── API-backed listings state ──────────────────────────────────────────────
  const [apiGigs, setApiGigs] = useState(null);   // null = not yet loaded
  const [apiJobs, setApiJobs] = useState(null);
  const [listingsLoading, setListingsLoading] = useState(false);

  useEffect(() => {
    if (tab !== "gigs") return;
    setListingsLoading(true);
    const params = new URLSearchParams({ page: 0, size: 40 });
    if (filterCat) params.set("category", filterCat);
    if (searchQ) params.set("keyword", searchQ);
    apiFetch(`/listings/gigs?${params.toString()}`)
      .then(res => {
        const items = res?.content ?? (Array.isArray(res) ? res : []);
        const mapped = items.map(g => {
          // Map seller from the nested seller object returned by the API
          const seller = g.seller || {};
          const sellerName = seller.displayName ||
            (seller.firstName ? `${seller.firstName} ${seller.lastName || ""}`.trim() : null) ||
            g.sellerName || g.sellerDisplayName || "Freelancer";
          return {
            id: g.gigId || g.listingId,
            backendId: g.gigId || g.listingId,
            title: g.title,
            category: g.category || "General",
            seller: sellerName,
            initials: sellerName[0].toUpperCase(),
            price: g.basePrice || g.startingPrice || 0,
            delivery: g.deliveryDays || 3,
            rating: seller.averageRating || g.averageRating || 0,
            reviews: seller.totalReviews || g.totalReviews || 0,
            img: g.thumbnailUrl || g.imageUrl || GIG_IMAGES[g.gigId?.charCodeAt(0) % GIG_IMAGES.length || 0],
          };
        });
        setApiGigs(mapped.length > 0 ? mapped : SAMPLE_GIGS);
      })
      .catch(() => setApiGigs(SAMPLE_GIGS))
      .finally(() => setListingsLoading(false));
  }, [tab, filterCat, searchQ]);
  useEffect(() => {
    if (tab !== "jobs") return;
    setListingsLoading(true);
    const params = new URLSearchParams({ page: 0, size: 40 });
    if (searchQ) params.set("keyword", searchQ);
    if (filterJobLoc) params.set("location", filterJobLoc);
    apiFetch(`/listings/jobs?${params.toString()}`)
      .then(res => {
        const items = res?.content ?? (Array.isArray(res) ? res : []);
        const mappedJobs = items.map(j => ({
          id: j.jobId || j.listingId, backendId: j.jobId || j.listingId, title: j.title,
          company: j.companyName || j.company || "Employer",
          location: j.location || "Sri Lanka",
          badge: j.employmentType || j.jobType || "Part-Time",
          wage: `LKR ${(j.hourlyRate || j.salaryMin || j.wageAmount || 0).toLocaleString()}`,
          wageType: j.rateType || j.wageType || "Daily Wage",
          time: "Recently", desc: j.description || "",
        }));
        setApiJobs(mappedJobs.length > 0 ? mappedJobs : SAMPLE_JOBS);
      })
      .catch(() => setApiJobs(SAMPLE_JOBS))
      .finally(() => setListingsLoading(false));
  }, [tab, filterJobLoc, searchQ]); // eslint-disable-line

  // Use API data if available and non-empty, otherwise show sample data
  const sourceGigs = (apiGigs && apiGigs.length > 0) ? apiGigs : SAMPLE_GIGS;
  const sourceJobs = (apiJobs && apiJobs.length > 0) ? apiJobs : SAMPLE_JOBS;

  const filteredGigs = sourceGigs.filter(g => {
    const matchSearch = !searchQ || g.title.toLowerCase().includes(searchQ.toLowerCase()) || g.category.toLowerCase().includes(searchQ.toLowerCase());
    const matchCat = !filterCat || g.category === filterCat;
    const matchPrice = !filterPrice || (filterPrice === "under2k" && g.price < 2000) || (filterPrice === "2k-10k" && g.price >= 2000 && g.price <= 10000) || (filterPrice === "over10k" && g.price > 10000);
    const matchDel = !filterDelivery || (filterDelivery === "1" && g.delivery <= 1) || (filterDelivery === "3" && g.delivery <= 3) || (filterDelivery === "7" && g.delivery <= 7);
    return matchSearch && matchCat && matchPrice && matchDel;
  }).sort((a, b) => filterSort === "price-asc" ? a.price - b.price : filterSort === "price-desc" ? b.price - a.price : filterSort === "rating" ? b.rating - a.rating : b.id - a.id);

  const filteredJobs = sourceJobs.filter(j => {
    const matchSearch = !searchQ || j.title.toLowerCase().includes(searchQ.toLowerCase()) || j.company.toLowerCase().includes(searchQ.toLowerCase());
    const matchType = !filterJobType || j.badge === filterJobType;
    const matchLoc = !filterJobLoc || j.location.includes(filterJobLoc);
    const w = parseInt(String(j.wage).replace(/\D/g, ""));
    const matchPay = !filterJobPay || (filterJobPay === "under1500" && w < 1500) || (filterJobPay === "1500-3000" && w >= 1500 && w <= 3000) || (filterJobPay === "over3000" && w > 3000);
    return matchSearch && matchType && matchLoc && matchPay;
  });

  return (
    <>
      <div className="page-header">
        <div className="page-header-inner">
          <div className="tabs">
            <button className={`tab ${tab === "gigs" ? "active" : ""}`} onClick={() => switchTab("gigs")}>🎯 Services</button>
            <button className={`tab ${tab === "jobs" ? "active" : ""}`} onClick={() => switchTab("jobs")}>💼 Part-Time Jobs</button>
          </div>
          <div className="page-header-title">{tab === "gigs" ? "Browse Freelance Services" : "Find Part-Time Jobs"}</div>
          <div className="page-header-sub">{tab === "gigs" ? `${filteredGigs.length} services found` : `${filteredJobs.length} jobs found`}</div>
        </div>
      </div>
      <div className="section" style={{ paddingTop: 28 }}>
        <div style={{ background: "var(--surface)", border: "1.5px solid var(--border)", borderRadius: 12, padding: "12px 16px", display: "flex", alignItems: "center", gap: 8, marginBottom: 16 }}>
          <span style={{ color: "#9CA3AF", fontSize: 18 }}>🔍</span>
          <input
            id="browse-search"
            style={{ flex: 1, border: "none", outline: "none", fontSize: 15, fontFamily: "inherit", background: "transparent", color: "var(--text)" }}
            placeholder={tab === "gigs" ? "Search gigs..." : "Search jobs, companies..."}
            value={searchQ}
            onChange={e => setSearchQ(e.target.value)}
          />
          {searchQ && <button onClick={() => setSearchQ("")} style={{ background: "none", border: "none", cursor: "pointer", color: "#9CA3AF", fontSize: 18 }}>✕</button>}
        </div>
        <div className="filter-bar">
          {tab === "gigs" ? (
            <>
              <select className="filter-select" value={filterCat} onChange={e => setFilterCat(e.target.value)}>
                <option value="">All Categories</option>
                {GIG_CATEGORIES.map(c => <option key={c.name} value={c.name}>{c.name}</option>)}
              </select>
              <select className="filter-select" value={filterSort} onChange={e => setFilterSort(e.target.value)}>
                <option value="newest">Sort: Newest</option>
                <option value="price-asc">Price ↑</option>
                <option value="price-desc">Price ↓</option>
                <option value="rating">Top Rated</option>
              </select>
              <select className="filter-select" value={filterPrice} onChange={e => setFilterPrice(e.target.value)}>
                <option value="">Any Price</option>
                <option value="under2k">Under LKR 2,000</option>
                <option value="2k-10k">LKR 2,000 – 10,000</option>
                <option value="over10k">LKR 10,000+</option>
              </select>
              <select className="filter-select" value={filterDelivery} onChange={e => setFilterDelivery(e.target.value)}>
                <option value="">Any Delivery</option>
                <option value="1">1 Day</option>
                <option value="3">Up to 3 Days</option>
                <option value="7">Up to 7 Days</option>
              </select>
              {(filterCat || filterPrice || filterDelivery || filterSort !== "newest") && (
                <button onClick={() => { setFilterCat(""); setFilterSort("newest"); setFilterPrice(""); setFilterDelivery(""); }} style={{ padding: "8px 14px", borderRadius: 8, border: "1px solid #FEE2E2", background: "#FEF2F2", color: "#EF4444", fontSize: 13, fontWeight: 500, cursor: "pointer", fontFamily: "inherit" }}>Clear filters ✕</button>
              )}
            </>
          ) : (
            <>
              <select className="filter-select" value={filterJobType} onChange={e => setFilterJobType(e.target.value)}>
                <option value="">All Types</option>
                <option value="Part-Time">Part-Time</option>
                <option value="Remote">Remote</option>
                <option value="One-time">One-time</option>
              </select>
              <select className="filter-select" value={filterJobLoc} onChange={e => setFilterJobLoc(e.target.value)}>
                <option value="">All Locations</option>
                <option value="Colombo">Colombo</option>
                <option value="Nugegoda">Nugegoda</option>
                <option value="Remote">Remote</option>
                <option value="Maharagama">Maharagama</option>
              </select>
              <select className="filter-select" value={filterJobPay} onChange={e => setFilterJobPay(e.target.value)}>
                <option value="">Any Pay</option>
                <option value="under1500">Under LKR 1,500</option>
                <option value="1500-3000">LKR 1,500 – 3,000</option>
                <option value="over3000">LKR 3,000+</option>
              </select>
              {(filterJobType || filterJobLoc || filterJobPay) && (
                <button onClick={() => { setFilterJobType(""); setFilterJobLoc(""); setFilterJobPay(""); }} style={{ padding: "8px 14px", borderRadius: 8, border: "1px solid #FEE2E2", background: "#FEF2F2", color: "#EF4444", fontSize: 13, fontWeight: 500, cursor: "pointer", fontFamily: "inherit" }}>Clear filters ✕</button>
              )}
            </>
          )}
        </div>
        {listingsLoading && (
          <div style={{ textAlign: "center", padding: "40px 0", color: C.gray, fontSize: 14 }}>
            <div style={{ fontSize: 28, marginBottom: 8 }}>⏳</div>Loading from backend…
          </div>
        )}
        {!listingsLoading && tab === "gigs" ? (
          filteredGigs.length > 0
            ? <div className="gig-grid">{filteredGigs.map(g => (
              <GigCard key={g.id} gig={g}
                onCardClick={id => { setSelectedGigId(id); setPage("gig-detail"); }}
                onSellerClick={id => { setSelectedGigId(id); setPage("seller-profile"); }}
                saved={savedGigs.includes(g.id)}
                onSave={id => setSavedGigs(prev => prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id])}
              />
            ))}</div>
            : <div style={{ textAlign: "center", padding: "60px 0", color: C.gray }}>
              <div style={{ fontSize: 40, marginBottom: 12 }}>🔍</div>
              <div style={{ fontWeight: 500 }}>No gigs found. Try adjusting your filters.</div>
              <button onClick={() => { setSearchQ(""); setFilterCat(""); setFilterPrice(""); setFilterDelivery(""); setFilterSort("newest"); }} style={{ marginTop: 16, padding: "9px 20px", borderRadius: 8, background: C.blue, color: "white", border: "none", cursor: "pointer", fontFamily: "inherit", fontWeight: 600 }}>Clear All</button>
            </div>
        ) : (
          filteredJobs.length > 0
            ? <div className="job-grid">{filteredJobs.map(j => (
              <JobCard key={j.id} job={j}
                onApply={() => { if (!user) { setAuthMode("login"); setAuthModal(true); } else showToast("Application submitted!"); }}
                onView={() => { setSelectedJobId(j.id); setPage("job-detail"); }}
                saved={savedJobs.includes(j.id)}
                onSave={id => setSavedJobs(prev => prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id])}
              />
            ))}</div>
            : <div style={{ textAlign: "center", padding: "60px 0", color: C.gray }}>
              <div style={{ fontSize: 40, marginBottom: 12 }}>💼</div>
              <div style={{ fontWeight: 500 }}>No jobs found. Try adjusting your filters.</div>
              <button onClick={() => { setSearchQ(""); setFilterJobType(""); setFilterJobLoc(""); setFilterJobPay(""); }} style={{ marginTop: 16, padding: "9px 20px", borderRadius: 8, background: C.green, color: "white", border: "none", cursor: "pointer", fontFamily: "inherit", fontWeight: 600 }}>Clear All</button>
            </div>
        )}
      </div>
    </>
  );
};

// ─── GigDetail ────────────────────────────────────────────────────────────────
const GigDetail = ({ selectedGigId, setPage, user, setAuthMode, setAuthModal, setDashTab, setMessageTarget, showToast }) => {
  const [gig, setGig] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!selectedGigId) return;
    setLoading(true);
    apiFetch(`/listings/gigs/${selectedGigId}`)
      .then(res => {
        // FIX: Extract name from the nested 'seller' object based on your JSON
        const sellerName = res.seller?.displayName ||
          (res.seller?.firstName ? `${res.seller.firstName} ${res.seller.lastName || ''}`.trim() : null) ||
          "Freelancer";

        setGig({
          id: res.gigId || res.listingId || res.id || selectedGigId,
          backendId: res.listingId || res.gigId || res.id || null,  // POST /orders expects listingId, not gigId
          status: res.status || "ACTIVE",
          title: res.title,
          category: res.category || "General",
          seller: sellerName,
          sellerId: res.seller?.userId || res.seller?.id || res.sellerId || null,
          initials: sellerName[0]?.toUpperCase() || "F",
          price: res.basePrice || res.startingPrice || 0,
          delivery: res.deliveryDays || 3,
          rating: res.seller?.averageRating || res.avgRating || 0,
          reviews: res.seller?.totalReviews || res.reviewCount || 0,
          img: res.imageUrl || res.thumbnailUrl || GIG_IMAGES[0],
          description: res.description || "",
        });
      })
      .catch((err) => console.error(err))
      .finally(() => setLoading(false));
  }, [selectedGigId]);

  if (!gig) return null;
  if (loading) return <div style={{ textAlign: "center", padding: "80px 0", color: C.gray }}>⏳ Loading gig…</div>;

  return (
    <div style={{ maxWidth: 1000, margin: "32px auto", padding: "0 2rem", overflowX: "hidden" }}>
      <button style={{ background: "none", border: "none", cursor: "pointer", color: C.blue, fontWeight: 600, marginBottom: 20, fontSize: 14, fontFamily: "inherit" }} onClick={() => setPage("browse")}>← Back to Gigs</button>

      <div style={{ background: "var(--surface)", borderRadius: 16, border: "1px solid var(--border)", overflow: "hidden" }}>
        <img src={gig.img} alt={gig.title} style={{ width: "100%", height: 340, objectFit: "cover" }} />

        <div style={{ padding: 32 }}>
          <span style={{ background: C.blueLight, color: C.blue, fontSize: 12, fontWeight: 600, padding: "4px 10px", borderRadius: 6 }}>{gig.category}</span>

          <h1 style={{ fontSize: 22, fontWeight: 700, margin: "12px 0 16px", color: C.text, overflowWrap: "anywhere", wordBreak: "break-word" }}>
            {gig.title}
          </h1>

          <div className="gig-seller" style={{ marginBottom: 24, display: "flex", alignItems: "center", gap: 8, flexWrap: "wrap" }}>
            <div className="avatar" style={{ background: AVATAR_COLORS[gig.id % AVATAR_COLORS.length], width: 38, height: 38, fontSize: 15 }}>{gig.initials}</div>
            <span style={{ fontWeight: 600, overflowWrap: "anywhere", wordBreak: "break-word", maxWidth: "100%" }}>
              {gig.seller}
            </span>
            <span className="verified-icon">✔</span>
            <span style={{ fontSize: 13, color: C.gray, whiteSpace: "nowrap" }}>Verified Freelancer</span>
          </div>

          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(280px, 1fr))", gap: 28 }}>
            <div style={{ minWidth: 0 }}>
              <h3 style={{ fontWeight: 600, marginBottom: 10 }}>About this gig</h3>
              <p style={{ color: "var(--text-secondary)", lineHeight: 1.8, marginBottom: 16, overflowWrap: "anywhere", wordBreak: "break-word", whiteSpace: "pre-wrap" }}>
                {gig.description || `Professional ${gig.category.toLowerCase()} service by a verified student. All orders protected by the Freelancelk escrow system.`}
              </p>
              <div className="gig-rating"><span className="star">★</span> <strong>{gig.rating}</strong> <span style={{ color: C.gray }}>({gig.reviews} reviews)</span></div>
            </div>

            <div style={{ background: "var(--bg-muted)", borderRadius: 12, padding: 20, border: "1px solid var(--border)", height: "fit-content" }}>
              <div style={{ fontSize: 11, color: C.gray }}>Starting at</div>
              <div style={{ fontSize: 28, fontWeight: 800, color: C.blue, marginBottom: 16 }}>LKR {gig.price.toLocaleString()}</div>
              {[`${gig.delivery}-day delivery`, "1 revision included", "Escrow protected"].map(f => (
                <div key={f} style={{ display: "flex", gap: 8, fontSize: 13, marginBottom: 8, color: "var(--text-secondary)" }}>✓ {f}</div>
              ))}
              <button className="btn-apply" style={{ marginTop: 16, background: C.blue }} onClick={async () => {
                if (!user) { setAuthMode("login"); setAuthModal(true); return; }
                if (user.role === "FREELANCER") { showToast("Only clients can place orders.", "error"); return; }
                const listingId = gig.backendId || gig.id;
                if (!listingId) { showToast("Could not identify this gig. Please go back and try again.", "error"); return; }
                if (gig.status && gig.status !== "ACTIVE") {
                  showToast(`This gig is not available for orders (status: ${gig.status}).`, "error"); return;
                }
                const buyerMessage = window.prompt("Add a message to the seller (optional):", "") ?? "";
                try {
                  await apiFetch("/orders", { method: "POST", body: JSON.stringify({ listingId, buyerMessage: buyerMessage || undefined, paymentMethod: "WALLET" }) });
                  showToast("Order placed! ✅"); setDashTab("Orders"); setPage("dashboard");
                } catch (e) { showToast("Order failed: " + e.message, "error"); }
              }}>Order Now</button>
              <button style={{ width: "100%", marginTop: 8, padding: 11, borderRadius: 8, fontSize: 14, fontWeight: 600, color: C.blue, background: C.blueLight, border: "none", cursor: "pointer", fontFamily: "inherit" }} onClick={async () => {
                if (!user) { setAuthModal(true); return; }
                if (gig.sellerId) {
                  try {
                    const conv = await apiFetch(`/chat/conversations/${gig.sellerId}`, { method: "POST" });
                    const convId = conv?.conversationId || conv?.id;
                    if (convId) setMessageTarget({ conversationId: convId, otherUserId: gig.sellerId, name: gig.seller });
                  } catch (_) { }
                }
                setDashTab("Messages"); setPage("dashboard");
              }}>💬 Message Seller</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

// ─── JobDetail ────────────────────────────────────────────────────────────────
const JobDetail = ({ selectedJobId, savedJobs, setSavedJobs, setPage, user, setAuthMode, setAuthModal, showToast, setDashTab, setMessageTarget }) => {
  const [job, setJob] = useState(null);
  const [loading, setLoading] = useState(true);
  const [applying, setApplying] = useState(false);
  const [myApplication, setMyApplication] = useState(null); // null | { applicationId, status }
  const [applicantsModal, setApplicantsModal] = useState(false);
  const [applicants, setApplicants] = useState([]);
  const [applicantsLoading, setApplicantsLoading] = useState(false);
  const [coverLetter, setCoverLetter] = useState("");
  const [applyModal, setApplyModal] = useState(false);

  useEffect(() => {
    if (!selectedJobId) return;
    setLoading(true);
    apiFetch(`/listings/jobs/${selectedJobId}`)
      .then(res => {
        const listingId = res.listingId || res.id || selectedJobId;
        const jobId = listingId; // /jobs/{listingId}/apply uses listingId, NOT res.jobId
        setJob({
          id: listingId,
          jobId,
          title: res.title || "Job Listing",
          company: res.seller?.displayName || res.clientName || res.companyName || "Client",
          clientUserId: res.seller?.userId || res.clientId || res.postedBy || null,
          clientName: res.seller?.displayName || res.clientName || "Client",
          clientInitials: (res.seller?.displayName || res.clientName || "C")[0].toUpperCase(),
          memberSince: res.seller?.memberSince ? new Date(res.seller.memberSince).getFullYear() : null,
          location: res.location || (res.isRemote ? "Remote" : "Sri Lanka"),
          badge: res.employmentType || res.jobType || "Part-Time",
          wage: `LKR ${(res.hourlyRate || res.monthlySalary || res.salaryMin || 0).toLocaleString()}`,
          wageType: res.hourlyRate ? "/ hour" : res.monthlySalary ? "/ month" : "negotiable",
          description: res.description || "",
          responsibilities: res.responsibilities || "",
          requirements: res.requirements || "",
          skills: res.requiredSkills || [],
          applicantCount: res.applicantCount || 0,
          status: res.status || "ACTIVE",
          isRemote: res.isRemote,
        });
      })
      .catch(() => showToast("Could not load job details.", "error"))
      .finally(() => setLoading(false));

    // If freelancer, check if they already applied
    if (user?.role === "FREELANCER") {
      apiFetch("/jobs/applications/my")
        .then(res => {
          const items = Array.isArray(res) ? res : (res?.content || []);
          const existing = items.find(a => (a.jobId || a.listingId) === selectedJobId);
          if (existing) setMyApplication({ applicationId: existing.applicationId || existing.id, status: existing.status });
        })
        .catch(() => { });
    }
  }, [selectedJobId]);

  const loadApplicants = async (jobId) => {
    setApplicantsLoading(true);
    setApplicantsModal(true);
    try {
      const res = await apiFetch(`/jobs/${jobId}/applicants`);
      const items = Array.isArray(res) ? res : (res?.content || []);
      setApplicants(items.map(a => ({
        id: a.applicationId || a.id,
        name: a.applicantName || a.freelancerName || "Freelancer",
        initials: (a.applicantName || a.freelancerName || "F")[0].toUpperCase(),
        status: a.status || "PENDING",
        coverLetter: a.coverLetter || a.message || "",
        appliedAt: a.appliedAt || a.createdAt ? new Date(a.appliedAt || a.createdAt).toLocaleDateString("en-LK", { month: "short", day: "numeric" }) : "—",
        rating: a.applicantRating || 0,
      })));
    } catch (e) {
      showToast("Could not load applicants: " + e.message, "error");
    } finally {
      setApplicantsLoading(false);
    }
  };

  const handleApply = async () => {
    if (!user) { setAuthMode("login"); setAuthModal(true); return; }
    setApplying(true);
    try {
      const res = await apiFetch(`/jobs/${job.jobId}/apply`, {
        method: "POST",
        body: coverLetter.trim() ? JSON.stringify({ coverLetter: coverLetter.trim() }) : undefined,
      });
      const appId = res?.applicationId || res?.id;
      setMyApplication({ applicationId: appId, status: "PENDING" });
      setApplyModal(false);
      setCoverLetter("");
      showToast("Application submitted! The employer will review it. ✅");
    } catch (e) {
      showToast("Apply failed: " + e.message, "error");
    } finally {
      setApplying(false);
    }
  };

  const handleWithdraw = async () => {
    if (!myApplication?.applicationId) return;
    if (!window.confirm("Withdraw your application?")) return;
    try {
      await apiFetch(`/jobs/applications/${myApplication.applicationId}/withdraw`, { method: "DELETE" });
      setMyApplication(null);
      showToast("Application withdrawn.");
    } catch (e) {
      showToast("Withdraw failed: " + e.message, "error");
    }
  };

  const handleApplicantAction = async (applicationId, action) => {
    const endpoint = action === "accept"
      ? `/jobs/applications/${applicationId}/accept`
      : `/jobs/applications/${applicationId}/reject`;
    try {
      await apiFetch(endpoint, { method: "POST" });
      setApplicants(prev => prev.map(a => {
        if (a.id === applicationId) return { ...a, status: action === "accept" ? "ACCEPTED" : "REJECTED" };
        // auto-reject others when one is accepted
        if (action === "accept" && a.status === "PENDING") return { ...a, status: "REJECTED" };
        return a;
      }));
      showToast(action === "accept" ? "Applicant accepted! 🎉" : "Applicant rejected.");
    } catch (e) {
      showToast(`Action failed: ${e.message}`, "error");
    }
  };

  const appStatusStyle = (s) => {
    if (s === "ACCEPTED") return { background: "#DCFCE7", color: "#16A34A" };
    if (s === "REJECTED") return { background: "#FEF2F2", color: "#EF4444" };
    return { background: "#F3F4F6", color: "#6B7280" };
  };

  if (loading) return <div style={{ padding: 60, textAlign: "center", color: C.gray }}>Loading job…</div>;
  if (!job) return null;

  const isSaved = savedJobs.includes(selectedJobId);
  const isFreelancer = !user || user.role === "FREELANCER";
  const isClient = user?.role === "CLIENT";

  return (
    <>
      {/* Apply modal */}
      {applyModal && (
        <div className="modal-overlay" onClick={() => setApplyModal(false)}>
          <div className="modal" style={{ maxWidth: 500 }} onClick={e => e.stopPropagation()}>
            <button className="modal-close" onClick={() => setApplyModal(false)}>✕</button>
            <div className="modal-title">Apply for this Job</div>
            <div className="modal-sub">{job.title} · {job.company}</div>
            <div className="form-group" style={{ marginTop: 16 }}>
              <label className="form-label">Cover Letter <span style={{ color: C.gray, fontWeight: 400 }}>(optional)</span></label>
              <textarea
                className="form-input" rows={5}
                placeholder="Tell the employer why you're a great fit…"
                value={coverLetter}
                onChange={e => setCoverLetter(e.target.value)}
                style={{ resize: "vertical" }}
              />
              <div style={{ fontSize: 12, color: C.gray, marginTop: 4 }}>{coverLetter.length} characters</div>
            </div>
            <button className="modal-btn" onClick={handleApply} disabled={applying}>
              {applying ? "Submitting…" : "Submit Application"}
            </button>
          </div>
        </div>
      )}

      {/* Applicants modal (client only) */}
      {applicantsModal && (
        <div className="modal-overlay" onClick={() => setApplicantsModal(false)}>
          <div className="modal" style={{ maxWidth: 620, maxHeight: "85vh", overflowY: "auto" }} onClick={e => e.stopPropagation()}>
            <button className="modal-close" onClick={() => setApplicantsModal(false)}>✕</button>
            <div className="modal-title">Applicants</div>
            <div className="modal-sub">{job.title}</div>
            {applicantsLoading && <div style={{ padding: 32, textAlign: "center", color: C.gray }}>Loading applicants…</div>}
            {!applicantsLoading && applicants.length === 0 && (
              <div style={{ padding: 32, textAlign: "center", color: C.gray }}>No applications yet.</div>
            )}
            {applicants.map(a => (
              <div key={a.id} style={{ borderBottom: "1px solid #F3F4F6", padding: "16px 0" }}>
                <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: 8 }}>
                  <div className="avatar" style={{ background: C.blue, width: 38, height: 38, fontSize: 14 }}>{a.initials}</div>
                  <div style={{ flex: 1 }}>
                    <div style={{ fontWeight: 600, fontSize: 14 }}>{a.name}</div>
                    <div style={{ fontSize: 12, color: C.gray }}>Applied {a.appliedAt}{a.rating > 0 ? ` · ★ ${a.rating}` : ""}</div>
                  </div>
                  <span style={{ fontSize: 11, padding: "3px 10px", borderRadius: 100, fontWeight: 600, ...appStatusStyle(a.status) }}>{a.status}</span>
                </div>
                {a.coverLetter && (
                  <p style={{ fontSize: 13, color: "var(--text-secondary)", lineHeight: 1.6, background: "var(--bg-muted)", padding: "10px 12px", borderRadius: 8, marginBottom: 10 }}>
                    {a.coverLetter}
                  </p>
                )}
                {a.status === "PENDING" && (
                  <div style={{ display: "flex", gap: 8 }}>
                    <button
                      style={{ padding: "6px 16px", borderRadius: 6, border: "none", background: "#16A34A", color: "white", fontSize: 12, cursor: "pointer", fontFamily: "inherit", fontWeight: 600 }}
                      onClick={() => handleApplicantAction(a.id, "accept")}>
                      ✅ Accept
                    </button>
                    <button
                      style={{ padding: "6px 16px", borderRadius: 6, border: "1px solid #FEE2E2", background: "#FEF2F2", fontSize: 12, cursor: "pointer", color: "#EF4444", fontFamily: "inherit" }}
                      onClick={() => handleApplicantAction(a.id, "reject")}>
                      ❌ Reject
                    </button>
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      <div className="page-header">
        <div className="page-header-inner">
          <button style={{ background: "none", border: "none", cursor: "pointer", color: C.blue, fontWeight: 600, fontSize: 14, fontFamily: "inherit", marginBottom: 12 }} onClick={() => setPage("browse")}>← Back to Jobs</button>
          <div style={{ display: "flex", alignItems: "flex-start", justifyContent: "space-between", flexWrap: "wrap", gap: 16 }}>
            <div>
              <span className={`job-badge ${job.badge === "Remote" ? "remote" : ""}`} style={{ marginBottom: 10, display: "inline-block" }}>{job.badge}</span>
              <div className="page-header-title">{job.title}</div>
              <div style={{ display: "flex", gap: 16, marginTop: 8, flexWrap: "wrap" }}>
                <span className="job-loc">🏢 {job.company} <span className="verified-icon">✔</span></span>
                <span className="job-loc">📍 {job.location}</span>
                {job.isRemote && <span className="job-badge remote" style={{ fontSize: 11 }}>Remote</span>}
              </div>
            </div>
            <div style={{ textAlign: "right" }}>
              <div style={{ fontSize: 30, fontWeight: 800, color: C.green }}>{job.wage}</div>
              <div style={{ fontSize: 13, color: C.gray }}>{job.wageType}</div>
            </div>
          </div>
        </div>
      </div>

      <div className="section" style={{ maxWidth: 860 }}>
        <div style={{ display: "grid", gridTemplateColumns: "1fr 300px", gap: 28 }}>
          <div>
            <div style={{ background: "var(--surface)", border: "1px solid var(--border)", borderRadius: 14, padding: 24, marginBottom: 16 }}>
              <h3 style={{ fontWeight: 700, fontSize: 16, marginBottom: 14, color: C.navy }}>Job Description</h3>
              <p style={{ fontSize: 14, color: "var(--text-secondary)", lineHeight: 1.8, marginBottom: 16, whiteSpace: "pre-wrap" }}>{job.description || "No description provided."}</p>
              {job.responsibilities && <>
                <h4 style={{ fontWeight: 600, fontSize: 14, marginBottom: 10, color: C.text }}>Responsibilities</h4>
                <p style={{ fontSize: 14, color: "var(--text-secondary)", lineHeight: 1.8, whiteSpace: "pre-wrap" }}>{job.responsibilities}</p>
              </>}
            </div>
            {(job.requirements || job.skills?.length > 0) && (
              <div style={{ background: "var(--surface)", border: "1px solid var(--border)", borderRadius: 14, padding: 24 }}>
                <h3 style={{ fontWeight: 700, fontSize: 16, marginBottom: 14, color: C.navy }}>Requirements</h3>
                {job.requirements && <p style={{ fontSize: 14, color: "var(--text-secondary)", lineHeight: 1.8, marginBottom: 12, whiteSpace: "pre-wrap" }}>{job.requirements}</p>}
                {job.skills?.length > 0 && (
                  <div style={{ display: "flex", flexWrap: "wrap", gap: 8, marginTop: 8 }}>
                    {job.skills.map(s => (
                      <span key={s} style={{ padding: "4px 12px", background: C.blueLight, color: C.blue, borderRadius: 100, fontSize: 12, fontWeight: 500 }}>{s}</span>
                    ))}
                  </div>
                )}
              </div>
            )}
          </div>

          <div style={{ position: "sticky", top: 80, height: "fit-content" }}>

            {/* ── Posted by card ── */}
            <div style={{ background: "var(--surface)", border: "1px solid var(--border)", borderRadius: 14, padding: 20, marginBottom: 12 }}>
              <div style={{ fontSize: 12, fontWeight: 600, color: C.gray, textTransform: "uppercase", letterSpacing: "0.05em", marginBottom: 14 }}>Posted by</div>
              <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: 14 }}>
                <div style={{ width: 44, height: 44, borderRadius: "50%", background: C.blue, color: "white", fontSize: 18, fontWeight: 700, display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0 }}>{job.clientInitials}</div>
                <div>
                  <div style={{ fontWeight: 600, fontSize: 14, color: C.navy }}>{job.clientName}</div>
                  <div style={{ fontSize: 12, color: C.gray }}>
                    Client{job.memberSince ? ` · Member since ${job.memberSince}` : ""}
                  </div>
                </div>
              </div>
              {isFreelancer && (
                <button
                  style={{ width: "100%", padding: "9px", borderRadius: 8, border: `1px solid ${C.blue}`, background: C.blueLight, cursor: "pointer", fontFamily: "inherit", fontWeight: 600, fontSize: 13, color: C.blue }}
                  onClick={async () => {
                    if (!user) { setAuthMode("login"); setAuthModal(true); return; }
                    if (job.clientUserId) {
                      try {
                        const conv = await apiFetch(`/chat/conversations/${job.clientUserId}`, { method: "POST" });
                        const convId = conv?.conversationId || conv?.id;
                        if (convId && setMessageTarget) setMessageTarget({ conversationId: convId, otherUserId: job.clientUserId, name: job.clientName });
                      } catch (_) { }
                    }
                    setDashTab("Messages"); setPage("dashboard");
                  }}>
                  💬 Message Client
                </button>
              )}
            </div>

            <div style={{ background: "var(--surface)", border: "1px solid var(--border)", borderRadius: 14, padding: 20, marginBottom: 12 }}>
              <div style={{ fontSize: 30, fontWeight: 800, color: C.green, marginBottom: 4 }}>{job.wage}</div>
              <div style={{ fontSize: 13, color: C.gray, marginBottom: 20 }}>{job.wageType}</div>
              {[
                ["📍", "Location", job.location],
                ["📅", "Type", job.badge],
                ["👥", "Applicants", `${job.applicantCount} applied`],
                ["📌", "Status", job.status],
              ].map(([icon, label, val]) => (
                <div key={label} style={{ display: "flex", justifyContent: "space-between", padding: "9px 0", borderBottom: "1px solid #F9FAFB", fontSize: 13 }}>
                  <span style={{ color: C.gray }}>{icon} {label}</span>
                  <span style={{ fontWeight: 500 }}>{val}</span>
                </div>
              ))}

              {/* ── Freelancer actions ── */}
              {isFreelancer && !myApplication && job.status === "ACTIVE" && (
                <button className="btn-apply" style={{ marginTop: 20 }} onClick={() => {
                  if (!user) { setAuthMode("login"); setAuthModal(true); return; }
                  setApplyModal(true);
                }}>
                  Apply Now
                </button>
              )}
              {isFreelancer && !myApplication && job.status !== "ACTIVE" && (
                <div style={{ marginTop: 20, padding: "10px 14px", borderRadius: 8, textAlign: "center", fontWeight: 500, fontSize: 13, background: "#F3F4F6", color: C.gray }}>
                  {job.status === "DRAFT" ? "🔒 This job is not published yet" : job.status === "CLOSED" ? "🔒 This job is closed" : `Applications not open (${job.status})`}
                </div>
              )}
              {isFreelancer && myApplication && (
                <>
                  <div style={{ marginTop: 20, padding: "10px 14px", borderRadius: 8, textAlign: "center", fontWeight: 600, fontSize: 13, ...appStatusStyle(myApplication.status) }}>
                    {myApplication.status === "PENDING" && "⏳ Application Pending"}
                    {myApplication.status === "ACCEPTED" && "🎉 Application Accepted!"}
                    {myApplication.status === "REJECTED" && "❌ Application Rejected"}
                  </div>
                  {myApplication.status === "PENDING" && (
                    <button
                      style={{ width: "100%", marginTop: 8, padding: 10, borderRadius: 8, border: "1px solid #FEE2E2", background: "#FEF2F2", cursor: "pointer", fontFamily: "inherit", fontWeight: 500, fontSize: 13, color: "#EF4444" }}
                      onClick={handleWithdraw}>
                      Withdraw Application
                    </button>
                  )}
                </>
              )}

              {/* ── Client actions ── */}
              {isClient && (
                <button
                  className="btn-apply"
                  style={{ marginTop: 20, background: C.blue }}
                  onClick={() => loadApplicants(job.jobId)}>
                  👥 View Applicants ({job.applicantCount})
                </button>
              )}

              {/* Save button (freelancers only) */}
              {isFreelancer && (
                <button
                  style={{ width: "100%", marginTop: 8, padding: "10px", borderRadius: 8, border: "1px solid var(--border)", background: "var(--surface)", cursor: "pointer", fontFamily: "inherit", fontWeight: 500, fontSize: 13, color: isSaved ? C.blue : C.gray }}
                  onClick={() => {
                    if (!user) { setAuthMode("login"); setAuthModal(true); return; }
                    setSavedJobs(prev => isSaved ? prev.filter(id => id !== selectedJobId) : [...prev, selectedJobId]);
                  }}>
                  {isSaved ? "🔖 Saved" : "🔖 Save Job"}
                </button>
              )}
            </div>
            <div style={{ background: C.blueLight, borderRadius: 12, padding: 16, fontSize: 13, color: C.blue, lineHeight: 1.6 }}>
              🛡️ This employer is <strong>verified</strong> on Freelancelk. Safe to apply.
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

// ─── SellerProfile ────────────────────────────────────────────────────────────
const SellerProfile = ({ selectedGigId, setSelectedGigId, setPage, user, setAuthMode, setAuthModal, setDashTab, trustScore, trustData, setMessageTarget }) => {
  const gig = { seller: "—", initials: "?", id: 0, rating: 0, reviews: 0, delivery: 0, category: "—" };
  return (
    <>
      <div style={{ background: "linear-gradient(135deg, #EEF1FF 0%, #E0F2FE 100%)", padding: "40px 2rem" }}>
        <div style={{ maxWidth: 900, margin: "0 auto", display: "flex", gap: 28, alignItems: "flex-start" }}>
          <div style={{ width: 90, height: 90, borderRadius: "50%", background: AVATAR_COLORS[gig.id % AVATAR_COLORS.length], color: "white", fontSize: 32, fontWeight: 700, display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0, border: "4px solid white", boxShadow: "0 4px 16px rgba(0,0,0,0.1)" }}>{gig.initials}</div>
          <div style={{ flex: 1 }}>
            <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 6 }}>
              <h1 style={{ fontSize: 24, fontWeight: 700, color: C.navy }}>{gig.seller}</h1>
              <span style={{ fontSize: 12, background: "#DCFCE7", color: C.green, padding: "3px 10px", borderRadius: 100, fontWeight: 600 }}>✔ Verified</span>
            </div>
            <div style={{ fontSize: 14, color: C.gray, marginBottom: 12 }}>Undergraduate · University of Sri Jayewardenepura · {gig.category} Specialist</div>
            <div style={{ display: "flex", gap: 20, fontSize: 13 }}>
              <span><strong style={{ color: C.navy }}>★ {gig.rating}</strong> <span style={{ color: C.gray }}>({gig.reviews} reviews)</span></span>
              <span><strong style={{ color: C.navy }}>12</strong> <span style={{ color: C.gray }}>orders completed</span></span>
              <span><strong style={{ color: C.navy }}>Trust: {trustScore}</strong> <span style={{ color: C.gray }}>/100</span></span>
              <span><strong style={{ color: C.navy }}>{gig.delivery} days</strong> <span style={{ color: C.gray }}>avg. delivery</span></span>
            </div>
          </div>
          <button className="btn-apply" style={{ width: "auto", padding: "10px 24px", background: C.blue, whiteSpace: "nowrap" }} onClick={async () => {
            if (!user) { setAuthModal(true); return; }
            if (gig.sellerId) {
              try {
                const conv = await apiFetch(`/chat/conversations/${gig.sellerId}`, { method: "POST" });
                const convId = conv?.conversationId || conv?.id;
                if (convId) setMessageTarget({ conversationId: convId, otherUserId: gig.sellerId, name: gig.seller });
              } catch (_) { }
            }
            setDashTab("Messages"); setPage("dashboard");
          }}>Message</button>
        </div>
      </div>
      <div style={{ maxWidth: 900, margin: "32px auto", padding: "0 2rem", display: "grid", gridTemplateColumns: "1fr 280px", gap: 28 }}>
        <div>
          <div style={{ background: "var(--surface)", border: "1px solid var(--border)", borderRadius: 14, padding: 24, marginBottom: 20 }}>
            <h3 style={{ fontWeight: 700, marginBottom: 12, color: C.navy }}>About</h3>
            <p style={{ fontSize: 14, color: "var(--text-secondary)", lineHeight: 1.8 }}>Hi! I'm a 3rd year IT undergraduate at the University of Sri Jayewardenepura. I specialize in {gig.category.toLowerCase()} and have been freelancing for 2+ years. I deliver high-quality work on time, every time.</p>
          </div>
          <div style={{ background: "var(--surface)", border: "1px solid var(--border)", borderRadius: 14, padding: 24, marginBottom: 20 }}>
            <h3 style={{ fontWeight: 700, marginBottom: 16, color: C.navy }}>Active Gigs</h3>
            <div className="gig-grid" style={{ gridTemplateColumns: "1fr 1fr" }}>
              {[].map(g => (
                <GigCard key={g.id} gig={g}
                  onCardClick={id => { setSelectedGigId(id); setPage("gig-detail"); }}
                  onSellerClick={id => { setSelectedGigId(id); setPage("seller-profile"); }}
                />
              ))}
            </div>
          </div>
          <div style={{ background: "var(--surface)", border: "1px solid var(--border)", borderRadius: 14, padding: 24 }}>
            <h3 style={{ fontWeight: 700, marginBottom: 16, color: C.navy }}>Reviews ({gig.reviews})</h3>
            {[
              { name: "Priya T.", initials: "P", rating: 5, text: "Excellent work! Delivered exactly what I needed, ahead of schedule.", date: "2 weeks ago", replied: false },
              { name: "Mohamed F.", initials: "M", rating: 5, text: "Super professional and easy to work with. Will definitely hire again.", date: "1 month ago", replied: true },
              { name: "Dilani S.", initials: "D", rating: 4, text: "Good quality work. Communication could be a bit faster but overall satisfied.", date: "2 months ago", replied: false },
            ].map(r => (
              <div key={r.name} style={{ borderBottom: "1px solid #F3F4F6", paddingBottom: 16, marginBottom: 16 }}>
                <div style={{ display: "flex", gap: 10, marginBottom: 8, alignItems: "center" }}>
                  <div className="avatar" style={{ background: C.blue, width: 34, height: 34, fontSize: 13 }}>{r.initials}</div>
                  <div style={{ flex: 1 }}>
                    <div style={{ fontSize: 13, fontWeight: 600 }}>{r.name}</div>
                    <div style={{ fontSize: 12, color: "#FBBF24" }}>{"★".repeat(r.rating)}{"☆".repeat(5 - r.rating)}</div>
                  </div>
                  <div style={{ fontSize: 12, color: C.gray }}>{r.date}</div>
                </div>
                <p style={{ fontSize: 13, color: "var(--text-secondary)", lineHeight: 1.6 }}>{r.text}</p>
                {r.replied
                  ? <div style={{ marginTop: 10, padding: "10px 14px", background: "var(--bg-muted)", borderRadius: 8, borderLeft: "3px solid #2B3FBF", fontSize: 13, color: "var(--text-secondary)" }}>
                    <strong style={{ fontSize: 12, color: C.blue }}>Seller's reply:</strong> Thank you so much for your kind words! It was a pleasure working with you. 😊
                  </div>
                  : user && <button style={{ marginTop: 8, fontSize: 12, color: C.blue, background: "none", border: "1px solid var(--border)", borderRadius: 6, padding: "4px 12px", cursor: "pointer", fontFamily: "inherit", fontWeight: 500 }}
                    onClick={() => showToast("Reply feature coming soon!")}>💬 Reply to review</button>
                }
              </div>
            ))}
          </div>
        </div>
        <div>
          <div style={{ background: "var(--surface)", border: "1px solid var(--border)", borderRadius: 14, padding: 20, marginBottom: 16 }}>
            <div style={{ fontSize: 14, fontWeight: 700, marginBottom: 14, color: C.navy }}>Trust Score Breakdown</div>
            {[
              { label: "Identity Verified", val: trustData.idVerification, color: C.green },
              { label: "Gig Ratings", val: trustData.gigRating, color: C.blue },
              { label: "Job Ratings", val: trustData.jobRating, color: C.teal },
              { label: "On-time Delivery", val: trustData.onTimeDelivery, color: "#7C3AED" },
            ].map(item => (
              <div key={item.label} style={{ marginBottom: 12 }}>
                <div style={{ display: "flex", justifyContent: "space-between", fontSize: 12, marginBottom: 4 }}>
                  <span style={{ color: C.gray }}>{item.label}</span>
                  <span style={{ fontWeight: 600 }}>{item.val}%</span>
                </div>
                <div style={{ background: "#F3F4F6", borderRadius: 100, height: 6 }}>
                  <div style={{ background: item.color, borderRadius: 100, height: 6, width: `${item.val}%`, transition: "width 0.5s" }} />
                </div>
              </div>
            ))}
            <div style={{ marginTop: 16, padding: "10px 14px", background: "#FEF9C3", borderRadius: 8, fontSize: 12, color: "#854D0E", fontWeight: 500, textAlign: "center" }}>
              ⭐ Overall Trust Score: {trustScore} / 100
            </div>
          </div>
          <div style={{ background: "var(--surface)", border: "1px solid var(--border)", borderRadius: 14, padding: 20 }}>
            <div style={{ fontSize: 14, fontWeight: 700, marginBottom: 14, color: C.navy }}>Skills</div>
            {["Graphic Design", "Adobe Illustrator", "Logo Design", "Brand Identity", "UI/UX", "Canva"].map(skill => (
              <span key={skill} style={{ display: "inline-block", background: C.blueLight, color: C.blue, fontSize: 12, fontWeight: 500, padding: "4px 10px", borderRadius: 100, margin: "0 4px 6px 0" }}>{skill}</span>
            ))}
          </div>
        </div>
      </div>
    </>
  );
};

// ─── About ────────────────────────────────────────────────────────────────────
const About = () => (
  <>
    <div className="page-header" style={{ textAlign: "center", padding: "60px 2rem" }}>
      <div className="page-header-inner" style={{ textAlign: "center" }}>
        <div style={{ fontSize: 12, color: C.blue, fontWeight: 600, textTransform: "uppercase", letterSpacing: 1, marginBottom: 12 }}>About Us</div>
        <div className="page-header-title" style={{ fontSize: 36 }}>Sri Lanka's First Hybrid<br />Freelance Marketplace</div>
        <div className="page-header-sub" style={{ maxWidth: 560, margin: "12px auto 0" }}>Built for the University of Sri Jayewardenepura community and the wider Sri Lankan SME sector.</div>
      </div>
    </div>
    <div className="section" style={{ maxWidth: 860 }}>
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 24, marginBottom: 48 }}>
        {[
          { icon: "🤝", title: "Our Mission", text: "Bridge the gap between talented university undergraduates and local SMEs by providing a verified, trust-first platform for gigs and part-time work." },
          { icon: "🔍", title: "The Problem We Solve", text: "Sri Lankan students and SMEs face a trust deficit — no local platform verifies identities, holds payments safely, or gives a meaningful reputation score." },
          { icon: "🛡️", title: "How Trust Works", text: "Every student is verified via NIC or University ID. Our Trust Score algorithm weighs ratings, consistency, and verification status to give employers real confidence." },
          { icon: "🇱🇰", title: "Made for Sri Lanka", text: "Designed around local payment norms, local job types (event support, tuition, data entry), and LKR pricing — not a copy-paste of Fiverr." },
        ].map(item => (
          <div key={item.title} style={{ background: "var(--surface)", border: "1px solid var(--border)", borderRadius: 14, padding: 24 }}>
            <div style={{ fontSize: 32, marginBottom: 12 }}>{item.icon}</div>
            <div style={{ fontSize: 16, fontWeight: 700, color: C.navy, marginBottom: 8 }}>{item.title}</div>
            <div style={{ fontSize: 14, color: "var(--text-secondary)", lineHeight: 1.7 }}>{item.text}</div>
          </div>
        ))}
      </div>
      <div style={{ background: C.navy, borderRadius: 16, padding: 36, color: "white", textAlign: "center" }}>
        <div style={{ fontSize: 13, color: "rgba(255,255,255,0.6)", marginBottom: 8, textTransform: "uppercase", letterSpacing: 1 }}>Final Year Project</div>
        <div style={{ fontSize: 22, fontWeight: 700, marginBottom: 8 }}>U.K. Dissanayake · AS2022955</div>
        <div style={{ fontSize: 14, color: "rgba(255,255,255,0.7)" }}>BSc Physical Science ICT · University of Sri Jayewardenepura</div>
        <div style={{ marginTop: 24, display: "flex", justifyContent: "center", gap: 16, flexWrap: "wrap" }}>
          {[["2,400+", "Verified Students"], ["850+", "SME Clients"], ["5,200+", "Completed Jobs"], ["98%", "Satisfaction"]].map(([n, l]) => (
            <div key={l} style={{ background: "rgba(255,255,255,0.08)", borderRadius: 10, padding: "14px 24px", minWidth: 130 }}>
              <div style={{ fontSize: 22, fontWeight: 800 }}>{n}</div>
              <div style={{ fontSize: 12, color: "rgba(255,255,255,0.6)", marginTop: 2 }}>{l}</div>
            </div>
          ))}
        </div>
      </div>
    </div>
  </>
);

// ─── PostJob ──────────────────────────────────────────────────────────────────
const PostJob = ({ user, setPage, setAuthMode, setAuthModal, showToast }) => (
  <>
    <div className="page-header">
      <div className="page-header-inner">
        <div className="page-header-title">Post a Part-Time Job</div>
        <div className="page-header-sub">Reach verified university students across Sri Lanka</div>
      </div>
    </div>
    <div className="section" style={{ maxWidth: 720 }}>
      <div style={{ background: "var(--surface)", border: "1px solid var(--border)", borderRadius: 16, padding: 32 }}>
        <h3 style={{ fontSize: 17, fontWeight: 700, marginBottom: 24, color: C.navy }}>Job Details</h3>
        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16 }}>
          <div style={{ gridColumn: "1 / -1" }} className="form-group">
            <label className="form-label" htmlFor="flk-job-title">Job Title *</label>
            <input className="form-input" placeholder='e.g. "Cashier - Weekend Shift"' id="flk-job-title" />
          </div>
          <div className="form-group">
            <label className="form-label" htmlFor="flk-company-employer-name">Company / Employer Name *</label>
            <input className="form-input" placeholder="Your company name" id="flk-company-employer-name" />
          </div>
          <div className="form-group">
            <label className="form-label" htmlFor="flk-job-type">Job Type *</label>
            <select className="form-select" id="flk-job-type">
              <option>Part-Time</option><option>One-time / Event</option><option>Remote</option><option>Flexible</option>
            </select>
          </div>
          <div className="form-group">
            <label className="form-label" htmlFor="flk-location">Location *</label>
            <input className="form-input" placeholder='e.g. "Nugegoda" or "Remote"' id="flk-location" />
          </div>
          <div className="form-group">
            <label className="form-label" htmlFor="flk-pay-rate-lkr">Pay Rate (LKR) *</label>
            <input className="form-input" placeholder="e.g. 2000" type="number" id="flk-pay-rate-lkr" />
          </div>
          <div className="form-group">
            <label className="form-label" htmlFor="flk-pay-type">Pay Type *</label>
            <select className="form-select" id="flk-pay-type">
              <option>Daily Wage</option><option>Hourly</option><option>Fixed (per task)</option><option>Monthly</option>
            </select>
          </div>
          <div className="form-group">
            <label className="form-label" htmlFor="flk-working-hours">Working Hours</label>
            <input className="form-input" placeholder='e.g. "Sat & Sun, 8am-5pm"' id="flk-working-hours" />
          </div>
          <div className="form-group">
            <label className="form-label" htmlFor="flk-start-date">Start Date</label>
            <input className="form-input" type="date" id="flk-start-date" />
          </div>
          <div style={{ gridColumn: "1 / -1" }} className="form-group">
            <label className="form-label" htmlFor="flk-job-description">Job Description *</label>
            <textarea className="form-input" rows={5} placeholder="Describe the role, responsibilities, and any requirements..." style={{ resize: "vertical", lineHeight: 1.6 }} id="flk-job-description" />
          </div>
          <div style={{ gridColumn: "1 / -1" }} className="form-group">
            <label className="form-label" htmlFor="flk-requirements-optional">Requirements (optional)</label>
            <textarea className="form-input" rows={3} placeholder="e.g. Must be a current university student. Good communication skills." style={{ resize: "vertical", lineHeight: 1.6 }} id="flk-requirements-optional" />
          </div>
          <div style={{ gridColumn: "1 / -1" }} className="form-group">
            <label className="form-label">Attachments (optional)</label>
            <div className="upload-zone" onClick={() => showToast("File upload coming soon!")}>
              <div style={{ fontSize: 28, marginBottom: 6 }}>📎</div>
              <div style={{ fontSize: 14, fontWeight: 500, color: C.navy }}>Attach Job Description or Brief</div>
              <div style={{ fontSize: 12, color: C.gray, marginTop: 4 }}>PDF, DOCX, JPG up to 10MB</div>
            </div>
          </div>
        </div>
        <div style={{ background: C.blueLight, borderRadius: 10, padding: 16, marginBottom: 24, fontSize: 13, color: C.blue }}>
          🛡️ Your job will be reviewed within 24 hours. Only verified employers can post jobs on Freelancelk.
        </div>
        <div style={{ display: "flex", gap: 12 }}>
          <button className="btn-apply" style={{ background: C.blue, width: "auto", padding: "12px 32px", flex: 1 }}
            onClick={async () => {
              if (!user) { setAuthMode("login"); setAuthModal(true); return; }
              const title = document.getElementById("flk-job-title")?.value?.trim();
              const company = document.getElementById("flk-company-employer-name")?.value?.trim();
              const location = document.getElementById("flk-location")?.value?.trim();
              const payRate = document.getElementById("flk-pay-rate-lkr")?.value;
              const desc = document.getElementById("flk-job-description")?.value?.trim();
              const jobType = document.getElementById("flk-job-type")?.value;
              const payType = document.getElementById("flk-pay-type")?.value;
              const requirements = document.getElementById("flk-requirements-optional")?.value?.trim();
              const startDateVal = document.getElementById("flk-start-date")?.value;
              if (!title) { showToast("Job title is required.", "error"); return; }
              if (title.length < 10) { showToast("Title must be at least 10 characters.", "error"); return; }
              if (!location) { showToast("Location is required.", "error"); return; }
              if (!payRate || parseFloat(payRate) <= 0) { showToast("Pay rate is required.", "error"); return; }
              if (!desc) { showToast("Job description is required.", "error"); return; }
              if (desc.length < 50) { showToast("Description must be at least 50 characters.", "error"); return; }
              // Map jobType → employmentType enum: PART_TIME | CONTRACT | FREELANCE
              const empTypeMap = { "Part-Time": "PART_TIME", "One-time / Event": "CONTRACT", "Remote": "FREELANCE", "Flexible": "PART_TIME" };
              const employmentType = empTypeMap[jobType] || "PART_TIME";
              const isRemote = jobType === "Remote" || location.toLowerCase() === "remote";
              // Map payType → correct salary field; min hourlyRate is LKR 500
              const rate = parseFloat(payRate);
              const payTypeMap = { "Hourly": "hourly", "Daily Wage": "daily", "Monthly": "monthly", "Fixed (per task)": "fixed" };
              const payKind = payTypeMap[payType] || "hourly";
              const salaryFields = payKind === "monthly"
                ? { monthlySalary: rate < 10000 ? 10000 : rate }
                : { hourlyRate: rate < 500 ? 500 : rate };
              const body = { title, description: desc, employmentType, location, isRemote, requirements: requirements || undefined, ...salaryFields };
              if (startDateVal) body.startDate = startDateVal;
              try {
                await apiFetch("/listings/jobs", { method: "POST", body: JSON.stringify(body) });
                showToast("Job posted! It will be reviewed within 24 hours. ✅");
                setPage("dashboard");
              } catch (e) { showToast("Post failed: " + e.message, "error"); }
            }}>
            Post Job
          </button>
          <button style={{ padding: "12px 24px", borderRadius: 8, border: "1.5px solid #E5E7EB", background: "var(--surface)", cursor: "pointer", fontFamily: "inherit", fontWeight: 600, fontSize: 14 }} onClick={() => setPage("home")}>Cancel</button>
        </div>
      </div>
    </div>
  </>
);

// ─── CreateGig ────────────────────────────────────────────────────────────────
// Has its own local state (step, gigForm, pricing) — that's fine because
// it is navigated to as a full page, so remounting on navigation is expected.
const CreateGig = ({ setPage, setDashTab, showToast }) => {
  const [step, setStep] = useState(1);
  const [gigForm, setGigForm] = useState({ title: "", category: "", tags: "", description: "", requirements: "" });
  const [pricing, setPricing] = useState({
    Basic: { price: "", delivery: "", revisions: "" },
    Standard: { price: "", delivery: "", revisions: "" },
    Premium: { price: "", delivery: "", revisions: "" },
  });
  const steps = ["Basic Info", "Pricing", "Description", "Publish"];
  return (
    <>
      <div className="page-header">
        <div className="page-header-inner">
          <button style={{ background: "none", border: "none", cursor: "pointer", color: C.blue, fontWeight: 600, fontSize: 14, fontFamily: "inherit", marginBottom: 12 }} onClick={() => setPage("dashboard")}>← Back to Dashboard</button>
          <div className="page-header-title">Create a New Gig</div>
          <div className="page-header-sub">List your service on Freelancelk and start earning</div>
        </div>
      </div>
      <div className="section" style={{ maxWidth: 760 }}>
        <div style={{ display: "flex", gap: 4, marginBottom: 32, background: "#F3F4F6", borderRadius: 12, padding: 4 }}>
          {steps.map((s, i) => (
            <div key={s} onClick={() => i + 1 < step && setStep(i + 1)} style={{ flex: 1, padding: "10px", textAlign: "center", borderRadius: 9, background: step === i + 1 ? "white" : "transparent", fontWeight: step === i + 1 ? 600 : 400, fontSize: 13, color: step === i + 1 ? C.text : C.gray, cursor: i + 1 < step ? "pointer" : "default", boxShadow: step === i + 1 ? "0 1px 4px rgba(0,0,0,0.08)" : "none", transition: "all 0.15s" }}>
              <span style={{ marginRight: 6 }}>{i + 1 < step ? "✅" : `${i + 1}.`}</span>{s}
            </div>
          ))}
        </div>
        <div style={{ background: "var(--surface)", border: "1px solid var(--border)", borderRadius: 16, padding: 32 }}>
          {step === 1 && (
            <>
              <div style={{ fontSize: 17, fontWeight: 700, marginBottom: 20, color: C.navy }}>Basic Information</div>
              <div className="form-group">
                <label className="form-label" htmlFor="flk-gig-title">Gig Title *</label>
                <input className="form-input" placeholder='Start with "I will..." e.g. "I will design a modern logo"' value={gigForm.title} id="flk-gig-title" onChange={e => setGigForm(f => ({ ...f, title: e.target.value }))} />
                <div style={{ fontSize: 12, color: C.gray, marginTop: 4 }}>{gigForm.title.length}/80 characters</div>
              </div>
              <div className="form-group">
                <label className="form-label" htmlFor="flk-category">Category *</label>
                <select className="form-select" value={gigForm.category} id="flk-category" onChange={e => setGigForm(f => ({ ...f, category: e.target.value }))}>
                  <option value="">Select a category</option>
                  {GIG_CATEGORIES.map(c => <option key={c.name} value={c.name}>{c.name}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label className="form-label" htmlFor="flk-search-tags-comma-separated">Search Tags (comma separated)</label>
                <input className="form-input" placeholder="e.g. logo, branding, design, illustrator" value={gigForm.tags} id="flk-search-tags-comma-separated" onChange={e => setGigForm(f => ({ ...f, tags: e.target.value }))} />
              </div>
            </>
          )}
          {step === 2 && (
            <>
              <div style={{ fontSize: 17, fontWeight: 700, marginBottom: 20, color: C.navy }}>Pricing Packages</div>
              {[["Basic", "Core deliverable, 1 revision"], ["Standard", "Everything in Basic + 2 revisions"], ["Premium", "Full package, unlimited revisions"]].map(([tier, hint]) => (
                <div key={tier} style={{ background: "var(--bg-muted)", borderRadius: 12, padding: 16, marginBottom: 12, border: "1px solid var(--border)" }}>
                  <div style={{ fontWeight: 600, fontSize: 14, marginBottom: 12 }}>{tier} Package</div>
                  <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: 10 }}>
                    <div className="form-group" style={{ marginBottom: 0 }}>
                      <label className="form-label" htmlFor={`flk-price-lkr-${tier}`}>Price (LKR)</label>
                      <input className="form-input" type="number" placeholder="5000" style={{ background: "var(--surface)" }} id={`flk-price-lkr-${tier}`} value={pricing[tier].price} onChange={e => setPricing(p => ({ ...p, [tier]: { ...p[tier], price: e.target.value } }))} />
                    </div>
                    <div className="form-group" style={{ marginBottom: 0 }}>
                      <label className="form-label" htmlFor={`flk-delivery-days-${tier}`}>Delivery (days)</label>
                      <input className="form-input" type="number" placeholder="3" style={{ background: "var(--surface)" }} id={`flk-delivery-days-${tier}`} value={pricing[tier].delivery} onChange={e => setPricing(p => ({ ...p, [tier]: { ...p[tier], delivery: e.target.value } }))} />
                    </div>
                    <div className="form-group" style={{ marginBottom: 0 }}>
                      <label className="form-label" htmlFor={`flk-revisions-${tier}`}>Revisions</label>
                      <input className="form-input" type="number" placeholder="1" style={{ background: "var(--surface)" }} id={`flk-revisions-${tier}`} value={pricing[tier].revisions} onChange={e => setPricing(p => ({ ...p, [tier]: { ...p[tier], revisions: e.target.value } }))} />
                    </div>
                  </div>
                  <div style={{ fontSize: 12, color: C.gray, marginTop: 8 }}>{hint}</div>
                </div>
              ))}
            </>
          )}
          {step === 3 && (
            <>
              <div style={{ fontSize: 17, fontWeight: 700, marginBottom: 20, color: C.navy }}>Describe Your Gig</div>
              <div className="form-group">
                <label className="form-label" htmlFor="flk-description">Description *</label>
                <textarea className="form-input" rows={6} placeholder="Describe what you'll deliver, your process, and why clients should choose you..." value={gigForm.description} id="flk-description" onChange={e => setGigForm(f => ({ ...f, description: e.target.value }))} style={{ resize: "vertical", lineHeight: 1.7 }} />
                <div style={{ fontSize: 12, color: C.gray, marginTop: 4 }}>{gigForm.description.length}/1200 characters</div>
              </div>
              <div className="form-group">
                <label className="form-label" htmlFor="flk-what-do-you-need-from-the-buyer">What do you need from the buyer?</label>
                <textarea className="form-input" rows={3} placeholder="e.g. Company name, brand colors, reference logos..." value={gigForm.requirements} id="flk-what-do-you-need-from-the-buyer" onChange={e => setGigForm(f => ({ ...f, requirements: e.target.value }))} style={{ resize: "vertical" }} />
              </div>
              <div className="form-group">
                <label className="form-label">Gig Images</label>
                <div className="upload-zone" onClick={() => showToast("Image upload coming soon!")}>
                  <div style={{ fontSize: 32, marginBottom: 8 }}>🖼️</div>
                  <div style={{ fontSize: 14, fontWeight: 500, color: C.navy }}>Upload Gig Images</div>
                  <div style={{ fontSize: 12, color: C.gray, marginTop: 4 }}>JPG, PNG up to 5MB · Upload up to 5 images</div>
                  <div style={{ marginTop: 12, padding: "8px 20px", background: C.blue, color: "white", borderRadius: 8, display: "inline-block", fontSize: 13, fontWeight: 600 }}>Choose Files</div>
                </div>
              </div>
            </>
          )}
          {step === 4 && (
            <div style={{ textAlign: "center", padding: "20px 0" }}>
              <div style={{ fontSize: 52, marginBottom: 16 }}>🎉</div>
              <div style={{ fontSize: 20, fontWeight: 700, color: C.navy, marginBottom: 8 }}>Ready to Publish!</div>
              <div style={{ fontSize: 14, color: C.gray, marginBottom: 28, maxWidth: 400, margin: "0 auto 28px" }}>Your gig will be reviewed and go live within a few minutes. You'll get a notification once it's published.</div>
              <div style={{ background: "var(--bg-muted)", border: "1px solid var(--border)", borderRadius: 12, padding: 20, marginBottom: 28, textAlign: "left" }}>
                <div style={{ fontWeight: 600, marginBottom: 12, fontSize: 14 }}>Gig Summary</div>
                {[
                  ["Title", gigForm.title || "Not set"],
                  ["Category", gigForm.category || "Not set"],
                  ["Tags", gigForm.tags || "Not set"],
                  ["Basic Price", pricing.Basic.price ? `LKR ${pricing.Basic.price}` : "Not set"],
                  ["Basic Delivery", pricing.Basic.delivery ? `${pricing.Basic.delivery} days` : "Not set"],
                ].map(([k, v]) => (
                  <div key={k} style={{ fontSize: 13, marginBottom: 6 }}><strong style={{ color: C.text }}>{k}:</strong> <span style={{ color: C.gray }}>{v}</span></div>
                ))}
              </div>
            </div>
          )}
          <div style={{ display: "flex", justifyContent: "space-between", marginTop: 28, paddingTop: 20, borderTop: "1px solid #F3F4F6" }}>
            {step > 1
              ? <button onClick={() => setStep(s => s - 1)} style={{ padding: "10px 24px", borderRadius: 8, border: "1.5px solid #E5E7EB", background: "var(--surface)", cursor: "pointer", fontFamily: "inherit", fontWeight: 600, fontSize: 14 }}>← Back</button>
              : <div />
            }
            {step < 4
              ? <button onClick={() => setStep(s => s + 1)} className="btn-join" style={{ padding: "10px 28px" }}>Continue →</button>
              : <button className="btn-join" style={{ padding: "10px 28px", background: C.green }} onClick={async () => {
                const basicPkg = pricing.Basic;
                // Validate against CreateGigRequest constraints
                if (!gigForm.title) { showToast("Gig title is required.", "error"); return; }
                if (gigForm.title.length < 10) { showToast("Title must be at least 10 characters.", "error"); return; }
                if (!gigForm.category) { showToast("Category is required.", "error"); return; }
                if (!gigForm.description || gigForm.description.trim().length < 50) { showToast("Description must be at least 50 characters.", "error"); return; }
                if (!basicPkg.price || parseFloat(basicPkg.price) < 100) { showToast("Base price must be at least LKR 100.", "error"); return; }
                if (!basicPkg.delivery || parseInt(basicPkg.delivery) < 1) { showToast("Delivery days must be at least 1.", "error"); return; }
                const revisionsVal = parseInt(basicPkg.revisions);
                if (isNaN(revisionsVal) || revisionsVal < 0 || revisionsVal > 10) { showToast("Revisions must be between 0 and 10.", "error"); return; }
                try {
                  await apiFetch("/listings/gigs", {
                    method: "POST", body: JSON.stringify({
                      title: gigForm.title.trim(),
                      category: gigForm.category,
                      description: gigForm.description.trim(),
                      requirements: gigForm.requirements || undefined,
                      pricingModel: "FIXED",
                      basePrice: parseFloat(basicPkg.price),
                      deliveryDays: parseInt(basicPkg.delivery),
                      revisionsIncluded: revisionsVal,
                    })
                  });
                  showToast("Gig published! It will be live shortly. 🚀");
                  setDashTab("My Gigs"); setPage("dashboard");
                } catch (e) { showToast("Publish failed: " + e.message, "error"); }
              }}>🚀 Publish Gig</button>
            }
          </div>
        </div>
      </div>
    </>
  );
};

// ─── Dashboard sub-panels ─────────────────────────────────────────────────────
const DashOverview = ({ trustScore, trustData, setDashTab, user }) => {
  const role = user?.role || "FREELANCER";

  const [summary, setSummary] = useState({ totalEarnings: 0, completedOrders: 0, activeGigs: 0, totalSpent: 0, activeOrders: 0 });
  const [recentOrders, setRecentOrders] = useState([]);
  const [overviewGigs, setOverviewGigs] = useState([]);

  useEffect(() => {
    apiFetch("/orders/stats")
      .then(res => {
        setSummary({
          totalEarnings: res.totalEarnings ?? 0,
          completedOrders: res.completedOrders ?? 0,
          activeGigs: res.activeGigs ?? res.activeOrders ?? 0,
          totalSpent: res.totalSpent ?? 0,
          activeOrders: res.activeOrders ?? 0,
        });
      })
      .catch(() => { });
    apiFetch("/orders?page=0&size=3")
      .then(res => {
        const items = res?.content ?? (Array.isArray(res) ? res : []);
        if (items.length > 0) setRecentOrders(items.map(o => ({
          title: o.listingTitle || "Order",
          client: role === "CLIENT" ? (o.sellerName || "Freelancer") : (o.buyerName || "Client"),
          status: (o.status || "PENDING").replace(/_/g, " ").replace(/\b\w/g, c => c.toUpperCase()),
          amount: o.totalAmount || 0,
          statusColor: o.status === "COMPLETED" ? C.green : o.status === "DELIVERED" ? "#F59E0B" : C.blue,
        })));
      })
      .catch(() => { });
    if (role === "FREELANCER" || role === "ADMIN") {
      apiFetch("/listings/gigs/my-gigs?page=0&size=3")
        .then(res => {
          const items = res?.content ?? (Array.isArray(res) ? res : []);
          if (items.length > 0) setOverviewGigs(items.map(g => ({
            id: g.gigId || g.listingId, title: g.title,
            price: g.basePrice || 0,
            img: g.imageUrl || g.thumbnailUrl || GIG_IMAGES[0],
          })));
        })
        .catch(() => { });
    }
  }, []); // eslint-disable-line

  // ── Stat cards per role ──────────────────────────────────────────────────────
  const freelancerStats = [
    ["💰", "Total Earnings", `LKR ${summary.totalEarnings.toLocaleString()}`],
    ["✅", "Completed", `${summary.completedOrders} Orders`],
    ["🎯", "Active Gigs", String(summary.activeGigs)],
    ["⭐", "Trust Score", `${trustScore}/100`],
  ];
  const clientStats = [
    ["💸", "Total Spent", `LKR ${summary.totalSpent.toLocaleString()}`],
    ["📋", "Active Orders", String(summary.activeOrders)],
    ["✅", "Completed Orders", String(summary.completedOrders)],
    ["⭐", "Trust Score", `${trustScore}/100`],
  ];
  const adminStats = [
    ["👥", "Total Users", "—"],
    ["📋", "Active Orders", String(summary.activeOrders)],
    ["✅", "Completed Orders", String(summary.completedOrders)],
    ["⭐", "Platform Health", "Good"],
  ];

  const statCards = role === "CLIENT" ? clientStats : role === "ADMIN" ? adminStats : freelancerStats;

  return (
    <>
      <div className="stat-cards">
        {statCards.map(([icon, label, val]) => (
          <div className="stat-card" key={label}>
            <div className="stat-card-icon">{icon}</div>
            <div className="stat-card-label">{label}</div>
            <div className="stat-card-val">{val}</div>
          </div>
        ))}
      </div>

      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16 }}>

        {/* Left panel: Active Gigs (freelancer/admin) or Browse Services CTA (client) */}
        {role === "CLIENT" ? (
          <div style={{ background: "var(--surface)", border: "1px solid var(--border)", borderRadius: 14, padding: 20 }}>
            <div style={{ fontWeight: 600, fontSize: 15, marginBottom: 16 }}>Find Freelancers</div>
            <div style={{ fontSize: 13, color: C.gray, marginBottom: 20, lineHeight: 1.6 }}>
              Browse hundreds of skilled Sri Lankan freelancers ready to help with your next project.
            </div>
            {["Graphic Design", "Web Development", "Content Writing", "Social Media"].map(cat => (
              <div key={cat} style={{ display: "flex", alignItems: "center", justifyContent: "space-between", padding: "10px 0", borderBottom: "1px solid #F9FAFB" }}>
                <span style={{ fontSize: 13, fontWeight: 500 }}>{cat}</span>
                <span style={{ fontSize: 11, background: C.blueLight, color: C.blue, padding: "2px 8px", borderRadius: 100, fontWeight: 600 }}>Browse →</span>
              </div>
            ))}
          </div>
        ) : (
          <div style={{ background: "var(--surface)", border: "1px solid var(--border)", borderRadius: 14, padding: 20 }}>
            <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 16, alignItems: "center" }}>
              <div style={{ fontWeight: 600, fontSize: 15 }}>Active Gigs</div>
              <button className="btn-join" style={{ padding: "7px 14px", fontSize: 12 }} onClick={() => setDashTab("My Gigs")}>View all</button>
            </div>
            {overviewGigs.map(gig => (
              <div key={gig.id} style={{ display: "flex", gap: 10, alignItems: "center", padding: "10px 0", borderBottom: "1px solid #F9FAFB" }}>
                <img src={gig.img} alt={gig.title} style={{ width: 44, height: 34, borderRadius: 6, objectFit: "cover", flexShrink: 0 }} />
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ fontSize: 12, fontWeight: 500, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{gig.title}</div>
                  <div style={{ fontSize: 11, color: C.gray }}>LKR {gig.price.toLocaleString()}</div>
                </div>
                <span style={{ fontSize: 10, background: "#DCFCE7", color: C.green, padding: "2px 7px", borderRadius: 100, fontWeight: 600, flexShrink: 0 }}>ACTIVE</span>
              </div>
            ))}
          </div>
        )}

        {/* Right panel: Recent Orders (both roles, label adapts) */}
        <div style={{ background: "var(--surface)", border: "1px solid var(--border)", borderRadius: 14, padding: 20 }}>
          <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 16, alignItems: "center" }}>
            <div style={{ fontWeight: 600, fontSize: 15 }}>Recent Orders</div>
            <button className="btn-join" style={{ padding: "7px 14px", fontSize: 12 }} onClick={() => setDashTab("Orders")}>View all</button>
          </div>
          {recentOrders.map((o, i) => (
            <div key={i} style={{ display: "flex", alignItems: "center", padding: "10px 0", borderBottom: "1px solid #F9FAFB" }}>
              <div style={{ flex: 1 }}>
                <div style={{ fontSize: 12, fontWeight: 500 }}>{o.title}</div>
                <div style={{ fontSize: 11, color: C.gray }}>{role === "CLIENT" ? "From" : "From"} {o.client}</div>
              </div>
              <div style={{ textAlign: "right" }}>
                <div style={{ fontSize: 13, fontWeight: 700, color: C.blue }}>LKR {o.amount.toLocaleString()}</div>
                <div style={{ fontSize: 11, color: o.statusColor, fontWeight: 500 }}>{o.status}</div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Trust Score Breakdown — hidden for admin */}
      {role !== "ADMIN" && (
        <div style={{ background: "var(--surface)", border: "1px solid var(--border)", borderRadius: 14, padding: 20, marginTop: 16 }}>
          <div style={{ fontWeight: 600, fontSize: 15, marginBottom: 16 }}>Trust Score Breakdown</div>
          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 12 }}>
            {[
              { label: "Identity Verified", val: trustData.idVerification, color: C.green },
              { label: "Gig Ratings", val: trustData.gigRating, color: C.blue },
              { label: "Job Ratings", val: trustData.jobRating, color: C.teal },
              { label: "On-time Delivery", val: trustData.onTimeDelivery, color: "#7C3AED" },
            ].map(item => (
              <div key={item.label}>
                <div style={{ display: "flex", justifyContent: "space-between", fontSize: 12, marginBottom: 4 }}>
                  <span style={{ color: C.gray }}>{item.label}</span>
                  <span style={{ fontWeight: 600 }}>{item.val}%</span>
                </div>
                <div style={{ background: "#F3F4F6", borderRadius: 100, height: 6 }}>
                  <div style={{ background: item.color, borderRadius: 100, height: 6, width: `${item.val}%` }} />
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </>
  );
};
const DashMyGigs = ({ setPage, showToast }) => {
  const [gigs, setGigs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [editGig, setEditGig] = useState(null); // gig being edited
  const [editForm, setEditForm] = useState({});
  const [editSaving, setEditSaving] = useState(false);

  const loadGigs = () => {
    setLoading(true);
    apiFetch("/listings/gigs/my-gigs")
      .then(res => {
        const items = res?.content ?? (Array.isArray(res) ? res : []);
        setGigs(items.map(g => ({
          id: g.gigId || g.listingId,
          title: g.title,
          category: g.category,
          description: g.description || "",
          basePrice: g.basePrice || 0,
          deliveryDays: g.deliveryDays || 3,
          revisionsIncluded: g.revisionsIncluded ?? 1,
          requirements: g.requirements || "",
          price: g.basePrice || 0,
          rating: g.avgRating || g.averageRating || 0,
          reviews: g.reviewCount || g.totalReviews || 0,
          img: g.imageUrl || g.thumbnailUrl || "https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=400",
          status: g.status || "ACTIVE",
        })));
      })
      .catch(() => { showToast && showToast("Could not load gigs.", "error"); })
      .finally(() => setLoading(false));
  };

  useEffect(() => { loadGigs(); }, []);

  const openEdit = (gig) => {
    setEditForm({
      title: gig.title,
      category: gig.category || "",
      description: gig.description || "",
      basePrice: gig.basePrice || gig.price || "",
      deliveryDays: gig.deliveryDays || "",
      revisionsIncluded: gig.revisionsIncluded ?? "",
      requirements: gig.requirements || "",
    });
    setEditGig(gig);
  };

  const handleSaveEdit = async () => {
    if (!editForm.title || editForm.title.length < 10) { showToast("Title must be at least 10 characters.", "error"); return; }
    if (!editForm.category) { showToast("Category is required.", "error"); return; }
    if (!editForm.description || editForm.description.trim().length < 50) { showToast("Description must be at least 50 characters.", "error"); return; }
    if (!editForm.basePrice || parseFloat(editForm.basePrice) < 100) { showToast("Price must be at least LKR 100.", "error"); return; }
    if (!editForm.deliveryDays || parseInt(editForm.deliveryDays) < 1) { showToast("Delivery days must be at least 1.", "error"); return; }
    const revisions = editForm.revisionsIncluded === "" ? 1 : parseInt(editForm.revisionsIncluded);
    if (isNaN(revisions) || revisions < 0 || revisions > 10) { showToast("Revisions must be between 0 and 10.", "error"); return; }

    setEditSaving(true);
    try {
      // Build payload — only include defined fields, no undefined values
      const payload = {
        title: editForm.title.trim(),
        category: editForm.category,
        description: editForm.description.trim(),
        pricingModel: "FIXED",
        basePrice: parseFloat(editForm.basePrice),
        deliveryDays: parseInt(editForm.deliveryDays),
        revisionsIncluded: revisions,
      };
      if (editForm.requirements && editForm.requirements.trim()) {
        payload.requirements = editForm.requirements.trim();
      }

      await apiFetch(`/listings/gigs/${editGig.id}`, {
        method: "PUT",
        body: JSON.stringify(payload),
      });
      setGigs(prev => prev.map(g => g.id === editGig.id ? { ...g, title: editForm.title, category: editForm.category, price: parseFloat(editForm.basePrice) } : g));
      showToast("Gig updated! ✅");
      setEditGig(null);
    } catch (e) { showToast("Update failed: " + e.message, "error"); }
    finally { setEditSaving(false); }
  };

  const handleDelete = async (gigId) => {
    if (!gigId) { showToast && showToast("Cannot delete — no gig ID.", "error"); return; }
    if (!window.confirm("Delete this gig? This cannot be undone.")) return;
    try {
      await apiFetch(`/listings/gigs/${gigId}`, { method: "DELETE" });
      setGigs(prev => prev.filter(g => g.id !== gigId));
      showToast && showToast("Gig deleted. ✅");
    } catch (e) { showToast && showToast("Delete failed: " + e.message, "error"); }
  };

  const handlePause = async (gigId, currentStatus) => {
    if (!gigId) { showToast && showToast("Cannot update — no gig ID.", "error"); return; }
    const isPaused = currentStatus === "PAUSED";
    const endpoint = isPaused ? `/listings/gigs/${gigId}/publish` : `/listings/gigs/${gigId}/pause`;
    try {
      await apiFetch(endpoint, { method: "POST" });
      setGigs(prev => prev.map(g => g.id === gigId ? { ...g, status: isPaused ? "ACTIVE" : "PAUSED" } : g));
      showToast && showToast(isPaused ? "Gig activated ✅" : "Gig paused ⏸️");
    } catch (e) { showToast && showToast("Update failed: " + e.message, "error"); }
  };

  const statusStyle = (s) => {
    if (s === "ACTIVE") return { background: "#DCFCE7", color: "#16A34A" };
    if (s === "PAUSED") return { background: "#FEF9C3", color: "#92400E" };
    if (s === "DRAFT") return { background: "#F3F4F6", color: "#6B7280" };
    return { background: "#F3F4F6", color: "#6B7280" };
  };

  return (
    <>
      {/* ── Edit modal ── */}
      {editGig && (
        <div className="modal-overlay" onClick={() => setEditGig(null)}>
          <div className="modal" style={{ maxWidth: 540, maxHeight: "90vh", overflowY: "auto" }} onClick={e => e.stopPropagation()}>
            <button className="modal-close" onClick={() => setEditGig(null)}>✕</button>
            <div className="modal-title">Edit Gig</div>
            <div className="modal-sub">Update your gig details below</div>

            <div className="form-group">
              <label className="form-label">Gig Title *</label>
              <input className="form-input" value={editForm.title} onChange={e => setEditForm(f => ({ ...f, title: e.target.value }))} placeholder='e.g. "I will design a modern logo"' />
              <div style={{ fontSize: 12, color: C.gray, marginTop: 4 }}>{(editForm.title || "").length}/80 characters</div>
            </div>
            <div className="form-group">
              <label className="form-label">Category *</label>
              <select className="form-select" value={editForm.category} onChange={e => setEditForm(f => ({ ...f, category: e.target.value }))}>
                <option value="">Select a category</option>
                {GIG_CATEGORIES.map(c => <option key={c.name} value={c.name}>{c.name}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Description * <span style={{ fontWeight: 400, color: C.gray }}>(min 50 chars)</span></label>
              <textarea className="form-input" rows={5} value={editForm.description} onChange={e => setEditForm(f => ({ ...f, description: e.target.value }))} style={{ resize: "vertical" }} />
              <div style={{ fontSize: 12, color: C.gray, marginTop: 4 }}>{(editForm.description || "").length} chars</div>
            </div>
            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: 12 }}>
              <div className="form-group">
                <label className="form-label">Price (LKR) *</label>
                <input className="form-input" type="number" value={editForm.basePrice} onChange={e => setEditForm(f => ({ ...f, basePrice: e.target.value }))} />
              </div>
              <div className="form-group">
                <label className="form-label">Delivery (days) *</label>
                <input className="form-input" type="number" value={editForm.deliveryDays} onChange={e => setEditForm(f => ({ ...f, deliveryDays: e.target.value }))} />
              </div>
              <div className="form-group">
                <label className="form-label">Revisions</label>
                <input className="form-input" type="number" value={editForm.revisionsIncluded} onChange={e => setEditForm(f => ({ ...f, revisionsIncluded: e.target.value }))} />
              </div>
            </div>
            <div className="form-group">
              <label className="form-label">Requirements from buyer</label>
              <textarea className="form-input" rows={2} value={editForm.requirements} onChange={e => setEditForm(f => ({ ...f, requirements: e.target.value }))} style={{ resize: "vertical" }} />
            </div>

            <button className="modal-btn" onClick={handleSaveEdit} disabled={editSaving}>
              {editSaving ? "Saving…" : "Save Changes"}
            </button>
          </div>
        </div>
      )}

      <div style={{ background: "var(--surface)", border: "1px solid var(--border)", borderRadius: 14, padding: 20 }}>
        <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 20, alignItems: "center" }}>
          <div style={{ fontWeight: 600, fontSize: 16 }}>My Gigs ({gigs.length})</div>
          <button className="btn-join" style={{ padding: "8px 16px", fontSize: 13 }} onClick={() => setPage("create-gig")}>+ Create New Gig</button>
        </div>
        {loading && <div style={{ color: "#6B7280", fontSize: 13, padding: 12 }}>Loading your gigs…</div>}
        {!loading && gigs.length === 0 && (
          <div style={{ textAlign: "center", padding: "40px 20px", color: "#6B7280" }}>
            <div style={{ fontSize: 36, marginBottom: 12 }}>🎯</div>
            <div style={{ fontSize: 15, fontWeight: 500, marginBottom: 6 }}>No gigs yet</div>
            <div style={{ fontSize: 13, marginBottom: 20 }}>Create your first gig and start earning.</div>
            <button className="btn-join" style={{ padding: "10px 24px" }} onClick={() => setPage("create-gig")}>Create a Gig</button>
          </div>
        )}
        {gigs.map(gig => (
          <div key={gig.id} style={{ display: "flex", gap: 14, alignItems: "center", padding: "14px 0", borderBottom: "1px solid #F3F4F6" }}>
            <img src={gig.img} alt={gig.title} style={{ width: 72, height: 50, borderRadius: 8, objectFit: "cover", flexShrink: 0 }} />
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ fontSize: 14, fontWeight: 500, marginBottom: 3, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{gig.title}</div>
              <div style={{ fontSize: 12, color: "#6B7280" }}>★ {gig.rating} ({gig.reviews} reviews) · LKR {gig.price.toLocaleString()}</div>
            </div>
            <div style={{ display: "flex", gap: 8, flexShrink: 0, alignItems: "center" }}>
              <span style={{ fontSize: 11, padding: "3px 8px", borderRadius: 100, fontWeight: 600, ...statusStyle(gig.status) }}>{gig.status}</span>
              <button
                style={{ padding: "5px 12px", borderRadius: 6, border: "1px solid var(--border)", background: "var(--surface)", fontSize: 12, cursor: "pointer", fontFamily: "inherit", fontWeight: 500 }}
                onClick={() => openEdit(gig)}>
                ✏️ Edit
              </button>
              {gig.status === "DRAFT" && (
                <button
                  style={{ padding: "5px 12px", borderRadius: 6, border: "none", background: "#16A34A", color: "white", fontSize: 12, cursor: "pointer", fontFamily: "inherit", fontWeight: 600 }}
                  onClick={async () => {
                    try {
                      await apiFetch(`/listings/gigs/${gig.id}/publish`, { method: "POST" });
                      setGigs(prev => prev.map(g => g.id === gig.id ? { ...g, status: "ACTIVE" } : g));
                      showToast && showToast("Gig published! 🚀");
                    } catch (e) { showToast && showToast("Publish failed: " + e.message, "error"); }
                  }}>
                  🚀 Publish
                </button>
              )}
              {gig.status === "ACTIVE" && (
                <button
                  style={{ padding: "5px 12px", borderRadius: 6, border: "1px solid var(--border)", background: "var(--surface)", fontSize: 12, cursor: "pointer", fontFamily: "inherit" }}
                  onClick={() => handlePause(gig.id, gig.status)}>
                  Pause
                </button>
              )}
              {gig.status === "PAUSED" && (
                <button
                  style={{ padding: "5px 12px", borderRadius: 6, border: "1px solid var(--border)", background: "var(--surface)", fontSize: 12, cursor: "pointer", fontFamily: "inherit" }}
                  onClick={() => handlePause(gig.id, gig.status)}>
                  Activate
                </button>
              )}
              <button
                style={{ padding: "5px 12px", borderRadius: 6, border: "1px solid #FEE2E2", background: "#FEF2F2", fontSize: 12, cursor: "pointer", color: "#EF4444", fontFamily: "inherit" }}
                onClick={() => handleDelete(gig.id)}>
                Delete
              </button>
            </div>
          </div>
        ))}
      </div>
    </>
  );
};

// ─── Deliverable Upload Modal ─────────────────────────────────────────────────
// Shown when a freelancer clicks "📦 Deliver" (or "📦 Re-deliver").
// Flow: pick file → upload to /files/upload?type=deliverable → show progress
//       → enable "Submit Deliverable" button → POST /orders/{id}/deliver
const DeliverModal = ({ order, onClose, onDelivered, showToast }) => {
  const ALLOWED = [".zip", ".rar", ".pdf", ".docx", ".doc", ".pptx", ".xlsx", ".mp4", ".mov", ".jpg", ".png"];
  const MAX_BYTES = 100 * 1024 * 1024; // 100 MB

  const [file, setFile] = useState(null);
  const [fileError, setFileError] = useState("");
  const [uploadProgress, setUploadProgress] = useState(0); // 0–100
  const [uploadState, setUploadState] = useState("idle"); // idle | uploading | done | error
  const [uploadResult, setUploadResult] = useState(null); // { filename, url, size }
  const [description, setDescription] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const fileRef = useRef(null);

  const validateFile = (f) => {
    if (!f) return "Please select a file.";
    const ext = "." + f.name.split(".").pop().toLowerCase();
    if (!ALLOWED.includes(ext)) return `File type not allowed. Allowed: ${ALLOWED.join(" ")}`;
    if (f.size > MAX_BYTES) return `File too large. Maximum size is 100 MB.`;
    return "";
  };

  const handleFileChange = (e) => {
    const f = e.target.files?.[0] || null;
    setFile(f);
    setFileError(f ? validateFile(f) : "");
    setUploadState("idle");
    setUploadResult(null);
    setUploadProgress(0);
  };

  const handleUpload = async () => {
    const err = validateFile(file);
    if (err) { setFileError(err); return; }
    setUploadState("uploading");
    setUploadProgress(0);
    setFileError("");
    try {
      const formData = new FormData();
      formData.append("file", file);
      // Use XMLHttpRequest so we get real upload progress events
      const result = await new Promise((resolve, reject) => {
        const xhr = new XMLHttpRequest();
        xhr.open("POST", `${API_BASE}/files/upload?type=deliverable`);
        const token = getToken();
        if (token) xhr.setRequestHeader("Authorization", `Bearer ${token}`);
        xhr.upload.addEventListener("progress", (e) => {
          if (e.lengthComputable) setUploadProgress(Math.round((e.loaded / e.total) * 100));
        });
        xhr.onload = () => {
          if (xhr.status >= 200 && xhr.status < 300) {
            try {
              const parsed = JSON.parse(xhr.responseText);
              // Unwrap backend ApiResponse wrapper if present
              resolve(parsed?.data ?? parsed);
            } catch { reject(new Error("Invalid response from server")); }
          } else {
            try {
              const errBody = JSON.parse(xhr.responseText);
              reject(new Error(errBody?.message || `Upload failed (${xhr.status})`));
            } catch { reject(new Error(`Upload failed (${xhr.status})`)); }
          }
        };
        xhr.onerror = () => reject(new Error("Network error during upload"));
        xhr.send(formData);
      });
      setUploadProgress(100);
      setUploadState("done");
      setUploadResult({ filename: result.filename || result.fileName || file.name, url: result.url || result.fileUrl, size: result.fileSize || result.size || file.size });
      showToast("File uploaded successfully ✅");
    } catch (e) {
      setUploadState("error");
      setFileError(e.message || "Upload failed. Please try again.");
    }
  };

  const handleSubmit = async () => {
    if (!uploadResult) return;
    setSubmitting(true);
    try {
      await apiFetch(`/orders/${order.backendId}/deliver`, {
        method: "POST",
        body: JSON.stringify({
          orderId: order.backendId,
          fileUrl: uploadResult.url,
          fileName: uploadResult.filename,
          fileSize: uploadResult.size || undefined,
          description: description.trim() || undefined,
        }),
      });
      showToast("Deliverable submitted! Buyer has been notified. 📦");
      onDelivered();
      onClose();
    } catch (e) {
      showToast(`Submit failed: ${e.message}`, "error");
    } finally {
      setSubmitting(false);
    }
  };

  const fmtBytes = (b) => b >= 1024 * 1024 ? `${(b / 1024 / 1024).toFixed(1)} MB` : `${Math.round(b / 1024)} KB`;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={e => e.stopPropagation()} style={{ maxWidth: 520 }}>
        <button className="modal-close" onClick={onClose}>✕</button>
        <div className="modal-title">📦 Submit Deliverable</div>
        <div className="modal-sub" style={{ marginBottom: 20 }}>
          Order <strong>{order.id}</strong> · {order.gig}
        </div>

        {/* ── Step 1: File picker ── */}
        <div style={{ marginBottom: 16 }}>
          <div style={{ fontSize: 13, fontWeight: 600, color: "var(--text)", marginBottom: 8 }}>
            Step 1 — Upload your work file
          </div>
          <div
            style={{
              border: `2px dashed ${fileError ? "#EF4444" : uploadState === "done" ? "var(--green)" : "var(--border)"}`,
              borderRadius: 12, padding: "20px 16px", textAlign: "center", cursor: "pointer",
              background: uploadState === "done" ? "var(--green-light)" : "var(--bg-subtle)",
              transition: "all 0.2s ease",
            }}
            onClick={() => uploadState !== "uploading" && fileRef.current?.click()}
          >
            <input ref={fileRef} type="file" accept={ALLOWED.join(",")} style={{ display: "none" }} onChange={handleFileChange} />
            {uploadState === "done" ? (
              <div>
                <div style={{ fontSize: 24, marginBottom: 6 }}>✅</div>
                <div style={{ fontSize: 14, fontWeight: 600, color: "var(--green)" }}>{uploadResult.filename}</div>
                <div style={{ fontSize: 12, color: "var(--text-muted)", marginTop: 2 }}>{fmtBytes(uploadResult.size)} · Uploaded successfully</div>
                <button onClick={(e) => { e.stopPropagation(); setFile(null); setUploadState("idle"); setUploadResult(null); setUploadProgress(0); }} style={{ marginTop: 8, fontSize: 12, color: "var(--text-muted)", background: "none", border: "none", cursor: "pointer", textDecoration: "underline" }}>Change file</button>
              </div>
            ) : file ? (
              <div>
                <div style={{ fontSize: 24, marginBottom: 6 }}>📄</div>
                <div style={{ fontSize: 14, fontWeight: 500, color: "var(--text)" }}>{file.name}</div>
                <div style={{ fontSize: 12, color: "var(--text-muted)", marginTop: 2 }}>{fmtBytes(file.size)}</div>
              </div>
            ) : (
              <div>
                <div style={{ fontSize: 28, marginBottom: 8 }}>☁️</div>
                <div style={{ fontSize: 14, fontWeight: 500, color: "var(--text)" }}>Click to choose a file</div>
                <div style={{ fontSize: 12, color: "var(--text-muted)", marginTop: 4 }}>ZIP · RAR · PDF · DOCX · PPTX · XLSX · MP4 · MOV · JPG · PNG · Max 100 MB</div>
              </div>
            )}
          </div>
          {fileError && <div style={{ fontSize: 12, color: "#EF4444", marginTop: 6 }}>⚠️ {fileError}</div>}
        </div>

        {/* ── Upload progress bar ── */}
        {uploadState === "uploading" && (
          <div style={{ marginBottom: 16 }}>
            <div style={{ display: "flex", justifyContent: "space-between", fontSize: 12, color: "var(--text-muted)", marginBottom: 6 }}>
              <span>Uploading…</span><span>{uploadProgress}%</span>
            </div>
            <div style={{ height: 6, borderRadius: 100, background: "var(--border)", overflow: "hidden" }}>
              <div style={{ height: "100%", width: `${uploadProgress}%`, background: "var(--brand)", borderRadius: 100, transition: "width 0.2s ease" }} />
            </div>
          </div>
        )}

        {/* ── Upload button (Step 1) ── */}
        {uploadState !== "done" && (
          <button
            onClick={handleUpload}
            disabled={!file || !!fileError || uploadState === "uploading"}
            style={{
              width: "100%", padding: "11px 0", borderRadius: 10, fontSize: 14, fontWeight: 600,
              color: "white", border: "none", cursor: (!file || !!fileError || uploadState === "uploading") ? "not-allowed" : "pointer",
              background: (!file || !!fileError || uploadState === "uploading") ? "var(--text-muted)" : "var(--brand)",
              marginBottom: 16, fontFamily: "inherit", transition: "background 0.2s ease",
            }}
          >
            {uploadState === "uploading" ? `Uploading… ${uploadProgress}%` : "⬆️ Upload File"}
          </button>
        )}

        {/* ── Step 2: Description + submit ── */}
        <div style={{ opacity: uploadState === "done" ? 1 : 0.4, pointerEvents: uploadState === "done" ? "auto" : "none", transition: "opacity 0.3s ease" }}>
          <div style={{ fontSize: 13, fontWeight: 600, color: "var(--text)", marginBottom: 8 }}>
            Step 2 — Add a note for the buyer <span style={{ fontWeight: 400, color: "var(--text-muted)" }}>(optional)</span>
          </div>
          <textarea
            className="form-input"
            rows={3}
            placeholder="Describe what you've delivered, any instructions, or notes for the buyer…"
            value={description}
            onChange={e => setDescription(e.target.value)}
            maxLength={1000}
            style={{ resize: "vertical", marginBottom: 4 }}
          />
          <div style={{ fontSize: 11, color: "var(--text-muted)", textAlign: "right", marginBottom: 16 }}>{description.length}/1000</div>
          <button
            onClick={handleSubmit}
            disabled={submitting || uploadState !== "done"}
            style={{
              width: "100%", padding: "12px 0", borderRadius: 10, fontSize: 15, fontWeight: 700,
              color: "white", border: "none", cursor: submitting ? "not-allowed" : "pointer",
              background: submitting ? "var(--text-muted)" : "var(--green)",
              fontFamily: "inherit", transition: "background 0.2s ease",
            }}
          >
            {submitting ? "Submitting…" : "✅ Submit Deliverable"}
          </button>
        </div>
      </div>
    </div>
  );
};

// ─── Deliverables Panel ───────────────────────────────────────────────────────
// Shown beneath an order row for both buyer and seller.
// Downloads stream through the API with auth header to avoid 403.
const DeliverablesPanel = ({ order, showToast }) => {
  const [deliverables, setDeliverables] = useState([]);
  const [loading, setLoading] = useState(true);
  const [downloading, setDownloading] = useState({}); // { [deliverableId]: true }

  useEffect(() => {
    if (!order?.backendId) { setLoading(false); return; }
    apiFetch(`/orders/${order.backendId}/deliverables`)
      .then(res => {
        const items = Array.isArray(res) ? res : (res?.content ?? []);
        setDeliverables(items.map(d => ({
          id: d.deliverableId || d.id,
          fileName: d.fileName || d.filename || "deliverable",
          fileSize: d.fileSize || d.size || 0,
          description: d.description || "",
          deliveredAt: d.deliveredAt || d.createdAt,
        })));
      })
      .catch(() => setDeliverables([]))
      .finally(() => setLoading(false));
  }, [order?.backendId]); // eslint-disable-line

  const handleDownload = async (d) => {
    setDownloading(prev => ({ ...prev, [d.id]: true }));
    try {
      const token = getToken();
      const res = await fetch(`${API_BASE}/orders/${order.backendId}/deliverables/${d.id}/file`, {
        headers: token ? { Authorization: `Bearer ${token}` } : {},
      });
      if (!res.ok) throw new Error(`Download failed (${res.status})`);
      const blob = await res.blob();
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = d.fileName;
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
      showToast(`Downloaded: ${d.fileName} ✅`);
    } catch (e) {
      showToast(`Download failed: ${e.message}`, "error");
    } finally {
      setDownloading(prev => ({ ...prev, [d.id]: false }));
    }
  };

  const fmtBytes = (b) => !b ? "" : b >= 1024 * 1024 ? `${(b / 1024 / 1024).toFixed(1)} MB` : `${Math.round(b / 1024)} KB`;
  const fmtDate = (s) => s ? new Date(s).toLocaleDateString("en-LK", { month: "short", day: "numeric", hour: "2-digit", minute: "2-digit" }) : "";

  if (loading) return <div style={{ padding: "10px 20px 14px 36px", fontSize: 13, color: "var(--text-muted)" }}>Loading deliverables…</div>;
  if (!deliverables.length) return <div style={{ padding: "10px 20px 14px 36px", fontSize: 13, color: "var(--text-muted)" }}>No deliverables submitted yet.</div>;

  return (
    <div style={{ padding: "4px 20px 14px 36px" }}>
      <div style={{ fontSize: 12, fontWeight: 600, color: "var(--text-muted)", textTransform: "uppercase", letterSpacing: "0.06em", marginBottom: 8 }}>
        Deliverables ({deliverables.length})
      </div>
      {deliverables.map((d) => (
        <div key={d.id} style={{ display: "flex", alignItems: "center", gap: 12, padding: "9px 12px", borderRadius: 10, background: "var(--bg-subtle)", border: "1px solid var(--border)", marginBottom: 6 }}>
          <div style={{ fontSize: 20, flexShrink: 0 }}>📎</div>
          <div style={{ flex: 1, minWidth: 0 }}>
            <div style={{ fontSize: 13, fontWeight: 600, color: "var(--text)", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{d.fileName}</div>
            <div style={{ fontSize: 11, color: "var(--text-muted)" }}>
              {[fmtBytes(d.fileSize), fmtDate(d.deliveredAt)].filter(Boolean).join(" · ")}
            </div>
            {d.description && <div style={{ fontSize: 12, color: "var(--text-secondary)", marginTop: 3, fontStyle: "italic" }}>"{d.description}"</div>}
          </div>
          <button
            onClick={() => handleDownload(d)}
            disabled={downloading[d.id]}
            style={{
              padding: "6px 14px", borderRadius: 8, fontSize: 12, fontWeight: 600,
              background: downloading[d.id] ? "var(--bg-muted)" : "var(--brand-light)",
              color: downloading[d.id] ? "var(--text-muted)" : "var(--brand)",
              border: "1px solid var(--border)", cursor: downloading[d.id] ? "not-allowed" : "pointer",
              fontFamily: "inherit", flexShrink: 0, transition: "all 0.2s ease",
            }}
          >
            {downloading[d.id] ? "⏳ Downloading…" : "⬇️ Download"}
          </button>
        </div>
      ))}
    </div>
  );
};

// ─── DashOrders ───────────────────────────────────────────────────────────────
const DashOrders = ({ setDashTab, showToast, user }) => {
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [reviewModal, setReviewModal] = useState(false);
  const [reviewRating, setReviewRating] = useState(0);
  const [reviewText, setReviewText] = useState("");
  const [orders, setOrders] = useState([]);
  const [deliverModal, setDeliverModal] = useState(null); // order object | null
  const [expandedDeliverables, setExpandedDeliverables] = useState({}); // { [orderId]: bool }

  // Normalize any backend status string to the title-case keys used in this component
  const normalizeStatus = (raw = "") => {
    const map = {
      PENDING: "Pending", ACCEPTED: "Accepted",
      IN_PROGRESS: "In Progress", DELIVERED: "Delivered",
      COMPLETED: "Completed", CANCELLED: "Cancelled",
      CANCELED: "Cancelled", REVISION_REQUESTED: "Revision Requested",
    };
    return map[raw.toUpperCase().replace(/ /g, "_")] || raw;
  };

  const loadOrders = () => {
    // /orders returns all orders for the logged-in user filtered by their JWT role.
    // The backend automatically returns seller-side orders for FREELANCERs and
    // buyer-side orders for CLIENTs — no separate endpoint needed.
    apiFetch("/orders?page=0&size=20")
      .then(res => {
        const items = res?.content ?? (Array.isArray(res) ? res : []);
        setOrders(items.map(o => ({
          id: o.orderNumber || o.orderId, backendId: o.orderId,
          gig: o.listingTitle || o.gigTitle || "Order",
          client: o.buyerName || o.clientName || "Client",
          status: normalizeStatus(o.status || "PENDING"),
          amount: o.totalAmount || o.amount || 0,
          date: o.createdAt ? new Date(o.createdAt).toLocaleDateString("en-LK", { month: "short", day: "numeric" }) : "—",
          deadline: o.deliveryDeadline ? new Date(o.deliveryDeadline).toLocaleDateString("en-LK", { month: "short", day: "numeric" }) : "—",
        })));
      })
      .catch(err => console.error("[DashOrders] fetch failed:", err));
  };

  useEffect(() => { loadOrders(); }, []); // eslint-disable-line

  const statusStyle = { "In Progress": { bg: "#EEF1FF", color: C.blue }, Accepted: { bg: "#EEF1FF", color: C.blue }, Delivered: { bg: "#FEF9C3", color: "#92400E" }, Completed: { bg: "#DCFCE7", color: C.green }, Pending: { bg: "#F3F4F6", color: C.gray }, Cancelled: { bg: "#FEF2F2", color: "#EF4444" }, "Revision Requested": { bg: "#FEF9C3", color: "#92400E" } };

  // isFreelancer: used to decide whether to show the deliver button vs. complete button
  const isFreelancer = user?.role === "FREELANCER";

  const updateStatus = async (orderId, newStatus) => {
    const order = orders.find(o => o.id === orderId);
    const bid = order?.backendId || orderId;

    // For "Delivered" status, open the proper upload modal instead of window.prompt
    if (newStatus === "Delivered") {
      setDeliverModal(order);
      return;
    }

    setOrders(os => os.map(o => o.id === orderId ? { ...o, status: newStatus } : o));
    let url, body;
    if (newStatus === "Accepted") { url = `/orders/${bid}/accept`; }
    else if (newStatus === "In Progress") { url = `/orders/${bid}/start`; }
    else if (newStatus === "Completed") { url = `/orders/${bid}/complete`; }
    else if (newStatus === "Cancelled") { url = `/orders/${bid}/cancel?reason=Cancelled+via+dashboard`; }
    else if (newStatus === "Revision Requested") { url = `/orders/${bid}/revision?revisionNotes=Revision+requested+via+dashboard`; }
    else return;
    try {
      await apiFetch(url, { method: "POST", ...(body ? { body } : {}) });
      showToast(`Order updated → ${newStatus} ✅`);
    } catch (e) {
      setOrders(os => os.map(o => o.id === orderId ? { ...o, status: order?.status || newStatus } : o));
      showToast(`Action failed: ${e.message}`, "error");
    }
  };

  const toggleDeliverables = (orderId) => {
    setExpandedDeliverables(prev => ({ ...prev, [orderId]: !prev[orderId] }));
  };

  const getActions = (order) => {
    const btn = (label, color, bg, border, onClick) => (
      <button key={label} className="action-btn" style={{ background: bg, color, border: border || "none", marginRight: 6, padding: "6px 14px", borderRadius: 7, fontSize: 13, fontWeight: 600, cursor: "pointer", fontFamily: "inherit" }} onClick={onClick}>{label}</button>
    );
    if (isFreelancer) {
      switch (order.status) {
        case "Pending": return [btn("✅ Accept", "white", C.green, null, () => updateStatus(order.id, "Accepted")), btn("❌ Decline", "#EF4444", "#FEF2F2", "1px solid #FEE2E2", () => updateStatus(order.id, "Cancelled"))];
        case "Accepted": return [btn("▶️ Start Work", "white", C.blue, null, () => updateStatus(order.id, "In Progress")), btn("❌ Cancel", "#EF4444", "#FEF2F2", "1px solid #FEE2E2", () => updateStatus(order.id, "Cancelled"))];
        case "In Progress": return [btn("📦 Deliver", "white", C.blue, null, () => updateStatus(order.id, "Delivered")), btn("❌ Cancel", "#EF4444", "#FEF2F2", "1px solid #FEE2E2", () => updateStatus(order.id, "Cancelled"))];
        case "Delivered": return [];
        case "Revision Requested": return [btn("📦 Re-deliver", "white", C.blue, null, () => updateStatus(order.id, "Delivered"))];
        case "Completed": return [btn("⭐ Leave Review", "white", C.blue, null, () => { setSelectedOrder(order); setReviewModal(true); })];
        default: return [];
      }
    } else {
      // CLIENT actions
      switch (order.status) {
        case "Delivered": return [btn("✅ Complete", "white", C.green, null, () => updateStatus(order.id, "Completed")), btn("🔄 Revision", "#92400E", "#FEF9C3", null, () => updateStatus(order.id, "Revision Requested"))];
        case "Completed": return [btn("⭐ Leave Review", "white", C.blue, null, () => { setSelectedOrder(order); setReviewModal(true); })];
        case "Pending": return [btn("❌ Cancel", "#EF4444", "#FEF2F2", "1px solid #FEE2E2", () => updateStatus(order.id, "Cancelled"))];
        default: return [];
      }
    }
  };

  // Orders where a deliverables panel makes sense (has been delivered or completed)
  const showDeliverablesFor = (order) => ["Delivered", "Completed", "Revision Requested"].includes(order.status);

  return (
    <>
      {/* ── Deliver Modal ── */}
      {deliverModal && (
        <DeliverModal
          order={deliverModal}
          onClose={() => setDeliverModal(null)}
          onDelivered={() => {
            // Optimistically update local status then re-fetch
            setOrders(os => os.map(o => o.id === deliverModal.id ? { ...o, status: "Delivered" } : o));
            setTimeout(loadOrders, 1000);
          }}
          showToast={showToast}
        />
      )}

      {/* ── Review Modal ── */}
      {reviewModal && selectedOrder && (
        <div className="modal-overlay" onClick={() => setReviewModal(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <button className="modal-close" onClick={() => setReviewModal(false)}>✕</button>
            <div className="modal-title">Leave a Review</div>
            <div className="modal-sub">How was your experience with {selectedOrder.gig}?</div>
            <div style={{ textAlign: "center", margin: "16px 0" }}>
              <div className="review-stars">
                {[1, 2, 3, 4, 5].map(s => (
                  <button key={s} onClick={() => setReviewRating(s)} style={{ fontSize: 32, background: "none", border: "none", cursor: "pointer", color: s <= reviewRating ? "#FBBF24" : "#D1D5DB" }}>★</button>
                ))}
              </div>
              <div style={{ fontSize: 13, color: C.gray, marginTop: 4 }}>{["", "Poor", "Fair", "Good", "Very Good", "Excellent"][reviewRating]}</div>
            </div>
            <div className="form-group">
              <label className="form-label">Your Review</label>
              <textarea className="form-input" rows={4} placeholder="Share your experience..." value={reviewText} onChange={e => setReviewText(e.target.value)} style={{ resize: "vertical" }} />
            </div>
            <button className="modal-btn" onClick={async () => {
              if (!reviewRating) { alert("Please select a rating."); return; }
              if (!reviewText || reviewText.trim().length < 10) { alert("Please write at least 10 characters in your review."); return; }
              if (!selectedOrder?.backendId) { alert("Invalid order — cannot submit review."); return; }
              try {
                await apiFetch("/reviews", { method: "POST", body: JSON.stringify({ orderId: selectedOrder?.backendId || null, rating: reviewRating, reviewText }) });
                showToast("Review submitted! ⭐");
              } catch (e) { showToast("Review saved locally (backend: " + e.message + ")"); }
              setReviewModal(false); setReviewText(""); setReviewRating(0);
            }}>Submit Review</button>
          </div>
        </div>
      )}

      <div style={{ background: "var(--surface)", border: "1px solid var(--border)", borderRadius: 14, overflow: "hidden" }}>
        <div style={{ padding: "16px 20px", borderBottom: "1px solid var(--border)", fontWeight: 600, fontSize: 16 }}>Orders ({orders.length})</div>
        {orders.map(o => (
          <div key={o.id} style={{ borderBottom: "1px solid var(--border)" }}>
            {/* ── Order row ── */}
            <div style={{ padding: "16px 20px" }}>
              <div style={{ display: "flex", alignItems: "flex-start", justifyContent: "space-between", gap: 16, flexWrap: "wrap" }}>
                <div style={{ flex: 1 }}>
                  <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 6 }}>
                    <span style={{ fontSize: 13, color: C.blue, fontWeight: 600 }}>{o.id}</span>
                    <span style={{ fontSize: 11, fontWeight: 600, padding: "3px 8px", borderRadius: 100, background: (statusStyle[o.status] || statusStyle.Cancelled).bg, color: (statusStyle[o.status] || statusStyle.Cancelled).color }}>{o.status}</span>
                  </div>
                  <div style={{ fontSize: 14, fontWeight: 500, marginBottom: 3 }}>{o.gig}</div>
                  <div style={{ fontSize: 12, color: C.gray }}>{isFreelancer ? `Client: ${o.client}` : `Freelancer`} · Due: {o.deadline} · LKR {o.amount.toLocaleString()}</div>
                </div>
                <div style={{ display: "flex", alignItems: "center", gap: 6, flexWrap: "wrap" }}>
                  {getActions(o)}
                  {showDeliverablesFor(o) && (
                    <button
                      className="action-btn"
                      style={{ background: expandedDeliverables[o.id] ? "var(--brand-light)" : "var(--bg-muted)", color: expandedDeliverables[o.id] ? "var(--brand)" : "var(--text-muted)", fontSize: 12 }}
                      onClick={() => toggleDeliverables(o.id)}
                    >
                      📁 {expandedDeliverables[o.id] ? "Hide Files" : "View Files"}
                    </button>
                  )}
                  <button className="action-btn" style={{ background: "var(--bg-muted)", color: "var(--text)", fontSize: 12 }} onClick={() => setDashTab("Messages")}>💬 Message</button>
                </div>
              </div>
            </div>

            {/* ── Deliverables panel (expandable) ── */}
            {showDeliverablesFor(o) && expandedDeliverables[o.id] && (
              <div style={{ borderTop: "1px solid var(--border)", background: "var(--bg-subtle)" }}>
                <DeliverablesPanel order={o} showToast={showToast} />
              </div>
            )}
          </div>
        ))}
        {orders.length === 0 && (
          <div style={{ padding: "40px 20px", textAlign: "center", color: "var(--text-muted)", fontSize: 14 }}>No orders yet.</div>
        )}
      </div>
    </>
  );
};

const DashApplications = ({ setPage, setTab }) => {
  const [apps, setApps] = useState([]);
  useEffect(() => {
    apiFetch("/listings/jobs/applications").then(res => {
      const items = res?.content ?? (Array.isArray(res) ? res : []);
      setApps(items.map(a => ({
        job: a.jobTitle || "Job", company: a.companyName || "Employer",
        location: a.location || "—", wage: `LKR ${(a.wageAmount || 0).toLocaleString()}`,
        date: a.appliedAt ? new Date(a.appliedAt).toLocaleDateString("en-LK", { month: "short", day: "numeric" }) : "—",
        status: a.status || "Under Review",
      })));
    }).catch(() => { });
  }, []); // eslint-disable-line
  const statusStyle = { "Under Review": { bg: "#EEF1FF", color: C.blue }, Shortlisted: { bg: "#DCFCE7", color: C.green }, Rejected: { bg: "#FEF2F2", color: "#EF4444" } };
  return (
    <div style={{ background: "var(--surface)", border: "1px solid var(--border)", borderRadius: 14, overflow: "hidden" }}>
      <div style={{ padding: "16px 20px", borderBottom: "1px solid #E5E7EB", fontWeight: 600, fontSize: 16 }}>My Job Applications ({apps.length})</div>
      {apps.map((a, i) => (
        <div key={i} style={{ display: "flex", alignItems: "center", padding: "16px 20px", borderBottom: "1px solid #F9FAFB", gap: 16 }}>
          <div style={{ width: 40, height: 40, borderRadius: 10, background: C.blueLight, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 18, flexShrink: 0 }}>🏢</div>
          <div style={{ flex: 1 }}>
            <div style={{ fontSize: 14, fontWeight: 600, marginBottom: 3 }}>{a.job}</div>
            <div style={{ fontSize: 12, color: C.gray }}>{a.company} · 📍 {a.location} · Applied {a.date}</div>
          </div>
          <div style={{ textAlign: "right", flexShrink: 0 }}>
            <div style={{ fontSize: 14, fontWeight: 700, color: C.green, marginBottom: 4 }}>{a.wage}/day</div>
            <span style={{ fontSize: 11, fontWeight: 600, padding: "3px 8px", borderRadius: 100, background: statusStyle[a.status]?.bg || "#F3F4F6", color: statusStyle[a.status]?.color || C.gray }}>{a.status}</span>
          </div>
        </div>
      ))}
      <div style={{ padding: 20, textAlign: "center" }}>
        <button className="btn-join" style={{ padding: "10px 24px" }} onClick={() => { setPage("browse"); setTab("jobs"); }}>Browse More Jobs</button>
      </div>
    </div>
  );
};

const DashWallet = () => {
  const [txns, setTxns] = useState([]);
  const [walletData, setWalletData] = useState({ balance: 4400, totalEarned: 24500, pendingClearance: 5000 });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    apiFetch("/users/me/wallet")
      .then(res => {
        const d = res;
        setWalletData({ balance: d.availableBalance ?? d.balance ?? 0, totalEarned: d.totalEarned ?? 0, pendingClearance: d.pendingAmount ?? d.pendingClearance ?? 0 });
      })
      .catch(() => { })
      .finally(() => setLoading(false));

    apiFetch("/users/me/transactions")
      .then(res => {
        const items = res?.content ?? (Array.isArray(res) ? res : []);
        if (items.length > 0) setTxns(items.map(t => ({
          type: t.type === "CREDIT" || t.amount > 0 ? "credit" : "debit",
          desc: t.description || t.desc || "Transaction",
          amount: Math.abs(t.amount || 0),
          date: t.createdAt ? new Date(t.createdAt).toLocaleDateString("en-LK", { month: "short", day: "numeric" }) : "—",
        })));
      })
      .catch(() => { });
  }, []);

  return (
    <>
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: 14, marginBottom: 16 }}>
        {[
          { label: "Available Balance", val: loading ? "Loading…" : `LKR ${walletData.balance.toLocaleString()}`, color: C.green, icon: "💰" },
          { label: "Total Earned", val: loading ? "Loading…" : `LKR ${walletData.totalEarned.toLocaleString()}`, color: C.blue, icon: "📈" },
          { label: "Pending Clearance", val: loading ? "Loading…" : `LKR ${walletData.pendingClearance.toLocaleString()}`, color: "#F59E0B", icon: "⏳" },
        ].map(w => (
          <div key={w.label} style={{ background: "var(--surface)", border: "1px solid var(--border)", borderRadius: 14, padding: 20 }}>
            <div style={{ fontSize: 22, marginBottom: 8 }}>{w.icon}</div>
            <div style={{ fontSize: 12, color: C.gray, marginBottom: 4 }}>{w.label}</div>
            <div style={{ fontSize: 22, fontWeight: 800, color: w.color }}>{w.val}</div>
          </div>
        ))}
      </div>
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 12, marginBottom: 16 }}>
        <button className="btn-join" style={{ padding: "12px", fontSize: 14, borderRadius: 10 }} onClick={async () => {
          const amount = window.prompt("Enter withdrawal amount (minimum LKR 1,000):");
          if (!amount || isNaN(amount) || parseFloat(amount) < 1000) { alert("Enter a valid amount (minimum LKR 1,000)."); return; }
          const bankName = window.prompt("Bank name (e.g. Commercial Bank):");
          if (!bankName?.trim()) return;
          const accountNumber = window.prompt("Account number:");
          if (!accountNumber?.trim()) return;
          const accountName = window.prompt("Account holder name:");
          if (!accountName?.trim()) return;
          try {
            await apiFetch("/users/me/withdrawals", { method: "POST", body: JSON.stringify({ amount: parseFloat(amount), bankName: bankName.trim(), accountNumber: accountNumber.trim(), accountName: accountName.trim() }) });
            alert("✅ Withdrawal request submitted! Processing in 3-5 business days.");
          } catch (e) { alert("Error: " + e.message); }
        }}>Withdraw to Bank</button>
        <button style={{ padding: "12px", fontSize: 14, borderRadius: 10, border: "1.5px solid #E5E7EB", background: "var(--surface)", cursor: "pointer", fontFamily: "inherit", fontWeight: 600 }} onClick={() => alert("Payment method management coming soon!")}>Add Payment Method</button>
      </div>
      <div style={{ background: "var(--surface)", border: "1px solid var(--border)", borderRadius: 14, overflow: "hidden" }}>
        <div style={{ padding: "16px 20px", borderBottom: "1px solid #E5E7EB", fontWeight: 600, fontSize: 15 }}>Transaction History</div>
        {txns.map((t, i) => (
          <div key={i} style={{ display: "flex", alignItems: "center", padding: "14px 20px", borderBottom: "1px solid #F9FAFB", gap: 14 }}>
            <div style={{ width: 36, height: 36, borderRadius: "50%", background: t.type === "credit" ? "#DCFCE7" : "#FEF2F2", display: "flex", alignItems: "center", justifyContent: "center", fontSize: 16, flexShrink: 0 }}>
              {t.type === "credit" ? "⬇️" : "⬆️"}
            </div>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 13, fontWeight: 500 }}>{t.desc}</div>
              <div style={{ fontSize: 12, color: C.gray }}>{t.date}</div>
            </div>
            <div style={{ fontSize: 15, fontWeight: 700, color: t.type === "credit" ? C.green : "#EF4444", flexShrink: 0 }}>
              {t.type === "credit" ? "+" : "-"}LKR {t.amount.toLocaleString()}
            </div>
          </div>
        ))}
      </div>
    </>
  );
};

// DashMessages has its own local state — that is fine; it is only ever mounted
// inside Dashboard, whose identity is now stable.
const DashMessages = ({ user, showToast, messageTarget, clearMessageTarget }) => {
  const [activeChat, setActiveChat] = useState(0);
  const [msgInput, setMsgInput] = useState("");
  const [contacts, setContacts] = useState([]);
  const [chatLogs, setChatLogs] = useState({});

  useEffect(() => {
    apiFetch("/chat/conversations").then(res => {
      const convos = Array.isArray(res) ? res : (res.content || []);
      const mapped = convos.map(c => ({
        name: c.otherUserName || "User",
        last: c.lastMessage || c.lastMessagePreview || "",
        time: c.lastMessageAt ? new Date(c.lastMessageAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : "",
        unread: c.unreadCount || 0,
        conversationId: c.conversationId,
        otherUserId: c.otherUserId
      }));
      setContacts(mapped);

      // If we arrived here from "Message Seller", auto-select that conversation
      if (messageTarget?.conversationId) {
        const idx = mapped.findIndex(c => c.conversationId === messageTarget.conversationId);
        if (idx >= 0) {
          setActiveChat(idx);
        } else {
          // Conversation not in list yet — prepend it
          setContacts(prev => [{ name: messageTarget.name, last: "", time: "", unread: 0, conversationId: messageTarget.conversationId, otherUserId: messageTarget.otherUserId }, ...prev]);
          setActiveChat(0);
        }
        if (clearMessageTarget) clearMessageTarget();
      }
    }).catch(err => console.error("Failed to fetch conversations", err));
  }, []); // eslint-disable-line

  useEffect(() => {
    const conv = contacts[activeChat];
    if (!conv?.conversationId) return;

    apiFetch(`/chat/conversations/${conv.conversationId}/messages`).then(res => {
      const msgs = Array.isArray(res) ? res : (res.content || []);
      const formattedMsgs = msgs.map(m => ({
        from: (m.senderId === user?.userId || m.isMine) ? "me" : "them",
        text: m.content,
        time: (m.createdAt || m.timestamp)
          ? new Date(m.createdAt || m.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
          : "Recently"
      }));

      setChatLogs(prev => ({
        ...prev,
        [activeChat]: formattedMsgs
      }));
    }).catch(err => console.error("Failed to fetch messages", err));
  }, [activeChat, contacts, user]);

  const sendMsg = async () => {
    if (!msgInput.trim()) return;
    const conv = contacts[activeChat];
    if (!conv?.conversationId) return;

    const text = msgInput;

    setChatLogs(prev => ({
      ...prev,
      [activeChat]: [...(prev[activeChat] || []), { from: "me", text, time: "Just now" }]
    }));
    setMsgInput("");

    try {
      await apiFetch(`/chat/conversations/${conv.conversationId}/messages`, {
        method: "POST",
        body: JSON.stringify({ content: text })
      });
    } catch (err) {
      setChatLogs(prev => ({
        ...prev,
        [activeChat]: (prev[activeChat] || []).slice(0, -1)
      }));
      showToast("Message failed to send", "error");
    }
  };

  return (
    <div style={{ background: "var(--surface)", border: "1px solid var(--border)", borderRadius: 14, overflow: "hidden", display: "grid", gridTemplateColumns: "260px 1fr", height: 500 }}>
      {/* Left Sidebar: Contacts */}
      <div style={{ borderRight: "1px solid #E5E7EB", overflowY: "auto", height: "100%" }}>
        <div style={{ padding: "14px 16px", borderBottom: "1px solid #E5E7EB", fontWeight: 600, fontSize: 14 }}>Messages</div>
        {contacts.map((c, i) => (
          <div key={i} onClick={() => setActiveChat(i)} style={{ padding: "12px 16px", cursor: "pointer", background: activeChat === i ? C.blueLight : "white", borderBottom: "1px solid #F9FAFB", display: "flex", gap: 10, alignItems: "center" }}>
            <div style={{ width: 36, height: 36, borderRadius: "50%", background: AVATAR_COLORS[i % AVATAR_COLORS.length], color: "white", fontSize: 13, fontWeight: 700, display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0 }}>{c.name[0]}</div>
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ fontSize: 13, fontWeight: 600, display: "flex", justifyContent: "space-between" }}>
                <span>{c.name}</span>
                {c.unread > 0 && <span style={{ background: C.blue, color: "white", fontSize: 10, borderRadius: "50%", width: 16, height: 16, display: "inline-flex", alignItems: "center", justifyContent: "center" }}>{c.unread}</span>}
              </div>
              <div style={{ fontSize: 12, color: C.gray, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{c.last}</div>
            </div>
          </div>
        ))}
        {contacts.length === 0 && <div style={{ padding: 20, textAlign: 'center', color: C.gray, fontSize: 13 }}>No conversations yet.</div>}
      </div>

      {/* Right Area: Active Chat */}
      {/* FIX: Added height: "100%" and overflow: "hidden" here */}
      <div style={{ display: "flex", flexDirection: "column", height: "100%", overflow: "hidden" }}>

        {/* Chat Header */}
        <div style={{ padding: "12px 16px", borderBottom: "1px solid #E5E7EB", fontWeight: 600, fontSize: 14, flexShrink: 0 }}>
          {contacts[activeChat]?.name || "Select a chat"}
        </div>

        {/* Chat Messages List */}
        <div style={{ flex: 1, overflowY: "auto", padding: 16, display: "flex", flexDirection: "column", gap: 10 }}>
          {(chatLogs[activeChat] || []).map((msg, i) => (
            <div key={i} style={{ display: "flex", justifyContent: msg.from === "me" ? "flex-end" : "flex-start" }}>
              <div style={{ maxWidth: "70%", padding: "9px 14px", borderRadius: msg.from === "me" ? "14px 14px 4px 14px" : "14px 14px 14px 4px", background: msg.from === "me" ? C.blue : "#F3F4F6", color: msg.from === "me" ? "white" : C.text, fontSize: 13, lineHeight: 1.5 }}>
                {msg.text}
                <div style={{ fontSize: 10, marginTop: 4, opacity: 0.65, textAlign: "right" }}>{msg.time}</div>
              </div>
            </div>
          ))}
        </div>

        {/* Input Bar */}
        {/* FIX: Added flexShrink: 0 here so it never gets crushed by the messages list */}
        <div style={{ padding: "12px 16px", borderTop: "1px solid #E5E7EB", display: "flex", gap: 8, flexShrink: 0 }}>
          <input
            style={{ flex: 1, padding: "9px 14px", borderRadius: 20, border: "1.5px solid #E5E7EB", fontSize: 13, fontFamily: "inherit", outline: "none" }}
            placeholder="Type a message..."
            value={msgInput}
            onChange={e => setMsgInput(e.target.value)}
            onKeyDown={e => e.key === "Enter" && sendMsg()}
            disabled={contacts.length === 0}
          />
          <button onClick={sendMsg} disabled={contacts.length === 0} style={{ padding: "9px 18px", borderRadius: 20, background: C.blue, color: "white", border: "none", cursor: "pointer", fontWeight: 600, fontSize: 13, fontFamily: "inherit", opacity: contacts.length === 0 ? 0.5 : 1 }}>Send</button>
        </div>

      </div>
    </div>
  );
};

const DashSettings = ({ user, setUser, setPage, showToast }) => {
  const [editForm, setEditForm] = useState({ name: user?.name || "", email: user?.email || "", bio: "", location: "" });
  const [skills, setSkills] = useState([]);
  const [newSkill, setNewSkill] = useState("");

  // Load real profile on mount
  // Load real profile on mount
  useEffect(() => {
    apiFetch("/users/me").then(res => {
      setEditForm(prev => ({
        ...prev,
        bio: res.bio || prev.bio,
        location: res.location || prev.location
      }));
    }).catch(() => { });
  }, []);
  const addSkill = async () => { const s = newSkill.trim(); if (!s || skills.includes(s)) return; try { await apiFetch("/users/me/skills", { method: "POST", body: JSON.stringify({ skillName: s, proficiencyLevel: 3 }) }); setSkills(prev => [...prev, s]); setNewSkill(""); showToast("Skill added!"); } catch (e) { showToast("Failed to add skill: " + e.message, "error"); } };
  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16 }}>
        <div style={{ background: "var(--surface)", border: "1px solid var(--border)", borderRadius: 14, padding: 24 }}>
          <div style={{ fontWeight: 600, fontSize: 15, marginBottom: 20 }}>Profile Information</div>
          {[["Full Name", "name", "text"], ["Email Address", "email", "email"], ["Location", "location", "text"]].map(([label, key, type]) => (
            <div className="form-group" key={key}>
              <label className="form-label" htmlFor={`flk-settings-${key}`}>{label}</label>
              <input id={`flk-settings-${key}`} className="form-input" type={type} value={editForm[key]} onChange={e => setEditForm(f => ({ ...f, [key]: e.target.value }))} />
            </div>
          ))}
          <div className="form-group">
            <label className="form-label" htmlFor="flk-bio">Bio</label>
            <textarea className="form-input" rows={3} value={editForm.bio} id="flk-bio" onChange={e => setEditForm(f => ({ ...f, bio: e.target.value }))} style={{ resize: "vertical" }} />
          </div>
          <button className="btn-join" style={{ padding: "10px 20px" }} onClick={async () => {
            try {
              const nameParts = editForm.name.trim().split(" ");
              await apiFetch("/users/me", { method: "PUT", body: JSON.stringify({ firstName: nameParts[0], lastName: nameParts.slice(1).join(" ") || nameParts[0], bio: editForm.bio, location: editForm.location }) });
              showToast("Profile updated!");
            } catch (e) { showToast("Update failed: " + e.message, "error"); }
          }}>Save Changes</button>
        </div>
        <div>
          <div style={{ background: "var(--surface)", border: "1px solid var(--border)", borderRadius: 14, padding: 24, marginBottom: 16 }}>
            <div style={{ fontWeight: 600, fontSize: 15, marginBottom: 16 }}>Verification Status</div>
            {[
              { label: "NIC / National ID", status: "Verified", icon: "🪪" },
              { label: "University ID", status: "Verified", icon: "🎓" },
              { label: "Phone Number", status: "Pending", icon: "📱" },
              { label: "Bank Account", status: "Not Added", icon: "🏦" },
            ].map(v => (
              <div key={v.label} style={{ display: "flex", alignItems: "center", justifyContent: "space-between", padding: "10px 0", borderBottom: "1px solid #F9FAFB" }}>
                <div style={{ display: "flex", gap: 10, alignItems: "center" }}>
                  <span style={{ fontSize: 18 }}>{v.icon}</span>
                  <span style={{ fontSize: 13, fontWeight: 500 }}>{v.label}</span>
                </div>
                <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
                  <span style={{ fontSize: 11, fontWeight: 600, padding: "3px 8px", borderRadius: 100, background: v.status === "Verified" ? "#DCFCE7" : v.status === "Pending" ? "#FEF9C3" : "#F3F4F6", color: v.status === "Verified" ? C.green : v.status === "Pending" ? "#92400E" : C.gray }}>{v.status}</span>
                  {v.status !== "Verified" && <button style={{ fontSize: 11, padding: "3px 10px", borderRadius: 6, border: "1px solid #2B3FBF", background: "#EEF1FF", color: C.blue, cursor: "pointer", fontFamily: "inherit", fontWeight: 600 }} onClick={async () => {
                    // Map label → verificationType enum (NIC | PASSPORT | DRIVING_LICENSE)
                    const typeMap = { "NIC / National ID": "NIC", "University ID": "NIC", "Phone Number": "NIC", "Bank Account": "NIC" };
                    const verificationType = typeMap[v.label] || "NIC";
                    const documentNumber = window.prompt(`Enter your ${v.label} number:`);
                    if (!documentNumber?.trim()) return;
                    const documentUrl = window.prompt("Enter document image URL (upload via Files first):", "https://");
                    if (!documentUrl?.trim() || documentUrl === "https://") return;
                    try {
                      await apiFetch("/users/me/verification", { method: "POST", body: JSON.stringify({ verificationType, documentNumber: documentNumber.trim(), documentUrl: documentUrl.trim() }) });
                      showToast("Verification request submitted! ✅");
                    } catch (e) { showToast("Failed: " + e.message, "error"); }
                  }}>Verify →</button>}
                </div>
              </div>
            ))}
            <div style={{ marginTop: 14 }}>
              <div className="upload-zone" onClick={() => showToast("Document upload coming soon!")}>
                <div style={{ fontSize: 28, marginBottom: 8 }}>📎</div>
                <div style={{ fontSize: 14, fontWeight: 500, color: C.navy }}>Upload Verification Document</div>
                <div style={{ fontSize: 12, color: C.gray, marginTop: 4 }}>NIC, Student ID, or University Letter · PDF, JPG, PNG</div>
              </div>
            </div>
          </div>
          <div style={{ background: "var(--surface)", border: "1px solid var(--border)", borderRadius: 14, padding: 24 }}>
            <div style={{ fontWeight: 600, fontSize: 15, marginBottom: 16 }}>Account</div>
            <button style={{ width: "100%", padding: "10px", marginBottom: 8, borderRadius: 8, border: "1px solid var(--border)", background: "var(--surface)", cursor: "pointer", fontFamily: "inherit", fontWeight: 500, fontSize: 13 }} onClick={async () => {
              const cp = window.prompt("Enter your current password:");
              if (!cp) return;
              const np = window.prompt("Enter new password (min 8 chars, include uppercase, digit, special char):");
              if (!np || np.length < 8) { showToast("Password too short.", "error"); return; }
              try {
                await apiFetch("/users/me/change-password", { method: "POST", body: JSON.stringify({ currentPassword: cp, newPassword: np }) });
                showToast("Password changed! ✅");
              } catch (e) { showToast("Failed: " + e.message, "error"); }
            }}>Change Password</button>
            <button style={{ width: "100%", padding: "10px", borderRadius: 8, border: "1px solid #FEE2E2", background: "#FEF2F2", cursor: "pointer", fontFamily: "inherit", fontWeight: 500, fontSize: 13, color: "#EF4444" }} onClick={async () => {
              try { const rt = getRefresh(); if (rt) await apiFetch("/auth/logout", { method: "POST", body: JSON.stringify({ refreshToken: rt }) }); } catch (_) { }
              clearToken(); clearRefresh(); setUser(null); setPage("home");
              setForm({ email: "", password: "", name: "", role: "FREELANCER" });
            }}>Sign Out</button>
          </div>
        </div>
      </div>
      <div style={{ background: "var(--surface)", border: "1px solid var(--border)", borderRadius: 14, padding: 24 }}>
        <div style={{ fontWeight: 600, fontSize: 15, marginBottom: 16 }}>Skills Management</div>
        <div style={{ marginBottom: 12 }}>
          {skills.map(s => (
            <span key={s} className="skill-tag">
              {s}
              <button onClick={() => { setSkills(prev => prev.filter(x => x !== s)); showToast("Skill removed!"); }} title="Remove skill">✕</button>
            </span>
          ))}
        </div>
        <div style={{ display: "flex", gap: 8 }}>
          <input className="form-input" style={{ flex: 1 }} placeholder="Add a skill (e.g. React, Figma)" value={newSkill} onChange={e => setNewSkill(e.target.value)} onKeyDown={e => e.key === "Enter" && addSkill()} />
          <button className="btn-join" style={{ padding: "10px 20px" }} onClick={addSkill}>+ Add Skill</button>
        </div>
        <div style={{ fontSize: 12, color: C.gray, marginTop: 6 }}>Skills help clients find you. Add up to 15 skills relevant to your work.</div>
      </div>
    </div>
  );
};

// ─── Admin Panel ─────────────────────────────────────────────────────────────
const AdminPanel = ({ setPage, showToast, startTab }) => {
  const [adminTab, setAdminTab] = useState(startTab || "Verifications");
  const [verifications, setVerifications] = useState([
  ]);
  const [users, setUsers] = useState([
  ]);
  const [adminGigs, setAdminGigs] = useState([]);
  const [adminJobs, setAdminJobs] = useState([]);
  const [disputes, setDisputes] = useState([
  ]);
  const [adminStats, setAdminStats] = useState(null);

  // Load real admin data on mount
  useEffect(() => {
    // --- Verifications Fetch ---
    apiFetch("/admin/verifications/pending").then(res => {
      const items = Array.isArray(res) ? res : (res?.content || []);
      if (items.length > 0) setVerifications(items.map(v => ({
        id: v.verificationId || v.id, backendId: v.verificationId,
        name: v.userName || v.userFullName || "User",
        type: v.documentType || v.type || "Document",
        submitted: v.submittedAt ? new Date(v.submittedAt).toLocaleDateString("en-LK", { month: "short", day: "numeric" }) : "—",
        status: (v.status || "PENDING").charAt(0) + (v.status || "PENDING").slice(1).toLowerCase(),
      })));
    }).catch(() => { });

    // --- Users Fetch ---
    apiFetch("/admin/users").then(res => {
      const items = res?.content ?? (Array.isArray(res) ? res : []);
      if (items.length > 0) console.log("ADMIN USERS RESPONSE [First User]:", items[0]);
      if (items.length > 0) {
        setUsers(items.map(u => {
          const isSuspended = u.status === "SUSPENDED" || u.status === "INACTIVE" || u.enabled === false || u.active === false || u.accountNonLocked === false;
          const rawRole = u.role || u.userRole || u.userType || u.accountType || u.type ||
            (Array.isArray(u.roles) ? (u.roles[0]?.name || u.roles[0]) : null) ||
            (Array.isArray(u.authorities) ? (u.authorities[0]?.authority || u.authorities[0]) : null);
          let mappedRole = "FREELANCER";
          if (rawRole && typeof rawRole === "string") mappedRole = rawRole.replace("ROLE_", "").toUpperCase();
          const rawDate = u.createdAt || u.createdDate || u.joinDate || u.registrationDate;
          return {
            id: u.userId || u.id, backendId: u.userId || u.id,
            name: u.displayName || `${u.firstName || ""} ${u.lastName || ""}`.trim() || u.email || "Unknown User",
            role: mappedRole,
            joined: rawDate ? new Date(rawDate).toLocaleDateString("en-LK", { year: "numeric", month: "short" }) : "—",
            status: isSuspended ? "Suspended" : "Active",
            trust: u.trustScore ?? 0,
          };
        }));
      }
    }).catch(err => console.error("Failed to load users:", err));

    // --- Gigs Fetch ---
    apiFetch("/listings/gigs?page=0&size=50").then(res => {
      const items = res?.content ?? (Array.isArray(res) ? res : []);
      setAdminGigs(items.map(g => ({
        id: g.gigId || g.listingId || g.id,
        gigId: g.gigId || g.id,
        title: g.title || "Untitled Gig",
        seller: g.sellerName || g.sellerDisplayName || "Freelancer",
        category: g.category || "—",
        price: g.basePrice || g.startingPrice || 0,
        status: g.status || "ACTIVE",
      })));
    }).catch(() => { });

    // --- Jobs Fetch ---
    apiFetch("/listings/jobs?page=0&size=50").then(res => {
      const items = res?.content ?? (Array.isArray(res) ? res : []);
      setAdminJobs(items.map(j => ({
        id: j.jobId || j.listingId || j.id,
        jobId: j.jobId || j.id,
        title: j.title || "Untitled Job",
        company: j.companyName || j.company || "Employer",
        location: j.location || "—",
        wage: `LKR ${(j.hourlyRate || j.salaryMin || 0).toLocaleString()}`,
        status: j.status || "ACTIVE",
      })));
    }).catch(() => { });

    // --- Disputes Fetch ---
    apiFetch("/admin/disputes").then(res => {
      const items = Array.isArray(res) ? res : (res?.content || []);
      if (items.length > 0) setDisputes(items.map(d => ({
        id: d.disputeId || d.id, backendId: d.disputeId,
        order: d.orderNumber || d.orderId || "—",
        buyer: d.buyerName || "Buyer",
        seller: d.sellerName || "Seller",
        reason: d.reason || d.description || "Dispute",
        status: (d.status || "OPEN").charAt(0) + (d.status || "OPEN").slice(1).toLowerCase(),
        amount: d.amount || 0,
      })));
    }).catch(() => { });

    apiFetch("/admin/stats").then(res => setAdminStats(res)).catch(() => { });
  }, []);

  const deleteAdminGig = async (gigId) => {
    if (!window.confirm("Delete this gig permanently? This cannot be undone.")) return;
    try {
      await apiFetch(`/listings/gigs/${gigId}`, { method: "DELETE" });
      setAdminGigs(prev => prev.filter(g => g.gigId !== gigId));
      showToast("Gig deleted ✅");
    } catch (e) { showToast("Delete failed: " + e.message, "error"); }
  };

  const deleteAdminJob = async (jobId) => {
    if (!window.confirm("Delete this job permanently? This cannot be undone.")) return;
    try {
      await apiFetch(`/listings/jobs/${jobId}`, { method: "DELETE" });
      setAdminJobs(prev => prev.filter(j => j.jobId !== jobId));
      showToast("Job deleted ✅");
    } catch (e) { showToast("Delete failed: " + e.message, "error"); }
  };

  const decideVerification = async (id, decision) => {
    setVerifications(vs => vs.map(v => v.id === id ? { ...v, status: decision === "approve" ? "Approved" : "Rejected" } : v));
    try {
      await apiFetch(`/admin/verifications/${id}/decide`, { method: "POST", body: JSON.stringify({ decision: decision === "approve" ? "APPROVE" : "REJECT" }) });
      showToast(`Verification ${id} ${decision === "approve" ? "approved ✅" : "rejected ❌"}`);
    } catch (e) { showToast(`Updated locally (backend: ${e.message})`); }
  };
  const toggleUser = async (id) => {
    const u = users.find(x => x.id === id);
    const newStatus = u.status === "Active" ? "Suspended" : "Active";
    setUsers(us => us.map(x => x.id === id ? { ...x, status: newStatus } : x));
    const endpoint = newStatus === "Suspended"
      ? `/admin/users/${u.backendId || id}/suspend`
      : `/admin/users/${u.backendId || id}/activate`;
    try {
      await apiFetch(endpoint, { method: "POST" });
      showToast(`${u.name} ${newStatus === "Suspended" ? "suspended 🚫" : "activated ✅"}`);
    } catch (e) { showToast(`Updated locally (backend: ${e.message})`); }
  };
  const resolveDispute = async (id) => {
    setDisputes(ds => ds.map(d => d.id === id ? { ...d, status: "Resolved" } : d));
    try {
      await apiFetch(`/admin/disputes/${id}/resolve`, { method: "POST", body: JSON.stringify({ resolution: "Admin resolved", status: "RESOLVED" }) });
      showToast(`Dispute ${id} resolved ⚖️`);
    } catch (e) { showToast(`Updated locally (backend: ${e.message})`); }
  };

  return (
    <div className="section">
      <div style={{ display: "flex", alignItems: "center", gap: 16, marginBottom: 24 }}>
        <button style={{ background: "none", border: "none", cursor: "pointer", color: C.blue, fontWeight: 600, fontSize: 14, fontFamily: "inherit" }} onClick={() => setPage("home")}>← Back</button>
        <h1 style={{ fontSize: 22, fontWeight: 700, color: "#EF4444" }}>⚙️ Admin Panel</h1>
        <span style={{ fontSize: 12, background: "#FEF2F2", color: "#EF4444", padding: "3px 10px", borderRadius: 100, fontWeight: 600 }}>ADMIN ONLY</span>
      </div>
      <div className="admin-grid">
        <div className="admin-sidebar">
          {ADMIN_TABS.map(t => (
            <div key={t} className={`admin-nav-item ${adminTab === t ? "active" : ""}`} onClick={() => setAdminTab(t)}>
              {ADMIN_ICONS[t]} {t}
            </div>
          ))}
        </div>
        <div>
          {adminTab === "Verifications" && (
            <div style={{ background: "var(--surface)", border: "1px solid var(--border)", borderRadius: 14, overflow: "hidden" }}>
              <div style={{ padding: "16px 20px", borderBottom: "1px solid #E5E7EB", fontWeight: 600, fontSize: 16 }}>Pending Verifications</div>
              {verifications.map(v => (
                <div key={v.id} style={{ display: "flex", alignItems: "center", gap: 16, padding: "14px 20px", borderBottom: "1px solid #F9FAFB" }}>
                  <div style={{ fontSize: 28 }}>🪪</div>
                  <div style={{ flex: 1 }}>
                    <div style={{ fontSize: 14, fontWeight: 600 }}>{v.name} <span style={{ fontSize: 12, color: C.gray }}>({v.id})</span></div>
                    <div style={{ fontSize: 12, color: C.gray }}>{v.type} · Submitted {v.submitted}</div>
                  </div>
                  <span style={{ fontSize: 11, fontWeight: 600, padding: "3px 8px", borderRadius: 100, background: v.status === "Approved" ? "#DCFCE7" : v.status === "Rejected" ? "#FEF2F2" : "#FEF9C3", color: v.status === "Approved" ? C.green : v.status === "Rejected" ? "#EF4444" : "#92400E" }}>{v.status}</span>
                  {v.status === "Pending" && (
                    <div style={{ display: "flex", gap: 6 }}>
                      <button className="action-btn" style={{ background: C.green, color: "white", fontSize: 12 }} onClick={() => decideVerification(v.id, "approve")}>✅ Approve</button>
                      <button className="action-btn" style={{ background: "#FEF2F2", color: "#EF4444", border: "1px solid #FEE2E2", fontSize: 12 }} onClick={() => decideVerification(v.id, "reject")}>❌ Reject</button>
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
          {adminTab === "Users" && (
            <div style={{ background: "var(--surface)", border: "1px solid var(--border)", borderRadius: 14, overflow: "hidden" }}>
              <div style={{ padding: "16px 20px", borderBottom: "1px solid #E5E7EB", fontWeight: 600, fontSize: 16 }}>User Management</div>
              {users.map(u => (
                <div key={u.id} style={{ display: "flex", alignItems: "center", gap: 16, padding: "14px 20px", borderBottom: "1px solid #F9FAFB" }}>
                  <div style={{ width: 40, height: 40, borderRadius: "50%", background: C.blue, color: "white", display: "flex", alignItems: "center", justifyContent: "center", fontWeight: 700, fontSize: 15, flexShrink: 0 }}>{u.name[0]}</div>
                  <div style={{ flex: 1 }}>
                    <div style={{ fontSize: 14, fontWeight: 600 }}>{u.name} <span style={{ fontSize: 11, color: C.gray, fontWeight: 400 }}>({u.role})</span></div>
                    <div style={{ fontSize: 12, color: C.gray }}>Joined {u.joined} · Trust Score: {u.trust}</div>
                  </div>
                  <span style={{ fontSize: 11, fontWeight: 600, padding: "3px 8px", borderRadius: 100, background: u.status === "Active" ? "#DCFCE7" : "#FEF2F2", color: u.status === "Active" ? C.green : "#EF4444" }}>{u.status}</span>
                  <button className="action-btn" style={{ fontSize: 12, background: u.status === "Active" ? "#FEF2F2" : "#DCFCE7", color: u.status === "Active" ? "#EF4444" : C.green, border: `1px solid ${u.status === "Active" ? "#FEE2E2" : "#BBF7D0"}` }} onClick={() => toggleUser(u.id)}>
                    {u.status === "Active" ? "🚫 Suspend" : "✅ Activate"}
                  </button>
                </div>
              ))}
            </div>
          )}
          {adminTab === "Gigs" && (
            <div style={{ background: "var(--surface)", border: "1px solid var(--border)", borderRadius: 14, overflow: "hidden" }}>
              <div style={{ padding: "16px 20px", borderBottom: "1px solid #E5E7EB", fontWeight: 600, fontSize: 16 }}>All Gigs ({adminGigs.length})</div>
              {adminGigs.length === 0 && <div style={{ padding: 32, textAlign: "center", color: C.gray }}>No gigs found.</div>}
              {adminGigs.map(g => (
                <div key={g.id} style={{ display: "flex", alignItems: "center", gap: 14, padding: "14px 20px", borderBottom: "1px solid #F9FAFB" }}>
                  <div style={{ width: 40, height: 40, borderRadius: 10, background: C.blueLight, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 18, flexShrink: 0 }}>🎯</div>
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{ fontSize: 14, fontWeight: 600, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{g.title}</div>
                    <div style={{ fontSize: 12, color: C.gray }}>By {g.seller} · {g.category} · LKR {g.price.toLocaleString()}</div>
                  </div>
                  <span style={{ fontSize: 11, fontWeight: 600, padding: "3px 8px", borderRadius: 100, background: g.status === "ACTIVE" ? "#DCFCE7" : "#F3F4F6", color: g.status === "ACTIVE" ? C.green : C.gray, flexShrink: 0 }}>{g.status}</span>
                  <button style={{ padding: "6px 14px", borderRadius: 7, border: "1px solid #FEE2E2", background: "#FEF2F2", color: "#EF4444", fontSize: 12, fontWeight: 600, cursor: "pointer", fontFamily: "inherit", flexShrink: 0 }}
                    onClick={() => deleteAdminGig(g.gigId)}>🗑 Delete</button>
                </div>
              ))}
            </div>
          )}
          {adminTab === "Jobs" && (
            <div style={{ background: "var(--surface)", border: "1px solid var(--border)", borderRadius: 14, overflow: "hidden" }}>
              <div style={{ padding: "16px 20px", borderBottom: "1px solid #E5E7EB", fontWeight: 600, fontSize: 16 }}>All Jobs ({adminJobs.length})</div>
              {adminJobs.length === 0 && <div style={{ padding: 32, textAlign: "center", color: C.gray }}>No jobs found.</div>}
              {adminJobs.map(j => (
                <div key={j.id} style={{ display: "flex", alignItems: "center", gap: 14, padding: "14px 20px", borderBottom: "1px solid #F9FAFB" }}>
                  <div style={{ width: 40, height: 40, borderRadius: 10, background: "#DCFCE7", display: "flex", alignItems: "center", justifyContent: "center", fontSize: 18, flexShrink: 0 }}>💼</div>
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{ fontSize: 14, fontWeight: 600, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{j.title}</div>
                    <div style={{ fontSize: 12, color: C.gray }}>{j.company} · 📍 {j.location} · {j.wage}</div>
                  </div>
                  <span style={{ fontSize: 11, fontWeight: 600, padding: "3px 8px", borderRadius: 100, background: j.status === "ACTIVE" ? "#DCFCE7" : "#F3F4F6", color: j.status === "ACTIVE" ? C.green : C.gray, flexShrink: 0 }}>{j.status}</span>
                  <button style={{ padding: "6px 14px", borderRadius: 7, border: "1px solid #FEE2E2", background: "#FEF2F2", color: "#EF4444", fontSize: 12, fontWeight: 600, cursor: "pointer", fontFamily: "inherit", flexShrink: 0 }}
                    onClick={() => deleteAdminJob(j.jobId)}>🗑 Delete</button>
                </div>
              ))}
            </div>
          )}
          {adminTab === "Disputes" && (
            <div style={{ background: "var(--surface)", border: "1px solid var(--border)", borderRadius: 14, overflow: "hidden" }}>
              <div style={{ padding: "16px 20px", borderBottom: "1px solid #E5E7EB", fontWeight: 600, fontSize: 16 }}>Disputes</div>
              {disputes.map(d => (
                <div key={d.id} style={{ padding: "16px 20px", borderBottom: "1px solid #F9FAFB" }}>
                  <div style={{ display: "flex", alignItems: "flex-start", justifyContent: "space-between", gap: 12 }}>
                    <div style={{ flex: 1 }}>
                      <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 4 }}>
                        <span style={{ fontSize: 13, color: C.blue, fontWeight: 600 }}>{d.id}</span>
                        <span style={{ fontSize: 11, fontWeight: 600, padding: "2px 7px", borderRadius: 100, background: d.status === "Open" ? "#FEF2F2" : "#DCFCE7", color: d.status === "Open" ? "#EF4444" : C.green }}>{d.status}</span>
                      </div>
                      <div style={{ fontSize: 14, fontWeight: 500, marginBottom: 3 }}>{d.reason}</div>
                      <div style={{ fontSize: 12, color: C.gray }}>Order {d.order} · Buyer: {d.buyer} · Seller: {d.seller} · LKR {d.amount.toLocaleString()}</div>
                    </div>
                    {d.status === "Open" && (
                      <button className="action-btn" style={{ background: C.blue, color: "white", fontSize: 12, whiteSpace: "nowrap" }} onClick={() => resolveDispute(d.id)}>⚖️ Resolve</button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

// ─── Dashboard (shell) ────────────────────────────────────────────────────────

// ─── DashMyApplications (Freelancer) ─────────────────────────────────────────
const DashMyApplications = ({ setPage, setSelectedJobId, showToast }) => {
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(null); // appId being actioned

  useEffect(() => {
    apiFetch("/jobs/applications/my")
      .then(res => {
        const items = Array.isArray(res) ? res : (res?.content || []);
        setApplications(items.map(a => ({
          id: a.applicationId || a.id,
          jobId: a.jobId || a.listingId,
          jobTitle: a.jobTitle || a.listingTitle || "Job",
          company: a.clientName || a.companyName || "Employer",
          status: a.status || "PENDING",
          appliedAt: (a.appliedAt || a.createdAt)
            ? new Date(a.appliedAt || a.createdAt).toLocaleDateString("en-LK", { month: "short", day: "numeric", year: "numeric" })
            : "—",
          coverLetter: a.coverLetter || "",
        })));
      })
      .catch(() => showToast("Could not load applications.", "error"))
      .finally(() => setLoading(false));
  }, []);

  const handleWithdraw = async (appId) => {
    if (!window.confirm("Withdraw this application?")) return;
    setActionLoading(appId);
    try {
      await apiFetch(`/jobs/applications/${appId}/withdraw`, { method: "DELETE" });
      setApplications(prev => prev.filter(a => a.id !== appId));
      showToast("Application withdrawn.");
    } catch (e) { showToast("Withdraw failed: " + e.message, "error"); }
    finally { setActionLoading(null); }
  };

  // Try candidate endpoints using raw axios (bypasses the interceptor entirely
  // so a 401/404 from a non-existent route can never trigger session-expired).
  const tryEndpoints = async (candidates) => {
    const token = getToken();
    const headers = {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    };
    for (const { url, method, body } of candidates) {
      try {
        const res = await axios({
          baseURL: API_BASE, url, method: method.toLowerCase(),
          headers,
          data: body || undefined,
          timeout: 10000,
          validateStatus: s => s >= 200 && s < 300, // only 2xx = success
        });
        return res.data?.data ?? res.data;
      } catch (e) {
        // Any non-2xx (404, 405, 401, 403, 500) → just try the next candidate
        continue;
      }
    }
    return null; // No endpoint worked — optimistic update already applied, that's fine
  };

  const handleStartWork = async (appId) => {
    setActionLoading(appId);
    // Optimistic update immediately so UI feels instant
    setApplications(prev => prev.map(a => a.id === appId ? { ...a, status: "IN_PROGRESS" } : a));
    try {
      await tryEndpoints([
        { url: `/jobs/applications/${appId}/start`, method: "POST" },
        { url: `/jobs/applications/${appId}/status`, method: "PATCH", body: { status: "IN_PROGRESS" } },
        { url: `/jobs/${appId}/start`, method: "POST" },
      ]);
      showToast("Work started! Give it your best. 💪");
    } catch (e) {
      // Roll back on real error (not 404)
      setApplications(prev => prev.map(a => a.id === appId ? { ...a, status: "ACCEPTED" } : a));
      showToast("Could not update status: " + e.message, "error");
    } finally { setActionLoading(null); }
  };

  const handleComplete = async (appId) => {
    if (!window.confirm("Mark this job as completed? The employer will be notified to confirm.")) return;
    setActionLoading(appId);
    setApplications(prev => prev.map(a => a.id === appId ? { ...a, status: "COMPLETED" } : a));
    try {
      await tryEndpoints([
        { url: `/jobs/applications/${appId}/complete`, method: "POST" },
        { url: `/jobs/applications/${appId}/status`, method: "PATCH", body: { status: "COMPLETED" } },
        { url: `/jobs/${appId}/complete`, method: "POST" },
      ]);
      showToast("Marked as complete! Waiting for employer confirmation. ✅");
    } catch (e) {
      setApplications(prev => prev.map(a => a.id === appId ? { ...a, status: "IN_PROGRESS" } : a));
      showToast("Could not update status: " + e.message, "error");
    } finally { setActionLoading(null); }
  };

  const statusConfig = (s) => {
    switch (s) {
      case "ACCEPTED": return { bg: "#DCFCE7", color: "#16A34A", label: "✅ Accepted" };
      case "IN_PROGRESS": return { bg: "#DBEAFE", color: "#1D4ED8", label: "🔄 In Progress" };
      case "COMPLETED": return { bg: "#EDE9FE", color: "#7C3AED", label: "🏆 Completed" };
      case "REJECTED": return { bg: "#FEF2F2", color: "#EF4444", label: "❌ Rejected" };
      default: return { bg: "#F3F4F6", color: "#6B7280", label: "⏳ Pending" };
    }
  };

  // Group by status for better UX
  const order = ["IN_PROGRESS", "ACCEPTED", "COMPLETED", "PENDING", "REJECTED"];
  const sorted = [...applications].sort((a, b) => order.indexOf(a.status) - order.indexOf(b.status));

  return (
    <div style={{ background: "var(--surface)", border: "1px solid var(--border)", borderRadius: 14, padding: 20 }}>
      <div style={{ fontWeight: 700, fontSize: 16, marginBottom: 20, color: "var(--text)" }}>
        My Applications ({applications.length})
      </div>
      {loading && <div style={{ color: C.gray, fontSize: 13, padding: 12 }}>Loading your applications…</div>}
      {!loading && applications.length === 0 && (
        <div style={{ textAlign: "center", padding: "40px 20px", color: C.gray }}>
          <div style={{ fontSize: 36, marginBottom: 12 }}>📝</div>
          <div style={{ fontSize: 15, fontWeight: 500, marginBottom: 6 }}>No applications yet</div>
          <div style={{ fontSize: 13, marginBottom: 20 }}>Browse jobs and apply to get started.</div>
          <button className="btn-join" style={{ padding: "10px 24px" }} onClick={() => setPage("browse")}>Find Jobs</button>
        </div>
      )}
      {sorted.map(app => {
        const cfg = statusConfig(app.status);
        const busy = actionLoading === app.id;
        return (
          <div key={app.id} style={{ padding: "16px 0", borderBottom: "1px solid var(--border)" }}>
            <div style={{ display: "flex", alignItems: "flex-start", gap: 12 }}>
              {/* Icon */}
              <div style={{ width: 44, height: 44, borderRadius: 10, background: C.blueLight, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 20, flexShrink: 0 }}>💼</div>

              {/* Info */}
              <div style={{ flex: 1, minWidth: 0 }}>
                <div
                  style={{ fontSize: 14, fontWeight: 600, marginBottom: 2, color: C.blue, cursor: "pointer" }}
                  onClick={() => { setSelectedJobId(app.jobId); setPage("job-detail"); }}>
                  {app.jobTitle}
                </div>
                <div style={{ fontSize: 12, color: C.gray, marginBottom: app.coverLetter ? 8 : 0 }}>
                  🏢 {app.company} · Applied {app.appliedAt}
                </div>
                {app.coverLetter && (
                  <div style={{ fontSize: 12, color: "var(--text-secondary)", background: "var(--bg-muted)", padding: "8px 12px", borderRadius: 8, lineHeight: 1.6 }}>
                    {app.coverLetter.length > 140 ? app.coverLetter.slice(0, 140) + "…" : app.coverLetter}
                  </div>
                )}

                {/* Action buttons — shown below cover letter for clarity */}
                {app.status === "ACCEPTED" && (
                  <div style={{ marginTop: 12, padding: "12px 14px", borderRadius: 10, background: "#F0FDF4", border: "1px solid #BBF7D0", display: "flex", alignItems: "center", justifyContent: "space-between", gap: 12, flexWrap: "wrap" }}>
                    <div>
                      <div style={{ fontSize: 13, fontWeight: 600, color: "#16A34A" }}>🎉 You got the job!</div>
                      <div style={{ fontSize: 12, color: "#15803D", marginTop: 2 }}>Ready to start? Click to begin working.</div>
                    </div>
                    <button
                      disabled={busy}
                      onClick={() => handleStartWork(app.id)}
                      style={{ padding: "8px 20px", borderRadius: 8, background: "#16A34A", color: "white", border: "none", fontWeight: 600, fontSize: 13, cursor: busy ? "not-allowed" : "pointer", fontFamily: "inherit", opacity: busy ? 0.7 : 1, whiteSpace: "nowrap" }}>
                      {busy ? "…" : "▶ Start Work"}
                    </button>
                  </div>
                )}

                {app.status === "IN_PROGRESS" && (
                  <div style={{ marginTop: 12, padding: "12px 14px", borderRadius: 10, background: "#EFF6FF", border: "1px solid #BFDBFE", display: "flex", alignItems: "center", justifyContent: "space-between", gap: 12, flexWrap: "wrap" }}>
                    <div>
                      <div style={{ fontSize: 13, fontWeight: 600, color: "#1D4ED8" }}>🔄 Work in progress</div>
                      <div style={{ fontSize: 12, color: "#1E40AF", marginTop: 2 }}>Done? Mark as complete to notify the employer.</div>
                    </div>
                    <button
                      disabled={busy}
                      onClick={() => handleComplete(app.id)}
                      style={{ padding: "8px 20px", borderRadius: 8, background: "#1D4ED8", color: "white", border: "none", fontWeight: 600, fontSize: 13, cursor: busy ? "not-allowed" : "pointer", fontFamily: "inherit", opacity: busy ? 0.7 : 1, whiteSpace: "nowrap" }}>
                      {busy ? "…" : "✅ Mark Complete"}
                    </button>
                  </div>
                )}

                {app.status === "COMPLETED" && (
                  <div style={{ marginTop: 12, padding: "10px 14px", borderRadius: 10, background: "#F5F3FF", border: "1px solid #DDD6FE", fontSize: 13, color: "#7C3AED", fontWeight: 500 }}>
                    🏆 Work completed — waiting for employer to confirm.
                  </div>
                )}
              </div>

              {/* Status badge + withdraw */}
              <div style={{ display: "flex", flexDirection: "column", gap: 6, alignItems: "flex-end", flexShrink: 0 }}>
                <span style={{ fontSize: 11, padding: "3px 10px", borderRadius: 100, fontWeight: 600, background: cfg.bg, color: cfg.color, whiteSpace: "nowrap" }}>
                  {cfg.label}
                </span>
                {app.status === "PENDING" && (
                  <button
                    disabled={busy}
                    style={{ padding: "4px 12px", borderRadius: 6, border: "1px solid #FEE2E2", background: "#FEF2F2", fontSize: 11, cursor: busy ? "not-allowed" : "pointer", color: "#EF4444", fontFamily: "inherit", opacity: busy ? 0.7 : 1 }}
                    onClick={() => handleWithdraw(app.id)}>
                    {busy ? "…" : "Withdraw"}
                  </button>
                )}
              </div>
            </div>
          </div>
        );
      })}
    </div>
  );
};

// ─── DashMyJobs (Client) ──────────────────────────────────────────────────────
const DashMyJobs = ({ setPage, showToast }) => {
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [applicantsModal, setApplicantsModal] = useState(false);
  const [activeJobId, setActiveJobId] = useState(null);
  const [activeJobTitle, setActiveJobTitle] = useState("");
  const [applicants, setApplicants] = useState([]);
  const [applicantsLoading, setApplicantsLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    apiFetch("/listings/jobs/my-jobs")
      .then(res => {
        const items = res?.content ?? (Array.isArray(res) ? res : []);
        setJobs(items.map(j => ({
          id: j.listingId || j.id || j.jobId,
          jobId: j.listingId || j.id || j.jobId, // /jobs/{listingId}/applicants uses listingId
          title: j.title,
          location: j.location || "Sri Lanka",
          badge: j.employmentType || j.jobType || "Part-Time",
          wage: `LKR ${(j.hourlyRate || j.salaryMin || j.wageAmount || 0).toLocaleString()}`,
          status: j.status || "ACTIVE",
          applicants: j.applicantCount || 0,
        })));
      })
      .catch(() => showToast("Could not load jobs.", "error"))
      .finally(() => setLoading(false));
  }, []); // eslint-disable-line

  const loadApplicants = async (jobId, jobTitle) => {
    setActiveJobId(jobId);
    setActiveJobTitle(jobTitle);
    setApplicantsLoading(true);
    setApplicantsModal(true);
    try {
      const res = await apiFetch(`/jobs/${jobId}/applicants`);
      const items = Array.isArray(res) ? res : (res?.content || []);
      setApplicants(items.map(a => ({
        id: a.applicationId || a.id,
        name: a.applicantName || a.freelancerName || "Freelancer",
        initials: (a.applicantName || a.freelancerName || "F")[0].toUpperCase(),
        status: a.status || "PENDING",
        coverLetter: a.coverLetter || "",
        appliedAt: a.appliedAt || a.createdAt
          ? new Date(a.appliedAt || a.createdAt).toLocaleDateString("en-LK", { month: "short", day: "numeric" })
          : "—",
        rating: a.applicantRating || 0,
      })));
    } catch (e) {
      showToast("Could not load applicants: " + e.message, "error");
    } finally {
      setApplicantsLoading(false);
    }
  };

  const handleApplicantAction = async (applicationId, action) => {
    const endpoint = action === "accept"
      ? `/jobs/applications/${applicationId}/accept`
      : `/jobs/applications/${applicationId}/reject`;
    try {
      await apiFetch(endpoint, { method: "POST" });
      setApplicants(prev => prev.map(a => {
        if (a.id === applicationId) return { ...a, status: action === "accept" ? "ACCEPTED" : "REJECTED" };
        if (action === "accept" && a.status === "PENDING") return { ...a, status: "REJECTED" };
        return a;
      }));
      // Update applicant count in job list
      if (action === "accept") {
        setJobs(prev => prev.map(j => j.id === activeJobId ? { ...j, status: "CLOSED" } : j));
      }
      showToast(action === "accept" ? "Applicant accepted! 🎉" : "Applicant rejected.");
    } catch (e) { showToast(`Action failed: ${e.message}`, "error"); }
  };

  const handleDelete = async (jobId) => {
    if (!window.confirm("Delete this job posting? This cannot be undone.")) return;
    try {
      await apiFetch(`/listings/jobs/${jobId}`, { method: "DELETE" });
      setJobs(prev => prev.filter(j => j.id !== jobId));
      showToast("Job deleted. ✅");
    } catch (e) { showToast("Delete failed: " + e.message, "error"); }
  };

  const statusStyle = (s) => {
    if (s === "ACTIVE") return { background: "#DCFCE7", color: C.green };
    if (s === "CLOSED") return { background: "#FEF2F2", color: "#EF4444" };
    if (s === "DRAFT") return { background: "#F3F4F6", color: C.gray };
    return { background: "#F3F4F6", color: C.gray };
  };

  const appStatusStyle = (s) => {
    if (s === "ACCEPTED") return { background: "#DCFCE7", color: "#16A34A" };
    if (s === "REJECTED") return { background: "#FEF2F2", color: "#EF4444" };
    return { background: "#F3F4F6", color: "#6B7280" };
  };

  return (
    <>
      {/* Applicants modal */}
      {applicantsModal && (
        <div className="modal-overlay" onClick={() => setApplicantsModal(false)}>
          <div className="modal" style={{ maxWidth: 620, maxHeight: "85vh", overflowY: "auto" }} onClick={e => e.stopPropagation()}>
            <button className="modal-close" onClick={() => setApplicantsModal(false)}>✕</button>
            <div className="modal-title">Applicants</div>
            <div className="modal-sub">{activeJobTitle}</div>
            {applicantsLoading && <div style={{ padding: 32, textAlign: "center", color: C.gray }}>Loading applicants…</div>}
            {!applicantsLoading && applicants.length === 0 && (
              <div style={{ padding: 32, textAlign: "center", color: C.gray }}>No applications yet.</div>
            )}
            {applicants.map(a => (
              <div key={a.id} style={{ borderBottom: "1px solid #F3F4F6", padding: "16px 0" }}>
                <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: a.coverLetter ? 8 : 0 }}>
                  <div className="avatar" style={{ background: C.blue, width: 38, height: 38, fontSize: 14 }}>{a.initials}</div>
                  <div style={{ flex: 1 }}>
                    <div style={{ fontWeight: 600, fontSize: 14 }}>{a.name}</div>
                    <div style={{ fontSize: 12, color: C.gray }}>Applied {a.appliedAt}{a.rating > 0 ? ` · ★ ${a.rating}` : ""}</div>
                  </div>
                  <span style={{ fontSize: 11, padding: "3px 10px", borderRadius: 100, fontWeight: 600, ...appStatusStyle(a.status) }}>{a.status}</span>
                </div>
                {a.coverLetter && (
                  <p style={{ fontSize: 13, color: "var(--text-secondary)", lineHeight: 1.6, background: "var(--bg-muted)", padding: "10px 12px", borderRadius: 8, marginBottom: 10 }}>
                    {a.coverLetter}
                  </p>
                )}
                {a.status === "PENDING" && (
                  <div style={{ display: "flex", gap: 8, marginTop: 8 }}>
                    <button
                      style={{ padding: "6px 16px", borderRadius: 6, border: "none", background: "#16A34A", color: "white", fontSize: 12, cursor: "pointer", fontFamily: "inherit", fontWeight: 600 }}
                      onClick={() => handleApplicantAction(a.id, "accept")}>
                      ✅ Accept
                    </button>
                    <button
                      style={{ padding: "6px 16px", borderRadius: 6, border: "1px solid #FEE2E2", background: "#FEF2F2", fontSize: 12, cursor: "pointer", color: "#EF4444", fontFamily: "inherit" }}
                      onClick={() => handleApplicantAction(a.id, "reject")}>
                      ❌ Reject
                    </button>
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      <div style={{ background: "var(--surface)", border: "1px solid var(--border)", borderRadius: 14, padding: 20 }}>
        <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 20, alignItems: "center" }}>
          <div style={{ fontWeight: 600, fontSize: 16 }}>My Job Postings ({jobs.length})</div>
          <button className="btn-join" style={{ padding: "8px 16px", fontSize: 13 }} onClick={() => setPage("post-job")}>+ Post a Job</button>
        </div>
        {loading && <div style={{ color: C.gray, fontSize: 13, padding: 12 }}>Loading your jobs…</div>}
        {!loading && jobs.length === 0 && (
          <div style={{ textAlign: "center", padding: "40px 20px", color: C.gray }}>
            <div style={{ fontSize: 36, marginBottom: 12 }}>💼</div>
            <div style={{ fontSize: 15, fontWeight: 500, marginBottom: 6 }}>No job postings yet</div>
            <div style={{ fontSize: 13, marginBottom: 20 }}>Post your first job and find the right talent.</div>
            <button className="btn-join" style={{ padding: "10px 24px" }} onClick={() => setPage("post-job")}>Post a Job</button>
          </div>
        )}
        {jobs.map(job => (
          <div key={job.id} style={{ display: "flex", gap: 14, alignItems: "center", padding: "14px 0", borderBottom: "1px solid #F3F4F6" }}>
            <div style={{ width: 44, height: 44, borderRadius: 10, background: C.blueLight, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 20, flexShrink: 0 }}>💼</div>
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ fontSize: 14, fontWeight: 600, marginBottom: 3, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{job.title}</div>
              <div style={{ fontSize: 12, color: C.gray }}>📍 {job.location} · {job.wage} · 👥 {job.applicants} applicants</div>
            </div>
            <div style={{ display: "flex", gap: 8, flexShrink: 0, alignItems: "center" }}>
              <span style={{ fontSize: 11, padding: "3px 8px", borderRadius: 100, fontWeight: 600, ...statusStyle(job.status) }}>{job.status}</span>
              <button
                style={{ padding: "5px 12px", borderRadius: 6, border: "1px solid var(--border)", background: "var(--surface)", fontSize: 12, cursor: "pointer", fontFamily: "inherit", fontWeight: 500, color: C.blue }}
                onClick={() => loadApplicants(job.jobId, job.title)}>
                👥 {job.applicants > 0 ? `Applicants (${job.applicants})` : "Applicants"}
              </button>
              <button
                style={{ padding: "5px 12px", borderRadius: 6, border: "1px solid #FEE2E2", background: "#FEF2F2", fontSize: 12, cursor: "pointer", color: "#EF4444", fontFamily: "inherit" }}
                onClick={() => handleDelete(job.jobId || job.id)}>
                Delete
              </button>
            </div>
          </div>
        ))}
      </div>
    </>
  );
};


const ROLE_TABS = {
  ADMIN: ["Overview", "Users", "Verifications", "Disputes", "Messages", "Settings"],
  FREELANCER: ["Overview", "My Gigs", "Orders", "Applications", "Wallet", "Messages", "Settings"],
  CLIENT: ["Overview", "My Jobs", "Orders", "Wallet", "Messages", "Settings"],
};

const ROLE_ICONS = {
  Overview: "📊",
  "My Gigs": "🎯",
  Orders: "📋",
  Applications: "📝",
  Wallet: "💰",
  Messages: "💬",
  Settings: "⚙️",
  Users: "👥",
  Verifications: "🪪",
  Disputes: "⚖️",
  "My Jobs": "💼",
};

const ROLE_LABELS = {
  ADMIN: "Administrator",
  FREELANCER: "Student / Freelancer",
  CLIENT: "Client / Employer",
};

const Dashboard = ({ user, setUser, page, setPage, tab, setTab, dashTab, setDashTab, trustScore, trustData, showToast, setForm, messageTarget, setMessageTarget, setSelectedJobId }) => {
  if (!user) return (
    <Browse
      tab={tab} setTab={setTab}
      searchQ="" setSearchQ={() => { }}
      filterCat="" setFilterCat={() => { }}
      filterSort="newest" setFilterSort={() => { }}
      filterPrice="" setFilterPrice={() => { }}
      filterDelivery="" setFilterDelivery={() => { }}
      filterJobType="" setFilterJobType={() => { }}
      filterJobLoc="" setFilterJobLoc={() => { }}
      filterJobPay="" setFilterJobPay={() => { }}
      savedGigs={[]} setSavedGigs={() => { }}
      savedJobs={[]} setSavedJobs={() => { }}
      user={user} setAuthMode={() => { }} setAuthModal={() => { }}
      setSelectedGigId={() => { }} setSelectedJobId={() => { }}
      setPage={setPage} showToast={showToast}
    />
  );

  const role = user.role || "FREELANCER"; // ADMIN | FREELANCER | CLIENT
  const tabs = ROLE_TABS[role] || ROLE_TABS.FREELANCER;

  // If current dashTab is not valid for this role, reset to Overview
  const activeTab = tabs.includes(dashTab) ? dashTab : "Overview";

  // Unread message count — fetched from conversations list, cleared when Messages tab is opened
  const [unreadMsgCount, setUnreadMsgCount] = useState(0);
  useEffect(() => {
    apiFetch("/chat/conversations")
      .then(res => {
        const convos = Array.isArray(res) ? res : (res.content || []);
        const total = convos.reduce((sum, c) => sum + (c.unreadCount || 0), 0);
        setUnreadMsgCount(total);
      })
      .catch(() => { });
  }, []);

  // Clear badge when user opens Messages tab
  useEffect(() => {
    if (activeTab === "Messages") setUnreadMsgCount(0);
  }, [activeTab]);

  // Increment every time the Orders tab becomes active so DashOrders remounts and refetches
  const ordersKeyRef = useRef(0);
  const [ordersKey, setOrdersKey] = useState(0);
  useEffect(() => {
    if (activeTab === "Orders") {
      ordersKeyRef.current += 1;
      setOrdersKey(ordersKeyRef.current);
    }
  }, [activeTab]);

  const allContent = {
    Overview: <DashOverview trustScore={trustScore} trustData={trustData} setDashTab={setDashTab} user={user} />,
    "My Gigs": <DashMyGigs setPage={setPage} showToast={showToast} />,
    Orders: <DashOrders key={ordersKey} setDashTab={setDashTab} showToast={showToast} user={user} />,
    Applications: <DashMyApplications setPage={setPage} setSelectedJobId={setSelectedJobId} showToast={showToast} />,
    "My Jobs": <DashMyJobs setPage={setPage} showToast={showToast} />,
    Wallet: <DashWallet />,
    Messages: <DashMessages user={user} showToast={showToast} messageTarget={messageTarget} clearMessageTarget={() => setMessageTarget(null)} />,
    Settings: <DashSettings user={user} setUser={setUser} setPage={setPage} showToast={showToast} />,
    Users: <AdminPanel setPage={setPage} showToast={showToast} startTab="Users" />,
    Verifications: <AdminPanel setPage={setPage} showToast={showToast} startTab="Verifications" />,
    Disputes: <AdminPanel setPage={setPage} showToast={showToast} startTab="Disputes" />,
  };

  return (
    <div className="section">
      <div className="dash-grid">
        <div className="sidebar">
          <div className="sidebar-user">
            <div className="sidebar-avatar">{user.initials}</div>
            <div style={{ fontWeight: 600, fontSize: 15 }}>{user.name}</div>
            <div style={{ fontSize: 12, color: C.gray, marginTop: 3 }}>{ROLE_LABELS[role] || role}</div>
            {role === "ADMIN"
              ? <div style={{ marginTop: 8, fontSize: 12, background: "#FEF2F2", color: "#EF4444", padding: "3px 10px", borderRadius: 100, fontWeight: 600, display: "inline-block" }}>Admin Access</div>
              : <div style={{ marginTop: 8, fontSize: 12, background: "#DCFCE7", color: C.green, padding: "3px 10px", borderRadius: 100, fontWeight: 600, display: "inline-block" }}>Trust Score: {trustScore}</div>
            }
          </div>
          {tabs.map(label => (
            <div key={label} role="button" tabIndex={0}
              onClick={() => setDashTab(label)}
              onKeyDown={e => e.key === "Enter" && setDashTab(label)}
              className={`sidebar-nav-item ${activeTab === label ? "active" : ""}`}
            >
              {ROLE_ICONS[label]} {label}
              {label === "Messages" && unreadMsgCount > 0 && (
                <span style={{ marginLeft: "auto", background: C.blue, color: "white", fontSize: 10, borderRadius: "50%", width: 16, height: 16, display: "inline-flex", alignItems: "center", justifyContent: "center" }}>{unreadMsgCount > 99 ? "99+" : unreadMsgCount}</span>
              )}
            </div>
          ))}
        </div>
        <div>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 20 }}>
            <h2 style={{ fontSize: 20, fontWeight: 700 }}>{activeTab}</h2>
            {activeTab === "My Gigs" && <button className="btn-join" style={{ padding: "8px 16px", fontSize: 13 }} onClick={() => setPage("create-gig")}>+ New Gig</button>}
            {activeTab === "Applications" && <button className="btn-join" style={{ padding: "8px 16px", fontSize: 13 }} onClick={() => { setPage("browse"); setTab("jobs"); }}>Find Jobs</button>}
          </div>
          {allContent[activeTab]}
        </div>
      </div>
    </div>
  );
};

// ─── Hash routing helpers ─────────────────────────────────────────────────────
const VALID_PAGES = ["home", "browse", "gig-detail", "job-detail", "seller-profile", "post-job", "create-gig", "about", "admin", "dashboard"];
const hashToPage = () => {
  const hash = window.location.hash.replace("#", "").split("?")[0];
  return VALID_PAGES.includes(hash) ? hash : "home";
};
const hashParams = () => {
  const search = window.location.hash.includes("?") ? window.location.hash.split("?")[1] : "";
  return new URLSearchParams(search);
};

// ─── App (state only — no component definitions) ──────────────────────────────
export default function App() {
  const [darkMode, setDarkMode] = useState(() => {
    try {
      const saved = localStorage.getItem("flk_theme");
      return saved ? saved === "dark" : true; // default to dark if no preference saved
    } catch { return true; }
  });
  useEffect(() => {
    document.documentElement.setAttribute("data-theme", darkMode ? "dark" : "light");
    try { localStorage.setItem("flk_theme", darkMode ? "dark" : "light"); } catch { }
  }, [darkMode]);

  const [page, setPage] = useState(hashToPage);
  const [tab, setTab] = useState(hashParams().get("tab") || "gigs");
  const [heroMode, setHeroMode] = useState("gigs");
  const [authModal, setAuthModal] = useState(false);
  const [authMode, setAuthMode] = useState("login");
  const [user, setUser] = useState(null);
  const [form, setForm] = useState({ email: "", password: "", name: "", role: "FREELANCER" });
  const [formError, setFormError] = useState("");
  const [searchQ, setSearchQ] = useState("");
  const [toast, setToast] = useState(null);
  const [selectedGigId, setSelectedGigId] = useState(null);
  const [selectedJobId, setSelectedJobId] = useState(null);
  const [dashTab, setDashTab] = useState("Overview");
  const [messageTarget, setMessageTarget] = useState(null); // { conversationId, otherUserId, name }
  const [filterCat, setFilterCat] = useState("");
  const [filterSort, setFilterSort] = useState("newest");
  const [filterPrice, setFilterPrice] = useState("");
  const [filterDelivery, setFilterDelivery] = useState("");
  const [filterJobType, setFilterJobType] = useState("");
  const [filterJobLoc, setFilterJobLoc] = useState("");
  const [filterJobPay, setFilterJobPay] = useState("");
  const [savedJobs, setSavedJobs] = useState([]);
  const [savedGigs, setSavedGigs] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [showNotifPanel, setShowNotifPanel] = useState(false);

  const showToast = (msg, type = "success") => { setToast({ msg, type }); setTimeout(() => setToast(null), 3000); };

  // Trust score — fetched from /users/{userId}/stats, updates whenever the logged-in user changes
  const [trustScore, setTrustScore] = useState(0);
  const [trustData, setTrustData] = useState({ idVerification: 0, gigRating: 0, jobRating: 0, onTimeDelivery: 0 });

  useEffect(() => {
    if (!user?.userId) return;
    apiFetch(`/users/${user.userId}/stats`)
      .then(res => {
        const overall = res.trustScore ?? 0;
        setTrustScore(overall);
        // If backend sends individual sub-scores, use them directly.
        // Otherwise derive realistic values from the single overall score.
        const verified = overall >= 80 ? 100 : overall > 0 ? 60 : 0;
        setTrustData({
          idVerification: res.idVerificationScore ?? verified,
          gigRating: res.gigRatingScore ?? Math.min(100, Math.max(0, overall + 2)),
          jobRating: res.jobRatingScore ?? Math.min(100, Math.max(0, overall - 6)),
          onTimeDelivery: res.onTimeDeliveryScore ?? Math.min(100, Math.max(0, overall - 2)),
        });
      })
      .catch(() => { }); // trust score is non-critical — never block the UI
  }, [user?.userId]); // eslint-disable-line

  // Map backend notification type → emoji icon
  const notifIcon = (type) => {
    if (!type) return "🔔";
    const t = type.toUpperCase();
    if (t.includes("ORDER")) return "📦";
    if (t.includes("MESSAGE") || t.includes("CHAT")) return "💬";
    if (t.includes("REVIEW") || t.includes("RATING")) return "⭐";
    if (t.includes("VERIF")) return "✅";
    if (t.includes("PAYMENT") || t.includes("WALLET")) return "💰";
    if (t.includes("JOB") || t.includes("APPLY") || t.includes("APPLICATION")) return "💼";
    if (t.includes("GIG")) return "🎯";
    if (t.includes("DISPUTE")) return "⚖️";
    if (t.includes("WELCOME")) return "🎉";
    return "🔔";
  };

  // Format timestamp relative to now
  const notifTime = (iso) => {
    if (!iso) return "Recently";
    const diff = Math.floor((Date.now() - new Date(iso).getTime()) / 1000);
    if (diff < 60) return "Just now";
    if (diff < 3600) return `${Math.floor(diff / 60)} min ago`;
    if (diff < 86400) return `${Math.floor(diff / 3600)} hr ago`;
    return `${Math.floor(diff / 86400)}d ago`;
  };

  const loadNotifications = () => {
    if (!user) return;
    apiFetch("/notifications")
      .then(res => {
        const items = Array.isArray(res) ? res : (res?.content ?? []);
        setNotifications(items.map(n => ({
          id: n.notificationId || n.id,
          icon: notifIcon(n.type || n.notificationType),
          text: n.message || n.content || n.body || "New notification",
          time: notifTime(n.createdAt),
          read: n.read ?? n.isRead ?? false,
        })));
      })
      .catch(() => {
        // Try unread-only as fallback
        apiFetch("/notifications/unread")
          .then(res => {
            const items = Array.isArray(res) ? res : [];
            setNotifications(items.map(n => ({
              id: n.notificationId || n.id,
              icon: notifIcon(n.type || n.notificationType),
              text: n.message || n.content || "New notification",
              time: notifTime(n.createdAt),
              read: false,
            })));
          })
          .catch(() => { });
      });
  };

  // Load on login, then poll every 30 seconds for new notifications
  useEffect(() => {
    if (!user) { setNotifications([]); return; }
    loadNotifications();
    const interval = setInterval(loadNotifications, 30000);
    return () => clearInterval(interval);
  }, [user]); // eslint-disable-line
  useEffect(() => { window.scrollTo({ top: 0, behavior: "smooth" }); }, [page]);

  // Sync page → URL hash
  useEffect(() => {
    const params = new URLSearchParams();
    if (page === "browse" && tab) params.set("tab", tab);
    const query = params.toString();
    const newHash = "#" + page + (query ? "?" + query : "");
    if (window.location.hash !== newHash) {
      window.history.pushState(null, "", newHash);
    }
  }, [page, tab]);

  // Sync URL hash → page state (back/forward button support)
  useEffect(() => {
    const onHashChange = () => {
      const nextPage = hashToPage();
      const nextTab = hashParams().get("tab");
      setPage(nextPage);
      if (nextTab) setTab(nextTab);
    };
    window.addEventListener("popstate", onHashChange);
    return () => window.removeEventListener("popstate", onHashChange);
  }, []);

  useEffect(() => {
    const close = () => setShowNotifPanel(false);
    if (showNotifPanel) document.addEventListener("click", close);
    return () => document.removeEventListener("click", close);
  }, [showNotifPanel]);

  const [authLoading, setAuthLoading] = useState(false);

  // Restore session from localStorage on first load
  useEffect(() => {
    const token = getToken();
    if (!token) return;
    apiFetch("/users/me").then(res => {
      const u = res;
      const displayName = u.displayName || `${u.firstName || ""} ${u.lastName || ""}`.trim() || u.email;
      setUser({ name: displayName, initials: displayName[0]?.toUpperCase() || "U", role: u.role, userId: u.userId, email: u.email });
    }).catch(() => { clearToken(); clearRefresh(); });
  }, []); // eslint-disable-line

  // Handle session expiry fired by the Axios 401 interceptor
  useEffect(() => {
    const onExpired = () => {
      clearToken(); clearRefresh(); setUser(null); setPage("home");
      showToast("Your session expired. Please log in again.", "error");
    };
    window.addEventListener("flk:session-expired", onExpired);
    return () => window.removeEventListener("flk:session-expired", onExpired);
  }, []); // eslint-disable-line

  const handleLogin = async () => {
    setFormError("");
    if (!form.email.trim()) { setFormError("Email is required."); return; }
    if (!/\S+@\S+\.\S+/.test(form.email)) { setFormError("Please enter a valid email."); return; }
    if (!form.password.trim() || form.password.length < 8) { setFormError("Password must be at least 8 characters."); return; }
    if (authMode === "register" && !/^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])/.test(form.password)) { setFormError("Password must contain uppercase, lowercase, a digit, and a special character (@#$%^&+=)."); return; }
    if (authMode === "register" && !form.name.trim()) { setFormError("Full name is required."); return; }
    if (authMode === "register" && form.name.trim().length < 4) { setFormError("Please enter your full name (first and last name)."); return; }

    setAuthLoading(true);
    try {
      if (authMode === "register") {
        // Split full name into first/last for backend RegistrationRequest
        const nameParts = form.name.trim().split(" ");
        const firstName = nameParts[0];
        const lastName = nameParts.slice(1).join(" ").trim() || (firstName.length >= 2 ? firstName : firstName + "x");
        await apiFetch("/auth/register", {
          method: "POST",
          body: JSON.stringify({ email: form.email, password: form.password, firstName, lastName, role: form.role }),
        });
        setAuthModal(false);
        setFormError("");
        setTimeout(() => { setAuthMode("verify-email"); setAuthModal(true); }, 400);
      } else {
        const res = await apiFetch("/auth/login", {
          method: "POST",
          body: JSON.stringify({ email: form.email, password: form.password }),
        });
        const { accessToken, refreshToken, user: u } = res;
        setToken(accessToken);
        setRefresh(refreshToken);
        const displayName = u.displayName || `${u.firstName || ""} ${u.lastName || ""}`.trim() || u.email;
        setUser({ name: displayName, initials: displayName[0]?.toUpperCase() || "U", role: u.role, userId: u.userId, email: u.email });
        setAuthModal(false);
        setFormError("");
        setForm({ email: "", password: "", name: "", role: "FREELANCER" });
        setPage(u.role === "ADMIN" ? "admin" : "home");
        showToast(`Welcome back, ${displayName.split(" ")[0]}! 🎉`);
      }
    } catch (err) {
      setFormError(err.message || "Something went wrong. Is the backend running?");
    } finally {
      setAuthLoading(false);
    }
  };

  // Always clear the form before showing the auth modal so stale credentials never pre-fill
  const openAuthModal = (mode = "login") => {
    setForm({ email: "", password: "", name: "", role: "FREELANCER" });
    setFormError("");
    setAuthMode(mode);
    setAuthModal(true);
  };

  // Shared prop bundles (avoids repeating the same props on every component)
  const navProps = { user, setPage, setTab, setAuthMode, openAuthModal, setAuthModal, setUser, setForm, notifications, setNotifications, showNotifPanel, setShowNotifPanel, darkMode, setDarkMode, loadNotifications };
  const authProps = { user, setAuthMode, setAuthModal: openAuthModal };
  const browseProps = {
    tab, setTab, searchQ, setSearchQ,
    filterCat, setFilterCat, filterSort, setFilterSort,
    filterPrice, setFilterPrice, filterDelivery, setFilterDelivery,
    filterJobType, setFilterJobType, filterJobLoc, setFilterJobLoc, filterJobPay, setFilterJobPay,
    savedGigs, setSavedGigs, savedJobs, setSavedJobs,
    user, setAuthMode, setAuthModal: openAuthModal,
    setSelectedGigId, setSelectedJobId, setPage, showToast,
  };

  return (
    <>
      <style dangerouslySetInnerHTML={{ __html: css }} />
      {toast && (
        <div style={{ position: "fixed", top: 20, right: 20, zIndex: 999, background: toast.type === "success" ? C.green : "#EF4444", color: "white", padding: "12px 20px", borderRadius: 12, fontSize: 14, fontWeight: 500, boxShadow: "0 4px 20px rgba(0,0,0,0.25)", display: "flex", alignItems: "center", gap: 8, backdropFilter: "blur(8px)" }}>
          {toast.type === "success" ? "✅" : "⚠️"} {toast.msg}
        </div>
      )}

      <Nav {...navProps} />

      {page === "home" && (
        <Home
          heroMode={heroMode} setHeroMode={setHeroMode}
          searchQ={searchQ} setSearchQ={setSearchQ}
          setTab={setTab} setPage={setPage}
          setSelectedGigId={setSelectedGigId} setSelectedJobId={setSelectedJobId}
          {...authProps}
        />
      )}
      {page === "browse" && <Browse         {...browseProps} />}
      {page === "gig-detail" && <GigDetail selectedGigId={selectedGigId} setPage={setPage} setDashTab={setDashTab} setMessageTarget={setMessageTarget} showToast={showToast} {...authProps} />}
      {page === "job-detail" && <JobDetail selectedJobId={selectedJobId} savedJobs={savedJobs} setSavedJobs={setSavedJobs} setPage={setPage} showToast={showToast} setDashTab={setDashTab} setMessageTarget={setMessageTarget} {...authProps} />}
      {page === "seller-profile" && <SellerProfile selectedGigId={selectedGigId} setSelectedGigId={setSelectedGigId} setPage={setPage} setDashTab={setDashTab} trustScore={trustScore} trustData={trustData} setMessageTarget={setMessageTarget} {...authProps} />}
      {page === "post-job" && <PostJob setPage={setPage} showToast={showToast} {...authProps} />}
      {page === "create-gig" && <CreateGig setPage={setPage} setDashTab={setDashTab} showToast={showToast} />}
      {page === "about" && <About />}
      {page === "admin" && <AdminPanel setPage={setPage} showToast={showToast} />}
      {page === "dashboard" && (
        <Dashboard
          user={user} setUser={setUser}
          page={page} setPage={setPage}
          tab={tab} setTab={setTab}
          dashTab={dashTab} setDashTab={setDashTab}
          trustScore={trustScore} trustData={trustData}
          showToast={showToast} setForm={setForm}
          messageTarget={messageTarget} setMessageTarget={setMessageTarget}
          setSelectedJobId={setSelectedJobId}
        />
      )}

      <Footer
        user={user} setPage={setPage} setTab={setTab}
        setSearchQ={setSearchQ} setAuthMode={setAuthMode} setAuthModal={setAuthModal} openAuthModal={openAuthModal}
      />

      {authModal && (
        <AuthModal
          authMode={authMode} setAuthMode={setAuthMode}
          form={form} setForm={setForm}
          formError={formError} setFormError={setFormError}
          handleLogin={handleLogin} setAuthModal={setAuthModal}
          setPage={setPage} authLoading={authLoading}
        />
      )}
    </>
  );
}