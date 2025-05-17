import React from 'react';
import '../styles/components/ToggleSwitch.css';

const ToggleSwitch = ({ isActive, onToggle }) => {
  return (
    <div
      className={`toggle-switch ${isActive ? 'active' : 'inactive'}`}
      onClick={onToggle}
    >
      <div className="toggle-track"></div>
      <div className="toggle-circle"></div>
    </div>
  );
};

export default ToggleSwitch;
