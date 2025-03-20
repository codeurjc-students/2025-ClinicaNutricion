import React from 'react';
import { Link } from 'react-router-dom';
import '../../styles/pages/MainPatientScreen.css';
import profileLogo from '../../assets/sidebar/LogoPerfil.png';
import logOutIcon from '../../assets/sidebar/LogoLogOut.png';
import { useAuth } from 'react-oidc-context';

const MainPatientScreen = () => {
  const auth = useAuth(); 

  const handleLogout = () => {
    auth.signoutRedirect({
      extraQueryParams: {
        client_id: auth.settings.client_id,
        logout_uri: window.location.origin + "/",
      },
    });
  };

  return (
    <div className="main-patient-screen container">
      <header className="header d-flex justify-content-between align-items-center">
        <button className="profile-button">
          <img src={profileLogo} alt="Logo de perfil" />
        </button>
      </header>

      <div className="logout-container">
        <button className="btn btn-danger logout-button" onClick={handleLogout}>
          <img src={logOutIcon} alt="Cerrar sesión" className="logout-icon" />
          Cerrar sesión
        </button>
      </div>

      <div className="buttons-container row justify-content-center">
        <div className="col-12 col-md-6 mb-3">
          <Link to="/patients/nutritionist-selection">
            <button className="btn btn-primary w-100">Pedir cita</button>
          </Link>
        </div>
        <div className="col-12 col-md-6">
          <button className="btn btn-secondary w-100">Historial de citas</button>
        </div>
      </div>
    </div>
  );
};

export default MainPatientScreen;
