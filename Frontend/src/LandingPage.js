import React, { useEffect, useState } from 'react';
import './LandingPage.css';
import { useNavigate } from 'react-router-dom';

const scrollToSection = (id) => {
  const section = document.getElementById(id);
  if (section) section.scrollIntoView({ behavior: 'smooth' });
};

const scrollToTop = () => {
  const top = document.getElementById('top');
  if (top) top.scrollIntoView({ behavior: 'smooth' });
};

function LandingPage() {
  const navigate = useNavigate();
  const [showTopBtn, setShowTopBtn] = useState(false);

  useEffect(() => {
    window.scrollTo(0, 0);
    const onScroll = () => setShowTopBtn(window.scrollY > 200);
    window.addEventListener('scroll', onScroll);
    return () => window.removeEventListener('scroll', onScroll);
  }, []);

  return (
    <div className="landing-container">
      <div id="top" />

      {/* NAVBAR */}
      <div className="navbar">
        <div className="logo">
          <img
            src={process.env.PUBLIC_URL + '/preview.png'}
            alt="WayFinder logo"
            className="logo-icon"
          />
          <span className="logo-text">WayFinder</span>
        </div>

        <div className="nav-links">
          <button onClick={() => scrollToSection('about')}>About</button>
          <button onClick={() => scrollToSection('features')}>Features</button>
          <button onClick={() => navigate('/register')}>Sign Up</button>
        </div>
      </div>

      {/* HERO */}
      <div className="hero">
        <div className="text-section">
          <h1>Plan Your Route with Ease</h1>
          <p className="hero-sub">
            Find the shortest route for your journey<br />
            View estimated distance and duration<br />
            Optimize your travel plans efficiently
          </p>
          <button className="cta" onClick={() => navigate('/welcome')}>
            Get Started
          </button>
        </div>

        <div className="map-section">
          <img src={process.env.PUBLIC_URL + '/Landingpage.png'} alt="map preview" />
          <div className="tooltip">
            <strong>Shortest Route</strong><br />
            Distance: 102.84 miles<br />
            Duration: 2 hr 13 mins
          </div>
        </div>
      </div>

      {/* ABOUT */}
      <section id="about" className="about-section section-anchor" style={{ scrollMarginTop: '80px' }}>
        <div className="section-intro">
          <h2>About WayFinder</h2>
          <p>
            WayFinder plans reliable routes using open data. Compare shortest, fastest, or scenic options; 
            see distance and ETA; add via-stops; save favourites; and learn from community feedback.
          </p>
        </div>

        <div className="highlight-row">
          <div className="highlight">
            <span className="hi-emoji">🧭</span>
            <div>
              <h4>Smart Routing</h4>
              <p>Shortest path with clear step-by-step directions and turn icons.</p>
            </div>
          </div>

          <div className="highlight">
            <span className="hi-emoji">🛣️</span>
            <div>
              <h4>Via Stops</h4>
              <p>Add detours and we stitch the route through each stop in order.</p>
            </div>
          </div>

          <div className="highlight">
            <span className="hi-emoji">⭐</span>
            <div>
              <h4>Community Wisdom</h4>
              <p>Save, rate and browse user-contributed alternatives.</p>
            </div>
          </div>
        </div>
      </section>

      {/* FEATURES */}
      <section id="features" className="features-section section-anchor" style={{ scrollMarginTop: '80px' }}>
        <h2 className="features-title">What You Get</h2>

        <div className="features-grid">
          <div className="feature-card">
            <div className="f-icon">🚗 |🚶| 🚚 | 🚴 |</div>
            <h3>Multi-Mode Routing</h3>
            <p>Driving, walking, cycling, and HGV — switch with a tap.</p>
          </div>

          <div className="feature-card">
            <div className="f-icon">📍</div>
            <h3>Use My Location</h3>
            <p>Start from where you are and follow live progress.</p>
          </div>

          <div className="feature-card">
            <div className="f-icon">🧩</div>
            <h3>Via Points & Detours</h3>
            <p>Add stops; we automatically connect every leg.</p>
          </div>

          <div className="feature-card">
            <div className="f-icon">🗺️</div>
            <h3>Beautiful Map Overlay</h3>
            <p>Clean polylines, markers, and instant zoom-to-route.</p>
          </div>

          <div className="feature-card">
            <div className="f-icon">💾</div>
            <h3>Save & Rate Routes</h3>
            <p>Keep favourites and see reviews from real users.</p>
          </div>

          <div className="feature-card">
            <div className="f-icon">💬</div>
            <h3>Chat Planner</h3>
            <p>Describe your trip in plain English — we parse and plan.</p>
          </div>
        </div>

        <div className="features-cta-row">
          <button className="cta secondary" onClick={() => navigate('/welcome')}>
            Try WayFinder
          </button>
          <button className="link-btn" onClick={scrollToTop}>↑ Back to Top</button>
        </div>
      </section>

      {/* Floating Back to Top */}
      {showTopBtn && (
        <button
          onClick={scrollToTop}
          title="Back to Top"
          style={{
            position: 'fixed',
            right: 20,
            bottom: 20,
            zIndex: 999,
            background: '#8e44ad',
            color: '#fff',
            border: 'none',
            borderRadius: 999,
            width: 44,
            height: 44,
            fontSize: 18,
            fontWeight: 800,
            cursor: 'pointer',
            boxShadow: '0 10px 28px rgba(0,0,0,.18)',
          }}
        >
          ↑
        </button>
      )}

      {/* FOOTER */}
      <footer className="footer">
        <div className="footer-content">
          <p>&copy; 2025 WayFinder. All rights reserved.</p>
        </div>
      </footer>
    </div>
  );
}

export default LandingPage;
