import React from 'react';
import './MainLayout.css';

import { Link, useLocation } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { GlobalOutlined } from '@ant-design/icons';

const MainLayout = ({ children }) => {
  const location = useLocation();
  const { t, i18n } = useTranslation();
  const isActive = (path) => location.pathname === path ? 'active' : '';

  const toggleLanguage = () => {
    const newLang = i18n.language === 'zh' ? 'en' : 'zh';
    i18n.changeLanguage(newLang);
    localStorage.setItem('language', newLang);
  };

  return (
    <div className="main-layout">
      <header className="header">
        <div className="logo">
          <span className="logo-icon">🌸</span>
          <span className="logo-text">Flow</span>
        </div>
        <nav className="nav">
          <Link to="/" className={`nav-link ${isActive('/')}`}>{t('nav.home')}</Link>
          <Link to="/ai" className={`nav-link ${isActive('/ai')}`}>{t('nav.ai')}</Link>
          <Link to="/ai/knowledge" className={`nav-link ${isActive('/ai/knowledge')}`}>{t('nav.knowledgeBase')}</Link>
          <Link to="/explore" className={`nav-link ${isActive('/explore')}`}>{t('nav.explore')}</Link>
        </nav>
        <div className="user-profile">
          <button 
            onClick={toggleLanguage} 
            className="lang-toggle"
            title={i18n.language === 'zh' ? 'Switch to English' : '切换到中文'}
          >
            <GlobalOutlined style={{ marginRight: 4 }} />
            {i18n.language === 'zh' ? 'EN' : '中文'}
          </button>
          <div className="avatar">A</div>
        </div>
      </header>
      <main className={`content ${location.pathname === '/ai' || location.pathname === '/ai/knowledge' ? 'full-width' : ''}`}>
        {children}
      </main>
    </div>
  );
};

export default MainLayout;
