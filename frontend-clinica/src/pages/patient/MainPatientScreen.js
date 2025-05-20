import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import '../../styles/pages/MainPatientScreen.css';
import profileLogo from '../../assets/sidebar/LogoPerfil.png';
import logOutIcon from '../../assets/sidebar/LogoLogOut.png';
import { useAuth } from 'react-oidc-context';

const MainPatientScreen = () => {
  const auth = useAuth();
  const token = localStorage.getItem('token');
  const BASE_URL = process.env.REACT_APP_API_BASE_URL;

  const [patient, setPatient] = useState(null);

  // Cierre de sesión
  const handleLogout = () => {
    auth.signoutRedirect({
      extraQueryParams: {
        client_id: auth.settings.client_id,
        logout_uri: window.location.origin + '/',
      },
    });
  };

  // Se obtiene el perfil del paciente autenticado
  useEffect(() => {
    const fetchPatientData = async () => {
      try {
        const response = await fetch(`${BASE_URL}/patients/profile`, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${token}`,
          },
        });
        if (!response.ok)
          throw new Error('Error obteniendo el perfil del paciente');
        const data = await response.json();
        setPatient({
          idUser: data.id,
          name: data.name,
          surname: data.surname,
        });
      } catch (error) {
        console.error('Error al obtener los datos del paciente:', error);
      }
    };

    fetchPatientData();
  }, [BASE_URL, token]);

  return (
    <div className="main-patient-screen">
      <header className="header d-flex justify-content-between align-items-center">
        {/* Icono para ir al perfil del paciente */}
        <Link
          to="/patients/profile"
          className="profile-button"
          aria-label="Ir a mi perfil"
        >
          <img src={profileLogo} alt="Perfil" />
        </Link>

        {/* Botón de cierre de sesión */}
        <button
          className="logout-icon-button"
          onClick={handleLogout}
          aria-label="Cerrar sesión"
        >
          <img src={logOutIcon} alt="Salir" />
        </button>
      </header>

      <div className="content-wrapper">
        <div className="buttons-container row justify-content-center">
          {/*Pedir cita*/}
          <div className="col-12 col-md-6 mb-3 mx-md-auto">
            <Link to="/patients/nutritionist-selection" state={{ patient }}>
              <button className="btn btn-primary w-100">Pedir cita</button>
            </Link>
          </div>

          {/*Citas pendientes*/}
          <div className="col-12 col-md-6 mb-3 mx-md-auto">
            <Link to="/patients/appointments/pending" state={{ patient }}>
              <button className="btn btn-primary w-100">
                Citas pendientes
              </button>
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default MainPatientScreen;
