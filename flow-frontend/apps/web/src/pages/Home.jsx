import React from 'react';
import MainLayout from '../components/layout/MainLayout';
import GlassPanel from '../components/ui/GlassPanel';
import Button from '../components/ui/Button';
import './Home.css';

const Home = () => {
  return (
    <MainLayout>
      <div className="hero-section">
        <div className="hero-content">
          <h1 className="hero-title">
            Capture the <span className="highlight">Moment</span>,<br />
            Unleash the <span className="highlight-blue">Flow</span>.
          </h1>
          <p className="hero-subtitle">
            Experience the next generation of multimodal storage. 
            Upload images, videos, or text, and let AI organize your memories.
          </p>
          <div className="hero-actions">
            <Button variant="primary" className="hero-btn">Start Uploading</Button>
            <Button variant="secondary" className="hero-btn">Learn More</Button>
          </div>
        </div>
        
        <div className="hero-visual">
          <GlassPanel className="visual-card card-1">
            <div className="card-icon">📸</div>
            <div className="card-text">Smart Tagging</div>
          </GlassPanel>
          <GlassPanel className="visual-card card-2">
            <div className="card-icon">🔍</div>
            <div className="card-text">Visual Search</div>
          </GlassPanel>
          <GlassPanel className="visual-card card-3">
            <div className="card-icon">✨</div>
            <div className="card-text">AI Analysis</div>
          </GlassPanel>
        </div>
      </div>
    </MainLayout>
  );
};

export default Home;
