import React from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import { useAuth } from 'react-oidc-context';
import '../styles/components/Sidebar.css';
import logOutIcon from '../assets/sidebar/LogoLogOut.png';
import ProfileIcon from '../assets/sidebar/LogoPerfil.png';
import CalendarIcon from '../assets/sidebar/LogoCalendario.png';
import ManageUsersIcon from '../assets/sidebar/LogoManageUsers.png';
import LogoClinica from '../assets/sidebar/LogoClinica.png';

const Sidebar = () => {
  const location = useLocation();
  const auth = useAuth();
  const roles = auth.user?.profile['cognito:groups'] || [];

  // Ocultar el sidebar en ciertas rutas
  const hideSidebar =
    location.pathname === '/' || location.pathname.startsWith('/patient');

  // Obtener opciones de menú según el rol del usuario
  const getMenuOptions = () => {
    if (roles.includes('admin')) {
      return [
        { path: '/admin/profile', icon: ProfileIcon, label: 'Perfil' },
        { path: '/admin/agenda', icon: CalendarIcon, label: 'Agenda' },
        {
          path: '/admin/manage-users',
          icon: ManageUsersIcon,
          label: 'Gestión de Usuarios',
        },
      ];
    } else if (roles.includes('nutritionist')) {
      return [
        { path: '/nutritionists/profile', icon: ProfileIcon, label: 'Perfil' },
        { path: '/nutritionists/agenda', icon: CalendarIcon, label: 'Agenda' },
        {
          path: '/nutritionists/patients',
          icon: ManageUsersIcon,
          label: 'Gestión de Pacientes',
        },
      ];
    } else if (roles.includes('auxiliary')) {
      return [
        { path: '/auxiliaries/profile', icon: ProfileIcon, label: 'Perfil' },
        { path: '/auxiliaries/agenda', icon: CalendarIcon, label: 'Agenda' },
        {
          path: '/auxiliaries/patients',
          icon: ManageUsersIcon,
          label: 'Gestión de Pacientes',
        },
      ];
    } else {
      return [];
    }
  };

  if (hideSidebar) {
    return null; // evitar renderizar el sidebar
  }

  const menuOptions = getMenuOptions();

  return (
    <div className="sidebar">
      <div className="sidebar-logo">
        <img src={LogoClinica} alt="Logo de la clínica" className="logo" />
      </div>

      <hr className="sidebar-separator" />

      {/* Botones de navegación según rol */}
      {menuOptions.map((option, index) => (
        <NavLink
          key={index}
          to={option.path}
          className="sidebar-button"
          activeclassname="active"
        >
          <img src={option.icon} alt={option.label} className="icon" />
        </NavLink>
      ))}

      {/* Botón para Logout */}
      <button
        className="sidebar-button logout"
        onClick={() =>
          auth.signoutRedirect({
            extraQueryParams: {
              client_id: auth.settings.client_id,
              logout_uri: window.location.origin + '/',
            },
          })
        }
      >
        <img src={logOutIcon} alt="Cerrar sesión" className="icon" />
      </button>
    </div>
  );
};

export default Sidebar;
