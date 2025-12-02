import React from 'react';
import './MainLayout.css';

const MainLayout = ({ children }) => {
  return (
    <div className="main-layout">
      <header className="header">
        <div className="logo">
          <span className="logo-icon">🌸</span>
          <span className="logo-text">Flow</span>
        </div>
        <nav className="nav">
          <a href="/" className="nav-link active">Home</a>
          <a href="/explore" className="nav-link">Explore</a>
        </nav>
        <div className="user-profile">
          <div className="avatar">A</div>
        </div>
      </header>
      <main className="content">
        {children}
      </main>
    </div>
  );
};

export default MainLayout;
