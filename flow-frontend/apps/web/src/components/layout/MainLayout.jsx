import React from 'react';
import './MainLayout.css';

import { Link, useLocation } from 'react-router-dom';

const MainLayout = ({ children }) => {
  const location = useLocation();
  const isActive = (path) => location.pathname === path ? 'active' : '';

  return (
    <div className="main-layout">
      <header className="header">
        <div className="logo">
          <span className="logo-icon">🌸</span>
          <span className="logo-text">Flow</span>
        </div>
        <nav className="nav">
          <Link to="/" className={`nav-link ${isActive('/')}`}>Home</Link>
          <Link to="/ai" className={`nav-link ${isActive('/ai')}`}>AI</Link>
          <Link to="/explore" className={`nav-link ${isActive('/explore')}`}>Explore</Link>
        </nav>
        <div className="user-profile">
          <div className="avatar">A</div>
        </div>
      </header>
      <main className={`content ${location.pathname === '/ai' ? 'full-width' : ''}`}>
        {children}
      </main>
    </div>
  );
};

export default MainLayout;
