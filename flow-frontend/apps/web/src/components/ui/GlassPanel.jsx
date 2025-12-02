import React from 'react';
import './GlassPanel.css';

const GlassPanel = ({ children, className = '', ...props }) => {
  return (
    <div className={`glass-panel ${className}`} {...props}>
      {children}
    </div>
  );
};

export default GlassPanel;
